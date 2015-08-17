package app.morningsignout.com.morningsignoff;

import android.graphics.Bitmap;
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
    SingleRow sr;
    ImageView image;

    public FetchCategoryImageTask(SingleRow singleRow, AdapterObject holder) {
        this.sr = singleRow;
        this.image = holder.image;
    }

    @Override
    protected void onPreExecute() {
        image.setVisibility(ImageView.INVISIBLE);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return (sr.imageURL != null) ? downloadBitmap(sr.imageURL) : null;
    }

    @Override
    protected void onPostExecute(final Bitmap b) {
        // imageView image
        // Preserve aspect ratio of image
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setCropToPadding(true);
        image.setImageBitmap(b);

        // Make image visible
        image.setVisibility(ImageView.VISIBLE);

        // Save image to SingleRow object for categoryAdapter's getView()
        sr.image = b;
    }

    // The dimension of the rescaled bitmap
    private static final int bitmapDimension = 100;

    // input an image URL, get its bitmap
    private Bitmap downloadBitmap(String url) {
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
            }
            else Log.e("FetchCategoryImageTask", "image url: " + url);
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
