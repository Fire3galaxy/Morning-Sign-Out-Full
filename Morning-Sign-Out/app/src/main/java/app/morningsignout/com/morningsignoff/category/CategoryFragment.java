package app.morningsignout.com.morningsignoff.category;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.network.FetchJSON;
import app.morningsignout.com.morningsignoff.network.FetchArticleListTask;
import app.morningsignout.com.morningsignoff.util.FragmentWithCache;
import app.morningsignout.com.morningsignoff.util.PhoneOrientation;
import app.morningsignout.com.morningsignoff.util.ProgressIndicator;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

public class CategoryFragment extends FragmentWithCache
        implements ProgressIndicator, FetchArticleListTask.OnFetchErrorListener {
    final static String EXTRA_TITLE = "EXTRA_TITLE";
    final static String EXTRA_URL = "EXTRA_URL";
    final static String TAG = "CategoryFragment";

    private String category = "";
    private String category_url = "";
    private FetchArticleListTask.FetchError fetchErrorObject = null;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView refreshTextView;
    private ProgressBar footerProgressBar;
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter;
    private CategoryAdapter categoryAdapter;
    protected static Integer index = null;

    @Override
    public void onDetach() {
        index = gridViewWithHeaderAndFooter.getFirstVisiblePosition();
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            category = getArguments().getString(EXTRA_TITLE);
            category_url = getArguments().getString(EXTRA_URL);
        }
    }

    private void setUpGridView(GridViewWithHeaderAndFooter grid){
        if (PhoneOrientation.isLandscape(getContext())) {
            grid.setNumColumns(2);
            grid.setPadding(5,10,5,10);
        }
        else
            grid.setNumColumns(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        TextView categoryHeaderView = rootView.findViewById(R.id.textView_categoryHeader);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefresh_category);
        progressBar = rootView.findViewById(R.id.progressBar);
        refreshTextView = rootView.findViewById(R.id.textView_categoryRefresh);
        footerProgressBar = getFooterProgressBarXml();
        gridViewWithHeaderAndFooter = rootView.findViewById(R.id.gridView);

        categoryHeaderView.setText(category);
        swipeRefreshLayout.setColorSchemeResources(R.color.mso_blue, android.R.color.white);
        setUpGridView(gridViewWithHeaderAndFooter);
        gridViewWithHeaderAndFooter.addFooterView(footerProgressBar);

        // Creates and loads new adapter or sets position of existing gridView
        if(categoryAdapter == null) {
            categoryAdapter = new CategoryAdapter(this, inflater);
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);

            // Do we need the latest category or a different one?
            FetchJSON.SearchType requestType = FetchJSON.SearchType.JCATLIST;
            if (category_url.equals("latest"))
                requestType = FetchJSON.SearchType.JLATEST;

            // Execute a task that fetches the list of articles, adds it to our adapter, and
            // calls the proper loading animations
            new FetchArticleListTask(this.getContext(),
                    this,
                    Type.Loading,
                    categoryAdapter,
                    category_url,
                    requestType,
                    1,
                    this).execute();
        } else {
            refreshTextView.setVisibility(View.GONE);
            categoryAdapter.notifyDataSetChanged();
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);
            if (index != null)
                gridViewWithHeaderAndFooter.setSelection(index);
        }

        // Load AdMob banner ad
        AdView mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Setup the click listener for the listView
        gridViewWithHeaderAndFooter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WrapperListAdapter wrappedAdapter = (WrapperListAdapter) parent.getAdapter();
                CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                int id_int = (int) id;

                // Do nothing if id is invalid
                if (id_int < 0 || id_int > adapter.getCount())
                    return;

                // Create new categoryActivity for the article here
                // feed the new categoryActivity with the URL of the page
                Article rowTemp = (Article) adapter.getItem(id_int);

                // Color of toolbar
                Resources res = CategoryFragment.this.getResources();
                builder.setToolbarColor(res.getColor(R.color.mso_blue));

                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getContext(), Uri.parse(rowTemp.getLink()));
            }
        });

        // To load more articles when the bottom of the page is reached
        gridViewWithHeaderAndFooter.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
                int pageNum = categoryAdapter.getPageNum();

                if (lastVisibleItem == totalItemCount - 1 && !matchesFetchError(pageNum + 1)) {
                    // Which category do we need, latest or something else?
                    FetchJSON.SearchType requestType = FetchJSON.SearchType.JCATLIST;
                    if (category_url.equals("latest"))
                        requestType = FetchJSON.SearchType.JLATEST;

                    // Fetch next page of articles
                    new FetchArticleListTask(CategoryFragment.this.getContext(),
                            CategoryFragment.this,
                            Type.LoadingMore,
                            categoryAdapter,
                            category_url,
                            requestType,
                            pageNum + 1,
                            CategoryFragment.this).execute();
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // This is a refresh. Do not keep the old error so that we can load articles in the
                // onScrollListener for the GridViewWithHeaderAndFooter.
                clearFetchError();

                // Which category do we need, latest or something else?
                FetchJSON.SearchType requestType = FetchJSON.SearchType.JCATLIST;
                if (category_url.equals("latest"))
                    requestType = FetchJSON.SearchType.JLATEST;

                // Fetch the first page of articles. The task will know to replace all existing
                // articles with this first page.
                new FetchArticleListTask(CategoryFragment.this.getContext(),
                        CategoryFragment.this,
                        Type.Refresh,
                        categoryAdapter,
                        category_url,
                        requestType,
                        1,
                        CategoryFragment.this).execute();
            }
        });

        return rootView;
    }

    public static CategoryFragment findOrCreateRetainFragment(FragmentManager fm) {
        CategoryFragment fragment = (CategoryFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            return new CategoryFragment();
        }
        return fragment;
    }

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
            Log.e("CategoryFragment", e.getMessage());
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
    public void onFetchError(FetchArticleListTask.FetchError error) {
        fetchErrorObject = error;
    }

    // This happens if, on this page, an error happened.
    // Right now, we just assume it's because there are no more pages for this category.
    // But FetchArticleListTask currently has two possible errors: no internet or no results.
    // Future FIXME, I guess. Solution would be to notice if this is internet and to allow a retry.
    private boolean matchesFetchError(int requestedPageNum) {
        return fetchErrorObject != null && fetchErrorObject.pageNum == requestedPageNum;
    }

    private void clearFetchError() {
        fetchErrorObject = null;
    }
}
