package app.morningsignout.com.morningsignoff.search_results;

//import android.app.Fragment; //https://stackoverflow.com/questions/9586218/fragmentactivity-cannot-cast-from-fragment-to-derived-class
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import org.w3c.dom.Text;

import java.util.concurrent.atomic.AtomicBoolean;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Created by shinr on 6/5/2017.
 */

public class SearchFragment extends Fragment {
    // these are the "key" strings for getArguments() and setArguments() and so on.
    final static String SEARCH_PARAM = "SEARCH_PARAM"; // have SearchResultsActivity set this so we can put this as header
    final static String SEARCH_REFRESH = "SEARCH_REFRESH"; // shouldn't this be a boolean?
    final static String TAG = "SearchFragment";

    // instance
    public static SearchFragment instance = null;

    // local copies of metadata
    String search = "";

    //Helpful stuff.
    private SwipeRefreshLayout swipeRefreshLayout; // used to handle refreshing on swipe
    private ProgressBar progressBar; // does what it's named
    private TextView refreshTextView; // refers to the text that shows when refreshing
    private ImageView splashScreenView; // refers to splash screen
    private ProgressBar footerProgressBar; // refers to footer progress bar
    private GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter; // gridview

    private AtomicBoolean isLoadingArticles;
    protected static Integer index = null; // marks index of current selection (I think?)

    public AtomicBoolean getIsLoadingArticles() { return isLoadingArticles; }

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
            search = getArguments().getString(SEARCH_PARAM);
        }

        isLoadingArticles = new AtomicBoolean(false);
        //setUpCache();
    }

    // Not sure why this is nullable. Doesn't seem to cause any harm, but I'll keep an eye on this.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        instance = this;
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Grab all the views and layouts from the layout file
        TextView headerView = (TextView) rootView.findViewById(R.id.textView_searchHeader); // how about we set this to the search params
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh_search);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_search);
        refreshTextView = (TextView) rootView.findViewById(R.id.textView_searchRefresh);
        splashScreenView = (ImageView) rootView.findViewById(R.id.imageView_splash_search);
//        footerProgressBar = getFooterProgressBarXml(); // FIXME
        gridViewWithHeaderAndFooter = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridView_search);

        // Custom adapter? SearchAdapter?

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

                Intent articleActivity = new Intent(gridViewWithHeaderAndFooter.getContext(), ArticleActivity.class);

                // Hmm, do we really want to start an entirely new activity? Maybe. Hopefully can handle
                // this, and if you press back, you end up back in the search results.
                gridViewWithHeaderAndFooter.getContext().startActivity(articleActivity);
            }
        });

        // Sets up a refreshlistener, to let us know when to refresh.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
            public void onRefresh() {
               if (!gridViewWithHeaderAndFooter.getAdapter().isEmpty() || !isLoadingArticles.get()) {

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

    // Not sure exactly what this does. Seems like it finds the fragment, or creates a new one if
    //      not already existing. Fail-safe?
    public static SearchFragment findOrCreateRetainFragment(FragmentManager fm) {
        SearchFragment fragment = (SearchFragment) fm.findFragmentByTag(TAG);
        if (fragment == null)
        {
            return new SearchFragment();
        }
        return fragment;
    }
}
