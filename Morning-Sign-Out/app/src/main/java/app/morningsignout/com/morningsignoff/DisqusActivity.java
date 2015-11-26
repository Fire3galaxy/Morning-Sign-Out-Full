package app.morningsignout.com.morningsignoff;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DisqusActivity extends ActionBarActivity {
    final static String SLUG = "slug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // Action Bar
        setActionBarDetails();

        // Commenting webview
        long id = 3635144879L; // long-type literals are SO BIG they need L on the end. who knew?
        String disqus_thread_id = String.valueOf(id);
        String commentsUrl = "http://www.morningsignout.com/showcomments.php?disqus_id="
                + disqus_thread_id;
//        String commentsUrl = "http://morningsignout.com/an-international-patchwork-of-healthcare/";

        ListView commentsView = (ListView) findViewById(R.id.listView_disqus);

        String slug = null;
        if (getIntent() != null)
            slug = getIntent().getStringExtra(SLUG);

        new DisqusGetComments(commentsView).execute(slug);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disqus, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // view parameter needed for title.xml onClick()
    public void returnToParent(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    void setActionBarDetails() {
        // ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
        // Setting imageButton to center of actionbar
        ImageButton home = (ImageButton) getLayoutInflater().inflate(R.layout.title_disqus, null);

        // setting imagebutton
        super.getSupportActionBar().setCustomView(home);

        // setting actionbar variables
        super.getSupportActionBar().setDisplayShowTitleEnabled(false);
        super.getSupportActionBar().setDisplayShowCustomEnabled(true);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

class DisqusAdapter extends BaseAdapter {
    Context c;
    ArrayList<Comments> commentsList;

    DisqusAdapter(Context c, ArrayList<Comments> commentsList) {
        this.c = c;
        this.commentsList = commentsList;
    }

    @Override
    public int getCount() {
        return commentsList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DsqViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(c);
            convertView = inflater.inflate(R.layout.comment_row, parent, false);

            viewHolder = new DsqViewHolder();
            viewHolder.username = (TextView) convertView.findViewById(R.id.textView_userDsq);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.textView_commentDsq);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DsqViewHolder) convertView.getTag();
        }

        viewHolder.username.setText(commentsList.get(position).username);
        viewHolder.comment.setText(commentsList.get(position).message);

        return convertView;
    }

    class DsqViewHolder {
        TextView username;
        TextView comment;
    }
}