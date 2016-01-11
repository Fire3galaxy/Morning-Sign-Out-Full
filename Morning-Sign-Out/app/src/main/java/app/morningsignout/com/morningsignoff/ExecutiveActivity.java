package app.morningsignout.com.morningsignoff;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liukwarm on 10/24/15.
 */
public class ExecutiveActivity extends ActionBarActivity {

    private SearchView searchView;
    private ArrayList<ExecutiveListItem> list;
    public static String EXTRA_LIST = "list";
    public static String EXTRA_INDEX = "index";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive);

        String teamName = getIntent().getExtras().getString(FetchMeetTheTeamTask.NAME_KEY);

        ListView lv = (ListView) findViewById(R.id.listView);
//        TypedArray names = getResources()
//                .obtainTypedArray(R.array.executive_names);
//        TypedArray positions = getResources()
//                .obtainTypedArray(R.array.executive_positions);
//
        list = getIntent().getExtras().getParcelableArrayList(FetchMeetTheTeamTask.TEAM_KEY);
//        for (int i = 0; i < names.length(); i++) {
//            list.add(new ExecutiveListItem(names.getString(i), positions.getString(i), null)); // FIXME: third thing is hyperlink, this may be changed later.
//        }

        TextView title = (TextView)findViewById(R.id.title);
        title.setText(teamName);

        lv.setAdapter(new ExecutiveListAdapter(getApplicationContext(),
                list));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToLink(position);
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent categoryPageIntent = new Intent(getApplicationContext(), CategoryActivity.class);
                categoryPageIntent.putExtra(Intent.EXTRA_TITLE, 0);
                finish();
                startActivity(categoryPageIntent);
            }
        });
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        this.getSupportActionBar().setCustomView(ib, params);

        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void goToLink(int position) {
        Intent intent = new Intent(this, MTTWebViewActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_LIST, list);
        intent.putExtra(EXTRA_INDEX, position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_category, menu);
        getMenuInflater().inflate(R.menu.menu_category, menu);
        /* Search results in new SearchResultsActivity, clicked article passed back to articleActivity
           Associate searchable configuration with the SearchView */
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        ComponentName componentName = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
//        return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // view parameter needed for title.xml onClick()
    public void returnToParent(View view) {
        finish();
    }
}
