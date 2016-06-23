package app.morningsignout.com.morningsignoff.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import app.morningsignout.com.morningsignoff.category.CategoryAdapter;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import app.morningsignout.com.morningsignoff.category.SingleRow;

/**
 * Created by Daniel on 6/24/2015.
 */
public class FetchCategoryImageTask extends AsyncTask<Void, Void, Bitmap> {
    CategoryFragment categoryFragment;
    SingleRow sr;
    WeakReference<ImageView> imageViewReference;

    public FetchCategoryImageTask(CategoryFragment categoryFragment, SingleRow singleRow, ImageView imageViewReference) {
        this.categoryFragment = categoryFragment;
        this.sr = singleRow;
        this.imageViewReference = new WeakReference<ImageView>(imageViewReference);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return downloadBitmap(sr.imageURL);
    }

    @Override
    protected void onPostExecute(final Bitmap b) {
        if (imageViewReference != null && b != null) {
            final ImageView imageView = imageViewReference.get();
            final FetchCategoryImageTask task = CategoryAdapter.getFetchCategoryImageTask(imageView);

            if (this == task) {
                // cache image
                categoryFragment.addBitmapToMemoryCache(sr.title, b); //Log.d("","");

                // Preserve aspect ratio of image
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // FIT_CENTER to just fit x or y
                imageView.setCropToPadding(true);
                imageView.setImageBitmap(b);
            }
        }
    }

    // input an imageViewReference URL, get its bitmap
    static public Bitmap downloadBitmap(String url) {
        if (url == null) return null;

        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                // Lowers resolution of images by subsampling imageViewReference, saves memory & time
                BitmapFactory.Options a = new BitmapFactory.Options();
                a.inSampleSize = 1;

                // Create bitmap from stream
                return BitmapFactory.decodeStream(inputStream, null, a);
            } else
                Log.e("FetchCategoryImageTask", "imageViewReference url: " + url);
        } catch (IOException e) {
            Log.e("FetchCategoryImageTask", "Error: " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    public String getUrl() {
        return sr.imageURL;
    }
}
