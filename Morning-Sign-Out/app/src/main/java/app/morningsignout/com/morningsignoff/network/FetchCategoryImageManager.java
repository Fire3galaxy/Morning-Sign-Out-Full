package app.morningsignout.com.morningsignoff.network;

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
//        if (imageViewReference != null && b != null) {
//            final ImageView imageView = imageViewReference.get();
//            final FetchCategoryImageTask task = CategoryAdapter.getFetchCategoryImageTask(imageView);
//
//            if (this == task) {
//                // cache image
//                categoryFragment.addBitmapToMemoryCache(sr.title, b);
//
//                // Preserve aspect ratio of image
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // FIT_CENTER to just fit x or y
//                imageView.setCropToPadding(true);
//                imageView.setImageBitmap(b);
//            }
//        }

                    CategoryImageSenderObject sentObject =
                            (CategoryImageSenderObject) inputMessage.obj;

                    final FetchCategoryImageRunnable task = CategoryAdapter.getFetchCategoryImageRunnable(sentObject.imageView);
                    if (sentObject.task.equals(task)) {
                        CategoryFragment.addBitmapToMemoryCache(sentObject.imageUrl, sentObject.downloadedImage);
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
        Log.d("FetchCategoryImageManager", "Interrupt thread with task: " + task.imageUrl);
        synchronized(instance) {
            if (task.currentThread != null)
                task.currentThread.interrupt();
        }
    }
}
