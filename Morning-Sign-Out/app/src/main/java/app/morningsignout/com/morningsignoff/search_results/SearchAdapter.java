package app.morningsignout.com.morningsignoff.search_results;

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
import android.widget.ImageView;
import android.widget.TextView;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.image_loading.ArticleListAdapter;
import app.morningsignout.com.morningsignoff.image_loading.UnusedBitmapPool;
import app.morningsignout.com.morningsignoff.image_loading.ImageTaskDrawable;
import app.morningsignout.com.morningsignoff.image_loading.FetchImageManager;
import app.morningsignout.com.morningsignoff.image_loading.FetchImageRunnable;
import app.morningsignout.com.morningsignoff.util.PhoneOrientation;

/**
 * Created by shinr on 6/1/2017.
 */

public class SearchAdapter extends ArticleListAdapter {
    // Holds the results from display metrics
    private static int REQ_IMG_WIDTH = 0, REQ_IMG_HEIGHT = 0;
    private final int VIEW_HEIGHT_DP = 86;  // Update this if xml layout single_row_search is updated

    // constructor
    SearchAdapter(Activity activity, LayoutInflater inflater) {
        super(inflater);

        // Get width/height of the images we download for use in FetchImageRunnable
        // (hardcoded height of imageview in single_row_search)
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources r = activity.getResources();

        REQ_IMG_WIDTH = metrics.widthPixels;
        REQ_IMG_HEIGHT = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, VIEW_HEIGHT_DP, r.getDisplayMetrics());
    }

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        // create a new rowItem object here
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row_search, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the author, imageViewReference and title of the row item
            viewHolder.title = (TextView) row.findViewById(R.id.textViewTitle_search);
            viewHolder.image = (ImageView) row.findViewById(R.id.imageView_search);
            viewHolder.excerpt = (TextView) row.findViewById(R.id.textViewExcerpt_search);
            row.setTag(viewHolder);
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }

        Article rowTemp = articles.get(i);

        // Set the values of the rowItem
        if(PhoneOrientation.isLandscape(row.getContext()))
            viewHolder.title.setLines(3);
        viewHolder.title.setText(rowTemp.getTitle());
        viewHolder.excerpt.setText(rowTemp.getExcerpt());

        // add image stuff here
        // I know it says categoryURL, but that's where image is stored for now.
        // TODO: change when appropriate for different image quality.
        final Bitmap b = SearchFragment.getBitmapFromMemCache(rowTemp.getMediumURL());

        // Load imageViewReference into row element
        if (b == null) {
            // task is interrupted or does not exist for imageView
            if (FetchImageManager
                    .cancelPotentialWork(rowTemp.getMediumURL(), viewHolder.image)) {
                // Recycle old bitmap if NOT in LruCache
                // tag: Set in FetchCategoryImageManager or else branch below here if bitmap was in
                //      the cache
                String oldImageUrl = (String) viewHolder.image.getTag();
                if (oldImageUrl != null && SearchFragment.getBitmapFromMemCache(oldImageUrl) == null) {
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
                        rowTemp.getMediumURL(),
                        viewHolder.image,
                        REQ_IMG_WIDTH,
                        REQ_IMG_HEIGHT,
                        FetchImageManager.SENT_PICTURE_SEARCH);
                ImageTaskDrawable taskWrapper = new ImageTaskDrawable(task);

                viewHolder.image.setImageDrawable(taskWrapper);
                FetchImageManager.runTask(task);
            }
        } else {
            viewHolder.image.setImageBitmap(b);
            viewHolder.image.setTag(rowTemp.getMediumURL());
        }

        return row;
    }
}
