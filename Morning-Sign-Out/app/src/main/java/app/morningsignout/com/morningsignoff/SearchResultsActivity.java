package app.morningsignout.com.morningsignoff;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Daniel on 7/26/2015.
 */
public class SearchResultsActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner

        handleSearch(getIntent());
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
                WebViewClient webViewClient = new SearchWebViewClient();
                webView.setWebViewClient(webViewClient);

//                new URLToMobileArticle(webView).execute(searchURI); //FIXME need parsing for this
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
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(Uri.parse(url).getHost().endsWith("morningsignout.com")) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }
}
