package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class DisqusMainActivity extends ActionBarActivity {
    final static String SLUG = "slug";

    Button actionButton;
    EditText commentText;
    ProgressBar dsqPb, dsqTextPb;

    String dsq_thread_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // Action Bar: MSO Logo in middle, Up button as X
        setActionBarDetails();

        // Listview contains comments,
        // button lets you login or post depending on if access token is stored in Preferences
        // Button's onClickListener set in DisqusDetails' DisqusGetComments task
        ListView commentsView = (ListView) findViewById(R.id.listView_disqus);
        actionButton = (Button) findViewById(R.id.button_disqus);

        dsqPb = (ProgressBar) findViewById(R.id.progressBar_dsq);

        // Slug to get dsq_thread_id from json of article for get disqus thread data
        String slug = null;
        if (getIntent() != null)
            slug = getIntent().getStringExtra(SLUG);

        // Load comments into listview, set button action
        new DisqusGetComments(commentsView, actionButton, dsqPb, this).execute(slug, dsq_thread_id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu: just transparent button that shouldn't show
        getMenuInflater().inflate(R.menu.menu_disqus, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Close disqus activity and return to article. No need to keep it around b/c
            // logins, refresh, etc.
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Returns from DisqusLogin
        if (resultCode == Activity.RESULT_OK) {
            String code = data.getStringExtra(DisqusDetails.CODE_KEY);
            Log.d("DisqusActivity", "Code: " + code);

            commentText = (EditText) findViewById(R.id.editText_commentMain);
            dsqTextPb = (ProgressBar) findViewById(R.id.progressBar_dsqText);

            new DisqusGetAccessToken(actionButton, commentText, dsqTextPb).execute(code, dsq_thread_id);
        } else if (resultCode == Activity.RESULT_CANCELED)
            Log.d("DisqusActivity", "Cancelled");
        //Log.d("","");
    }

    public void setDsq_thread_id(String id) {
        dsq_thread_id = id;
    }

    // view parameter needed for title.xml onClick()
    // Currently not used because we shouldn't return home using button in comments.
    // Use home button instead.
    public void returnToParent(View view) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // Note: X button is in style.xml
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
    static final int INDENT = 20;
    static final int INDENT_MAX = 40;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        DsqViewHolder viewHolder;

        if (convertView == null) {
            //Log.d("","");
            LayoutInflater inflater = LayoutInflater.from(c);
            convertView = inflater.inflate(R.layout.comment_row, parent, false);

            viewHolder = new DsqViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView_userDsq);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.textView_commentDsq);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DsqViewHolder) convertView.getTag();
        }

        if (commentsList.get(position).indent != 0)                     // A subcomment
            convertView.setPadding(getPxFromDp(INDENT * commentsList.get(position).indent),
                    0, 0, 0);
        viewHolder.name.setText(commentsList.get(position).name);       // username
        viewHolder.name.setOnClickListener(new View.OnClickListener() { // Link to Disqus Profile
            @Override
            public void onClick(View v) {
                Uri profile = Uri.parse(commentsList.get(position).profile_url);
                Intent visitProfile = new Intent(Intent.ACTION_VIEW, profile);
                c.startActivity(visitProfile);
            }
        });
        viewHolder.comment.setText(commentsList.get(position).message); // comment

        return convertView;
    }

    int getPxFromDp(int dp) {
        Resources r = c.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    class DsqViewHolder {
        TextView name;
        TextView comment;
    }
}