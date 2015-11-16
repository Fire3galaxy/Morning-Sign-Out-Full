package app.morningsignout.com.morningsignoff;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DisqusActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // Action Bar
        setActionBarDetails();

        // Commenting webview
        long id = 3635144879L; // long-type literals are SO BIG they need L on the end. who knew?
        String disqus_thread_id = String.valueOf(id);
        String commentsUrl = "http://www.morningsignout.com/showcomments.php?disqus_id="
                + disqus_thread_id;
//        String commentsUrl = "http://morningsignout.com/an-international-patchwork-of-healthcare/";

        WebView webView = (WebView) findViewById(R.id.webView_disqus);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // needed for Disqus
        webSettings.setBuiltInZoomControls(true);

        webView.requestFocusFromTouch();
        webView.setWebViewClient(new DisqusWebViewClient(commentsUrl));
        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl(commentsUrl);

        new DoDisqusAPITask().execute("Forgot a needed argument");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disqus, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // view parameter needed for title.xml onClick()
    public void returnToParent(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

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
}

class DisqusWebViewClient extends WebViewClient {
    String url;

    public DisqusWebViewClient(String myUrl) {
        url = myUrl;
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        if (url.contains("logout") || url.contains("disqus.com/next/login-success")) {
            webView.loadUrl(this.url);
        }
        if (url.contains("disqus.com/_ax/twitter/complete") ||
                url.contains("disqus.com/_ax/facebook/complete") ||
                url.contains("disqus.com/_ax/google/complete")) {
            webView.loadUrl(this.url);
        }
        if (url.contains("www.morningsignout.com/login.php")) {
            webView.loadUrl(this.url);
        }
    }
}

class DoDisqusAPITask extends AsyncTask<String, Void, Void> {
    final String PARAM = "grant_type=authorization_code&" +
            "client_id=W7S5K8Iad6l5z9pWLgdWMg58rVTmGtOPSBtx30eZcXBVaDB7gPYYv3XgztKtQDuS&" +
            "client_secret=P8QbTcCBz9lMn5Dw5sjBSnhB76VFrGfMR4Jb7el6qJmfQOm2CmdbvEjlKTpYbjFR&" +
            "code=6735fbd2b06240eaa758b97860ec2fbd";

    protected Void doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("https://disqus.com/api/oauth/2.0/access_token/");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");

            urlConnection.setDoOutput(true);
            DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
            os.writeBytes(PARAM);
            os.flush();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            Log.d("Disqus", "POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                Log.d("Disqus", response.toString());
            } else {
                Log.e("Disqus", "POST request not worked");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }


        return null;
    }
}