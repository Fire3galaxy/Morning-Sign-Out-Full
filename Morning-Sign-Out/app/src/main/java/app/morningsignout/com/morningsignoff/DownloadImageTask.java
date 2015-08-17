package app.morningsignout.com.morningsignoff;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import app.morningsignout.com.morningsignoff.HeadlineArtContract.*;

/**
 * Created by Daniel on 6/22/2015.
 */
// For downloading images from latest articles. Code partially from
// http://developer.android.com/guide/components/processes-and-threads.html
// http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
public class DownloadImageTask extends AsyncTask<Integer, Void, Article> {
    MyContextWrapper mCWrapper;
    HeadlineFragment mFragment;
    ImageButton i;
    ProgressBar progressBar;
    TextView textView;

    // Courtesy to http://stackoverflow.com/questions/7880657/best-practice-to-pass-context-to-non-activity-classes
    // ET-CS
    private class MyContextWrapper extends ContextWrapper {
        public MyContextWrapper(Context base) {
            super(base);
        }
    }

    DownloadImageTask(HeadlineFragment fragment, Context context, View view, ImageButton imageButton) {
        mCWrapper = new MyContextWrapper(context);
        mFragment = fragment;
        i = imageButton;
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar_headline);
        textView = (TextView) view.findViewById(R.id.textView_headline);
    }

    // Displays progress bar
    protected void onPreExecute() {
        Log.e("onPreExecute", "Setting progress bar~~~~~~~~~~~~");
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() */
    protected Article doInBackground(Integer... headlinePageNumber) {
        // Get basic article info from internet
        Article article = FetchHeadlineArticles.getArticles("featured",
                headlinePageNumber[0]);

        try {
            byte[] bytes;

            // Lowers resolution of images by subsampling image, saves memory & time
            BitmapFactory.Options a = new BitmapFactory.Options();
            a.inSampleSize = 1;

            // Download image from website
            InputStream in = new URL(article.getImageURL()).openStream();

            // Save bitmap here
            article.setBitmap(BitmapFactory.decodeStream(in, null, a));
        } catch (IOException e) {
            Log.e("HEADLINE IMAGE DOWNLOAD", e.getMessage());
        }

        return article;
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
    protected void onPostExecute(final Article result) {
        // Preserve aspect ratio of image
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        i.setCropToPadding(true);

        // Set downloaded bitmap
        i.setImageBitmap(result.getBitmap());

        // When clicked, should open webview to article
        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent articlePageIntent = new Intent(mCWrapper, ArticleActivity.class)
                        .putExtra(Intent.EXTRA_HTML_TEXT, result.getLink())
                        .putExtra(Intent.EXTRA_SHORTCUT_NAME, result.getTitle());
                mFragment.startActivity(articlePageIntent);
            }
        });

        // Title
        textView.setText(result.getTitle());

        // Remove loading bar
        progressBar.setVisibility(ProgressBar.GONE);

        // Make title visible
        textView.setVisibility(TextView.VISIBLE);

        // Save new data to HeadlineFragment
        mFragment.setArticle(result);
    }

    private Cursor getH_articleLINK(String link) {
        return mCWrapper.getContentResolver().query(
                H_articleEntry.CONTENT_URI,
                null,
                H_articleEntry.COLUMN_LINK + "=?",
                new String[]{link},
                null);
    }

    private Cursor getH_articleID(long _id) {
        return mCWrapper.getContentResolver().query(
                H_articleEntry.CONTENT_URI,
                null,
                H_articleEntry._ID + "=?",
                new String[]{Long.toString(_id)},
                null);
    }

    private long addH_article(String title, String link, byte[] imageByteStream) {
        Log.e("addH_article", "HERE NOW!!!~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        long _id = 0;

        // Query database to see if article exists
        Cursor c = getH_articleLINK(link);

        try {
            // Insert H_articleEntry if not found
            if (c.getCount() == 0) {
                ContentValues articleValues = new ContentValues();
                articleValues.put(H_articleEntry.COLUMN_TITLE, title);
                articleValues.put(H_articleEntry.COLUMN_LINK, link);
                articleValues.put(H_articleEntry.COLUMN_IMAGEBYTESTREAM, imageByteStream);

                _id = H_articleEntry.getIndexFromUri(
                        mCWrapper.getContentResolver().insert(H_articleEntry.CONTENT_URI,
                                articleValues));

                Log.e("addH_article", "Article getting inserted~~~~~~~~~~~~~~~");
            }

            // Or just return _id of existing row
            else {
                _id = c.getLong(c.getColumnIndex(H_articleEntry._ID));

                Log.e("addH_article", "Article already inserted~~~~~~~~~~~~~~~");
            }
        } finally {
            if (c != null) c.close();
        }

        return _id;
    }
}
