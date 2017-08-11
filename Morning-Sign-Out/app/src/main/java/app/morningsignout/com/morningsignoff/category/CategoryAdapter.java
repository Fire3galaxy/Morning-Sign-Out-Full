package app.morningsignout.com.morningsignoff.category;

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
import app.morningsignout.com.morningsignoff.image_loading.FetchImageManager;
import app.morningsignout.com.morningsignoff.image_loading.FetchImageRunnable;
import app.morningsignout.com.morningsignoff.image_loading.ArticleListAdapter;
import app.morningsignout.com.morningsignoff.image_loading.ImageTaskDrawable;
import app.morningsignout.com.morningsignoff.image_loading.UnusedBitmapPool;
import app.morningsignout.com.morningsignoff.util.FragmentWithCache;
import app.morningsignout.com.morningsignoff.util.PhoneOrientation;

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in CategoryFragment and needs a reference to the GridView
// to fix the weird "GridView calls getView(0, ..) so often" issue.
public class CategoryAdapter extends ArticleListAdapter {
    private static int REQ_IMG_WIDTH = 0, REQ_IMG_HEIGHT = 0;
    private final int VIEW_HEIGHT_DP = 220; // From single_row_category's imageview

    private FragmentWithCache fwc;

    CategoryAdapter(FragmentWithCache fwc, LayoutInflater inflater) {
        super(inflater);
        this.fwc = fwc;

        // Get width/height of the images we download for use in FetchImageRunnable
        // (hardcoded height of imageview in single_row_category)
        DisplayMetrics metrics = new DisplayMetrics();
        fwc.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources r = fwc.getActivity().getResources();

        REQ_IMG_WIDTH = metrics.widthPixels;
        REQ_IMG_HEIGHT = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, VIEW_HEIGHT_DP, r.getDisplayMetrics());
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
        final Bitmap b = fwc.getBitmapFromMemCache(rowTemp.getMediumURL());

        // Load imageViewReference into row element
        if (b == null) {
            // task is interrupted or does not exist for imageView
            if (FetchImageManager
                    .cancelPotentialWork(rowTemp.getMediumURL(), viewHolder.image)) {
                // Recycle old bitmap if NOT IN LRUCACHE
                // tag: Set in FetchCategoryImageManager or else branch below here if bitmap was in
                //      the cache
                String oldImageUrl = (String) viewHolder.image.getTag();
                if (oldImageUrl != null && fwc.getBitmapFromMemCache(oldImageUrl) == null) {
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
                        fwc,
                        viewHolder.image,
                        rowTemp.getMediumURL(),
                        REQ_IMG_WIDTH,
                        REQ_IMG_HEIGHT);
                ImageTaskDrawable taskWrapper = new ImageTaskDrawable(task);

                viewHolder.image.setImageDrawable(taskWrapper);
                FetchImageManager.runTask(task);    // After imageDrawable is set, so no race
            }
        } else {    // set saved imageViewReference
            viewHolder.image.setImageBitmap(b);
            viewHolder.image.setTag(rowTemp.getMediumURL());
        }

        return row;
    }
}
