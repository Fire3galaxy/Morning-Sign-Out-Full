package app.morningsignout.com.morningsignoff.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.image_loading.ArticleListAdapter;
import app.morningsignout.com.morningsignoff.util.ProgressIndicator;
import app.morningsignout.com.morningsignoff.util.ProgressReactor;

/**
 * Created by shinray on 5/20/2017.
 * Daniel: Only use this task to fetch the "next page" or a new "first page" of a list of articles
 * Next page: requestedPageNum == adapter.getPageNum() + 1
 * First page: requestedPageNum == 0
 *
 * Context is for checking network connection and toasting.
 * ProgressReactor is a class that handles the progressBar-related views during the task. It's up
 *   to you to define the functions and values of ProgressReactor in the necessary activity.
 * ProgressReactor.Type defines what kind of view loading animations you expect (there are several
 *   in this app...)
 * Adapter is for adding new articles
 * PageNum is to validate that the task is allowed to add new articles to the adapter.
 */

public class FetchListSearchTask extends AsyncTask<String, Void, List<Article>> {
    private Context context;
    private ProgressReactor progReactor;
    private ArticleListAdapter adapter;
    private int requestedPageNum;

    public static boolean activeTaskLock = false;

    public FetchListSearchTask(Context context,
                               ProgressIndicator progressIndicator,
                               ProgressIndicator.Type type,
                               ArticleListAdapter adapter,
                               int requestedPageNum) {
        this.context = context;
        this.progReactor = new ProgressReactor(progressIndicator, type);
        this.adapter = adapter;
        this.requestedPageNum = requestedPageNum;
    }

    @Override
    protected void onPreExecute() {
        // This "lock" should prevent multiple tasks from running at the same time. It is only ever
        // assigned to in the UI thread, so the earliest created task will lock the task first.
        if (activeTaskLock) {
            cancel(true);   // Should not start doInBackground(), should not continue task
            return;
        }

        // If the page num is not one of these two, the task should cancel.
        boolean isFirstPage = (requestedPageNum == 0);
        boolean isNextPage = (adapter.getPageNum() + 1 == requestedPageNum);
        if (!(isFirstPage || isNextPage)) {
            cancel(true);
            return;
        }

        activeTaskLock = true;
        progReactor.reactToProgress(true);
    }

    @Override
    protected List<Article> doInBackground(String... params) {
        if (!isCancelled() && CheckConnection.isConnected(context)) {
            try {
                Log.d("Task", "A request is made " + requestedPageNum);
                return FetchJSON.getResultsJSON(FetchJSON.SearchType.JSEARCH, params[0], requestedPageNum);
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

        // If result is not null, load items into adapter based on requested page number
        if (articles != null) {
            if (requestedPageNum != 0)
                adapter.loadMoreItems(articles, requestedPageNum);
            else
                adapter.loadNewItems(articles);
        }
        else if (adapter.isEmpty())
            Toast.makeText(context, "We had trouble trying to connect", Toast.LENGTH_SHORT).show();

        activeTaskLock = false;
    }

    @Override
    protected void onCancelled(List<Article> articles) {
        activeTaskLock = false;
    }
}
