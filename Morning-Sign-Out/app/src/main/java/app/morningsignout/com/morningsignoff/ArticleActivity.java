package app.morningsignout.com.morningsignoff;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

// Activity class created in FetchListArticleTask when user clicks on an article from the ListView
public class ArticleActivity extends ActionBarActivity {
    private String category;
    private int lastSavedY;

    private WebView webView;
    private ArticleWebViewClient webViewClient;

    private SearchView searchView;
    private Intent shareIntent;

    public ArticleActivity() {
        super();
        lastSavedY = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner

        // Set the title for this myActivity to the article title
        Intent intent = getIntent();
        if (intent != null) {
            // Setting variable category (healthcare, wellness, etc.) and title of myActivity (article name)
            category = getIntent().getStringExtra(Intent.EXTRA_TITLE);
            setTitle(getIntent().getStringExtra(Intent.EXTRA_SHORTCUT_NAME));

            // ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
            // Setting imageButton to center of actionbar
            ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title, null);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
            this.getSupportActionBar().setCustomView(ib, params);

            // Disabling title text of actionbar, enabling imagebutton
            this.getSupportActionBar().setDisplayShowTitleEnabled(false);
            this.getSupportActionBar().setDisplayShowCustomEnabled(true);

            // Getting article from URL and stripping away extra parts of website for better reading
            webView = (CustomWebView) findViewById(R.id.webView_article);
            webView.getSettings().setBuiltInZoomControls(true);
            webViewClient = new ArticleWebViewClient(this);
            webView.setWebViewClient(webViewClient);

            // Setting up "tumblr" scroll button
            ImageButton scrollButton = (ImageButton) findViewById(R.id.button_scroll);
            scrollButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (webView.getScrollY() != 0) {
                        lastSavedY = webView.getScrollY();
//                        webView.setScrollY(0);
                        ObjectAnimator scrollUp = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), 0);
                        scrollUp.setDuration(400);
                        scrollUp.start();
                    } else {
                        if (lastSavedY != 0) {
//                            webView.setScrollY(lastSavedY);
                            ObjectAnimator scrollDown = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), lastSavedY);
                            scrollDown.setDuration(400);
                            scrollDown.start();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article, menu);

        /* Search results in new SearchResultsActivity, clicked article passed back to articleActivity
           Associate searchable configuration with the SearchView */
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        ComponentName componentName = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        // Setting up share intent for first web page
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_HTML_TEXT));

        return super.onCreateOptionsMenu(menu);
    }

    /* Handle action bar item clicks here. The action bar will
       automatically handle clicks on the Home/Up button, so long
       as you specify a parent myActivity in AndroidManifest.xml. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (webView != null && webView.canGoBack())    // Go back in webView history
                    webView.goBack();
                else                     // Return to front page (without recreating parent)
                    returnToParent(null);
                return true;
            case R.id.action_share:
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share using"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (!searchView.isIconified()) {    // Check if searchView is expanded
                getSupportActionBar().collapseActionView();
                return true;
            }
            else if (webView.canGoBack()) {     // Check if webView has history
                webView.goBack();                   // Back through web history
                return true;
            }
        }

        // If it wasn't the Back key or none of the conditions are met, use default system behavior
        return super.onKeyDown(keyCode, event);
    }

    /* Note: The myActivity cycle says that onNewIntent and onResume will occur even in normal app
     * function. Hence, these two functions should NOT change the normal function of the myActivity
     * and only load a new url if given through SearchResultsActivity's new intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        // change myActivity intent to the one from SearchResultsActivity
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String searchReturnUrl = getIntent().getStringExtra(Intent.EXTRA_RETURN_RESULT);
        String webviewUrl = webView.getUrl();
        String intentUrl = getIntent().getStringExtra(Intent.EXTRA_HTML_TEXT);

        // 1. App has returned from search w/ result
        if (searchReturnUrl != null) {
            searchReturnUrl = new String(getIntent().getStringExtra(Intent.EXTRA_RETURN_RESULT));   // copy string
            getIntent().removeExtra(Intent.EXTRA_RETURN_RESULT);        // Return url only valid once, remove it after use
            new URLToMobileArticle(webView).execute(searchReturnUrl);
            Log.d("ArticleActivity", "Loading: " + intentUrl);
        }
        // 2. App was stopped/return to this myActivity from search w/o a result (do nothing)
        else if (webviewUrl != null && !webviewUrl.isEmpty());
            // 3. App has not loaded its first article yet
        else if (intentUrl != null) new URLToMobileArticle(webView).execute(intentUrl);

        getSupportActionBar().collapseActionView(); // collapse search bar on return from search
    }

    // view parameter needed for title.xml onClick()
    public void returnToParent(View view) {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intent.EXTRA_TITLE, category);
        NavUtils.navigateUpTo(this, intent);
    }
    public void resetLastSavedY() {
        lastSavedY = 0;
    }
    public void setShareIntent(Intent shareIntent) {
        this.shareIntent = shareIntent;
    }
}


// Create a customized webview client to disable website navigation bar
class ArticleWebViewClient extends WebViewClient {
    static final String mimeType = "text/html";
    static final String encoding = "gzip"; // Find encoding https://en.wikipedia.org/wiki/HTTP_compression

    final String LOG_NAME = "ArticleWebViewClient";
    final String ERROR_404_FILE = "error_404.html";

    final int CURRENTYEAR = Calendar.getInstance().get(Calendar.YEAR);
    static final int MINYEAR = 2014;

    Context c;

    public ArticleWebViewClient(Context c) {
        super();
        this.c = c;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("ArticleActivity", "In webviewclient, loading " + url);

        if (Uri.parse(url).getHost().endsWith("morningsignout.com")) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    // For API # < 21
    @SuppressWarnings("deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView wb, String url) {
        WebResourceResponse wbresponse = super.shouldInterceptRequest(wb, url);
        Uri requestUrl = Uri.parse(url);

        /* Not from morningsignout, e.g. googleapis, gstatic, or gravatar
         * or an image/theme/plugin from wordpress
         * or a .* file, e.g. favicon.ico
         */
        if (!requestUrl.getHost().endsWith("morningsignout.com")
                || requestUrl.getPathSegments().get(0).equals("wp-content")
                || requestUrl.getPathSegments().get(0).matches(".*\\.[a-zA-Z]+"))
            return wbresponse;

        String html = null;
        ByteArrayInputStream bais;

        // Article Page
        if (requestUrl.getPathSegments().size() == 1) {
            Log.d(LOG_NAME, "changing webresponse to article page");
            html = URLToMobileArticle.getArticle(requestUrl.toString());

            // Change share intent to new article
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, requestUrl.toString());
            ((ArticleActivity) c).setShareIntent(shareIntent);
        }
        // Author, Tag, Date pages (tag/dermatitis, tag/dermatitis/page/2)
        else if (requestUrl.getPathSegments().size() == 2 || requestUrl.getPathSegments().size() == 4) {
            String pathSeg0 = requestUrl.getPathSegments().get(0);
            int pathYear = -1;
            try {
                pathYear = Integer.parseInt(pathSeg0);
            } catch (NumberFormatException nfe) {
                // do nothing, basically throw the exception
            }

            if (pathSeg0.equals("author") ||
                    pathSeg0.equals("tag") ||
                    (pathYear >= MINYEAR && pathYear <= CURRENTYEAR)) {
                Log.d(LOG_NAME, "changing webresponse to other kind of page");
                try {
                    html = URLToMobileArticle.getOther(requestUrl.toString());
                } catch (IOException e) {
                    Log.e(LOG_NAME, e.getMessage());
                }
            }
        }

        // Let webView load default action (either webpage w/o mobile view, or webpage not found)
        if (html == null)
            return null;

        // New webpage loads successfully
        bais = new ByteArrayInputStream(html.getBytes());
        wbresponse = new WebResourceResponse(mimeType,
                encoding,
                bais);

        // Reset tumblr scroll button (If new page is loaded)
        // FIXME: does not reset if you click an image link
        ((ArticleActivity) c).resetLastSavedY();

        return wbresponse;
    }

    // Requires API 21
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView wb, WebResourceRequest wbrequest) {
//        if (wbrequest.getUrl().getPathSegments().get(0).matches(".*\\.[a-zA-Z]+"))
//            Log.d(LOG_NAME, wbrequest.getMethod() + ", " + wbrequest.getUrl());
//        for (String s : wbrequest.getRequestHeaders().keySet()) {
//            Log.d(LOG_NAME, s + " - " + wbrequest.getRequestHeaders().get(s));
//        }
        Log.d(LOG_NAME, wbrequest.getUrl().toString());

        WebResourceResponse wbresponse = super.shouldInterceptRequest(wb, wbrequest);
        Uri requestUrl = wbrequest.getUrl();

        /* Not from morningsignout, e.g. googleapis, gstatic, or gravatar
         * or an image/theme/plugin from wordpress
         * or a .* file, e.g. favicon.ico
         */
        if (!requestUrl.getHost().endsWith("morningsignout.com")
                || requestUrl.getPathSegments().get(0).equals("wp-content")
                || requestUrl.getPathSegments().get(0).matches(".*\\.[a-zA-Z]+"))
            return wbresponse;

        Map<String, String> responseHeaders = new HashMap<>(); // Finish responseHeaders for other accept types "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
        responseHeaders.put("Content-Language", "en");
        responseHeaders.put("URI", requestUrl.toString());

        String html = null;
        ByteArrayInputStream bais = null;

        // Article Page
        if (wbrequest.getUrl().getPathSegments().size() == 1) {
            Log.d(LOG_NAME, "changing webresponse to article page");
            html = URLToMobileArticle.getArticle(requestUrl.toString());

            // Change share intent to new article
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, requestUrl.toString());
            ((ArticleActivity) c).setShareIntent(shareIntent);
        }
        // Author, Tag, Date pages (tag/dermatitis, tag/dermatitis/page/2)
        else if (requestUrl.getPathSegments().size() == 2 || requestUrl.getPathSegments().size() == 4) {
            String pathSeg0 = requestUrl.getPathSegments().get(0);
            int pathYear = -1;
            try {
                pathYear = Integer.parseInt(pathSeg0);
            } catch (NumberFormatException nfe) {
                // do nothing, basically throw the exception
            }

            if (pathSeg0.equals("author") ||
                    pathSeg0.equals("tag") ||
                    (pathYear >= MINYEAR && pathYear <= CURRENTYEAR)) {
                Log.d(LOG_NAME, "changing webresponse to other kind of page");
                try {
                    html = URLToMobileArticle.getOther(requestUrl.toString());
                } catch (IOException e) {
                    Log.e(LOG_NAME, e.getMessage());
                }
            }
        }

        // Let webView load default action (either webpage w/o mobile view, or webpage not found)
        if (html == null)
            return wbresponse;

        // New Page loads successfully
        bais = new ByteArrayInputStream(html.getBytes());

        wbresponse = new WebResourceResponse(mimeType,
                encoding,
                200,
                "Download html of valid url (from App team!)",
                responseHeaders,
                bais);

        // Reset tumblr scroll button (If new page is loaded)
        // FIXME: does not reset if you click an image link
        ((ArticleActivity) c).resetLastSavedY();

        return wbresponse;
    }
}
