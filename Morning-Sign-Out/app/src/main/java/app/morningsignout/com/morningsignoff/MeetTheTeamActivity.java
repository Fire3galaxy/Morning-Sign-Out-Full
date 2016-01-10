package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.GridView;
import android.widget.EditText;
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

        searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String query = v.getText().toString();
                    if (!query.isEmpty()) {
                        Log.d("","");
                    }
                }
                return false;
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
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}