package app.morningsignout.com.morningsignoff.meet_the_team;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.network.FetchMeetTheTeamTask;

/**
 * Created by tanvikamath on 12/26/15.
 */
public class MeetTheTeamActivity extends ActionBarActivity {
    GridView gridView;
    MeetTheTeamAdapter gridViewCustomAdapter;
    LinearLayout mttLayout;
    ProgressBar taskProgress;
    Map<String, ArrayList<MTTListViewItem>> departments;

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
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    String query = v.getText().toString();

                    if (!query.isEmpty()) {
                        Intent intent = new Intent(MeetTheTeamActivity.this, MTTListViewActivity.class);
                        intent.putParcelableArrayListExtra(FetchMeetTheTeamTask.TEAM_KEY, searchForPerson(query));
                        intent.putExtra(FetchMeetTheTeamTask.NAME_KEY, "Search: " + query);
                        startActivity(intent);
                    }

                    handled = true;
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

    public void setTeamsMap(Map<String, ArrayList<MTTListViewItem>> fromTask) {
        departments = fromTask;

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // NOTE: The exact order of the team is hardcoded, button click is assumed
                // to correctly correspond to team
                MeetTheTeamAdapter.TeamTitle team =
                        (MeetTheTeamAdapter.TeamTitle) gridViewCustomAdapter.getItem(position);
                Intent intent = new Intent(MeetTheTeamActivity.this, MTTListViewActivity.class);
                intent.putParcelableArrayListExtra(FetchMeetTheTeamTask.TEAM_KEY, departments.get(team.realName));
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
        Toast.makeText(this, "Could not finish request, try again later?", Toast.LENGTH_SHORT).show();
        finish();
    }

    ArrayList<MTTListViewItem> searchForPerson(String query){
        ArrayList<MTTListViewItem> results = new ArrayList<>();
        final String regexWordSplit = "[\\p{javaWhitespace},-\\\\+_.]+";

        // All words reduced to lowercase to simplify search
        String[] parsedQuery = query.toLowerCase().split(regexWordSplit);

        // iterate through departments to find query
        for (String s : departments.keySet()) {
            for (MTTListViewItem m : departments.get(s)) {
                // Super simple "if name or position matches, add to list" search
                // name - partial word matches ok
                if (containsAny(m.name.toLowerCase(), parsedQuery)) {
                    if (!results.contains(m)) results.add(m);
                    break;
                }

                // position - do not want partial word matches here,
                // but partial full title (e.g. "editor (in chief)") is acceptable
                String[] positionWords = m.position.toLowerCase().split(regexWordSplit);
                for (String p : positionWords) {
                    if (equalsAny(p, parsedQuery)) {
                        if (!results.contains(m)) results.add(m);
                        break;
                    }
                }
            }
        }

        return results;
    }

    private boolean containsAny(String s, String[] words) {
        for (String w : words)
            if (s.contains(w))
                return true;

        return false;
    }

    private boolean equalsAny(String s, String[] words) {
        for (String w : words)
            if (s.equals(w))
                return true;

        return false;
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