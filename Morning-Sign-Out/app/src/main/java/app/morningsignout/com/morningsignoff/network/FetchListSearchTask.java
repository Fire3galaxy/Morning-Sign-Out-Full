package app.morningsignout.com.morningsignoff.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.image_loading.ArticleListAdapter;
import app.morningsignout.com.morningsignoff.search_results.SearchAdapter;
import app.morningsignout.com.morningsignoff.util.ProgressIndicator;
import app.morningsignout.com.morningsignoff.util.ProgressReactor;

import static android.view.View.GONE;

/**
 * Created by shinr on 5/20/2017.
 */

public class FetchListSearchTask extends AsyncTask<String, Void, List<Article>> {
    private Context context;
    private ProgressReactor progReactor;
    private ArticleListAdapter adapter;
    private int pageNum;

    public static boolean activeTaskLock = false;

    public FetchListSearchTask(Context context,
                               ProgressIndicator progressIndicator,
                               ProgressIndicator.Type type,
                               ArticleListAdapter adapter,
                               int pageNum) {
        this.context = context;
        this.progReactor = new ProgressReactor(progressIndicator, type);
        this.adapter = adapter;
        this.pageNum = pageNum;
    }

    @Override
    protected void onPreExecute() {
        // This "lock" should prevent multiple tasks from running at the same time. It is only ever
        // assigned to in the UI thread, so the earliest created task will lock the task first.
        if (activeTaskLock)
            cancel(true);   // Should not start doInBackground(), should not continue task

        activeTaskLock = true;
        progReactor.reactToProgress(true);
    }

    @Override
    protected List<Article> doInBackground(String... params) {
        if (!isCancelled()
                && adapter.getPageNum() == pageNum - 1
                && CheckConnection.isConnected(context)) {
            try {
                Log.d("Task", "A request is made " + pageNum);
                return FetchJSON.getResultsJSON(FetchJSON.SearchType.JSEARCH, params[0], pageNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Articles retrived online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        // Loading should only show on first loading list
        // hide progressbar, refresh message, and refresh icon (if loading is successful)
        progReactor.reactToProgress(false);

        // If result and adapter are not null and fragment still exists, load items
        if (articles != null)
            adapter.loadMoreItems(articles, pageNum);
        else if (adapter.isEmpty())
            Toast.makeText(context, "We had trouble trying to connect", Toast.LENGTH_SHORT).show();

        activeTaskLock = false;
    }

    @Override
    protected void onCancelled(List<Article> articles) {
        activeTaskLock = false;
    }
}
