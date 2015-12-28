package app.morningsignout.com.morningsignoff;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/26/2015.
 */
// Argument: Slug of mso post (ArticleWebViewClient)
class DisqusGetComments extends AsyncTask<String, Void, ArrayList<Comments>> {
    WeakReference<ListView> commentsView;
    WeakReference<ProgressBar> pb;
    WeakReference<DisqusMainActivity> act; // FIXME: we need to pass in dsq_thread_id, for now we get it here and set it for act.
    TextView noComments = null;
    TextView errorRefresh = null;
    boolean hasToken = false, justRefresh = false;

    DisqusGetComments(ListView commentsView, ProgressBar pb, DisqusMainActivity act, boolean hasToken, boolean justRefresh) {
        this.commentsView = new WeakReference<>(commentsView);
        this.pb = new WeakReference<>(pb);
        this.act = new WeakReference<>(act);
        this.hasToken = hasToken;
        this.justRefresh = justRefresh;
    }

    @Override
    protected void onPreExecute() {
        // Loading
        if (pb.get() != null)
            pb.get().setVisibility(View.VISIBLE);

        addNoCommentsHeader();
    }

    // args[0] = slug for article
    // returns list of comments (thread id stored in reference)
    @Override
    protected ArrayList<Comments> doInBackground(String... args) {
        DisqusDetails disqus = new DisqusDetails();

        TempCommentsAndThreadId ret = disqus.getComments(args[0]);
        if (ret != null) {
            // If first time, give activity the thread id
            if (!justRefresh && act.get() != null)
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

        // Set up list of comments
        if (commentsView.get() != null && comments != null) {
            // remove header if comments exist
            if (!comments.isEmpty()) commentsView.get().removeHeaderView(noComments);

            commentsView.get().setAdapter(
                    new DisqusAdapter(commentsView.get().getContext(), comments));

            if (comments.isEmpty() && commentsView.get().getHeaderViewsCount() == 0)
                addNoCommentsHeader();
        }

        // Set up action button (if relevant - first time getting)
        if (!justRefresh && act.get() != null) {
            if (!hasToken)
                act.get().setActionButtonToLogin();
            else {
                act.get().setupEditText();
                act.get().setActionButtonToPost();
            }
        }
//        else
//            act.get().setRefreshOff(); // Turn off refreshing animation (if relevant - refresh w/ layout)
    }

    private void addNoCommentsHeader() {
        // If listview is null, user probably exited activity early. Don't bother with task.
        boolean isNotNull = commentsView.get() != null;
        // Don't add header if it already exists
        boolean noHeader = isNotNull && (commentsView.get().getHeaderViewsCount() == 0);
        // Don't add header if this is refresh and comments already exist in listview
        boolean isEmpty = isNotNull &&
                (commentsView.get().getAdapter() == null || commentsView.get().getCount() == 0);

        // No comments here yet. Be the first!
        if (noHeader && isEmpty) {
            if (noComments == null) {
                noComments = new TextView(commentsView.get().getContext());
                noComments.setText("No comments here yet. Be the first!\n\n" +
                        "Swipe down here or hit refresh in the menu to check for more comments.");
                noComments.setPadding(12, 8, 12, 0);
                noComments.setTypeface(Typeface.DEFAULT);
                noComments.setTextColor(Color.BLACK);
            }

            commentsView.get().addHeaderView(noComments);
        }
    }
}

// Called after code is returned in DisqusActivity. Meant to trade code for token, then set button
// to post. So far have code for getting token done (not tested yet).
class DisqusGetAccessToken extends AsyncTask<String, Void, AccessToken> {
    String dsq_thread_id;
    WeakReference<ProgressBar> dsqTextPb;

    // For getComments call in editorAction listener
    WeakReference<DisqusMainActivity> act;

    public DisqusGetAccessToken(ProgressBar dsqTextPb, DisqusMainActivity act) {
        this.dsqTextPb = new WeakReference<>(dsqTextPb);
        this.act = new WeakReference<>(act);
    }

    @Override
    protected void onPreExecute() {
        if (dsqTextPb.get() != null) dsqTextPb.get().setVisibility(View.VISIBLE);
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
        if (dsqTextPb.get() != null) dsqTextPb.get().setVisibility(View.GONE);

        if (act.get() != null) {
            // Save access token
            act.get().saveLogin(token);

            // Set up EditText widget's EditorActionListener
            act.get().setupEditText();

            // Change action button listener from login to post
            act.get().setActionButtonToPost();
        }
    }
}

class DisqusPostComment extends AsyncTask<String, Void, Void> {
    WeakReference<DisqusMainActivity> act;

    public DisqusPostComment(DisqusMainActivity act) {
        this.act = new WeakReference<>(act);
    }

    // 1. Access token
    // 2. thread id
    // 3. message (not encoded for url)
    @Override
    protected Void doInBackground(String... args) {
        if (args.length != 2) return null;

        if (act.get() != null) {
            String code = "\"code\":";

            AccessToken token = act.get().getAccessToken();
            DisqusDetails details = new DisqusDetails();

            // Refresh test
            Log.d("DisqusPostComment", "Original: " + token);

            AccessToken token2 = details.refreshAccessToken(token.refresh_token);
            Log.d("DisqusPostComment", "Refresh: " + token2);

            act.get().saveLogin(token2);

//            String response = details.postComment(token.access_token, args[0], args[1]);
//
//            // Token is expired. Refresh token, then try again.
//            if (response.contains(code + "18")) {
//                AccessToken newToken = details.refreshAccessToken(token.refresh_token);
//
//                // FIXME: do this toast with failed comments too.
//                // Try to comment again, if refresh fails, toast.
//                if (newToken != null) {
//                    act.get().saveLogin(newToken);
//                    details.postComment(token.access_token, args[0], args[1]);
//                } else {
//                    Toast.makeText(act.get(), "Error: try again later", Toast.LENGTH_SHORT).show();
//                }
//            } else if (response.contains(code + "12")) {
////                Log.d("DisqusPostComments", "Token: " + token.access_token);
//
////                act.get().logout();
////                act.get().hideCommentText();
////                act.get().setActionButtonToLogin();
//            } else
//                Log.d("DisqusPostComment", "Posted!");
        }

        return null; // Do nothing
    }
}

