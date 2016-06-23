package app.morningsignout.com.morningsignoff.meet_the_team;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import app.morningsignout.com.morningsignoff.article.ArticleActivity;
import app.morningsignout.com.morningsignoff.network.URLToMobileArticle;

/**
 * Created by pokeforce on 6/22/16.
 */
public class MttWebViewClient extends WebViewClient {
    static final String mimeType = "text/html";
    static final String encoding = "gzip"; // Find encoding https://en.wikipedia.org/wiki/HTTP_compression

    String baseUrl = null; // Useful for if team member has articles and pages

    public MttWebViewClient(String url) {
        baseUrl = url;
    }

    // Needed if reloading page
    public void changeBaseUrl(String newBaseUrl) {
        baseUrl = newBaseUrl;
    }

//    // Show logo once page completes loading
//    @Override
//    public void onPageFinished(WebView view, String url) {
//        super.onPageFinished(view, url);
//
//        if (url.contains(baseUrl))
//            logoView.setVisibility(View.VISIBLE);
//    }

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
            intent.putExtra(Intent.EXTRA_HTML_TEXT, url); // Put url in intent
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
            html = URLToMobileArticle.getOther(requestUrl.toString());
        } catch (IOException e) {
            if (e.getMessage() != null)
                Log.e("MTTWebViewActivity", e.getMessage());
            else
                Log.e("MTTWebViewActivity", "IOException in Meet the team");
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
