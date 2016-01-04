package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DisqusMainActivity extends ActionBarActivity {
    final static String SLUG = "slug";
    private final static String LOGIN = "Login",
            ACCESS_TOKEN = "Access Token",
            REFRESH_TOKEN = "Refresh Token",
            EXPIRES_IN = "Expires in",
            USERNAME = "Username";

    ListView commentsView;
    Button actionButton;
    EditText commentText;
    ProgressBar dsqPb, dsqTextPb;
    SwipeRefreshLayout swipeRefresh;

    String slug;
    String dsq_thread_id;
    AccessToken accessToken;
    boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disqus);

        // Action Bar: MSO Logo in middle, Up button as X
        setActionBarDetails();

        final DisqusMainActivity ref = this; // FIXME

        // Views that will change later
        commentsView = (ListView) findViewById(R.id.listView_disqus);   // list of comments
        commentsView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // FIXME
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (commentsView.getAdapter() != null) {
                    final DisqusAdapter adapter = (DisqusAdapter) commentsView.getAdapter();
                    adapter.selectItem(id, DisqusAdapter.OPTIONS_ROW);
                }
            }
        });
        actionButton = (Button) findViewById(R.id.button_disqus);       // login/post
        dsqPb = (ProgressBar) findViewById(R.id.progressBar_dsq);       // pb for comments
        commentText = (EditText) findViewById(R.id.editText_commentMain);   // editText
        dsqTextPb = (ProgressBar) findViewById(R.id.progressBar_dsqText);   // pb for button/editText
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh_disqus); // Swipe to refresh
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshComments(false);
                setRefreshOff();
            }
        });

        // FIXME: Later, when we use json to load an article, dsq_thread_id will be passed in
        // FIXME: intent, instead of slug
        // Slug to get dsq_thread_id from json of article for get disqus thread data
        slug = null;
        if (getIntent() != null)
            slug = getIntent().getStringExtra(SLUG);

        // Load comments into listview, set button action
        accessToken = getLogin();
        boolean hasToken = accessToken != null;
        new DisqusGetComments(commentsView, dsqPb, this, hasToken, false).execute(slug);
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
            case R.id.action_login:
                Intent intent = new Intent(this, DisqusLogin.class);
                startActivityForResult(intent, 1);
                return true;
            case R.id.action_logout:
                logout();
                commentText.setVisibility(View.GONE);
                setActionButtonToLogin();
                return true;
            case R.id.action_refresh:
                refreshComments(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 0: login, 1: logout
        if (loggedIn) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(true);
        } else {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Returns from DisqusLogin
        if (resultCode == Activity.RESULT_OK) {
            String code = data.getStringExtra(DisqusDetails.CODE_KEY);
            new DisqusGetAccessToken(dsqTextPb, this).execute(code, dsq_thread_id);
        } else if (resultCode == Activity.RESULT_CANCELED)
            Log.d("DisqusActivity", "Cancelled");
        //Log.d("",""); // can delete this. just htc m8 testing bugs.
    }

    public void refreshComments(boolean pause) {
        int pauseTime = 0;
        if (pause) pauseTime = 4 * 1000;

        final DisqusMainActivity ref = this;

        // Delay 4 seconds, then refresh
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new DisqusGetComments(commentsView, dsqPb, ref, false, true).execute(slug);
            }
        }, pauseTime);
    }

    public void setRefreshOff() {
        swipeRefresh.setRefreshing(false);
    }

    public void setDsq_thread_id(String id) {
        dsq_thread_id = id;
    }

    // Save accesstoken, special "set" method
    public void saveLogin(AccessToken accessToken) {
        this.accessToken = accessToken;

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(LOGIN, true);

        // Access token fields
        editor.putString(ACCESS_TOKEN, accessToken.access_token);
        editor.putString(REFRESH_TOKEN, accessToken.refresh_token);
        editor.putString(EXPIRES_IN, accessToken.expires_in);
        editor.putString(USERNAME, accessToken.username);

        editor.commit(); // .commit() runs on main thread, .apply() does it in background

        loggedIn = true;
        invalidateOptionsMenu();
    }

    // Get from sharedpreferences
    public AccessToken getLogin() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        loggedIn = settings.getBoolean(LOGIN, false);
        invalidateOptionsMenu(); // Change menu based on value of loggedIn

        if (loggedIn) return new AccessToken(settings.getString(ACCESS_TOKEN, ""),
                    settings.getString(EXPIRES_IN, ""),
                    settings.getString(USERNAME, ""),
                    settings.getString(REFRESH_TOKEN, ""));
        else return null;
    }

    // normal "get" method
    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void logout() {
        loggedIn = false;
        invalidateOptionsMenu(); // Change menu based on value of loggedIn

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(LOGIN, false);
        editor.commit(); // chose main thread for logout. security?
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

    // Methods used to affect UI in asynctask
    public void setActionButtonToLogin() {
        String login = (String) getResources().getText(R.string.disqus_login);
        actionButton.setText(login);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DisqusLogin.class);
                ((DisqusMainActivity) v.getContext()).startActivityForResult(intent, 1);
            }
        });

        if (actionButton.getVisibility() == View.GONE) actionButton.setVisibility(View.VISIBLE);
    }

    public void setActionButtonToPost() {
        // Change action button listener from login to post
        String post = (String) getResources().getText(R.string.disqus_post);
        actionButton.setText(post);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentText.onEditorAction(EditorInfo.IME_ACTION_SEND);
            }
        });

        if (actionButton.getVisibility() == View.GONE) actionButton.setVisibility(View.VISIBLE);
    }

    public void setupEditText() {
        final DisqusMainActivity ref = this;

        commentText.setHorizontallyScrolling(false);

        // Set up post comment "enter" button
        commentText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    // Post comment
                    String message = v.getText().toString();
                    if (!message.isEmpty()) {
                        new DisqusPostComment(ref).execute(dsq_thread_id, null, v.getText().toString());

                        //"2430241165" // FIXME
                        v.setText(""); // Clear text from editText
                        refreshComments(true); // Refresh comments
                    }

                    handled = true;
                }

                return handled;
            }
        });

        commentText.setVisibility(View.VISIBLE);  // Add EditText widget
    }
}

class DisqusAdapter extends BaseAdapter {
    static final int INDENT = 20;
    static final int COMMENTS_ROW = 0, OPTIONS_ROW = 1, SUBCOMMENT_ROW = 2;
    static final int POST = 0, TEXT = 1;

    Context c;
    ArrayList<Comments> commentsList;
    Resources resources;
    long itemSelected = -1;
    int extraViews = 0;

    DisqusAdapter(Context c, ArrayList<Comments> commentsList) {
        this.c = c;
        this.commentsList = commentsList;

        resources = c.getResources();
    }

    void selectItem(long itemSelected, int typeSelected) {
        // Clicking comment again after options or subcomment already inflated
        // Note: if typeSelected is SUBCOMMENT, then selectItem() is called by reply button
        // else, called by comment click listener
        if (itemSelected == this.itemSelected && typeSelected == OPTIONS_ROW) {
            this.itemSelected = -1;
            this.extraViews = COMMENTS_ROW;
            notifyDataSetChanged();
            return;
        }

        this.itemSelected = itemSelected;
        this.extraViews = typeSelected;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return commentsList.size() + extraViews; // Change based on int extraViews
    }

    @Override
    public Object getItem(int position) {
        if (position <= itemSelected)
            return commentsList.get(position);
        else if (position > itemSelected + extraViews)
            return commentsList.get(position - extraViews);
        else
            return "";
    }

    // Based on index in comments list or which extra view it is
    @Override
    public long getItemId(int position) {
        if (position <= itemSelected)
            return position;
        else if (position > itemSelected + extraViews)
            return position - extraViews;

        // All extra view ids are after ids of comments
        return commentsList.size() - 1 + (position - itemSelected);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        // True if comments, false if extra view
        return position <= itemSelected || position > itemSelected + extraViews;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DsqViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(c);

        if (convertView == null) {
            // Extra row
            if (itemSelected != -1) {
                if (extraViews == OPTIONS_ROW && position == itemSelected + 1) {
                    View optionsRow = inflater.inflate(R.layout.options_row, parent, false);
                    viewHolder = new DsqViewHolder();
                    viewHolder.type = OPTIONS_ROW;
                    optionsRow.setTag(viewHolder);

                    Button reply = (Button) optionsRow.findViewById(R.id.button_reply);
                    Button seeUser = (Button) optionsRow.findViewById(R.id.button_seeUser);
                    reply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectItem(itemSelected, SUBCOMMENT_ROW);

                        }
                    });
                    seeUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri profile = Uri.parse(
                                    commentsList.get(Long.valueOf(itemSelected).intValue())
                                            .profile_url);
                            Intent visitProfile = new Intent(Intent.ACTION_VIEW, profile);
                            c.startActivity(visitProfile);
                        }
                    });

                    return optionsRow;
                } else if (extraViews == 2) {
                    Log.e("DisqusAdapter", "error: extra views = 2 and view was null");
                    if (position == itemSelected + 1) {
                        // Inflate post button
                        View subcomment = inflater.inflate(R.layout.post_row, parent, false);

                        DsqViewHolder holder = new DsqViewHolder();
                        holder.type = SUBCOMMENT_ROW;
                        holder.subtype = POST;
                        subcomment.setTag(holder);

                        return subcomment;
                    } else if (position == itemSelected + 2) {
                        // Inflate EditText
                        View editText = inflater.inflate(R.layout.text_row, parent, false);

                        DsqViewHolder holder = new DsqViewHolder();
                        holder.type = SUBCOMMENT_ROW;
                        holder.subtype = TEXT;
                        editText.setTag(holder);

                        return editText;
                    }
                }
            }

            // Comment row
            convertView = inflater.inflate(R.layout.comment_row, parent, false);
            viewHolder = new DsqViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView_userDsq);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.textView_commentDsq);
            viewHolder.type = COMMENTS_ROW;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DsqViewHolder) convertView.getTag();

            if (itemSelected != -1) {
                // Need new view
                if (extraViews == OPTIONS_ROW && position == itemSelected + 1) {
                    if (viewHolder.type == OPTIONS_ROW) // Is already an option row
                        return convertView;

                    final View optionsRow = inflater.inflate(R.layout.options_row, parent, false);
                    viewHolder = new DsqViewHolder();
                    viewHolder.type = OPTIONS_ROW;
                    optionsRow.setTag(viewHolder);

                    Button reply = (Button) optionsRow.findViewById(R.id.button_reply);
                    final Button seeUser = (Button) optionsRow.findViewById(R.id.button_seeUser);
                    reply.setOnClickListener(new View.OnClickListener() {
                        WeakReference<Button> su = new WeakReference<>(seeUser);

                        @Override
                        public void onClick(View v) { // Reply to comment
                            selectItem(itemSelected, SUBCOMMENT_ROW);

                        }
                    });
                    seeUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { // See User's Disqus profile
                            Uri profile = Uri.parse(
                                    commentsList.get(Long.valueOf(itemSelected).intValue())
                                            .profile_url);
                            Intent visitProfile = new Intent(Intent.ACTION_VIEW, profile);
                            c.startActivity(visitProfile);
                        }
                    });

                    return optionsRow;
                } else if (extraViews == 2) {
                    if (position == itemSelected + 1) {
                        if (viewHolder.type == SUBCOMMENT_ROW && viewHolder.subtype == POST)
                            return convertView;

                        // Inflate post button
                        View subcomment = inflater.inflate(R.layout.post_row, parent, false);

                        DsqViewHolder holder = new DsqViewHolder();
                        holder.type = SUBCOMMENT_ROW;
                        holder.subtype = POST;
                        subcomment.setTag(holder);

                        return subcomment;
                    } else if (position == itemSelected + 2) {
                        if (viewHolder.type == SUBCOMMENT_ROW && viewHolder.subtype == TEXT)
                            return convertView;

                        // Inflate EditText
                        View editText = inflater.inflate(R.layout.text_row, parent, false);

                        DsqViewHolder holder = new DsqViewHolder();
                        holder.type = SUBCOMMENT_ROW;
                        holder.subtype = TEXT;
                        editText.setTag(holder);

                        return editText;
                    }
                }
            }

            // Did not return from code above. Definitely NOT an extra row, must be a comment row
            if (viewHolder.type != COMMENTS_ROW) {  // Used to be an extra row.
                convertView = inflater.inflate(R.layout.comment_row, parent, false);
                viewHolder = new DsqViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.textView_userDsq);
                viewHolder.comment = (TextView) convertView.findViewById(R.id.textView_commentDsq);
                viewHolder.type = COMMENTS_ROW;
                convertView.setTag(viewHolder);
            }
        }

        // If extra views are in listview, then positions will be off from index for comments
        int pos = position;
        if (position > itemSelected + extraViews) pos = position - extraViews;
        final int i = pos;

        // Padding will change for views based on where extra views are
        int padding = getPxFromDp(INDENT * commentsList.get(i).indent);

        if (convertView.getPaddingLeft() != padding)                // A subcomment
            convertView.setPadding(padding, 0, 0, 0);
        viewHolder.name.setText(commentsList.get(i).name);          // username
        viewHolder.name.setOnClickListener(new View.OnClickListener() { // Link to Disqus Profile
            @Override
            public void onClick(View v) {
                Uri profile = Uri.parse(commentsList.get(i).profile_url);
                Intent visitProfile = new Intent(Intent.ACTION_VIEW, profile);
                c.startActivity(visitProfile);
            }
        });
        viewHolder.comment.setText(commentsList.get(i).message);    // comment

        return convertView;
    }

    int getPxFromDp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    class DsqViewHolder {
        TextView name;
        TextView comment;
        int type = 0;
        int subtype = 0;
    }
}