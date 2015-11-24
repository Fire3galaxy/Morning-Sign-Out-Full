package app.morningsignout.com.morningsignoff;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class SearchResultsActivity extends ActionBarActivity {
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner

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
            setTitle(intent.getStringExtra(SearchManager.QUERY)); // In actionbar
            String searchURI = getURI(intent.getStringExtra(SearchManager.QUERY));

            // Open webView with search results of query
            if (searchURI != null) {
                WebView webView = (WebView) findViewById(R.id.webView_search);
                WebViewClient webViewClient = new SearchWebViewClient(intent.getStringExtra(SearchManager.QUERY));
                webView.setWebViewClient(webViewClient);

                new URLToMobileArticle(webView).execute(searchURI);
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
            String searchURI = null;
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

class SearchWebViewClient extends WebViewClient {
    static final String mimeType = "text/html";
    static final String encoding = "gzip"; // Find encoding https://en.wikipedia.org/wiki/HTTP_compression

    String query;

    public SearchWebViewClient(String search) {
        query = search;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // if it is morning sign out AND is an article, send url to ArticleActivity
        if(Uri.parse(url).getHost().endsWith("morningsignout.com")) {
            Log.d("SearchWebViewClient", Uri.parse(url).getPath());

            // When offline or changing page, do not create intent
            if (url.contains("?s=" + query))
                return false;

            Intent intent = new Intent(view.getContext(), ArticleActivity.class);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, url); // Put url in intent
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Open w/ old articleActivity if exists
            view.getContext().startActivity(intent);
            return true;
        }

//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        view.getContext().startActivity(intent);
        return false;
    }

    // For API # < 21
    @SuppressWarnings("deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView wb, String url) {
        WebResourceResponse wbresponse = super.shouldInterceptRequest(wb, url);
        Uri requestUrl = Uri.parse(url);

        if (!url.contains("?s=" + query))
            return wbresponse;

        String html = null;
        ByteArrayInputStream bais;

        try {
            html = URLToMobileArticle.getOther(requestUrl.toString());
        } catch (IOException e) {
            Log.e("SearchResultsActivity", e.getMessage());
        }

        // Let webView load default action (either webpage w/o mobile view, or webpage not found)
        if (html == null)
            return wbresponse;

        bais = new ByteArrayInputStream(html.getBytes());

        wbresponse = new WebResourceResponse(mimeType,
                encoding,
                bais);

        return wbresponse;
    }
}
