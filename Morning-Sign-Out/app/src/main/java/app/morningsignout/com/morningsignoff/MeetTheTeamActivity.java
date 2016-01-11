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
import android.widget.GridView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.KeyEvent;

/**
 * Created by tanvikamath on 12/26/15.
 */
public class MeetTheTeamActivity extends ActionBarActivity {
    EditText searchBar;
    GridView gridView;
    MeetTheTeamAdapter gridViewCustomAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meet_the_team_view);
        setupActionBar();

        // Background of edittext, menu of actionbar to disqus (blank) menu, asynctask for mso staff
        searchBar = (EditText) findViewById(R.id.search_bar);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grid_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    void setupActionBar() {
        // ImageButton is Morning Sign Out logo, which sends user back to home screen (see XML)
        // Setting imageButton to center of actionbar
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title, null);
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