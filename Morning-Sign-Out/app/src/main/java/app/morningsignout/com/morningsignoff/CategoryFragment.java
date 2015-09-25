package app.morningsignout.com.morningsignoff;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
    final static String EXTRA_TITLE = "EXTRA_TITLE";

    String category = "";
    LruCache<String, Bitmap> memoryCache;

    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        category = getArguments() != null ? getArguments().getString(EXTRA_TITLE) + "/" : "";

        /* Thanks to http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
         * for caching code */
        // max memory of hdpi ~32 MB
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // memory for images ~4.5 MB = 7-8 images
        final int cacheSize = maxMemory / 7;

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

        listView.setAdapter(new CategoryAdapter(this.getActivity()));

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
}

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in the FetchListArticlesTask which is called in CategoryActivity
class CategoryAdapter extends BaseAdapter {
    ArrayList<SingleRow> articles;
    Context context;
    int pageNum;
    Boolean canLoadMore;

    CategoryAdapter(Context c) {
        this.articles = new ArrayList<SingleRow>();
        context = c;
        pageNum = 0;
        canLoadMore = true;
    }

    public synchronized int getPageNum() {
        return pageNum;
    }

    public void enableLoading() {
        canLoadMore = true;
    }

    public void disableLoading() {
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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the description, image and title of the row item
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

        /* On getting view, set this to invisible until loaded. (issue before: old image seen
           before new image on fast scroll) Mostly fixed by this, but on fast scroll down, still
           shows a little */
        viewHolder.image.setImageDrawable(null);

        // Set the values of the rowItem
        SingleRow rowTemp = articles.get(i);
        viewHolder.title.setText(rowTemp.title);
        viewHolder.description.setText(rowTemp.description);

        String s = "null";
        if (rowTemp.image != null) s = "not null";
        Log.e("ImageLog", "Item " + Integer.toString(i) + ", is " + s);

        // Load image into row element
        if (rowTemp.image == null)      // download
            new FetchCategoryImageTask(rowTemp, viewHolder.image, context.getResources()).execute();
        else {                          // set saved image
            // Cropping image to preserve aspect ratio
            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.image.setCropToPadding(true);
            viewHolder.image.setImageBitmap(rowTemp.image);
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
}

