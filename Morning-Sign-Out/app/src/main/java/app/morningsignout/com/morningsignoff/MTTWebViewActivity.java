package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/16/2015.
 */
public class MTTWebViewActivity extends ActionBarActivity {
    // Need ExecutiveListItem list for previous/next buttons
    // Need index of which person on list is picked
    ArrayList<ExecutiveListItem> teamArray;
    MttWebViewClient client;
    WebView webView;
    int index;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mttwebview);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //made back arrow in top left corner

        String baseUrl = null;

        // Need to initialize list and index variables here
        if (getIntent() != null) {
            Intent ref = getIntent();

            teamArray = ref.getParcelableArrayListExtra(FetchMeetTheTeamTask.TEAM_KEY);
            index = ref.getIntExtra(FetchMeetTheTeamTask.TEAM_INDEX_KEY, 0);

            baseUrl = teamArray.get(index).hyperlink;
        }

        // Need to load webviewclient with correct url here
        webView = (WebView) findViewById(R.id.webView_mtt);
        client = new MttWebViewClient(baseUrl);
        webView.setWebViewClient(client);
        new URLToMobileArticle(webView, true).execute(baseUrl);

        // FIXME: Test, Transition to a ViewPager and make the prev/next buttons scroll the page (not swipe). Think of way to hide buttons when not in use.
        // set up buttons (URLToMobileArticle)
        Button prev = (Button) findViewById(R.id.button_mttPrev),
            next = (Button) findViewById(R.id.button_mttNext);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index > 0) {
                    client.changeBaseUrl(teamArray.get(index - 1).hyperlink);
                    webView.loadUrl(teamArray.get(index - 1).hyperlink);
                    index -= 1;
                }
            }
        });
    }
}

class MttWebViewClient extends WebViewClient {
    static final String mimeType = "text/html";
    static final String encoding = "gzip"; // Find encoding https://en.wikipedia.org/wiki/HTTP_compression

    // Useful for if team member has articles and pages
    String baseUrl = null;

    public MttWebViewClient(String url) {
        baseUrl = url;
    }

    // Needed if reloading page
    public void changeBaseUrl(String newBaseUrl) {
        baseUrl = newBaseUrl;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // If url is an email, send intent to open mail app
        if (url.startsWith("mailto:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }

        // if it is morning sign out AND is an article, send url to ArticleActivity
        else if(Uri.parse(url).getHost().endsWith("morningsignout.com")) {
            Log.d("SearchWebViewClient", Uri.parse(url).getPath());

            // When offline or changing page, do not create intent, stay in webviewclient
            if (url.contains(baseUrl))
                return false;

            Intent intent = new Intent(view.getContext(), ArticleActivity.class);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, url); // Put url in intent
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Open w/ old articleActivity if exists
            view.getContext().startActivity(intent);
            return true;
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

        if (!url.contains(baseUrl))
            return wbresponse;

        String html = null;
        ByteArrayInputStream bais;

        try {
            html = URLToMobileArticle.getAuthorMTT(requestUrl.toString());
        } catch (IOException e) {
            Log.e("MTTWebViewActivity", e.getMessage());
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


