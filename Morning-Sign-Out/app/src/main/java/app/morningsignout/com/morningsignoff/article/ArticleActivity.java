package app.morningsignout.com.morningsignoff.article;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import app.morningsignout.com.morningsignoff.category.CategoryActivity;
import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.search_results.SearchResultsActivity;
import app.morningsignout.com.morningsignoff.disqus.DisqusMainActivity;
import app.morningsignout.com.morningsignoff.network.URLToMobileArticle;

// Activity class created in FetchListArticleTask when user clicks on an article from the ListView
public class ArticleActivity extends ActionBarActivity {
    final static String AD_WAS_LEFT = "AdView is left";

    Integer lastSavedY;
    boolean isPortrait;
    float xOfAdView = 0;
    AnimatorSet showArticleBar;
    AnimatorSet hideArticleBar;
    ObjectAnimator scrollWebviewAnimator;

    private RelativeLayout bottomBar;
    private WebView webView;
    private ArticleWebViewClient webViewClient;
    private SearchView searchView;
    AdView mAdView;

    private Intent shareIntent;

    public ArticleActivity() {
        super();
        lastSavedY = 0;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        ActionBar actionBar = getSupportActionBar();

        isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // ACTION BAR
        //      ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
        //      Setting imageButton to center of actionbar
        ImageButton home = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null); // could replace null with new LinearLayout. properties not needed though.
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        actionBar.setCustomView(home, params);
        actionBar.setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        // BOTTOM BAR(s)
        //      Setting global var for bar at the bottom for disappearance/reappearance
        bottomBar = (RelativeLayout) findViewById(R.id.container_articleBar);

        // For Ads by Admobs!
        mAdView = (AdView) findViewById(R.id.adView_article);
        mAdView.loadAd(new AdRequest.Builder().build());

        // Setting up objectAnimators for articleBar's show/hide animation (Portrait only)
        if (isPortrait) {
            showArticleBar = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.show_article_bar);
            showArticleBar.setDuration(300);
            showArticleBar.setTarget(bottomBar);
            hideArticleBar = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.hide_article_bar);
            hideArticleBar.setDuration(300);
            hideArticleBar.setTarget(bottomBar);

            //      How the animation of the articleBar is programmed
            LinearLayout container = (LinearLayout) findViewById(R.id.container_article);
            container.setLayoutTransition(getCustomLayoutTransition());
        }
        // SWAP BUTTON (Landscape only)
        else {
            // If user is left handed, put adview on the right before user can see it
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            final boolean adWasLeft = preferences.getBoolean(AD_WAS_LEFT, true);

            if (!adWasLeft) {
                LinearLayout containerTwoBars =
                        (LinearLayout) findViewById(R.id.container_articleTwoBars);
                containerTwoBars.removeView(mAdView);
                containerTwoBars.addView(mAdView, 1);
            }

            // Set up swap button (Left/Right handed people)
            ImageButton swapButton = (ImageButton) findViewById(R.id.button_swap_bar);
            swapButton.setOnClickListener(new View.OnClickListener() {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                boolean adIsLeft = adWasLeft;

                @Override
                public void onClick(View v) {
                    // Swap the ad to the right
                    if (adIsLeft) {
                        mAdView.animate().x(bottomBar.getWidth());
                        bottomBar.animate().x(0);
                        adIsLeft = false;
                        bottomBar.setTag(0);
                    }
                    // Swap the ad to the left
                    else {
                        mAdView.animate().x(0);
                        bottomBar.animate().x(mAdView.getWidth());
                        adIsLeft = true;
                        bottomBar.setTag(1);
                    }

                    // Save user's setting
                    editor.putBoolean(AD_WAS_LEFT, adIsLeft);
                    editor.apply();
                }
            });
        }

        // WEBVIEW - Getting article from URL and stripping away extra parts of website for better reading
        debugView = (TextView) findViewById(R.id.textView);

        webView = (WebView) findViewById(R.id.webView_article);
        String data = readRawTextFile(this, R.raw.html_test);
//                .replace("\\u00a0", "&#160;")   // Non-breaking space
//                .replace("\\u201c", "&#8220;")  // Left quote
//                .replace("\\u201d", "&#8221;")  // Right quote
//                .replace("\\/", "/")            // For end html tags
//                .replace("\\n", "\n");          // For newlines
        data = replaceUnicodeAndEscapes(data).replace("//embed", "embed");

        webView.loadData(data, "text/html", "utf-8");

        debugView.setText(data);
//        final ProgressBar loadPage = (ProgressBar) findViewById(R.id.progressBar_article);
//        webView.setWebChromeClient(new WebChromeClient() { // Progress bar
//            @Override
//            public void onProgressChanged(WebView v, int newProgress) {
//                if (newProgress < 90) {
//                    if (loadPage.getVisibility() == View.GONE)
//                        loadPage.setVisibility(View.VISIBLE);
//
//                    loadPage.setProgress(newProgress);
//                } else if (newProgress >= 90) {
//                    loadPage.setProgress(newProgress);
//
//                    if (loadPage.getVisibility() == View.VISIBLE)
//                        loadPage.setVisibility(View.GONE);
//                }
//            }
//        });
//        webView.getSettings().setDisplayZoomControls(false);
//        webView.getSettings().setBuiltInZoomControls(false);
//        webViewClient = new ArticleWebViewClient(this);
//        webView.setWebViewClient(webViewClient);
//
//        // Load first website or restore webview if destroyed and recreated
//        if (savedInstanceState != null)
//            webView.restoreState(savedInstanceState);
//
//        if (savedInstanceState == null || webView.getUrl() == null) {
//            if (getIntent() != null) {
//                String article = getIntent().getStringExtra(Intent.EXTRA_HTML_TEXT);
//                webView.loadUrl(article);
//                shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_TEXT, article);
//            }
//        }
//
//        // Hiding bar before onResume() if activity was recreated by orientation change
//        if (!ArticleWebViewClient.isArticle(webView.getUrl()))
//            hideArticleBar();
//
//        // Refresh listener for webview
//        final SwipeRefreshLayout refreshLayout =
//                (SwipeRefreshLayout) findViewById(R.id.swipeRefresh_article);
//        refreshLayout.setColorSchemeColors(Color.argb(255, 0x81, 0xbf, 0xff), Color.WHITE);
//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                webView.reload();
//                refreshLayout.setRefreshing(false);
//            }
//        });

        // SHARE BUTTON
        ImageButton shareButton = (ImageButton) findViewById(R.id.button_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share using"));
            }
        });

        // SCROLL BUTTON
        //      Setting up "tumblr" scroll button
        scrollWebviewAnimator = ObjectAnimator.ofInt(webView, "scrollY", 0);
        scrollWebviewAnimator.setDuration(400);
        ImageButton scrollButton = (ImageButton) findViewById(R.id.button_scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.getScrollY() != 0) {
                    lastSavedY = webView.getScrollY();
                    scrollWebviewAnimator.setIntValues(0);
                } else {
                    scrollWebviewAnimator.setIntValues(lastSavedY);
                }

                scrollWebviewAnimator.start();
            }
        });

        // COMMENT BUTTON
        ImageButton commentButton = (ImageButton) findViewById(R.id.button_comment);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webViewClient.lastArticleSlug != null) {
                    Intent intent = new Intent(ArticleActivity.this, DisqusMainActivity.class);
                    intent.putExtra(DisqusMainActivity.SLUG, webViewClient.lastArticleSlug);
                    startActivity(intent);
                }
            }
        });
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
                    finish();
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

    @Override
    protected void onNewIntent(Intent intent) {
        // change myActivity intent to the one from SearchResultsActivity
        if (intent != null) {
            setIntent(intent);
            String searchUrl = intent.getStringExtra(Intent.EXTRA_HTML_TEXT);
            getSupportActionBar().collapseActionView();
            webView.loadUrl(searchUrl);
        }
    }

    // view parameter needed for imageview of logo at top: title.xml onClick()
    public void returnToParent(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void resetLastSavedY() {
        lastSavedY = 0;
    }
    public void setShareIntent(Intent shareIntent) {
        this.shareIntent = shareIntent;
    }
    public void showArticleBar() {
        if (bottomBar.getVisibility() != RelativeLayout.VISIBLE) {
            bottomBar.setVisibility(View.VISIBLE);

            if (!isPortrait)
                mAdView.animate().x(xOfAdView);
        }
    }

    public void hideArticleBar() {
        if (!isPortrait) {
            if (bottomBar.getVisibility() != RelativeLayout.INVISIBLE) {
                xOfAdView = mAdView.getX();

                bottomBar.setVisibility(View.INVISIBLE);
                mAdView.animate().x((webView.getWidth() - mAdView.getWidth()) / 2f);
            }
        }
        // Portrait
        else {
            if (bottomBar.getVisibility() != RelativeLayout.GONE)
                bottomBar.setVisibility(RelativeLayout.GONE);
        }
    }

    LayoutTransition getCustomLayoutTransition() {
        LayoutTransition customTransition = new LayoutTransition();
//            customTransition.enableTransitionType(LayoutTransition.CHANGING);
//            customTransition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
//            customTransition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        customTransition.setAnimator(LayoutTransition.APPEARING, showArticleBar);
        customTransition.setAnimator(LayoutTransition.DISAPPEARING, hideArticleBar);
        customTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        customTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        customTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        customTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
//            customTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        customTransition.setDuration(LayoutTransition.APPEARING, showArticleBar.getDuration());
        customTransition.setDuration(LayoutTransition.CHANGE_APPEARING, showArticleBar.getDuration());
        customTransition.setDuration(LayoutTransition.DISAPPEARING, hideArticleBar.getDuration());
        customTransition.setDuration(LayoutTransition.CHANGE_DISAPPEARING, hideArticleBar.getDuration());
//            customTransition.setDuration(LayoutTransition.APPEARING, showArticleBar.getDuration());

        return customTransition;
    }

    // DEBUG: get html from file http://stackoverflow.com/questions/4087674/android-read-text-raw-resource-file
    public static String readRawTextFile(Context ctx, int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    TextView debugView;
    public void toggleDebugView(View v) {
        if (debugView.getVisibility() == View.INVISIBLE)
            debugView.setVisibility(View.VISIBLE);
        else
            debugView.setVisibility(View.INVISIBLE);
    }

    // hex to int
    String replaceUnicodeAndEscapes(String s) {
        StringBuffer buffer = new StringBuffer(s);
        int unicode = buffer.indexOf("\\u");

        while (unicode != -1) {
            int codepoint = Integer.parseInt(buffer.substring(unicode + 2, unicode + 6), 16);
            buffer.delete(unicode, unicode + 6);
            buffer.insert(unicode, "&#" + codepoint + ";");
            unicode = buffer.indexOf("\\u");
        }

        // Any other escaped character
        int escape = buffer.indexOf("\\");

        while (escape != -1) {
            char nextChar = buffer.charAt(escape + 1);

            // eg. html end tags
            if (!Character.isAlphabetic(nextChar)) {
                buffer.deleteCharAt(escape);
                escape = buffer.indexOf("\\");
            }
            // eg. new lines or tabs. Handling by case, but this could be changed later.
            else {
                if (nextChar == 'n') {
                    buffer.delete(escape, escape + 2);
                    buffer.insert(escape, "\n");
                    escape = buffer.indexOf("\\", escape);
                } else if (nextChar == 't') {
                    buffer.delete(escape, escape + 2);
                    buffer.insert(escape, "\t");
                    escape = buffer.indexOf("\\", escape);
                } else
                    escape = buffer.indexOf("\\", escape + 1);
            }
        }

        return buffer.toString();
    }
}


// Create a customized webview client to disable website navigation bar
class ArticleWebViewClient extends WebViewClient {
    static final String LOG_NAME = "ArticleWebViewClient";
    static final String mimeType = "text/html";
    static final String encoding = "gzip"; // Find encoding https://en.wikipedia.org/wiki/HTTP_compression
    static final int MINYEAR = 2014;
    final int CURRENTYEAR = Calendar.getInstance().get(Calendar.YEAR);

    ArticleActivity caller;
    public String lastArticleSlug;

    public ArticleWebViewClient(ArticleActivity c) {
        super();
        caller = c;
        lastArticleSlug = null;
    }

    @Override
    public void onPageStarted(WebView webView, String url, Bitmap favicon) {
        WebSettings settings = webView.getSettings();

        // Hide/show bottom bar
        if (isArticle(url))
            caller.showArticleBar();
        else
            caller.hideArticleBar();

        // Set image page to width of image
        // Layout rendering in pre kitkat (API < 19) is pretty bad. SINGLE_COLUMN helps any isOther
        // page and images.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isImage(url)) {
                if (!settings.getLoadWithOverviewMode()) settings.setLoadWithOverviewMode(true);
                if (!settings.getUseWideViewPort()) settings.setUseWideViewPort(true);
            }
        } else {
            if (isImage(url) || isOther(url))
                if (!settings.getLayoutAlgorithm().equals(WebSettings.LayoutAlgorithm.SINGLE_COLUMN))
                    settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        }
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        WebSettings settings = webView.getSettings();

        // Changes zoom controls depending on image/nonimage page
        if (isImage(url)) {
            if (!settings.getBuiltInZoomControls()) settings.setBuiltInZoomControls(true);
        } else {
            if (settings.getBuiltInZoomControls()) settings.setBuiltInZoomControls(false);
        }

        // Reset width change from images or authors. Must be done differently for API < 19 (kitkat)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!isImage(url)) {
                if (settings.getLoadWithOverviewMode()) settings.setLoadWithOverviewMode(false);
                if (settings.getUseWideViewPort()) settings.setUseWideViewPort(false);
            }
        } else {
            if (!isImage(url) && !isOther(url))
                if (!settings.getLayoutAlgorithm().equals(WebSettings.LayoutAlgorithm.NORMAL))
                    settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }

        // Reset tumblr scroll button (If new page is loaded)
        caller.resetLastSavedY();

        super.onPageFinished(webView, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("ArticleActivity", "In webviewclient, loading " + url);

        if (Uri.parse(url).getHost().endsWith("morningsignout.com"))
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    // For API # < 21
    @SuppressWarnings("deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView wb, String url) {
        Log.d(LOG_NAME, url);

        WebResourceResponse wbresponse = super.shouldInterceptRequest(wb, url);
        Uri requestUrl = Uri.parse(url);

        /* Not from morningsignout, e.g. googleapis, gstatic, or gravatar
         * or an imageViewReference/theme/plugin from wordpress
         * or a .* file, e.g. favicon.ico */
        if (!requestUrl.getHost().endsWith("morningsignout.com")
                || requestUrl.getPathSegments().get(0).equals("wp-content")
                || requestUrl.getPathSegments().get(0).matches(".*\\.[a-zA-Z]+"))
            return wbresponse;

        String html = null;
        ByteArrayInputStream bais;

        // Article Page
        if (requestUrl.getPathSegments().size() == 1) {
            Log.d(LOG_NAME, "changing webresponse to article page");
            html = URLToMobileArticle.getArticleRevised(requestUrl.toString());

            // Prep slug string
            lastArticleSlug = requestUrl.getLastPathSegment();

            // Change share intent to new article
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, requestUrl.toString());
            caller.setShareIntent(shareIntent);
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

        return wbresponse;
    }

    public static boolean isArticle(String url) {
        if (url == null || url.isEmpty())
            return false;

        Uri requestUrl = Uri.parse(url);

        // imageViewReference/etc.
        if (!requestUrl.getHost().endsWith("morningsignout.com")
                || requestUrl.getPathSegments().get(0).equals("wp-content")
                || requestUrl.getPathSegments().get(0).matches(".*\\.[a-zA-Z]+"))
            return false;

        // article vs date/author/tag
        return (requestUrl.getPathSegments().size() == 1);
    }

    boolean isImage(String url) {
        Uri requestUrl = Uri.parse(url);
        List<String> segments = requestUrl.getPathSegments();

        return segments.size() >= 2 &&
                segments.get(0).equals("wp-content") &&
                segments.get(1).equals("uploads");
    }

    boolean isOther(String url) {
        if (url == null) return false;

        Uri requestUrl = Uri.parse(url);
        return requestUrl.getPathSegments().size() == 2 || requestUrl.getPathSegments().size() == 4;
    }
}
