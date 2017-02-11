package app.morningsignout.com.morningsignoff.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import app.morningsignout.com.morningsignoff.category.CategoryAdapter;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import app.morningsignout.com.morningsignoff.category.SingleRow;

/*
 * Created by Daniel on 6/24/2015. Used by CategoryAdapter class.
 */
public class FetchCategoryImageTask extends AsyncTask<Integer, Void, Bitmap> {
    private CategoryFragment categoryFragment;
    private SingleRow sr;
    private WeakReference<ImageView> imageViewReference;

    public FetchCategoryImageTask(CategoryFragment categoryFragment, SingleRow singleRow, ImageView imageViewReference) {
        this.categoryFragment = categoryFragment;
        this.sr = singleRow;
        this.imageViewReference = new WeakReference<>(imageViewReference);
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        if (params.length != 2)
            return null;


        Bitmap newImage = downloadBitmap(sr.imageURL, params[0], params[1]);
        // Store newImage in temporary cache HERE
        // https://developer.android.com/guide/topics/data/data-storage.html#filesInternal
        // See Saving cache files and wipe cache upon changing activities/categories/closing app

        // New ideas:
        // Compress bitmaps before caching: https://developer.android.com/reference/android/graphics/Bitmap.html#compress(android.graphics.Bitmap.CompressFormat, int, java.io.OutputStream)
        // Use a disk cache or contentProvider: https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html#disk-cache
        // Do away with asyncTasks. Try using background threads: https://developer.android.com/training/multiple-threads/communicate-ui.html

        Log.d("FetchCategoryImageTask", sr.imageURL + ": " + String.valueOf(newImage.getByteCount() / 1024.0));
//        File cache = categoryFragment.getActivity().getCacheDir();

        return newImage;
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
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // FIT_CENTER to just fit x or y
                imageView.setCropToPadding(true);
                imageView.setImageBitmap(b);
            }
        }
    }

    // input an imageViewReference URL, get its bitmap
    private static Bitmap downloadBitmap(String url, int width, int height) {
        if (url == null) return null;

        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            // Connect to image twice. Once for resolution, once for image.
            int inSampleSize = 1;

            // 1. Resolution
            InputStream inputStream = urlConnection.getInputStream();
            BitmapFactory.Options downloadOptions = new BitmapFactory.Options();
            if (inputStream != null) {
                // Check resolution of image and downscale to desired size if needed
                downloadOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, downloadOptions);
                inSampleSize = calculateInSampleSize(downloadOptions, width, height);
                inputStream.close();

                String details = width + " " + height + "; ";
                details += downloadOptions.outWidth + " " + downloadOptions.outHeight + "; ";
                details += inSampleSize;
                Log.d("FetchCategoryImageTask", details);
            }

            // 2. Image
            urlConnection.disconnect(); // Reconnect to link because you only get one go at inputStreams
            urlConnection = (HttpURLConnection) uri.openConnection();
            inputStream = urlConnection.getInputStream();

            if (inputStream != null) {
                // Create bitmap from stream
                downloadOptions.inJustDecodeBounds = false;
                downloadOptions.inSampleSize = inSampleSize;
                return BitmapFactory.decodeStream(inputStream, null, downloadOptions);
            }
        } catch (IOException e) {
//            Log.e("FetchCategoryImageTask", "Error: " + url);
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

    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
