package app.morningsignout.com.morningsignoff.category;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.network.FetchCategoryImageManager;
import app.morningsignout.com.morningsignoff.network.FetchCategoryImageRunnable;

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in CategoryFragment and needs a reference to the GridView
// to fix the weird "GridView calls getView(0, ..) so often" issue.
public class CategoryAdapter extends BaseAdapter {
    private static final int VISIBLE_PADDING = 8; // To avoid network requests for calls to getView(0,..)

    private LayoutInflater inflater;
    private AdapterView adapterView = null;

    private ArrayList<SingleRow> articles;
    private Set<String> uniqueArticleNames; // FIXME: This was a temp fix a long time ago for repeats that somehow got in the list
    private int pageNum;
    private int firstVisibleItem, lastVisibleItem;

    static public int REQ_IMG_WIDTH = 0, REQ_IMG_HEIGHT = 0;

    CategoryAdapter(Activity activity, LayoutInflater inflater) {
        this.articles = new ArrayList<>();
        this.uniqueArticleNames = new HashSet<>();
        this.inflater = inflater;
        pageNum = 0;
        firstVisibleItem = 0;
        lastVisibleItem = 0;

        // Downloaded images need to be good quality for landscape and portrait orientations
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources r = activity.getResources();

        REQ_IMG_WIDTH = metrics.widthPixels;
        REQ_IMG_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, r.getDisplayMetrics());
    }

    public int getPageNum() {
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

    static boolean isLandscape(Context con) {
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

    // FIXME: Next Time, I notice that getView is called 10+ times for the 0th view. This could mean that
    // if an image existed in the cache already, it would be setting the bitmap from the cache into the
    // imageview multiple times.

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup){
        Log.d("CategoryAdapter", "index: " + Integer.toString(i));

        // crate a new rowItem object here
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the author, imageViewReference and title of the row item
            viewHolder.title = (TextView) row.findViewById(R.id.textViewTitle);
            viewHolder.author = (TextView) row.findViewById(R.id.textViewAuthor);
            viewHolder.image = (ImageView) row.findViewById(R.id.imageView);
            row.setTag(viewHolder);

            // Set imageView settings
            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.image.setCropToPadding(true);
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }

        SingleRow rowTemp = articles.get(i);

        // Set the values of the rowItem
        if(isLandscape(row.getContext()))
            viewHolder.title.setLines(3);
        viewHolder.title.setText(rowTemp.title);
        viewHolder.author.setText(rowTemp.description);

//        // Do not process/add images that aren't supposed to be visible
//        // GridViewWithHeaderAndFooter calls getView(0,..) REALLY often
//        if (adapterView != null && i + VISIBLE_PADDING < adapterView.getFirstVisiblePosition()) {
//            Log.d("CategoryAdapter", "View " + Integer.toString(i) + " not visible: " + adapterView.getFirstVisiblePosition());
//            return row;
//        }

        final Bitmap b = CategoryFragment.getBitmapFromMemCache(rowTemp.imageURL);
//        final Bitmap b = null;

        // Load imageViewReference into row element
        if (b == null) {
            // task is interrupted or does not exist for imageView
            if (cancelPotentialWork(rowTemp.imageURL, viewHolder.image)) {
                // Recycle old bitmap if NOT IN LRUCACHE
                String oldImageUrl = (String) viewHolder.image.getTag();
                if (oldImageUrl != null && CategoryFragment.getBitmapFromMemCache(oldImageUrl) == null) {
                    Drawable d = viewHolder.image.getDrawable();

                    if (d != null && d instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) d;

                        if (bitmapDrawable.getBitmap() != null) {
                            CategoryBitmapPool.recycle(bitmapDrawable.getBitmap());
                            viewHolder.image.setImageDrawable(null);
                        }
                    }
                }

//                Log.d("CategoryAdapter", "Made task for index " + i + ", imageView " + viewHolder.image.hashCode());
                FetchCategoryImageRunnable task = FetchCategoryImageManager
                        .getDownloadImageTask(rowTemp.imageURL, viewHolder.image);
                CategoryImageTaskDrawable taskWrapper = new CategoryImageTaskDrawable(task);

                viewHolder.image.setImageDrawable(taskWrapper);
                FetchCategoryImageManager.runTask(task);    // After imageDrawable is set, so no race
            }
        } else {    // set saved imageViewReference
            viewHolder.image.setImageBitmap(b);
        }

        return row;
    }

    public void setAdapterView(AdapterView adapterView) {
        this.adapterView = adapterView;
    }

    // This function is called along with .notifyDataSetChanged() in Asynctask's onScrollListener function
    // when the viewers scroll to the bottom of the articles
    public void loadMoreItems(List<Article> moreArticles, int pageNum){
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

    public static FetchCategoryImageRunnable getFetchCategoryImageTask(ImageView imageView) {
        if (imageView != null) {
            if (imageView.getDrawable() instanceof CategoryImageTaskDrawable) {
                CategoryImageTaskDrawable taskDrawable = (CategoryImageTaskDrawable) imageView.getDrawable();
                return taskDrawable.getFetchCategoryImageRunnable();
            }
        }
        return null;
    }

    private static boolean cancelPotentialWork(String url, ImageView imageView) {
        FetchCategoryImageRunnable task = getFetchCategoryImageTask(imageView);

        if (task != null) {
//            Log.d("CategoryAdapter", "Task is not null");
            String imageViewUrl = task.imageUrl;

            if (imageViewUrl == null || !imageViewUrl.equals(url))
                FetchCategoryImageManager.interruptThread(task);
            else
                return false;
        }
//        Log.d("CategoryAdapter", "Task is null");

        return true;
    }
}
