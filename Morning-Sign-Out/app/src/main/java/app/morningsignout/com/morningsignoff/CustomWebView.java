package app.morningsignout.com.morningsignoff;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;

import java.util.Map;

/**
 * Created by Daniel on 8/7/2015.
 */
public class CustomWebView extends WebView {
    public CustomWebView(Context context) {
        super(context);
    }

    public CustomWebView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

//    @Override
//    public void loadUrl(String url) {
//        new URLToMobileArticle(this).execute(url);
//        Log.d("CustomWebView", "loadUrl");
//    }

//    @Override
//    public void reload() {
//        super.reload();
//        Log.d("CustomWebView", "reloading page");
//    }
}
