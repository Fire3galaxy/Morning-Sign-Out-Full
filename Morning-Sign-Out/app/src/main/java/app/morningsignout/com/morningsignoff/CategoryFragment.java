package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.LruCache;
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

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
    final static String EXTRA_TITLE = "EXTRA_TITLE";
    final static String TAG = "CategoryFragment";

    String category = "";
    public LruCache<String, Bitmap> memoryCache;

    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        category = getArguments() != null ? getArguments().getString(EXTRA_TITLE) + "/" : "";

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

        final ListView listView = (ListView) rootView.findViewById(R.id.listView);
        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        listView.setAdapter(new CategoryAdapter(this, inflater));

        // Use Asynctask to fetch article from the given category
        new FetchListArticlesTask(getActivity(), listView, progressBar, 1).execute(category);

        // Setup the click listener for the listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryAdapter categoryAdapter = (CategoryAdapter) parent.getAdapter();
                SingleRow rowTemp = (SingleRow) categoryAdapter.getItem(position);
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
                    CategoryAdapter adapter = (CategoryAdapter) listView.getAdapter();

                    // Only make one request per page request
                    if (totalItemCount != 0 && lastPageNum != adapter.getPageNum()) {
                        lastPageNum = adapter.getPageNum();
                        new FetchListArticlesTask(listView.getContext(),
                                listView,
                                progressBar,
                                adapter.getPageNum() + 1).execute(category);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
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

    CategoryFragment categoryFragment;
    LayoutInflater inflater;
    Boolean canLoadMore;
    int pageNum;

    CategoryAdapter(CategoryFragment categoryFragment, LayoutInflater inflater) {
        this.categoryFragment = categoryFragment;
        this.articles = new ArrayList<SingleRow>();
        this.inflater = inflater;
        canLoadMore = true;
        pageNum = 0;
    }

    public synchronized int getPageNum() {
        return pageNum;
    }

    public synchronized void enableLoading() {
        canLoadMore = true;
    }

    public synchronized void disableLoading() {
        canLoadMore = false;
    }

    public Boolean canLoadMore() {
        return canLoadMore;
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
                articles.add(SingleRow.newInstance(moreArticles.get(i)));
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

