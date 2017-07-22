package app.morningsignout.com.morningsignoff.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import app.morningsignout.com.morningsignoff.search_results.SearchAdapter;
import app.morningsignout.com.morningsignoff.search_results.SearchFragment;

import static android.view.View.GONE;

/**
 * Created by shinr on 5/20/2017.
 */

public class FetchListSearchTask extends AsyncTask<String, Void, List<Article>>{
    private WeakReference<SearchFragment> fragmentRef;
    private int pageNum;
    private boolean isFirstLoad, isRefresh, isCancelled;

    private int adapterPageNum;

    public FetchListSearchTask(SearchFragment fragment,
                               int pageNum,
                               boolean isFirstLoad,
                               boolean isRefresh) {
        this.fragmentRef = new WeakReference<>(fragment);
        this.pageNum = pageNum;
        this.isFirstLoad = isFirstLoad;
        this.isRefresh = isRefresh;
        this.isCancelled = false;
    }

    @Override
    protected void onPreExecute() {
        if (fragmentRef.get() == null || !CheckConnection.isConnected(fragmentRef.get().getContext())) {
            isCancelled = true;
            return;
        }
        if (isFirstLoad) {
            if (isRefresh)
                fragmentRef.get().getSwipeRefreshLayout().setRefreshing(true);
            else // start refresh animation
                fragmentRef.get().getProgressBar().setVisibility(View.VISIBLE);
        } else
            fragmentRef.get().getFooterProgressBar().setVisibility(View.VISIBLE);

        WrapperListAdapter wrappedAdapter =
                (WrapperListAdapter) fragmentRef.get().getGridViewWithHeaderAndFooter().getAdapter();
        SearchAdapter adapter = (SearchAdapter) wrappedAdapter.getWrappedAdapter();
        adapterPageNum = adapter.getPageNum();
    }

    @Override
    protected List<Article> doInBackground( String... params) {
        if (isCancelled)
            return null;

        if (adapterPageNum == pageNum - 1) {
            try {
                return FetchJSON.getResultsJSON(FetchJSON.SearchType.JSEARCH, params[0], pageNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Articles retrived online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        if (isCancelled) {
            if (fragmentRef.get() != null)
                fragmentRef.get().getIsLoadingArticles().set(false);
            return;
        }

        WrapperListAdapter wrappedAdapter =
                (WrapperListAdapter) fragmentRef.get().getGridViewWithHeaderAndFooter().getAdapter();
        SearchAdapter adapter = (SearchAdapter) wrappedAdapter.getWrappedAdapter();;

        // Loading should only show on first loading list
        // hide progressbar, refresh message, and refresh icon (if loading is successful)
        if (isFirstLoad) {
            if (isRefresh)
                fragmentRef.get().getSwipeRefreshLayout().setRefreshing(false);
            else
                fragmentRef.get().getProgressBar().setVisibility(GONE);

            // hide how-to-refresh textView
            if (articles != null)
                fragmentRef.get().getRefreshTextView().setVisibility(GONE);
        } else
            fragmentRef.get().getFooterProgressBar().setVisibility(GONE);

        // If result and adapter are not null and fragment still exists, load items
        if (adapter != null && fragmentRef.get() != null) {
            if (articles != null)
                adapter.loadMoreItems(articles, pageNum);
            else if (adapter.isEmpty())
                Toast.makeText(fragmentRef.get().getContext(),
                        "We had trouble trying to connect", Toast.LENGTH_SHORT).show();
            fragmentRef.get().getIsLoadingArticles().set(false);
        }
    }

    List<Article> getResultsJSON(String arg, int pageNum)
    {
        StringBuilder builder = new StringBuilder();
        List<Article> articleList = new ArrayList<Article>();
        String urlPath = "http://morningsignout.com/?json=get_search_results&search=" + arg
                + "&count=5&page=" + pageNum + "&include=author,url,title,thumbnail,content";
        HttpURLConnection connection = null;

        // open http connection
        try {
            URL url = new URL(urlPath);
            connection = (HttpURLConnection) url.openConnection();
            InputStream response = connection.getInputStream();

            byte[] bytes = new byte[128];
            int bytesRead = 0;
            while ((bytesRead = response.read(bytes)) > 0) {
                if (bytesRead == bytes.length)
                    builder.append(new String(bytes));
                else
                    builder.append(new String(bytes).substring(0, bytesRead));
            }
        } catch (MalformedURLException e) {
            Log.e("FetchListSearchTask", "MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FetchListSearchTask", "IOException: " + e.getMessage());
            e.printStackTrace();
        } catch(Exception e) {
            Log.e("FetchListSearchTask", "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the connection
            if (connection != null) {
                connection.disconnect();
            }
        }

        // if no articles found, return nothing
        return articleList.isEmpty() ? null : articleList;
    }
}
