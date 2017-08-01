package app.morningsignout.com.morningsignoff.category;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.image_loading.FetchImageManager;
import app.morningsignout.com.morningsignoff.image_loading.FetchImageRunnable;
import app.morningsignout.com.morningsignoff.image_loading.ImageTaskDrawable;
import app.morningsignout.com.morningsignoff.image_loading.UnusedBitmapPool;
import app.morningsignout.com.morningsignoff.util.PhoneOrientation;

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in CategoryFragment and needs a reference to the GridView
// to fix the weird "GridView calls getView(0, ..) so often" issue.
public class CategoryAdapter extends BaseAdapter {
    private LayoutInflater inflater;

    private ArrayList<Article> articles;
    private Set<String> uniqueArticleNames; // FIXME: This was a temp fix a long time ago for repeats that somehow got in the list
    private int pageNum;

    private static int REQ_IMG_WIDTH = 0, REQ_IMG_HEIGHT = 0;
    private final int VIEW_HEIGHT_DP = 220; // From single_row_category's imageview

    CategoryAdapter(Activity activity, LayoutInflater inflater) {
        this.articles = new ArrayList<>();
        this.uniqueArticleNames = new HashSet<>();
        this.inflater = inflater;
        pageNum = 0;

        // Get width/height of the images we download for use in FetchImageRunnable
        // (hardcoded height of imageview in single_row_category)
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources r = activity.getResources();

        REQ_IMG_WIDTH = metrics.widthPixels;
        REQ_IMG_HEIGHT = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, VIEW_HEIGHT_DP, r.getDisplayMetrics());
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

    // Return based on if list has items
    @Override
    public boolean isEmpty() {
        return articles.isEmpty();
    }

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup){
        View row;
        AdapterObject viewHolder;

        // Fill up adapter object, or get old one
        if (view == null) {
            row = inflater.inflate(R.layout.single_row_category, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the author, imageViewReference and title of the row item
            viewHolder.title = (TextView) row.findViewById(R.id.textViewTitle);
            viewHolder.author = (TextView) row.findViewById(R.id.textViewAuthor);
            viewHolder.image = (ImageView) row.findViewById(R.id.imageView);
            row.setTag(viewHolder);
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }

        // Set the values of the row text
        Article rowTemp = articles.get(i);
        if(PhoneOrientation.isLandscape(row.getContext()))
            viewHolder.title.setLines(3);
        viewHolder.title.setText(rowTemp.getTitle());
        viewHolder.author.setText(rowTemp.getAuthor());

        // Set the bitmap image
        final Bitmap b = CategoryFragment.getBitmapFromMemCache(rowTemp.getCategoryURL());

        // Load imageViewReference into row element
        if (b == null) {
            // task is interrupted or does not exist for imageView
            if (FetchImageManager
                    .cancelPotentialWork(rowTemp.getCategoryURL(), viewHolder.image)) {
                // Recycle old bitmap if NOT IN LRUCACHE
                // tag: Set in FetchCategoryImageManager or else branch below here if bitmap was in
                //      the cache
                String oldImageUrl = (String) viewHolder.image.getTag();
                if (oldImageUrl != null && CategoryFragment.getBitmapFromMemCache(oldImageUrl) == null) {
                    Drawable d = viewHolder.image.getDrawable();

                    if (d != null && d instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) d;

                        if (bitmapDrawable.getBitmap() != null) {
                            UnusedBitmapPool.recycle(bitmapDrawable.getBitmap());
                            viewHolder.image.setImageDrawable(null);
                        }
                    }
                }

                FetchImageRunnable task = new FetchImageRunnable(
                        rowTemp.getCategoryURL(),
                        viewHolder.image,
                        REQ_IMG_WIDTH,
                        REQ_IMG_HEIGHT,
                        FetchImageManager.SENT_PICTURE_CATEGORY);
                ImageTaskDrawable taskWrapper = new ImageTaskDrawable(task);

                viewHolder.image.setImageDrawable(taskWrapper);
                FetchImageManager.runTask(task);    // After imageDrawable is set, so no race
            }
        } else {    // set saved imageViewReference
            viewHolder.image.setImageBitmap(b);
            viewHolder.image.setTag(rowTemp.getCategoryURL());
        }

        return row;
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
                    articles.add(moreArticles.get(i));
                uniqueArticleNames.add(article);
//                notifyDataSetChanged();
            }

            notifyDataSetChanged();
        }
    }
}
