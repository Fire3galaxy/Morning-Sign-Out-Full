package app.morningsignout.com.morningsignoff.search_results;

//import android.app.Fragment; //https://stackoverflow.com/questions/9586218/fragmentactivity-cannot-cast-from-fragment-to-derived-class
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.network.FetchListSearchTask;
import app.morningsignout.com.morningsignoff.util.FragmentWithCache;
import app.morningsignout.com.morningsignoff.util.ProgressIndicator;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Created by shinr on 6/5/2017.
 */

public class SearchFragment extends FragmentWithCache
        implements ProgressIndicator, FetchListSearchTask.OnSearchErrorListener {
    // these are the "key" strings for getArguments() and setArguments() and so on.
    final static String SEARCH_PARAM = "SEARCH_PARAM"; // have SearchResultsActivity set this so we can put this as header
    final static String SEARCH_REFRESH = "SEARCH_REFRESH"; // shouldn't this be a boolean? e: no, this is a tag.
    final static String TAG = "SearchFragment";

    // local copies of metadata
    String search = ""; // holds the search argument

    //Helpful stuff.
    private SwipeRefreshLayout swipeRefreshLayout; // used to handle refreshing on swipe
    private ProgressBar progressBar; // does what it's named
    private TextView refreshTextView; // refers to the text that shows when refreshing
    private ProgressBar footerProgressBar; // refers to footer progress bar
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter; // gridview

    private SearchAdapter searchAdapter; // custom adapter for our listview
    private FetchListSearchTask.SearchError searchErrorObject = null;
    protected static Integer index = null; // marks index of current selection when orientation changes

    @Override
    public void onDetach() {
        // Save index to set gridview back to this on orientation change
        index = gridViewWithHeaderAndFooter.getFirstVisiblePosition();
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null)
            search = getArguments().getString(SEARCH_PARAM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Grab all the views and layouts from the layout file
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_search);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_search);
        refreshTextView = (TextView) rootView.findViewById(R.id.textView_searchRefresh);
        footerProgressBar = getFooterProgressBarXml();
        gridViewWithHeaderAndFooter = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView_search);

        swipeRefreshLayout.setColorSchemeResources(R.color.mso_blue, R.color.background_white);
        gridViewWithHeaderAndFooter.setNumColumns(1);
        gridViewWithHeaderAndFooter.addFooterView(footerProgressBar);

        // Creates and loads new adapter or sets position of existing gridView
        if(searchAdapter == null) {
            searchAdapter = new SearchAdapter(this, inflater);
            gridViewWithHeaderAndFooter.setAdapter(searchAdapter);
            new FetchListSearchTask(this.getContext(), this, Type.Loading, searchAdapter, 1, this)
                    .execute(search);
        } else {
            refreshTextView.setVisibility(View.GONE);
            searchAdapter.notifyDataSetChanged();
            gridViewWithHeaderAndFooter.setAdapter(searchAdapter);
            if (index != null)
                gridViewWithHeaderAndFooter.setSelection(index);
        }

        // Add a click listener for the returned articles.
        gridViewWithHeaderAndFooter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WrapperListAdapter wrapperListAdapter = (WrapperListAdapter) parent.getAdapter();
                SearchAdapter adapter = (SearchAdapter) wrapperListAdapter.getWrappedAdapter();
                int id_int = (int) id;

                // Do nothing if id is invalid
                if (id_int < 0 || id_int > adapter.getCount())
                    return;

                // Spawn a new ArticleActivity using the data.
                Article rowTemp = (Article) adapter.getItem(id_int);
                Intent articleActivity = new Intent(gridViewWithHeaderAndFooter.getContext(), ArticleActivity.class);

                // Load title
                articleActivity.putExtra(ArticleActivity.TITLE, rowTemp.getTitle());
                // Load link
                articleActivity.putExtra(ArticleActivity.LINK, rowTemp.getLink());
                // Load content
                articleActivity.putExtra(ArticleActivity.CONTENT, rowTemp.getContent());
                // Load header image
                articleActivity.putExtra(ArticleActivity.IMAGE_URL, rowTemp.getFullImageURL());

                gridViewWithHeaderAndFooter.getContext().startActivity(articleActivity);
            }
        });

        // To load more articles when the bottom of the page is reached
        // The progress bar footer counts in the visible item count, even though it may be View.GONE
        // Hence, RIGHT at the bottom, the visible item count increases by one.
        // Loading new articles should occur right at the bottom, where the progress bar is.
        gridViewWithHeaderAndFooter.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
                int pageNum = searchAdapter.getPageNum();

                // At last item (should be progress bar)
                if (lastVisibleItem == totalItemCount - 1 && !doesMatchSearchError(pageNum + 1)) {
                    // Only make one request per page request
                    new FetchListSearchTask(SearchFragment.this.getContext(),
                            SearchFragment.this,
                            Type.LoadingMore,
                            searchAdapter,
                            pageNum + 1,
                            SearchFragment.this).execute(search);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        // Sets up a refreshlistener, to let us know when to refresh.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
            public void onRefresh() {
               if (!gridViewWithHeaderAndFooter.getAdapter().isEmpty() && !FetchListSearchTask.activeTaskLock) {
                   // seems like this series of lines is what creates an instance of this fragment.
                   // Reload search fragment
                   SearchFragment fragment =
                           SearchFragment.findOrCreateRetainFragment(getActivity().getSupportFragmentManager());
                   Bundle args = new Bundle();
                   args.putString(SearchFragment.SEARCH_PARAM, search);
                   args.putBoolean(SearchFragment.SEARCH_REFRESH, true); // we are refreshing!
                   fragment.setArguments(args);

                   getActivity().getSupportFragmentManager().beginTransaction()
                           .replace(R.id.container_search, fragment) // replace with this new frag.
                           .commit();
               }
               swipeRefreshLayout.setRefreshing(false);
           }
        });

        return rootView;
    }

    // On orientation change, recovers the old fragment rather than creating a new one (to save
    // things like scroll state, loaded rows, etc.)
    public static SearchFragment findOrCreateRetainFragment(FragmentManager fm) {
        SearchFragment fragment = (SearchFragment) fm.findFragmentByTag(TAG);
        if (fragment == null)
            return new SearchFragment();
        return fragment;
    }

    void onNewSearch(String query) {
        Log.d("SearchFragment", query);
        if (query != null && !query.isEmpty()) {
            clearSearchError();
            new FetchListSearchTask(getContext(), this, Type.Refresh, searchAdapter, 1, this)
                    .execute(query);
            search = query;
        }
    }

    // Helper functions, used in the Fetch/Async task
    ProgressBar getFooterProgressBarXml() {
        // -get attributes for footerProgressBar
        XmlPullParser pullParser = getResources().getXml(R.xml.footer_progressbar);

        // -get first tag of xml
        try {
            int type = 0;
            while (type != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG) {
                type = pullParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e("SearchFragment", e.getMessage());
        }

        // -get attriuteSet
        AttributeSet attrs = Xml.asAttributeSet(pullParser);

        // -create progressBar and add to listView
        return new ProgressBar(getActivity(), attrs);
    }

    @Override
    public void loadingStart() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadingEnd() {
        progressBar.setVisibility(View.GONE);
        refreshTextView.setVisibility(View.GONE);
    }

    @Override
    public void refreshStart() {
        swipeRefreshLayout.setRefreshing(true);
        gridViewWithHeaderAndFooter.setSelection(0);
    }

    @Override
    public void refreshEnd() {
        swipeRefreshLayout.setRefreshing(false);
        refreshTextView.setVisibility(View.GONE);
    }

    @Override
    public void loadingMoreStart() {
        footerProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadingMoreEnd() {
        footerProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSearchError(FetchListSearchTask.SearchError error) {
        searchErrorObject = error;
    }

    private boolean doesMatchSearchError(int requestedPageNum) {
        return searchErrorObject != null
                && searchErrorObject.query.equals(search)
                && searchErrorObject.pageNum == requestedPageNum;
    }

    private void clearSearchError() {
        searchErrorObject = null;
    }
}
