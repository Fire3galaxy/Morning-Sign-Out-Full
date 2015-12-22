package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

    WeakReference<DisqusMainActivity> act; // FIXME: Will not need once we can just pass in dsq_thread_id

    DisqusGetComments(ListView commentsView, Button actionButton, ProgressBar pb, DisqusMainActivity act) {
        this.commentsView = new WeakReference<>(commentsView);
        this.actionButton = new WeakReference<>(actionButton);
        this.pb = new WeakReference<>(pb);

        this.act = new WeakReference<>(act);
    }

    // leave action button and activity null, just refresh commentsView
    DisqusGetComments(ListView commentsView, ProgressBar pb) {
        this.commentsView = new WeakReference<>(commentsView);
        this.pb = new WeakReference<>(pb);

        this.actionButton = new WeakReference<>(null);
        this.act = new WeakReference<>(null);
    }

    @Override
    protected void onPreExecute() {
        // Loading
        if (pb.get() != null)
            pb.get().setVisibility(View.VISIBLE);

        // If listview is null, user probably exited activity early. Don't bother with task.
        boolean isNotNull = !(commentsView.get() == null);
        // Don't add header if it already exists
        boolean noHeader = isNotNull && (commentsView.get().getHeaderViewsCount() == 0);
        // Don't add header if this is refresh and comments already exist in listview
        boolean isEmpty = isNotNull &&
                (commentsView.get().getAdapter() == null || commentsView.get().getCount() == 0);

        // No comments here yet. Be the first!
        if (isNotNull && noHeader && isEmpty) {
            noComments = new TextView(commentsView.get().getContext());
            noComments.setText("No comments here yet. Be the first!");
            noComments.setPadding(12, 8, 12, 0);
            noComments.setTypeface(Typeface.DEFAULT);
            noComments.setTextColor(Color.BLACK);

            commentsView.get().addHeaderView(noComments);
        }
    }

    // args[0] = slug for article
    // returns list of comments (thread id stored in reference)
    @Override
    protected ArrayList<Comments> doInBackground(String... args) {
        DisqusDetails disqus = new DisqusDetails();

        TempCommentsAndThreadId ret = disqus.getComments(args[0]);
        if (act.get() != null && ret != null) {
            act.get().setDsq_thread_id(ret.dsq_thread_id);

            return ret.comments;
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Comments> comments) {
        // Remove progress bar for comments
        if (pb.get() != null)
            pb.get().setVisibility(View.GONE);

        // If no comments, put nothing.
        if (comments == null)
            return;

        // Set up list of comments
        if (commentsView.get() != null) {
            // remove header
            if (!comments.isEmpty())
                commentsView.get().removeHeaderView(noComments);

            commentsView.get().setAdapter(
                    new DisqusAdapter(commentsView.get().getContext(), comments));
        }

        // Set up Login Button (if relevant)
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
    WeakReference<Button> actionButton;
    WeakReference<EditText> commentText;
    WeakReference<ProgressBar> dsqTextPb;

    // For getComments call in editorAction listener
    WeakReference<DisqusMainActivity> act;

    public DisqusGetAccessToken(Button actionButton, EditText commentText, ProgressBar dsqTextPb,
                                DisqusMainActivity act) {
        this.actionButton = new WeakReference<>(actionButton);
        this.commentText = new WeakReference<>(commentText);
        this.dsqTextPb = new WeakReference<>(dsqTextPb);
        this.act = new WeakReference<>(act);
    }

    @Override
    protected void onPreExecute() {
        if (commentText.get() != null && dsqTextPb.get() != null) {
            commentText.get().setText("");
            dsqTextPb.get().setVisibility(View.VISIBLE);
        }
    }

    // parameters: code, dsq_thread_id
    // returns: access token object
    @Override
    public AccessToken doInBackground(String... disqusItems) {
        dsq_thread_id = disqusItems[1];
        DisqusDetails disqus = new DisqusDetails();

        return disqus.getAccessToken(disqusItems[0]);
    }

    @Override
    public void onPostExecute(final AccessToken token) {
        if (actionButton.get() != null && commentText.get() != null &&
                dsqTextPb.get() != null && act.get() != null) {
            // Properties to force line wrapping in edit text (not working in xml)
            commentText.get().setHorizontallyScrolling(false);

            // Set up post comment "enter" button
            commentText.get().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                AccessToken accessToken = token;
                String thread_id = dsq_thread_id;

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        // Post comment
                        String message = v.getText().toString();
                        if (!message.isEmpty()) {
                            new DisqusPostComment().execute(accessToken.access_token,
                                    thread_id,
                                    v.getText().toString());

                            v.setText(""); // Clear text from editText
                            act.get().refreshComments(); // Refresh comments

                            Log.d("DisqusPostComments", "Posted!");
                        }

                        handled = true;
                    }

                    return handled;
                }
            });

            // Changing layout
            dsqTextPb.get().setVisibility(View.GONE);       // Remove progressbar
            commentText.get().setVisibility(View.VISIBLE);  // Add EditText widget

            // Change action button listener from login to post
            String post = (String) act.get().getResources().getText(R.string.disqus_post);
            actionButton.get().setText(post);
            actionButton.get().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentText.get().onEditorAction(EditorInfo.IME_ACTION_SEND);
                }
            });
        }
    }
}

class DisqusPostComment extends AsyncTask<String, Void, Void> {
    // 1. Access token
    // 2. thread id
    // 3. message (not encoded for url)
    @Override
    protected Void doInBackground(String... args) {
        if (args.length != 3) return null;

        DisqusDetails details = new DisqusDetails();
        details.postComment(args[0], args[1], args[2]);

        return null;
    }
}

