package app.morningsignout.com.morningsignoff.search_results;

//import android.app.Fragment; //https://stackoverflow.com/questions/9586218/fragmentactivity-cannot-cast-from-fragment-to-derived-class
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.network.FetchListSearchTask;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Created by shinr on 6/5/2017.
 */

public class SearchFragment extends Fragment {
    // these are the "key" strings for getArguments() and setArguments() and so on.
    final static String SEARCH_PARAM = "SEARCH_PARAM"; // have SearchResultsActivity set this so we can put this as header
    final static String SEARCH_REFRESH = "SEARCH_REFRESH"; // shouldn't this be a boolean? e: no, this is a tag.
    final static String TAG = "SearchFragment";

    // instance
    public static SearchFragment instance = null;

    // local copies of metadata
    String search = ""; // holds the search argument

    //Helpful stuff.
    private SwipeRefreshLayout swipeRefreshLayout; // used to handle refreshing on swipe
    private ProgressBar progressBar; // does what it's named
    private TextView refreshTextView; // refers to the text that shows when refreshing
    private ImageView splashScreenView; // refers to splash screen
    private ProgressBar footerProgressBar; // refers to footer progress bar
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter; // gridview

    private SearchAdapter searchAdapter; // custom adapter for our "listview"
    private LruCache<String, Bitmap> memoryCache; // sets up a cache for images
    private AtomicBoolean isLoadingArticles;
    protected static Integer index = null; // marks index of current selection (I think?)

    public AtomicBoolean getIsLoadingArticles() { return isLoadingArticles; }

    @Override
    public void onDetach() {
        index = gridViewWithHeaderAndFooter.getFirstVisiblePosition();
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            search = getArguments().getString(SEARCH_PARAM);
        }

        isLoadingArticles = new AtomicBoolean(false);
        setUpCache();
    }

    private void setUpGridView(GridViewWithHeaderAndFooter grid) {
//        if (SearchAdapter.isLandscape(getContext())) {
//            grid.setNumColumns(2);
//            grid.setPadding(5,10,5,10);
//        } else
//        {
            grid.setNumColumns(1); // for now let's just try only the one column
//        }
    }

    private void setUpCache() {
        /*  Thanks to http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
        * for caching code */
        // max memory of hdpi ~32 MB
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // memory for images ~4.5 MB = 7-8 images
        final int cacheSize;

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if (width <= 320)
            cacheSize = maxMemory / 3;
        else
            cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than the number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    // Not sure why this is nullable. Doesn't seem to cause any harm, but I'll keep an eye on this.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        instance = this;
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Grab all the views and layouts from the layout file
        TextView headerView = (TextView) rootView.findViewById(R.id.textView_searchHeader); // how about we set this to the search params
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_search);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_search);
        refreshTextView = (TextView) rootView.findViewById(R.id.textView_searchRefresh);
        splashScreenView = (ImageView) rootView.findViewById(R.id.imageView_splash_search);
        footerProgressBar = getFooterProgressBarXml(); // FIXME
        gridViewWithHeaderAndFooter = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView_search);
        boolean isRefresh = getArguments().getBoolean(SEARCH_REFRESH, false);

        // Custom adapter? SearchAdapter?
        headerView.setText(search); //header may be unnecessary
        swipeRefreshLayout.setColorSchemeColors(Color.argb(255,0x81,0xbf,0xff), Color.WHITE);
        setUpGridView(gridViewWithHeaderAndFooter);
        gridViewWithHeaderAndFooter.addFooterView(footerProgressBar);

        // Creates and loads new adapter or sets position of existing gridView
        if(searchAdapter == null) {
            searchAdapter = new SearchAdapter(getActivity(), inflater);
            gridViewWithHeaderAndFooter.setAdapter(searchAdapter);
            isLoadingArticles.set(true);
            new FetchListSearchTask(this, 1, true, isRefresh).execute(search);
        } else {
            refreshTextView.setVisibility(View.GONE);
            searchAdapter.notifyDataSetChanged();
            gridViewWithHeaderAndFooter.setAdapter(searchAdapter);
            if (index != null)
                gridViewWithHeaderAndFooter.setSelection(index);
        }

        // Add a click listener for the returned articles.
        gridViewWithHeaderAndFooter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WrapperListAdapter wrapperListAdapter = (WrapperListAdapter) parent.getAdapter();
                SearchAdapter adapter = (SearchAdapter) wrapperListAdapter.getWrappedAdapter();
                int id_int = (int) id;

                // Do nothing if id is invalid
                if (id_int < 0 || id_int > adapter.getCount())
                    return;

                // Spawn a new ArticleActivity using the data.
                Article rowTemp = (Article) adapter.getItem(id_int);
                Intent articleActivity = new Intent(gridViewWithHeaderAndFooter.getContext(), ArticleActivity.class);

                // Load title
                articleActivity.putExtra(ArticleActivity.TITLE, rowTemp.getTitle());
                // Load link
                articleActivity.putExtra(ArticleActivity.LINK, rowTemp.getLink());
                // Load content
                articleActivity.putExtra(ArticleActivity.CONTENT, rowTemp.getContent());
                // Load header image
                articleActivity.putExtra(ArticleActivity.IMAGE_URL, rowTemp.getImageURL());

                // Hmm, do we really want to start an entirely new activity? Maybe. Hopefully can handle
                // this, and if you press back, you end up back in the search results.
                gridViewWithHeaderAndFooter.getContext().startActivity(articleActivity);
            }
        });

        // To load more articles when the bottom of the page is reached
        gridViewWithHeaderAndFooter.setOnScrollListener(new AbsListView.OnScrollListener() {
            // Last seen by this listener, not by the program.
            // So, say, the program has seen page 1 already, but this value is 0 still
//            int lastSeenPageNum = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
//                Log.d("SearchFragment","onscroll @ lastvisibleitem: " + lastVisibleItem + ", totalitemcount: " + totalItemCount);
                // At last item
                if (lastVisibleItem >= totalItemCount) {
                    WrapperListAdapter wrappedAdapter = (WrapperListAdapter) gridViewWithHeaderAndFooter.getAdapter();
                    SearchAdapter adapter = (SearchAdapter) wrappedAdapter.getWrappedAdapter();
//                    Log.d("SearchFragment","onscroll - first if");
                    int pageNum = adapter.getPageNum();
                    Log.d("SearchFragment","pageNum: " + pageNum);
                    // Only make one request per page request
                    if (totalItemCount != 0 /*&& lastSeenPageNum != pageNum*/ && isLoadingArticles.weakCompareAndSet(false, true)) {
//                        lastSeenPageNum = pageNum;
                        Log.d("SearchFragment","start loading more");
                        new FetchListSearchTask(SearchFragment.this, pageNum + 1, false, false).execute(search);
                        Log.d("SearchFragment", "Making new request");
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        // Sets up a refreshlistener, to let us know when to refresh.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
            public void onRefresh() {
               if (!gridViewWithHeaderAndFooter.getAdapter().isEmpty() || !isLoadingArticles.get()) {

                   // seems like this series of lines is what creates an instance of this fragment.
                   // Reload search fragment
                   SearchFragment fragment =
                           SearchFragment.findOrCreateRetainFragment(getActivity().getSupportFragmentManager());
                   Bundle args = new Bundle();
                   args.putString(SearchFragment.SEARCH_PARAM, search);
                   args.putBoolean(SearchFragment.SEARCH_REFRESH, true); // we are refreshing!
                   fragment.setArguments(args);

                   getActivity().getSupportFragmentManager().beginTransaction()
                           .replace(R.id.container_search, fragment) // replace with this new frag.
                           .commit();
               }
               swipeRefreshLayout.setRefreshing(false);
           }
        });

        return rootView;
    }

    // FIXME: Debug the crashing issues next time before trying an external cache
    public static boolean addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            instance.memoryCache.put(key,bitmap);
            return true;
        }

        return false;
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return (instance != null) ? instance.memoryCache.get(key) : null;
    }

    // Not sure exactly what this does. Seems like it finds the fragment, or creates a new one if
    //      not already existing. Fail-safe?
    public static SearchFragment findOrCreateRetainFragment(FragmentManager fm) {
        SearchFragment fragment = (SearchFragment) fm.findFragmentByTag(TAG);
        if (fragment == null)
        {
            return new SearchFragment();
        }
        return fragment;
    }

    // Helper functions, used in the Fetch/Async task
    ProgressBar getFooterProgressBarXml() {
        // -get attributes for footerProgressBar
        XmlPullParser pullParser = getResources().getXml(R.xml.footer_progressbar);

        // -get first tag of xml
        try {
            int type = 0;
            while (type != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG) {
                type = pullParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e("SearchFragment", e.getMessage());
        }

        // -get attriuteSet
        AttributeSet attrs = Xml.asAttributeSet(pullParser);

        // -create progressBar and add to listView
        return new ProgressBar(getActivity(), attrs);
    }
    public SwipeRefreshLayout getSwipeRefreshLayout() { return swipeRefreshLayout; }

    public ProgressBar getProgressBar() { return progressBar; }

    public TextView getRefreshTextView() { return refreshTextView; }

    public ProgressBar getFooterProgressBar() { return footerProgressBar; }

    public GridViewWithHeaderAndFooter getGridViewWithHeaderAndFooter()
    { return gridViewWithHeaderAndFooter; }
}
