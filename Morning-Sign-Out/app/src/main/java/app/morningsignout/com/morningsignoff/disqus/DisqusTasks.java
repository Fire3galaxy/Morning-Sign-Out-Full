package app.morningsignout.com.morningsignoff.disqus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by Daniel on 11/26/2015.
 */
// Argument: Slug of mso post (ArticleWebViewClient)
class DisqusGetComments extends AsyncTask<String, Void, TempCommentsAndThreadId> {
    WeakReference<ListView> commentsView;
    WeakReference<ProgressBar> pb;
    WeakReference<DisqusMainActivity> act;
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

//        addNoCommentsHeaderIfNeeded();
    }

    // args[0] = slug for article in !justRefresh, dsq thread id in justRefresh
    // returns list of comments (thread id stored in reference)
    @Override
    protected TempCommentsAndThreadId doInBackground(String... args) {
        TempCommentsAndThreadId catiPair = new DisqusDetails().getComments(args[0], justRefresh);

        // Try again once if network issue
        if (catiPair.code == 2) {
            try {
                Thread.sleep(1000); // 1 second
            } catch (InterruptedException e) {
                Log.e("DisqusGetComments", "Error in thread sleep");
            }

            catiPair = new DisqusDetails().getComments(args[0], justRefresh);
        }

        return catiPair;
    }

    @Override
    protected void onPostExecute(TempCommentsAndThreadId catiPair) { // cati - Comments And Thread Id
        // Remove progress bar for comments
        if (pb.get() != null)
            pb.get().setVisibility(View.GONE);

        // Cannot comment on page b/c not allowed/no thread id? Close activity and toast
        if (catiPair.code == 1) {
            if (act.get() != null) {
                act.get().closeActivity();
            }

            return;
        }

        // If first time, give activity the thread id
        if (!justRefresh && act.get() != null)
            if (catiPair.code == 0 || catiPair.code == 3)
                act.get().setDsq_thread_id(catiPair.dsq_thread_id);

        // I guess user will just have to try again later?
        if (catiPair.code == 2 || catiPair.code == 3) {
            if (act.get() != null)
                Toast.makeText(act.get(), "Internet issue. Try refreshing!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up list of comments (code == 0)
        if (commentsView.get() != null && act.get() != null) {
            ListAdapter adapter = commentsView.get().getAdapter();

            // Set new list of comments
            if (adapter == null)
                commentsView.get().setAdapter(new DisqusAdapter(act.get(), catiPair.comments));

            // Change data list of old adapter
            else
                ((DisqusAdapter) adapter).switchList(catiPair.comments);
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

    // 1. thread id
    // 2. parent post id
    // 3. message (not encoded until postComment())
    @Override
    protected Void doInBackground(String... args) {
        if (args.length != 3) return null;

        if (act.get() != null) {
            AccessToken token = act.get().getAccessToken();
            DisqusDetails details = new DisqusDetails();

            DisqusDetails.DisqusResponse response =
                    details.postComment(token.access_token, args[0], args[1], args[2]);

            // Errors: Should never happen!!!!! Test this like crazy.
            if (!response.isSuccess()) {
                // FIXME: Error 18 may not be an indicator of expired token. A token lasts 30 days.
                // FIXME: What to toast/do if posting comment fails.
                // Token is expired. Refresh token, then try again.
                if (response.getCode() == 18) {
                    AccessToken newToken = details.refreshAccessToken(token.refresh_token);

                    if (newToken != null && !newToken.access_token.isEmpty()) { // could return token w/ blank strings on fail.
                        act.get().saveLogin(newToken);
                        details.postComment(token.access_token, args[0], args[1], args[2]);
                    }
                }
                // Token is null? Debug this.
                else if (response.getCode() == 12) {
                    Log.e("DisqusPostComment", "Error: 12 - null token?");
                    Log.e("DisqusPostComment", token.toString());
                }
            } else
                Log.d("DisqusPostComment", "Posted!");
        }

        return null; // Do nothing
    }
}

class DisqusDeleteComment extends AsyncTask<String, Void, Boolean> {
    WeakReference<Context> c;

    DisqusDeleteComment(Context c) {
        this.c = new WeakReference<>(c);
    }

    // params[0] = token
    // params[1] = post id
    @Override
    protected Boolean doInBackground(String... params) {
        if (params.length != 2) // Fail
            return false;

        DisqusDetails details = new DisqusDetails();
        return details.deleteComment(params[0], params[1]);
        // FIXME: if login expires or delete fails do something to try again
    }

    @Override
    protected void onPostExecute(Boolean b) {
        if (!b && c.get() != null)
            Toast.makeText(c.get(), "Delete failed", Toast.LENGTH_SHORT).show();
    }
}