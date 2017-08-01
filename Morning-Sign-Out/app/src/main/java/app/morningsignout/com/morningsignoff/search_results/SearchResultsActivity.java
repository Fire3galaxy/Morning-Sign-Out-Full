package app.morningsignout.com.morningsignoff.search_results;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

public class SearchResultsActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner

        // New search, added by shinray
        // Fragments added to activity
        if (savedInstanceState == null) {
            SearchFragment fragment = SearchFragment.findOrCreateRetainFragment(getSupportFragmentManager());
            // create a Bundle to pass strings into the fragment
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
        // FIXME: While search is unavalaible
        getMenuInflater().inflate(R.menu.menu_justlogo, menu);

        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
//
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

