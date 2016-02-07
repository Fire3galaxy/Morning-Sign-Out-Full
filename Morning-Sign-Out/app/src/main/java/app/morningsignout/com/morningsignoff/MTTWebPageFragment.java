package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Daniel on 11/21/2015.
 */
public class MTTWebPageFragment extends Fragment {
    final static String MEMBER_URL = "member_url";

    ProgressBar loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_mttwebview, container, false);

        String baseUrl = null;

        // Need to initialize list and index variables here
        if (getArguments() != null)
            baseUrl = getArguments().getString(MEMBER_URL);

        loading = (ProgressBar) rootView.findViewById(R.id.progressBar_mttwebview);
        WebView webView = (WebView) rootView.findViewById(R.id.webView_mtt);

        // Settings for webview
        WebSettings settings = webView.getSettings();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        } else {
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
        }

        // Need to load webviewclient with correct url here
        MttWebViewClient client = new MttWebViewClient(baseUrl);
        webView.setWebViewClient(client);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                    if (loading.getVisibility() != View.VISIBLE)
                        loading.setVisibility(View.VISIBLE);

                    loading.setProgress(progress);
                } else if (progress == 100)
                    loading.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(baseUrl);

        return rootView;
    }
}

class MttWebViewClient extends WebViewClient {
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