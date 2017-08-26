package app.morningsignout.com.morningsignoff.category;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.network.FetchListArticlesTask;
import app.morningsignout.com.morningsignoff.util.FragmentWithCache;
import app.morningsignout.com.morningsignoff.util.PhoneOrientation;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

public class CategoryFragment extends FragmentWithCache {
    final static String EXTRA_TITLE = "EXTRA_TITLE";
    final static String EXTRA_REFRESH = "EXTRA_REFRESH";
    final static String EXTRA_URL = "EXTRA_URL";
    final static String TAG = "CategoryFragment";

    String category = "";
    String category_url = "";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView refreshTextView;
    private ImageView splashScreenView;
    private ProgressBar footerProgressBar;
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter;

    private CategoryAdapter categoryAdapter;
    private AtomicBoolean isLoadingArticles;
    protected static Integer index = null;

    public AtomicBoolean getIsLoadingArticles() {
        return isLoadingArticles;
    }

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

        isLoadingArticles = new AtomicBoolean(false);
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

        TextView headerView = (TextView) rootView.findViewById(R.id.textView_categoryHeader);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_category);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        refreshTextView = (TextView) rootView.findViewById(R.id.textView_categoryRefresh);
        splashScreenView = (ImageView) rootView.findViewById(R.id.imageView_splash);
        footerProgressBar = getFooterProgressBarXml();
        gridViewWithHeaderAndFooter = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView);
        boolean isRefresh = getArguments().getBoolean(EXTRA_REFRESH, false);

        headerView.setText(category);
        swipeRefreshLayout.setColorSchemeResources(R.color.mso_blue, R.color.background_white);
        setUpGridView(gridViewWithHeaderAndFooter);
        gridViewWithHeaderAndFooter.addFooterView(footerProgressBar);

        // Creates and loads new adapter or sets position of existing gridView
        if(categoryAdapter == null) {
            categoryAdapter = new CategoryAdapter(this, inflater);
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);
            isLoadingArticles.set(true);
            new FetchListArticlesTask(this, 1, true, isRefresh).execute(category_url); // First round of articles
        } else {
            refreshTextView.setVisibility(View.GONE);
            categoryAdapter.notifyDataSetChanged();
            gridViewWithHeaderAndFooter.setAdapter(categoryAdapter);
            if (index != null)
                gridViewWithHeaderAndFooter.setSelection(index);
        }

        // Setup the click listener for the listView
        gridViewWithHeaderAndFooter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WrapperListAdapter wrappedAdapter = (WrapperListAdapter) parent.getAdapter();
                CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();
                int id_int = (int) id;

                // Do nothing if id is invalid
                if (id_int < 0 || id_int > adapter.getCount())
                    return;

                // Create new categoryActivity for the article here
                // feed the new categoryActivity with the URL of the page
                Article rowTemp = (Article) adapter.getItem(id_int);
                Intent articleActivity = new Intent(gridViewWithHeaderAndFooter.getContext(), ArticleActivity.class);

                // TITLE holds the html link for the article
                articleActivity.putExtra(ArticleActivity.TITLE, rowTemp.getTitle());

                // LINK holds the name of the article
                articleActivity.putExtra(ArticleActivity.LINK, rowTemp.getLink());

                // CONTENT holds the html text of the article
                articleActivity.putExtra(ArticleActivity.CONTENT, rowTemp.getContent());

                // IMAGE_URL holds the link to the article's header image
                articleActivity.putExtra(ArticleActivity.IMAGE_URL, rowTemp.getFullImageURL());

                gridViewWithHeaderAndFooter.getContext().startActivity(articleActivity);
            }
        });

        // To load more articles when the bottom of the page is reached
        gridViewWithHeaderAndFooter.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                // At last item
                if (lastVisibleItem >= totalItemCount) {
                    WrapperListAdapter wrappedAdapter = (WrapperListAdapter) gridViewWithHeaderAndFooter.getAdapter();
                    CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();

                    int pageNum = adapter.getPageNum();
                    // Only make one request per page request
                    if (totalItemCount != 0 /*&& lastSeenPageNum != pageNum*/ && isLoadingArticles.weakCompareAndSet(false, true)) {
                        new FetchListArticlesTask(CategoryFragment.this, pageNum + 1, false, false).execute(category_url);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!gridViewWithHeaderAndFooter.getAdapter().isEmpty() || !isLoadingArticles.get()) {
                    // Reload categoryFragment
                    CategoryFragment fragment =
                            CategoryFragment.findOrCreateRetainFragment(getActivity().getSupportFragmentManager());
                    Bundle args = new Bundle();
                    args.putString(CategoryFragment.EXTRA_TITLE, category);
                    args.putString(CategoryFragment.EXTRA_URL, category_url);
                    args.putBoolean(CategoryFragment.EXTRA_REFRESH, true);
                    fragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_category, fragment)
                            .commit();
                }
                swipeRefreshLayout.setRefreshing(false);
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

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getRefreshTextView() {
        return refreshTextView;
    }

    public ImageView getSplashScreenView() {
        return splashScreenView;
    }

    public ProgressBar getFooterProgressBar() {
        return footerProgressBar;
    }

    public GridViewWithHeaderAndFooter getGridViewWithHeaderAndFooter() {
        return gridViewWithHeaderAndFooter;
    }
}
