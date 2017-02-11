package app.morningsignout.com.morningsignoff.category;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;

import app.morningsignout.com.morningsignoff.network.FetchCategoryImageTask;

// Thanks to http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
// http://stackoverflow.com/questions/27526831/how-to-prevent-images-loading-multiple-times-when-fast-scrolling-a-gridview
// for asynctask image loading concurrency code
class CategoryImageTaskDrawable extends ColorDrawable {
    private WeakReference<FetchCategoryImageTask> taskReference;

    CategoryImageTaskDrawable(FetchCategoryImageTask task) {
        super(Color.WHITE);
        taskReference = new WeakReference<>(task);
    }

    FetchCategoryImageTask getFetchCategoryImageTask() {
        return taskReference.get();
    }
}
