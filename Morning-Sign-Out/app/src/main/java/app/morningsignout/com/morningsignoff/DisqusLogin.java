package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

// Used by DisqusActivity
public class DisqusLogin extends ActionBarActivity {
    WebView webView;
    ProgressBar pb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disquslogin);

        // MSO Logo in center
        setActionBarDetails();

        // Initialize progressBar with layout pb for loading pages
        pb = (ProgressBar) findViewById(R.id.progressBar_disqusLogin);

        // Webview that loads Disqus login site
        webView = (WebView) findViewById(R.id.webView_login);
        webView.setWebViewClient(new LoginClient());    // Client only loads disqus and closes activity after login (returning code)
        webView.setWebChromeClient(new WebChromeClient() {  // Set up a progressbar that shows page loading
            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                // Make progressbar VISIBLE, then change progress
                if (newProgress < 100) {
                    if (pb.getVisibility() == View.GONE)
                        pb.setVisibility(View.VISIBLE);

                    pb.setProgress(newProgress);
                }
                // Show that it reached 100 (vs just making it GONE right away), then set to GONE
                else {
                    pb.setProgress(newProgress);

                    if (pb.getVisibility() == View.VISIBLE)
                        pb.setVisibility(View.GONE);
                }
            }
        });


        webView.loadUrl(DisqusDetails.AUTHORIZE_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Actionbar is MSO logo in middle, X button on left, and NOTHING in menu
        // but transparent button for spacing purposes.
        getMenuInflater().inflate(R.menu.menu_disqus, menu);

        return true; // show menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If X button hit, cancel login attempt and return to DisqusActivity
            case(android.R.id.home) :
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
        }

        return false;
    }

    // Overload back button to support going back in web browser
    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }

    // Use with all activities: sets up logo in front and up button
    // Note: X button is in style.xml
    void setActionBarDetails() {
        // ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
        // Setting imageButton to center of actionbar
        ImageButton home = (ImageButton) getLayoutInflater().inflate(R.layout.title_disqus, null);

        // setting imagebutton
        super.getSupportActionBar().setCustomView(home);

        // setting actionbar variables
        super.getSupportActionBar().setDisplayShowTitleEnabled(false);
        super.getSupportActionBar().setDisplayShowCustomEnabled(true);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Should close webview and return code if user finishes logging in and
    // redirects back to morningsignout.com
    class LoginClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            Uri parsedUrl = Uri.parse(url);
            Log.d("LoginClient", parsedUrl.getHost());

            if (parsedUrl.getHost().equals("morningsignout.com")) {
                String code = parsedUrl.getQuery();
                code = code.substring("code=".length());

                Log.d("LoginClient", "Code: " + code);

                Intent intent = new Intent();
                intent.putExtra(DisqusDetails.CODE_KEY, code);
                setResult(Activity.RESULT_OK, intent);
                finish();

                return true;
            }

            return false;
        }
    }
}
