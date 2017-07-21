package app.morningsignout.com.morningsignoff.search_results;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;

import app.morningsignout.com.morningsignoff.network.FetchCategoryImageRunnable;

/**
 * Created by shinr on 7/10/2017.
 */

public class SearchImageTaskDrawable extends ColorDrawable {
    private WeakReference<FetchCategoryImageRunnable> taskReference;

    SearchImageTaskDrawable(FetchCategoryImageRunnable task) {
        super(Color.WHITE);
        taskReference = new WeakReference<>(task);
    }

    FetchCategoryImageRunnable getFetchCategoryImageRunnable() {return taskReference.get();}
}
