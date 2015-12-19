package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/26/2015.
 */
// Argument: Slug of mso post (ArticleWebViewClient)
class DisqusGetComments extends AsyncTask<String, Void, ArrayList<Comments>> {
    WeakReference<ListView> commentsView;
    WeakReference<Button> actionButton;
    WeakReference<ProgressBar> pb;
    TextView noComments;

    DisqusGetComments(ListView commentsView, Button actionButton, ProgressBar pb) {
        this.commentsView = new WeakReference<>(commentsView);
        this.actionButton = new WeakReference<>(actionButton);
        this.pb = new WeakReference<>(pb);
    }

    @Override
    protected void onPreExecute() {
        if (pb.get() != null)
            pb.get().setVisibility(View.VISIBLE);

        // No comments here yet. Be the first!
        if (commentsView.get() != null) {
            noComments = new TextView(commentsView.get().getContext());
            noComments.setText("No comments here yet. Be the first!");
            noComments.setPadding(12, 8, 12, 0);
            noComments.setTypeface(Typeface.DEFAULT);
            noComments.setTextColor(Color.BLACK);

            commentsView.get().addHeaderView(noComments);
        }
    }

    @Override
    protected ArrayList<Comments> doInBackground(String... args) {
        DisqusDetails disqus = new DisqusDetails();

        return disqus.getComments(args[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<Comments> comments) {
        if (pb.get() != null)
            pb.get().setVisibility(View.GONE);

        if (commentsView.get() != null) {
            // remove header
            if (!comments.isEmpty())
                commentsView.get().removeHeaderView(noComments);

            commentsView.get().setAdapter(
                    new DisqusAdapter(commentsView.get().getContext(), comments));
        }
        if (actionButton.get() != null) {
            actionButton.get().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DisqusLogin.class);
                    ((DisqusMainActivity) v.getContext()).startActivityForResult(intent, 1);
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


