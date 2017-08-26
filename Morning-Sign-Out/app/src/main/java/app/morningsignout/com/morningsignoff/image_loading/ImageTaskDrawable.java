package app.morningsignout.com.morningsignoff.image_loading;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;

// Thanks to http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
// http://stackoverflow.com/questions/27526831/how-to-prevent-images-loading-multiple-times-when-fast-scrolling-a-gridview
// for asynctask image loading concurrency code
public class ImageTaskDrawable extends ColorDrawable {
    private WeakReference<FetchImageRunnable> taskReference;

    public ImageTaskDrawable(FetchImageRunnable task) {
        super(Color.WHITE);
        taskReference = new WeakReference<>(task);
    }

    public FetchImageRunnable getFetchCategoryImageRunnable() {
        return taskReference.get();
    }
}
