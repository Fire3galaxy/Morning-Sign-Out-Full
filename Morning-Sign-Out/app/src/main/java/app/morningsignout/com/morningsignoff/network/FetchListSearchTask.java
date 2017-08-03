package app.morningsignout.com.morningsignoff.network;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.search_results.SearchAdapter;
import app.morningsignout.com.morningsignoff.search_results.SearchFragment;

import static android.view.View.GONE;

/**
 * Created by shinr on 5/20/2017.
 */

public class FetchListSearchTask extends AsyncTask<String, Void, List<Article>>{
    private WeakReference<SearchFragment> fragmentRef;
    private int pageNum;
    private boolean isFirstLoad, isRefresh, noConnection;

    private int adapterPageNum;

    public FetchListSearchTask(SearchFragment fragment,
                               int pageNum,
                               boolean isFirstLoad,
                               boolean isRefresh) {
        this.fragmentRef = new WeakReference<>(fragment);
        this.pageNum = pageNum;
        this.isFirstLoad = isFirstLoad;
        this.isRefresh = isRefresh;
        this.noConnection = false;
    }

    @Override
    protected void onPreExecute() {
        if (fragmentRef.get() == null || !CheckConnection.isConnected(fragmentRef.get().getContext())) {
            noConnection = true;
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
    protected List<Article> doInBackground(String... params) {
        if (noConnection)
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
        if (noConnection) {
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
        if (fragmentRef.get() != null) {
            if (articles != null)
                adapter.loadMoreItems(articles, pageNum);
            else if (adapter.isEmpty())
                Toast.makeText(fragmentRef.get().getContext(),
                        "We had trouble trying to connect", Toast.LENGTH_SHORT).show();
            fragmentRef.get().getIsLoadingArticles().set(false);
        }
    }
}
