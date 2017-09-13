package app.morningsignout.com.morningsignoff.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.image_loading.ArticleListAdapter;
import app.morningsignout.com.morningsignoff.util.ProgressIndicator;
import app.morningsignout.com.morningsignoff.util.ProgressReactor;

/**
 * Created by shinray on 5/20/2017.
 * Daniel: Only use this task to fetch the "next page" or a new "first page" of a list of articles
 * Next page: requestPageNum == adapter.getPageNum() + 1
 * First page: requestPageNum == 1
 *
 * Context is for checking network connection and toasting.
 * ProgressIndicator is a class that handles the progressBar-related views during the task. It's up
 *   to you to define the functions and values of ProgressReactor in the necessary activity.
 * ProgressIndicator.Type defines what kind of view loading animations you expect (there are several
 *   in this app...)
 * Adapter is for adding new articles
 * PageNum is to validate that the task is allowed to add new articles to the adapter.
 *
 * ERRORS:
 * Are you here because of the error listener? As you can see in onPostExecute, unless these
 * comments are out of date, the only two reasons for the error listener to be called is if
 * the app is disconnected from the internet or if there are no articles given the parameters
 * (e.g. no more pages of results).
 */

public class FetchArticleListTask extends AsyncTask<String, Void, List<Article>> {
    private Context context;
    private ProgressReactor progReactor;
    private ArticleListAdapter adapter;
    private String requestParam;
    private FetchJSON.SearchType requestType;
    private int requestPageNum;

    // Flags to determine what to do if articles list returned is null
    // FIXME: other causes may exist, like an exception or disconnect in FetchJSON.getResultsJSON()
    // FIXME: think about it and add more actions in onPostExecute
    private boolean isDisconnectedFlag;
    private OnFetchErrorListener errorListener;
    private FetchError errorObject;

    public static boolean activeTaskLock = false;

    public FetchArticleListTask(Context context,
                                ProgressIndicator progressIndicator,
                                ProgressIndicator.Type type,
                                ArticleListAdapter adapter,
                                String requestParam,
                                FetchJSON.SearchType requestType,
                                int requestPageNum,
                                OnFetchErrorListener errorListener) {
        this.context = context;
        this.progReactor = new ProgressReactor(progressIndicator, type);
        this.adapter = adapter;
        this.requestParam = requestParam;
        this.requestType = requestType;
        this.requestPageNum = requestPageNum;
        this.errorListener = errorListener;
        this.errorObject = new FetchError();
    }

    @Override
    protected void onPreExecute() {
        // This "lock" should prevent multiple tasks from running at the same time. It is only ever
        // assigned to in the UI thread, so the earliest created task will lock the task first.
        if (activeTaskLock) {
            cancel(false);   // Should not start doInBackground(), should not continue task
            return;
        }

        // If the page num is not one of these two, the task should cancel.
        boolean isFirstPage = (requestPageNum == 1);
        boolean isNextPage = (adapter.getPageNum() + 1 == requestPageNum);
        if (!(isFirstPage || isNextPage)) {
            cancel(true);
            return;
        }

        activeTaskLock = true;
        progReactor.reactToProgress(ProgressReactor.START);
    }

    // Params are ignored! All values given in constructor.
    @Override
    protected List<Article> doInBackground(String... params) {
        isDisconnectedFlag = !CheckConnection.isConnected(context);
        errorObject.query = requestParam;
        errorObject.pageNum = requestPageNum;
        if (!isCancelled() && !isDisconnectedFlag) {
            try {
//                return FetchJSON.getResultsJSON(FetchJSON.SearchType.JLATEST, "latest", 1);

                switch (requestType) {
                    case JLATEST:
                    case JCATLIST:
                        return FetchJSON.getResultsJSON(requestType, requestParam, requestPageNum);
                    // Spaces and/or unsafe chars possible in query. Must encode first.
                    case JSEARCH:
                        return FetchJSON.getResultsJSON(FetchJSON.SearchType.JSEARCH,
                                URLEncoder.encode(requestParam.trim(), "UTF-8"),
                                requestPageNum);
                    default:
                        Log.e("FetchArticleListTask", "Something went wrong. Please report this to the developer.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Articles retrieved online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        // Loading should only show on first loading list
        // hide progressbar, refresh message, and refresh icon (if loading is successful)
        progReactor.reactToProgress(ProgressReactor.STOP);

        // If result is not null, load items into adapter based on requested page number
        if (articles != null) {
            if (requestPageNum != 1)
                adapter.loadMoreItems(articles, requestPageNum);
            else
                adapter.loadNewItems(articles);
        }
        else {
            // Articles is null because no internet
            if (isDisconnectedFlag) {
                Toast.makeText(context, "We had trouble trying to connect", Toast.LENGTH_SHORT).show();
            }
            // Articles is null because no results in search.
            // If the search was for a first page, then the adapter should have "no items" for results
            else if (requestPageNum == 1) {
                Toast.makeText(context,
                        "No results for \"" + errorObject.query + "\"",
                        Toast.LENGTH_LONG).show();
            }

            if (errorListener != null)
                errorListener.onFetchError(errorObject);
        }

        activeTaskLock = false;
    }

    @Override
    protected void onCancelled(List<Article> articles) {}

    public interface OnFetchErrorListener {
        void onFetchError(FetchError error);
    }

    public class FetchError {
        public String query;
        public int pageNum;
    }
}
