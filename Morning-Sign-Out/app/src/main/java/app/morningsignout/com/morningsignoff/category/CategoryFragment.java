package app.morningsignout.com.morningsignoff.category;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.util.Xml;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.disqus.DisqusMainActivity;
import app.morningsignout.com.morningsignoff.network.FetchListArticlesTask;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

public class CategoryFragment extends Fragment {
    final static String EXTRA_TITLE = "EXTRA_TITLE";
    final static String EXTRA_REFRESH = "EXTRA_REFRESH";
    final static String EXTRA_URL = "EXTRA_URL";
    final static String TAG = "CategoryFragment";

    public static CategoryFragment instance = null;

    String category = "";
    String category_url = "";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView refreshTextView;
    private ImageView splashScreenView;
    private ProgressBar footerProgressBar;
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter;

    private CategoryAdapter categoryAdapter;
    public LruCache<String, Bitmap> memoryCache;
    private AtomicBoolean isLoadingArticles;
    protected static Integer index = null;

    public AtomicBoolean getIsLoadingArticles() {
        return isLoadingArticles;
    }

    @Override
    public void onDetach() {
        index = gridViewWithHeaderAndFooter.getFirstVisiblePosition();
        super.onDetach();
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        instance = null;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            category = getArguments().getString(EXTRA_TITLE);
            category_url = getArguments().getString(EXTRA_URL);
        }

        isLoadingArticles = new AtomicBoolean(false);
        setUpCache();
    }

    private void setUpCache() {
         /* Thanks to http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
         * for caching code */
        // max memory of hdpi ~32 MB
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // memory for images ~4.5 MB = 7-8 images
        final int cacheSize;

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if (width <= 320) {
            cacheSize = maxMemory / 3;
        } else
            cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void setUpGridView(GridViewWithHeaderAndFooter grid){
        if (CategoryAdapter.isLandscape(getContext())) {
            grid.setNumColumns(2);
            grid.setPadding(5,10,5,10);
        }
        else
            grid.setNumColumns(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        instance = this;
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        TextView headerView = (TextView) rootView.findViewById(R.id.textView_categoryHeader);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_category);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        refreshTextView = (TextView) rootView.findViewById(R.id.textView_categoryRefresh);
        splashScreenView = (ImageView) rootView.findViewById(R.id.imageView_splash);
        footerProgressBar = getFooterProgressBarXml();
        gridViewWithHeaderAndFooter = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView);
        boolean isRefresh = getArguments().getBoolean(EXTRA_REFRESH, false);

        headerView.setText(category);
        swipeRefreshLayout.setColorSchemeColors(Color.argb(255, 0x81, 0xbf, 0xff), Color.WHITE);
        setUpGridView(gridViewWithHeaderAndFooter);
        gridViewWithHeaderAndFooter.addFooterView(footerProgressBar);

        // Creates and loads new adapter or sets position of existing gridView
        if(categoryAdapter == null) {
            categoryAdapter = new CategoryAdapter(getActivity(), inflater);
            categoryAdapter.setAdapterView(gridViewWithHeaderAndFooter);
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);
            isLoadingArticles.set(true);
            new FetchListArticlesTask(this, 1, true, isRefresh).execute(category_url); // First round of articles
        } else {
            refreshTextView.setVisibility(View.GONE);
            categoryAdapter.notifyDataSetChanged();
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);
            if (index != null)
                gridViewWithHeaderAndFooter.setSelection(index);
        }

        // Setup the click listener for the listView
        gridViewWithHeaderAndFooter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WrapperListAdapter wrappedAdapter = (WrapperListAdapter) parent.getAdapter();
                CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

                int id_int = (int) id;

                // Do nothing if id is invalid
                if (id_int < 0 || id_int > adapter.getCount())
                    return;

                // Create new categoryActivity for the article here
                // feed the new categoryActivity with the URL of the page
                Article rowTemp = (Article) adapter.getItem(id_int);
                Intent articleActivity = new Intent(gridViewWithHeaderAndFooter.getContext(), ArticleActivity.class);

                // TITLE holds the html link for the article
                articleActivity.putExtra(ArticleActivity.TITLE, rowTemp.getTitle());

                // LINK holds the name of the article
                articleActivity.putExtra(ArticleActivity.LINK, rowTemp.getLink());

                // CONTENT holds the html text of the article
                articleActivity.putExtra(ArticleActivity.CONTENT, rowTemp.getContent());

                // IMAGE_URL holds the link to the article's header image
                articleActivity.putExtra(ArticleActivity.IMAGE_URL, rowTemp.getImageURL());

                //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_comment_black_24dp);
                Intent commentsIntent = new Intent(getContext(), DisqusMainActivity.class);
                PendingIntent commentsPendingIntent = PendingIntent.getActivity(getContext(), 0, commentsIntent, 0);

                builder.setToolbarColor(0x81bfff);
                //builder.setActionButton(icon, "Comments", pendingIntent, false);
                builder.addMenuItem("Open comments", commentsPendingIntent);

                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getContext(), Uri.parse(rowTemp.getLink()));

                //gridViewWithHeaderAndFooter.getContext().startActivity(articleActivity);
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
                // At last item
                if (lastVisibleItem >= totalItemCount) {
                    WrapperListAdapter wrappedAdapter = (WrapperListAdapter) gridViewWithHeaderAndFooter.getAdapter();
                    CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();

                    int pageNum = adapter.getPageNum();
                    // Only make one request per page request
                    if (totalItemCount != 0 /*&& lastSeenPageNum != pageNum*/ && isLoadingArticles.weakCompareAndSet(false, true)) {
//                        lastSeenPageNum = pageNum;
                        new FetchListArticlesTask(CategoryFragment.this, pageNum + 1, false, false).execute(category_url);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!gridViewWithHeaderAndFooter.getAdapter().isEmpty() || !isLoadingArticles.get()) {
                    // Reload categoryFragment
                    CategoryFragment fragment =
                            CategoryFragment.findOrCreateRetainFragment(getActivity().getSupportFragmentManager());
                    Bundle args = new Bundle();
                    args.putString(CategoryFragment.EXTRA_TITLE, category);
                    args.putString(CategoryFragment.EXTRA_URL, category_url);
                    args.putBoolean(CategoryFragment.EXTRA_REFRESH, true);
                    fragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_category, fragment)
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
            instance.memoryCache.put(key, bitmap);
            return true;
        }

        return false;
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return (instance != null) ? instance.memoryCache.get(key) : null;
    }

    public static CategoryFragment findOrCreateRetainFragment(FragmentManager fm) {
        CategoryFragment fragment = (CategoryFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            return new CategoryFragment();
        }
        return fragment;
    }

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
            Log.e("CategoryFragment", e.getMessage());
        }

        // -get attriuteSet
        AttributeSet attrs = Xml.asAttributeSet(pullParser);

        // -create progressBar and add to listView
        return new ProgressBar(getActivity(), attrs);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getRefreshTextView() {
        return refreshTextView;
    }

    public ImageView getSplashScreenView() {
        return splashScreenView;
    }

    public ProgressBar getFooterProgressBar() {
        return footerProgressBar;
    }

    public GridViewWithHeaderAndFooter getGridViewWithHeaderAndFooter() {
        return gridViewWithHeaderAndFooter;
    }

    public void debugCache() {
        Map<String, Bitmap> cacheSnapshot = memoryCache.snapshot();
        if (cacheSnapshot.isEmpty())
            Log.d(TAG, "No entries");
        else
            for (Map.Entry<String, Bitmap> e : cacheSnapshot.entrySet())
                Log.d(TAG, Integer.toString(e.getValue().hashCode()) + ": " + e.getKey() + ", " + e.getKey().length());
    }
}
