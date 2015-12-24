package app.morningsignout.com.morningsignoff;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/26/2015.
 */
// Argument: Slug of mso post (ArticleWebViewClient)
class DisqusGetComments extends AsyncTask<String, Void, ArrayList<Comments>> {
    WeakReference<ListView> commentsView;
    WeakReference<ProgressBar> pb;
    TextView noComments;
    WeakReference<DisqusMainActivity> act; // FIXME: we need to pass in dsq_thread_id, for now we get it here and set it for act.
    boolean hasToken;

    DisqusGetComments(ListView commentsView, ProgressBar pb, DisqusMainActivity act, boolean hasToken) {
        this.commentsView = new WeakReference<>(commentsView);
        this.pb = new WeakReference<>(pb);
        this.act = new WeakReference<>(act);
        this.hasToken = hasToken;
    }

    // leave action button and activity null, just refresh commentsView
    DisqusGetComments(ListView commentsView, ProgressBar pb) {
        this.commentsView = new WeakReference<>(commentsView);
        this.pb = new WeakReference<>(pb);
        this.act = new WeakReference<>(null);
    }

    @Override
    protected void onPreExecute() {
        // Loading
        if (pb.get() != null)
            pb.get().setVisibility(View.VISIBLE);

        // If listview is null, user probably exited activity early. Don't bother with task.
        boolean isNotNull = commentsView.get() != null;
        // Don't add header if it already exists
        boolean noHeader = isNotNull && (commentsView.get().getHeaderViewsCount() == 0);
        // Don't add header if this is refresh and comments already exist in listview
        boolean isEmpty = isNotNull &&
                (commentsView.get().getAdapter() == null || commentsView.get().getCount() == 0);

        // No comments here yet. Be the first!
        if (noHeader && isEmpty) {
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

        return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<Comments> comments) {
        // Remove progress bar for comments
        if (pb.get() != null)
            pb.get().setVisibility(View.GONE);

        // Set up list of comments
        if (commentsView.get() != null) {
            // remove header if comments exist
            if (!comments.isEmpty()) commentsView.get().removeHeaderView(noComments);

            DisqusAdapter adapter = (DisqusAdapter) commentsView.get().getAdapter();
            adapter

//            WrapperListAdapter wrapper = (WrapperListAdapter) commentsView.get().getAdapter();
//            if (wrapper == null) {
//                commentsView.get().setAdapter(
//                        new DisqusAdapter(commentsView.get().getContext(), comments));
//            } else {
//                DisqusAdapter adapter = (DisqusAdapter) wrapper.getWrappedAdapter();
//                adapter.changeList(comments);
//            }

//            commentsView.get().setAdapter(
//                    new DisqusAdapter(commentsView.get().getContext(), comments));
        }

        // Set up action button (if relevant)
        if (act.get() != null) {
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

