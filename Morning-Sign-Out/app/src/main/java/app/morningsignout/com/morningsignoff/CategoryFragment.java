package app.morningsignout.com.morningsignoff;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.util.Xml;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import in.srain.cube.views.GridViewWithHeaderAndFooter;

public class CategoryFragment extends Fragment {
    static public class CategoryViews {
        public WeakReference<SwipeRefreshLayout> swipeRefresh;
        public WeakReference<ProgressBar> progressBar;
        public WeakReference<TextView> refreshTextView;
        public WeakReference<TextView> headerTextView;
        public WeakReference<ProgressBar> footerProgress;

        public boolean firstLoad;
        public boolean refresh;

        public CategoryViews() {
            swipeRefresh = null;
            progressBar = null;
            refreshTextView = null;
            headerTextView = null;
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
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter;
    private TextView headerTitle;
    protected static Integer index = null;
    private CategoryAdapter categoryAdapter;

    public CategoryFragment() {
    }

    @Override
    public void onDetach() {
        index = gridViewWithHeaderAndFooter.getFirstVisiblePosition();
        Log.d("Avi","Index value stored is "+index);
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("Avi","Category fragment created");

        category = getArguments() != null ? getArguments().getString(EXTRA_TITLE) : "";
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
            Log.d("Avi", "Old phone, change cache size");
            cacheSize = maxMemory / 3;
        } else
            cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
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
        Log.d("Avi","Category fragment view on create called");


        View rootView = inflater.inflate(R.layout.fragment_category_main, container, false);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_category);
        gridViewWithHeaderAndFooter = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView);
        String text = category.substring(0, 1).toUpperCase() + category.substring(1);
        ((TextView)rootView.findViewById(R.id.textView_categoryHeader)).setText(text);

        // Colors for refresh layout
        refreshLayout.setColorSchemeColors(Color.argb(255, 0x81, 0xbf, 0xff), Color.WHITE);

        // Views for first FetchListArticlesTask to affect

        final CategoryViews loadingViews = new CategoryViews();
        loadingViews.swipeRefresh = new WeakReference<SwipeRefreshLayout>(refreshLayout);
        loadingViews.progressBar = new WeakReference<ProgressBar>((ProgressBar) rootView.findViewById(R.id.progressBar));
        loadingViews.refreshTextView = new WeakReference<TextView>((TextView) rootView.findViewById(R.id.textView_categoryRefresh));
        loadingViews.headerTextView = new WeakReference<TextView>((TextView) headerTitle);
        loadingViews.refresh = getArguments().containsKey(EXTRA_REFRESH);
        loadingViews.firstLoad = true;
        loadingViews.refresh = getArguments().containsKey(EXTRA_REFRESH);

        //set up gridview
        setUpGridView(gridViewWithHeaderAndFooter);

        // Footer progressbar view
        footerProgressBar = getFooterProgressBar();
        gridViewWithHeaderAndFooter.addFooterView(footerProgressBar);

        // Adapter
        if(categoryAdapter == null) {
            categoryAdapter = new CategoryAdapter(this, inflater, gridViewWithHeaderAndFooter);
            // Use Asynctask to fetch article from the given category
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);
            isLoadingArticles.set(true);
            new FetchListArticlesTask(this, gridViewWithHeaderAndFooter, loadingViews, 1).execute(category);
        }
        else {
            loadingViews.refreshTextView.get().setVisibility(View.GONE);
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
                int id_int = (int) id;

                // Do nothing if id is invalid
                if (id_int < 0 || id_int > adapter.getCount())
                    return;

                SingleRow rowTemp = (SingleRow) adapter.getItem(id_int);
                String articleTitle = rowTemp.title;

                Log.d("CategoryFragment", "Position: " + String.valueOf(position) + ", id: " + String.valueOf(id));

                // Create new activity for the article here
                // feed the new activity with the URL of the page
                String articleLink = rowTemp.link;
                Intent articleActivity = new Intent(gridViewWithHeaderAndFooter.getContext(), ArticleActivity.class);

                // EXTRA_HTML_TEXT holds the html link for the article
                articleActivity.putExtra(Intent.EXTRA_HTML_TEXT, articleLink);

                // EXTRA_SHORTCUT_NAME holds the name of the article, e.g. "what life sucks in hell"
                articleActivity.putExtra(Intent.EXTRA_SHORTCUT_NAME, articleTitle);

                // EXTRA_TITLE holds the category name, e.g. "wellness/"
                articleActivity.putExtra(Intent.EXTRA_TITLE, category);

                gridViewWithHeaderAndFooter.getContext().startActivity(articleActivity);
            }
        });

        gridViewWithHeaderAndFooter.setOnScrollListener(new AbsListView.OnScrollListener() {
            int lastPageNum = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                // At last item
                if (lastVisibleItem >= totalItemCount) {
                    WrapperListAdapter wrappedAdapter = (WrapperListAdapter) gridViewWithHeaderAndFooter.getAdapter();
                    CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();

                    int pageNum = adapter.getPageNum();
                    // Only make one request per page request
                    if (totalItemCount != 0 && lastPageNum != pageNum && isLoadingArticles.weakCompareAndSet(false, true)) {
                        CategoryViews views = new CategoryViews();
                        views.firstLoad = false;
                        views.footerProgress = new WeakReference<ProgressBar>(footerProgressBar);

                        lastPageNum = pageNum;
                        new FetchListArticlesTask(CategoryFragment.this, gridViewWithHeaderAndFooter, views, pageNum + 1).execute(category);
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
                if (!gridViewWithHeaderAndFooter.getAdapter().isEmpty() || !isLoadingArticles.get()) {
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

    TextView getHeaderTextView() {
        // -get attributes
        XmlPullParser pullParser = getResources().getXml(R.xml.header_textview);

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

        // Set text to category
        TextView textView = new TextView(getActivity(), attrs);
        Log.d("CategoryFragment", "category is " + category);
        textView.setText(getReadableCategory(category));
        return textView;
    }

    ProgressBar getFooterProgressBar() {
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

    String getReadableCategory(String category) {
        // O(n) operation. Using static strings since we know all categories.
        String readableCategories[] = getResources().getStringArray(R.array.categories),
                urlCategories[] = getResources().getStringArray(R.array.categories_for_url);

        int index = -1;
        for (int i = 0; i < urlCategories.length; i++)
            if (urlCategories[i].equals(category))
                index = i;

        // Should never return -1
        return readableCategories[index];
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
    GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter;

    CategoryAdapter(CategoryFragment categoryFragment, LayoutInflater inflater, GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter) {
        this.categoryFragment = categoryFragment;
        this.articles = new ArrayList<SingleRow>();
        this.uniqueArticleNames = new HashSet<String>();
        this.inflater = inflater;
        canLoadMore = true;
        pageNum = 0;
        this.gridViewWithHeaderAndFooter = gridViewWithHeaderAndFooter;
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

    public static boolean isLandscape(Context con) {
        Display display = ((WindowManager) con.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        int orientation = display.getRotation();

        return (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270);
    }

    // Return based on if list has items
    @Override
    public boolean isEmpty() {
        return articles.isEmpty();
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
        if(isLandscape(row.getContext())){
            viewHolder.title.setLines(3);
        }
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



    // This function is called along with .notifyDataSetChanged() in Asynctask's onScrollListener function
    // when the viewers scroll to the bottom of the articles
    public synchronized void loadMoreItems(List<Article> moreArticles, int pageNum){
        // if prevent the late page from loading twice
        if(moreArticles != null && this.pageNum != pageNum){
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i) {
                String article = moreArticles.get(i).getTitle();

                // Hack-ish way of preventing the list from being populated with doubles
                // which happens if request occurs multiple times...
                if (!uniqueArticleNames.contains(article) && moreArticles.get(i).getImageURL() != null)
                    articles.add(SingleRow.newInstance(moreArticles.get(i)));
                uniqueArticleNames.add(article);
//                notifyDataSetChanged();
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

