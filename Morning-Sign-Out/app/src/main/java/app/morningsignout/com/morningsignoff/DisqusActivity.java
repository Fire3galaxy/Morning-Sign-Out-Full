package app.morningsignout.com.morningsignoff;

import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class DisqusActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
        // Setting imageButton to center of actionbar
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title, null); // could replace null with new LinearLayout. properties not needed though.
        LayoutParams params = new LayoutParams(Gravity.CENTER);

        // setting imagebutton
        super.getSupportActionBar().setCustomView(ib, params);

        // Disabling title text of actionbar, enabling imagebutton
        super.getSupportActionBar().setDisplayShowTitleEnabled(false);
        super.getSupportActionBar().setDisplayShowCustomEnabled(true);

        WebView webView = (WebView) findViewById(R.id.webView_disqus);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        webView.requestFocusFromTouch();
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

//        webView.loadUrl("http://<PATH TO WEB SERVER>/showcomments.php?disqus_id="+<disqus thread id>);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
