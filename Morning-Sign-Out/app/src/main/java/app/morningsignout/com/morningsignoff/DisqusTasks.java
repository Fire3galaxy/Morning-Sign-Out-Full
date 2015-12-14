package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
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
                    ((DisqusMain) v.getContext()).startActivityForResult(intent, 1);
                }
            });
        }
    }
}

// Called after code is returned in DisqusActivity. Meant to trade code for token, then set button
// to post. So far have code for getting token done (not tested yet).
class DisqusGetAccessToken extends AsyncTask<String, Void, AccessToken> {
    String dsq_thread_id;
    WeakReference<Button> button;

    public DisqusGetAccessToken(Button button) {
        this.button = new WeakReference<>(button);
    }

    @Override
    public AccessToken doInBackground(String... disqusItems) {
        dsq_thread_id = disqusItems[1];
        DisqusDetails disqus = new DisqusDetails();

        return disqus.getAccessToken(disqusItems[0]);
    }

    @Override
    public void onPostExecute(final AccessToken token) {
        if (button.get() != null)
            button.get().setOnClickListener(new View.OnClickListener() {
                AccessToken accessToken = token;
                String thread_id = dsq_thread_id;

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DisqusCommentActivity.class);
                    intent.putExtra(DisqusDetails.ACCESS_TOKEN, accessToken.access_token);
                    intent.putExtra(DisqusDetails.DSQ_THREAD_ID, thread_id);

                    v.getContext().startActivity(intent);
                }
            });
    }
}


