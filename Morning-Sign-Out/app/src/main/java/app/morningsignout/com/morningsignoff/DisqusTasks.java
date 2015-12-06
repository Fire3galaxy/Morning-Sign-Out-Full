package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/26/2015.
 */
// Argument: Slug of mso post (ArticleWebViewClient)
class DisqusGetComments extends AsyncTask<String, Void, ArrayList<Comments>> {
    WeakReference<ListView> commentsView;
    WeakReference<Button> actionButton;

    DisqusGetComments(ListView commentsView, Button actionButton) {
        this.commentsView = new WeakReference<>(commentsView);
        this.actionButton = new WeakReference<>(actionButton);
    }

    @Override
    protected ArrayList<Comments> doInBackground(String... args) {
        DisqusDetails disqus = new DisqusDetails();

        return disqus.getComments(args[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<Comments> comments) {
        if (commentsView.get() != null)
            commentsView.get().setAdapter(
                    new DisqusAdapter(commentsView.get().getContext(), comments));
        if (actionButton.get() != null) {
            actionButton.get().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DisqusLogin.class);
                    ((DisqusActivity) v.getContext()).startActivityForResult(intent, 1);
                }
            });
        }

        // So what I changed: Made on click listener in the getCommentsAsyncTask for the button to
        // initiate the webview activity disqusLogin
        // set up LoginClient to finish and return the code to whatever called it
        // set up DisqusActivity to receive it and store the code.
    }
}

