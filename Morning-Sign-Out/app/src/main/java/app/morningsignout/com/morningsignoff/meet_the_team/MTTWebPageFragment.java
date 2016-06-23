package app.morningsignout.com.morningsignoff.meet_the_team;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.ProgressBar;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import app.morningsignout.com.morningsignoff.ArticleActivity;
import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.network.URLToMobileArticle;

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
                    if (loading.getVisibility() == View.GONE)
                        loading.setVisibility(View.VISIBLE);

                    loading.setProgress(progress);
                } else if (progress == 100) {
                    loading.setProgress(progress);

                    if (loading.getVisibility() == View.VISIBLE)
                        loading.setVisibility(View.GONE);
                }
            }
        });
        webView.loadUrl(baseUrl);

        return rootView;
    }
}