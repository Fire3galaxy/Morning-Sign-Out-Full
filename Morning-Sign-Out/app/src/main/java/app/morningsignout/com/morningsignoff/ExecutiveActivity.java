package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by liukwarm on 10/24/15.
 */
public class ExecutiveActivity extends ActionBarActivity {

    private SearchView searchView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive);

        ListView lv = (ListView) findViewById(R.id.listView);
        TypedArray names = getResources()
                .obtainTypedArray(R.array.executive_names);
        TypedArray positions = getResources()
                .obtainTypedArray(R.array.executive_positions);

        ArrayList<ExecutiveListItem> list = new ArrayList<>();
        for (int i = 0; i < names.length(); i++) {
            list.add(new ExecutiveListItem(names.getString(i), positions.getString(i), null));
        }


        lv.setAdapter(new ExecutiveListAdapter(getApplicationContext(),
                list));

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title, null);
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
}
