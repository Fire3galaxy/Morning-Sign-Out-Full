package app.morningsignout.com.morningsignoff;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;

// Thanks to http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
// http://stackoverflow.com/questions/27526831/how-to-prevent-images-loading-multiple-times-when-fast-scrolling-a-gridview
// for asynctask image loading concurrency code
public class CategoryImageTaskDrawable extends ColorDrawable {
    WeakReference<FetchCategoryImageTask> taskReference;

    public CategoryImageTaskDrawable(FetchCategoryImageTask task) {
        super(Color.GRAY);
        taskReference = new WeakReference<FetchCategoryImageTask>(task);
    }

    public FetchCategoryImageTask getFetchCategoryImageTask() {
        return taskReference.get();
    }
}
