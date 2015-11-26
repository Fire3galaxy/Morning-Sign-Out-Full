package app.morningsignout.com.morningsignoff;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/26/2015.
 */
// Argument: Slug of mso post (ArticleWebViewClient)
class DisqusGetComments extends AsyncTask<String, Void, ArrayList<Comments>> {
    WeakReference<ListView> commentsView;

    DisqusGetComments(ListView commentsView) {
        this.commentsView = new WeakReference<>(commentsView);
    }

    @Override
    protected ArrayList<Comments> doInBackground(String... args) {
        DisqusDetails disqus = new DisqusDetails();

        return disqus.getComments(args[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<Comments> comments) {
        Log.d("DisqusTasks", "Comment onPostExecute");
        if (commentsView.get() != null)
            commentsView.get().setAdapter(
                    new DisqusAdapter(commentsView.get().getContext(), comments));
    }
}

