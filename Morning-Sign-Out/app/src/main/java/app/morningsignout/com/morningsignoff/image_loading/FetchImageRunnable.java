package app.morningsignout.com.morningsignoff.image_loading;

import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.lang.Runnable;
import java.lang.Thread;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.util.FragmentWithCache;

/**
 * Created by Daniel on 2/11/2017. A Runnable task that will run in the background and fetch images
 * a la Instagram I hope.
 */

public class FetchImageRunnable implements Runnable {
    public static String NO_IMAGE = "No image";

    Thread currentThread;
    FragmentWithCache fwc;
    ImageView imageView;
    String imageUrl;
    private int viewWidth, viewHeight;
    private int managerMessage;

    public FetchImageRunnable(FragmentWithCache fwc, ImageView imageView, String imageUrl, int viewWidth, int viewHeight, int message) {
        this.fwc = fwc;
        this.imageView = imageView;
        this.imageUrl = imageUrl;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.managerMessage = message;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        currentThread = Thread.currentThread();

        if (Thread.interrupted())
            return;

        Bitmap downloadedImage = null;
        if (!imageUrl.equals(NO_IMAGE))
            downloadedImage = downloadBitmap();
        else
            downloadedImage = BitmapFactory.decodeResource(
                    imageView.getResources(), R.drawable.no_image);

        ImageSenderObject objectToSend =
                new ImageSenderObject(fwc, imageView, downloadedImage, this, imageUrl);
        Handler handler = FetchImageManager.getHandler();
        if (handler != null)
            handler.obtainMessage(managerMessage, objectToSend).sendToTarget();
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
                }

                urlConnection.disconnect(); // Reconnect to link because you only get one go at inputStreams
                urlConnection = (HttpURLConnection) uri.openConnection();
                inputStream = urlConnection.getInputStream();
            }

            // 2. Image
            if (inputStream != null) {
                // Create bitmap from stream or get one from bitmapPool
                downloadOptions.inJustDecodeBounds = false;
                downloadOptions.inSampleSize = inSampleSize;
                downloadOptions.inMutable = true;
                Bitmap bitmapToUse = UnusedBitmapPool.getBitmap(downloadOptions);

                // Use bitmap pool's given bitmap if it exists
                if (bitmapToUse != null)
                    downloadOptions.inBitmap = bitmapToUse;

                // Don't decode the image if thread is interrupted
                if (Thread.interrupted()) {
                    if (bitmapToUse != null) {
                        bitmapToUse.recycle();
                    }
                    return null;
                }

                return BitmapFactory.decodeStream(inputStream, null, downloadOptions);
            }
        } catch (IOException e) {
            Log.e("Silver Lining", "Error downloading image: " + imageUrl);
        } catch (IllegalArgumentException e) {
            Log.e("Silver Lining", "Thread interrupted during decoding (A natural fact of life)");
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
