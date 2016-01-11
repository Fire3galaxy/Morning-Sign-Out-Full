package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by tanvikamath on 12/26/15.
 */
public class MeetTheTeamActivity extends ActionBarActivity {
    GridView gridView;
    MeetTheTeamAdapter gridViewCustomAdapter;
    LinearLayout mttLayout;
    ProgressBar taskProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meet_the_team_view);
        setupActionBar();

        // Background of edittext, menu of actionbar to disqus (blank) menu, asynctask for mso staff
        EditText searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String query = v.getText().toString();
                    if (!query.isEmpty()) {
                        //Log.d("","");
                    }
                }
                return handled;
            }
        });

        gridView=(GridView)findViewById(R.id.meetTheTeamGridView);
        // Create the Custom Adapter Object
        gridViewCustomAdapter = new MeetTheTeamAdapter(this);
        // Set the Adapter to GridView
        gridView.setAdapter(gridViewCustomAdapter);

        mttLayout = (LinearLayout) findViewById(R.id.layout_mtt);
        taskProgress = (ProgressBar) findViewById(R.id.progressBar_mttTask);

        new FetchMeetTheTeamTask(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_justlogo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void setTeamsMap(final Map<String, ArrayList<ExecutiveListItem>> fromTask) {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // NOTE: The exact order of the team is hardcoded, button click is assumed
                // to correctly correspond to team
                MeetTheTeamAdapter.TeamTitle team =
                        (MeetTheTeamAdapter.TeamTitle) gridViewCustomAdapter.getItem(position);

                Intent intent = new Intent(MeetTheTeamActivity.this, ExecutiveActivity.class);
                intent.putParcelableArrayListExtra(FetchMeetTheTeamTask.TEAM_KEY, fromTask.get(team.realName));
                intent.putExtra(FetchMeetTheTeamTask.NAME_KEY, team.caption);
                startActivity(intent);
            }
        });
    }

    public void showActivity() {
        taskProgress.setVisibility(View.GONE);
        mttLayout.setVisibility(View.VISIBLE);
    }

    public void cancel() {
        Toast.makeText(this, "Could not finish request. Is there an internet issue?", Toast.LENGTH_SHORT).show();
        finish();
    }

    void setupActionBar() {
        // ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
        // Setting imageButton to center of actionbar
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        this.getSupportActionBar().setCustomView(ib, params);

        // Disabling title text of actionbar, enabling imagebutton
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    // view parameter needed for title.xml onClick()
    public void returnToParent(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }
}