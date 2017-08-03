package app.morningsignout.com.morningsignoff.search_results;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.category.SplashFragment;
import app.morningsignout.com.morningsignoff.network.URLToMobileArticle;

public class SearchResultsActivity extends AppCompatActivity {
    SearchFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner
        getSupportActionBar().setTitle("");

        // New search, added by shinray
        // Fragments added to activity
        if (savedInstanceState == null) {
            fragment = SearchFragment.findOrCreateRetainFragment(getSupportFragmentManager());
            Bundle args = new Bundle();

            // set SEARCH_PARAM in the fragment to the query
            args.putString(SearchFragment.SEARCH_PARAM, getIntent().getStringExtra(SearchManager.QUERY));
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_search, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category, menu);

        /* Search results in new SearchResultsActivity, clicked article passed back to articleActivity
           Associate searchable configuration with the SearchView */
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchResultsActivity.class);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (fragment == null) {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            if (!fragmentList.isEmpty())
                fragment = (SearchFragment) fragmentList.get(0);
        }
        fragment.onNewSearch(intent.getStringExtra(SearchManager.QUERY));
    }

//    private void handleSearch(Intent intent) {
//        if (intent == null) return;
//
//        // Create search url and load webView
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            setTitle(query); // In actionbar
//            String searchURI = getURI(query);
//
//            // Open webView with search results of query
//            if (searchURI != null) {
//                searchWebViewClient.setQuery(query);
//                webView.loadUrl(searchURI);
//            } else {
//                Log.e("Search", "Error: Failed Search (null string)");
//            }
//        }
//
//        else Log.e("Search", "Error: Failed Search (intent not for search)");
//    }
}

