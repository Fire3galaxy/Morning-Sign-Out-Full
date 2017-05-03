package app.morningsignout.com.morningsignoff.network;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.lang.Runnable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

import app.morningsignout.com.morningsignoff.category.CategoryAdapter;
import app.morningsignout.com.morningsignoff.category.CategoryBitmapPool;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;

/**
 * Created by Daniel on 2/11/2017. Manager that houses thread pool.
 */

public class FetchCategoryImageManager {
    public static FetchCategoryImageManager instance = new FetchCategoryImageManager();
    public static int SENT_PICTURE = 1;

    public Handler myHandler;
    private LinkedBlockingQueue<Runnable> imagesWorkQueue;
    private ThreadPoolExecutor imagesThreadPool;

    private FetchCategoryImageManager() {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        final int KEEP_ALIVE_TIME = 1;
        final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        myHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                if (inputMessage.what == SENT_PICTURE) {
                    CategoryImageSenderObject sentObject =
                            (CategoryImageSenderObject) inputMessage.obj;

                    final FetchCategoryImageRunnable task = CategoryAdapter.getFetchCategoryImageTask(sentObject.imageView);
                    if (sentObject.task.equals(task)                    // A newer thread is getting image for this view
                            && !task.currentThread.isInterrupted()      // Thread interrupt = don't use bitmap
                            && sentObject.downloadedImage != null) {    // No internet = no bitmap
                        CategoryFragment.addBitmapToMemoryCache(sentObject.imageUrl, sentObject.downloadedImage);
                        sentObject.imageView.setTag(sentObject.imageUrl); // Checked in CategoryAdapter
                        sentObject.imageView.setImageBitmap(sentObject.downloadedImage);
                    }
                }
            }
        };

        imagesWorkQueue = new LinkedBlockingQueue<Runnable>();
        imagesThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                imagesWorkQueue);
    }

    static public FetchCategoryImageRunnable getDownloadImageTask(String imageUrl, ImageView imageView) {
        return new FetchCategoryImageRunnable(imageUrl, imageView);
    }

    static public void runTask(FetchCategoryImageRunnable task) {
        instance.imagesThreadPool.execute(task);
    }

    static public void interruptThread(FetchCategoryImageRunnable task) {
        // Don't interrupt same task twice (is this an issue? not sure)
        synchronized(instance) {
            if (task.currentThread != null)
                task.currentThread.interrupt();
        }
    }
}
