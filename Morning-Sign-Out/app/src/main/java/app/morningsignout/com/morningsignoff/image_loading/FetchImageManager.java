package app.morningsignout.com.morningsignoff.image_loading;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import java.lang.Runnable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import app.morningsignout.com.morningsignoff.search_results.SearchFragment;

/**
 * Created by Daniel on 2/11/2017. Manager that houses thread pool.
 */

public class FetchImageManager {
    private static final FetchImageManager instance = new FetchImageManager();
    public static int SENT_PICTURE_CATEGORY = 1, SENT_PICTURE_SEARCH = 2;

    private Handler myHandler;
    private ThreadPoolExecutor imagesThreadPool;

    private FetchImageManager() {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        final int KEEP_ALIVE_TIME = 1;
        final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        myHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                ImageSenderObject sentObject =
                        (ImageSenderObject) inputMessage.obj;

                final FetchImageRunnable task = getFetchCategoryImageTask(sentObject.imageView);
                if (sentObject.task.equals(task)                    // A newer thread is getting image for this view
                        && !task.currentThread.isInterrupted()      // Thread interrupt = don't use bitmap
                        && sentObject.downloadedImage != null) {    // No internet = no bitmap

                    sentObject.fwc.addBitmapToMemoryCache(sentObject.imageUrl, sentObject.downloadedImage);
                    sentObject.imageView.setTag(sentObject.imageUrl); // Checked in CategoryAdapter
                    sentObject.imageView.setImageBitmap(sentObject.downloadedImage);
                }
            }
        };

        LinkedBlockingQueue<Runnable> imagesWorkQueue = new LinkedBlockingQueue<>();
        imagesThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                imagesWorkQueue);
    }

    static public void runTask(FetchImageRunnable task) {
        instance.imagesThreadPool.execute(task);
    }

    private static void interruptThread(FetchImageRunnable task) {
        // Don't interrupt same task twice (is this an issue? not sure)
        synchronized(instance) {
            if (task.currentThread != null)
                task.currentThread.interrupt();
        }
    }

    private static FetchImageRunnable getFetchCategoryImageTask(ImageView imageView) {
        if (imageView != null) {
            if (imageView.getDrawable() instanceof ImageTaskDrawable) {
                ImageTaskDrawable taskDrawable = (ImageTaskDrawable) imageView.getDrawable();
                return taskDrawable.getFetchCategoryImageRunnable();
            }
        }
        return null;
    }

    public static boolean cancelPotentialWork(String url, ImageView imageView) {
        FetchImageRunnable task = getFetchCategoryImageTask(imageView);
        if (task != null) {
            String imageViewUrl = task.imageUrl;

            if (imageViewUrl == null || !imageViewUrl.equals(url))
                interruptThread(task);
            else
                return false;
        }
        return true;
    }

    static Handler getHandler() {
        return (instance != null) ? instance.myHandler : null;
    }
}
