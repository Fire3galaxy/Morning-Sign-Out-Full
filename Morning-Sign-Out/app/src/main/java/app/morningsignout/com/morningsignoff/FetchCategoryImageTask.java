package app.morningsignout.com.morningsignoff;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

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
                categoryFragment.addBitmapToMemoryCache(sr.title, b);

                // Preserve aspect ratio of image
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setCropToPadding(true);
                imageView.setImageBitmap(b);
            }
        }
    }

    // input an imageViewReference URL, get its bitmap
    private Bitmap downloadBitmap(String url) {
        if (url == null) return null;

        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                // Lowers resolution of images by subsampling imageViewReference, saves memory & time
                BitmapFactory.Options a = new BitmapFactory.Options();
                a.inSampleSize = 2;

                // Create bitmap from stream
                return BitmapFactory.decodeStream(inputStream, null, a);
            } else Log.e("FetchCategoryImageTask", "imageViewReference url: " + url);
        } catch (Exception e) {
            Log.e("FetchCategoryImageTask", "Error downloading imageViewReference from " + url);
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
