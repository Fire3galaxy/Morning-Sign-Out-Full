package app.morningsignout.com.morningsignoff;

import android.graphics.Bitmap;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Daniel on 6/24/2015.
 */
public class FetchCategoryImageTask extends AsyncTask<Void, Void, Bitmap> {
    CategoryFragment categoryFragment;
    SingleRow sr;
    ImageView image;

    public FetchCategoryImageTask(CategoryFragment categoryFragment, SingleRow singleRow, ImageView image) {
        this.categoryFragment = categoryFragment;
        this.sr = singleRow;
        this.image = image;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap b = downloadBitmap(sr.imageURL);
        categoryFragment.addBitmapToMemoryCache(sr.title, b);

        return b;
    }

    @Override
    protected void onPostExecute(final Bitmap b) {
        // imageView image
        // Preserve aspect ratio of image
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setCropToPadding(true);
        image.setImageBitmap(b);
    }

    // input an image URL, get its bitmap
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
                // Lowers resolution of images by subsampling image, saves memory & time
                BitmapFactory.Options a = new BitmapFactory.Options();
                a.inSampleSize = 2;

                // Create bitmap from stream
                return BitmapFactory.decodeStream(inputStream, null, a);
            } else Log.e("FetchCategoryImageTask", "image url: " + url);
        } catch (Exception e) {
            Log.e("FetchCategoryImageTask", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

}
