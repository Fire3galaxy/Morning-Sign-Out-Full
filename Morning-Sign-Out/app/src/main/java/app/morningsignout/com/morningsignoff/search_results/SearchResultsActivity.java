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

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.network.URLToMobileArticle;

public class SearchResultsActivity extends ActionBarActivity {
    SearchView searchView;
    WebView webView;
    SearchWebViewClient searchWebViewClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner

        // Initialize webview with WebViewClient and WebChromeClient
        webView = (WebView) findViewById(R.id.webView_search);
        searchWebViewClient = new SearchWebViewClient(getIntent().getStringExtra(SearchManager.QUERY));
        webView.setWebViewClient(searchWebViewClient);

        final ProgressBar loadPage = (ProgressBar) findViewById(R.id.progressBar_search);
        webView.setWebChromeClient(new WebChromeClient() { // Progress bar
            @Override
            public void onProgressChanged(WebView v, int newProgress) {
                if (newProgress < 100) {
                    if (loadPage.getVisibility() == View.GONE)
                        loadPage.setVisibility(View.VISIBLE);

                    loadPage.setProgress(newProgress);
                } else if (newProgress == 100) {
                    loadPage.setProgress(newProgress);

                    if (loadPage.getVisibility() == View.VISIBLE)
                        loadPage.setVisibility(View.GONE);
                }
            }
        });

        handleSearch(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds search to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article, menu);

        /* Search results in new myActivity, clicked article passed back to articleActivity
           Associate searchable configuration with the SearchView */
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        ComponentName componentName = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        menu.findItem(R.id.action_search).expandActionView();
        searchView.setQuery(getIntent().getStringExtra(SearchManager.QUERY), false); // Set contents of searchview to query
        searchView.clearFocus(); // Ensure no keyboard shows

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK && !searchView.hasFocus()) {
            // If back is hit, do not collapse searchView, just return to previous activity
            finish();
            return true;
        }

        // If it wasn't the Back key or none of the conditions are met, use default system behavior
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearch(intent);
    }

    private void handleSearch(Intent intent) {
        if (intent == null) return;

        // Create search url and load webView
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            setTitle(query); // In actionbar
            String searchURI = getURI(query);

            // Open webView with search results of query
            if (searchURI != null) {
                searchWebViewClient.setQuery(query);
                webView.loadUrl(searchURI);
            } else {
                Log.e("Search", "Error: Failed Search (null string)");
            }
        }

        else Log.e("Search", "Error: Failed Search (intent not for search)");
    }

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

    static String getURI(String query) {
        if (query != null) {
            String searchURI;
            try {
                searchURI = "http://morningsignout.com/?s=" + URLEncoder.encode(query, "UTF-8");
                return searchURI;
            } catch (UnsupportedEncodingException e) {
                Log.e("Search", e.getMessage());
            }
        }

        return null;
    }
}
