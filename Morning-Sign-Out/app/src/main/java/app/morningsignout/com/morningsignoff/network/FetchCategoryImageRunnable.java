package app.morningsignout.com.morningsignoff.network;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Runnable;
import java.lang.Thread;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import app.morningsignout.com.morningsignoff.category.CategoryAdapter;

/**
 * Created by Daniel on 2/11/2017. A Runnable task that will run in the background and fetch images
 * a la Instagram I hope.
 */

public class FetchCategoryImageRunnable implements Runnable {
    Thread currentThread;

    public String imageUrl;
    int viewWidth, viewHeight;
    WeakReference<ImageView> imageViewRef;

    public FetchCategoryImageRunnable(String imageUrl, ImageView imageView) {
        this.viewWidth = CategoryAdapter.REQ_IMG_WIDTH;
        this.viewHeight = CategoryAdapter.REQ_IMG_HEIGHT;
        this.imageUrl = imageUrl;
        this.imageViewRef = new WeakReference<>(imageView);
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        currentThread = Thread.currentThread();

//        long start = System.currentTimeMillis();

        Bitmap downloadedImage = downloadBitmap();

        // Include chance to cancel thread (return) instead of trying to pass bitmap up to UI
        // if img is not needed anymore or imageviewref is null

//        CategoryBitmapPool.push(downloadedImage);

        CategoryImageSenderObject objectToSend =
                new CategoryImageSenderObject(imageUrl, imageViewRef.get(), downloadedImage, this);
        FetchCategoryImageManager.instance.myHandler
                .obtainMessage(FetchCategoryImageManager.SENT_PICTURE, objectToSend)
                .sendToTarget();

//        Log.d("FetchCategoryImageRunnable", "Running time for " + imageUrl + ": " +
//                Float.toString((System.currentTimeMillis() - start) / 1000.0f));
    }

    // input an imageViewReference URL, get its bitmap
    private Bitmap downloadBitmap() {
        if (imageUrl == null) return null;

        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(imageUrl);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            // Connect to image twice. Once for resolution, once for image.
            InputStream inputStream = urlConnection.getInputStream();
            BitmapFactory.Options downloadOptions = new BitmapFactory.Options();
            int inSampleSize = 1;

            // 1. Resolution
            if (viewWidth != 0 && viewHeight != 0) {
                if (inputStream != null) {
                    // Check resolution of image and downscale to desired size if needed
                    downloadOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, downloadOptions);
                    inSampleSize = calculateInSampleSize(downloadOptions);
                    inputStream.close();

                    String details = viewWidth + " " + viewHeight + "; ";
                    details += downloadOptions.outWidth + " " + downloadOptions.outHeight + "; ";
                    details += inSampleSize;
                    Log.d("FetchCategoryImageTask", details);
                }

                urlConnection.disconnect(); // Reconnect to link because you only get one go at inputStreams
                urlConnection = (HttpURLConnection) uri.openConnection();
                inputStream = urlConnection.getInputStream();
            }

            // 2. Image
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

    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    private int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > viewHeight || width > viewWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= viewHeight
                    && (halfWidth / inSampleSize) >= viewWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
