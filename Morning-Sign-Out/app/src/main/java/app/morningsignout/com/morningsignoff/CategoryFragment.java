package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CategoryFragment extends Fragment {
    static public class CategoryViews {
        public WeakReference<SwipeRefreshLayout> swipeRefresh;
        public WeakReference<ProgressBar> progressBar;
        public WeakReference<TextView> refreshTextView;
        public WeakReference<ProgressBar> footerProgress;

        public boolean firstLoad;
        public boolean refresh;

        public CategoryViews() {
            swipeRefresh = null;
            progressBar = null;
            refreshTextView = null;
            footerProgress = null;
        }
    }

    final static String EXTRA_TITLE = "EXTRA_TITLE";
    final static String EXTRA_REFRESH = "EXTRA_REFRESH";
    final static String TAG = "CategoryFragment";

    String category = "";
    AtomicBoolean isLoadingArticles;
    public LruCache<String, Bitmap> memoryCache;
    ProgressBar footerProgressBar;

    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        category = getArguments() != null ? getArguments().getString(EXTRA_TITLE) : "";
        isLoadingArticles = new AtomicBoolean(false);

        /* Thanks to http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
         * for caching code */
        // max memory of hdpi ~32 MB
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // memory for images ~4.5 MB = 7-8 images
        final int cacheSize = maxMemory / 7;

        Log.d("CategoryFragment", Integer.toString(cacheSize));

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_main, container, false);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_category);
        final ListView listView = (ListView) rootView.findViewById(R.id.listView);

        // Views for first FetchListArticlesTask to affect
        final CategoryViews loadingViews = new CategoryViews();
        loadingViews.swipeRefresh = new WeakReference<SwipeRefreshLayout>(refreshLayout);
        loadingViews.progressBar = new WeakReference<ProgressBar>((ProgressBar) rootView.findViewById(R.id.progressBar));
        loadingViews.refreshTextView = new WeakReference<TextView>((TextView) rootView.findViewById(R.id.textView_categoryRefresh));
        loadingViews.firstLoad = true;

        if (getArguments().containsKey(EXTRA_REFRESH)) loadingViews.refresh = true;
        else loadingViews.refresh = false;

        // Footer progressbar view
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
        footerProgressBar = new ProgressBar(getActivity(), attrs);
        listView.addFooterView(footerProgressBar);

        //FIXME: Change fetchListArticlesTask to get the footer and change visibility

        // Adapter
        listView.setAdapter(new CategoryAdapter(this, inflater));

        // Use Asynctask to fetch article from the given category
        isLoadingArticles.set(true);
        new FetchListArticlesTask(this, listView, loadingViews, 1).execute(category);

        // Setup the click listener for the listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WrapperListAdapter wrappedAdapter = (WrapperListAdapter) parent.getAdapter();

                CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();
                SingleRow rowTemp = (SingleRow) adapter.getItem(position);
                String articleTitle = rowTemp.title;

                // Create new activity for the article here
                // feed the new activity with the URL of the page
                String articleLink = rowTemp.link;
                Intent articleActivity = new Intent(listView.getContext(), ArticleActivity.class);

                // EXTRA_HTML_TEXT holds the html link for the article
                articleActivity.putExtra(Intent.EXTRA_HTML_TEXT, articleLink);

                // EXTRA_SHORTCUT_NAME holds the name of the article, e.g. "what life sucks in hell"
                articleActivity.putExtra(Intent.EXTRA_SHORTCUT_NAME, articleTitle);

                // EXTRA_TITLE holds the category name, e.g. "wellness/"
                articleActivity.putExtra(Intent.EXTRA_TITLE, category);

                listView.getContext().startActivity(articleActivity);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int lastPageNum = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;

                // At last item
                if (lastVisibleItem >= totalItemCount) {
                    WrapperListAdapter wrappedAdapter = (WrapperListAdapter) listView.getAdapter();
                    CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();

                    int pageNum = adapter.getPageNum();
                    // Only make one request per page request
                    if (totalItemCount != 0 && lastPageNum != pageNum && isLoadingArticles.weakCompareAndSet(false, true)) {
                        CategoryViews views = new CategoryViews();
                        views.firstLoad = false;
                        views.footerProgress = new WeakReference<ProgressBar>(footerProgressBar);

                        lastPageNum = pageNum;
                        new FetchListArticlesTask(CategoryFragment.this, listView, views, pageNum + 1).execute(category);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!listView.getAdapter().isEmpty() || !isLoadingArticles.get()) {
                    // Reload categoryFragment
                    CategoryFragment fragment =
                            CategoryFragment.findOrCreateRetainFragment(getActivity().getSupportFragmentManager());
                    Bundle args = new Bundle();
                    args.putString(CategoryFragment.EXTRA_TITLE, category);
                    args.putBoolean(CategoryFragment.EXTRA_REFRESH, true);
                    fragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_category, fragment)
                            .commit();
                }
                refreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    public void setisLoadingFalse() {
        isLoadingArticles.set(false);
    }

    public static CategoryFragment findOrCreateRetainFragment(FragmentManager fm) {
        CategoryFragment fragment = (CategoryFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            return new CategoryFragment();
        }
        return fragment;
    }
}

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in the FetchListArticlesTask which is called in CategoryActivity
class CategoryAdapter extends BaseAdapter {
    ArrayList<SingleRow> articles;
    Set<String> uniqueArticleNames;

    CategoryFragment categoryFragment;
    LayoutInflater inflater;
    Boolean canLoadMore;
    int pageNum;

    CategoryAdapter(CategoryFragment categoryFragment, LayoutInflater inflater) {
        this.categoryFragment = categoryFragment;
        this.articles = new ArrayList<SingleRow>();
        this.uniqueArticleNames = new HashSet<String>();
        this.inflater = inflater;
        canLoadMore = true;
        pageNum = 0;
    }

    public synchronized int getPageNum() {
        return pageNum;
    }

    // Get the number of row items
    @Override
    public int getCount(){
        return articles.size();
    }

    @Override
    public Object getItem(int i){
        return articles.get(i);
    }

    // Get the item id, since no database, the id is its assignment
    @Override
    public long getItemId(int i){
        return i;
    }

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup){
        // crate a new rowItem object here
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the description, imageViewReference and title of the row item
            viewHolder.title = (TextView) row.findViewById(R.id.textView);
            viewHolder.description = (TextView) row.findViewById(R.id.textView2);
            viewHolder.image = (ImageView) row.findViewById(R.id.imageView);
            viewHolder.pb = (ProgressBar) row.findViewById(R.id.progressBarSingleRow);
            row.setTag(viewHolder);
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }

        // Set the values of the rowItem
        SingleRow rowTemp = articles.get(i);
        viewHolder.title.setText(rowTemp.title);
        viewHolder.description.setText(rowTemp.description);

        final Bitmap b = categoryFragment.getBitmapFromMemCache(rowTemp.title);

        // Load imageViewReference into row element
        if (b == null) {    // download
            if (cancelPotentialWork(rowTemp.imageURL, viewHolder.image)) {
                FetchCategoryImageTask task = new FetchCategoryImageTask(categoryFragment,
                        rowTemp, viewHolder.image);
                CategoryImageTaskDrawable taskWrapper = new CategoryImageTaskDrawable(task);

                viewHolder.image.setImageDrawable(taskWrapper);
                task.execute();
            }
        } else {            // set saved imageViewReference
            // Cropping imageViewReference to preserve aspect ratio
            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.image.setCropToPadding(true);
            viewHolder.image.setImageBitmap(b);
        }

        return row;
    }

    // This function is called along with .notifyDataSetChange() in Asynctask's onScrollListener function
    // when the viewers scroll to the bottom of the articles
    public synchronized void loadMoreItems(List<Article> moreArticles, int pageNum){
        // if prevent the late page from loading twice
        if(moreArticles != null && this.pageNum != pageNum){
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i) {
                String article = moreArticles.get(i).getTitle();

                if (!uniqueArticleNames.contains(article))
                    articles.add(SingleRow.newInstance(moreArticles.get(i)));
                uniqueArticleNames.add(article);
//                notifyDataSetChanged();
                Log.d("Nothing","at all");
            }

            notifyDataSetChanged();
        }
    }

    private static boolean cancelPotentialWork(String url, ImageView imageView) {
        FetchCategoryImageTask task = getFetchCategoryImageTask(imageView);

        if (task != null) {
            String imageViewUrl = task.getUrl();

            if (imageViewUrl == null || !imageViewUrl.equals(url))
                task.cancel(true);
            else
                return false;
        }
        return true;
    }

    public static FetchCategoryImageTask getFetchCategoryImageTask(ImageView imageView) {
        if (imageView != null) {
            if (imageView.getDrawable() instanceof CategoryImageTaskDrawable) {
                CategoryImageTaskDrawable taskDrawable = (CategoryImageTaskDrawable) imageView.getDrawable();
                return taskDrawable.getFetchCategoryImageTask();
            }
        }
        return null;
    }
}

