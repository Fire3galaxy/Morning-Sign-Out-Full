package app.morningsignout.com.morningsignoff.meet_the_team;

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

import java.util.ArrayList;

import app.morningsignout.com.morningsignoff.category.CategoryActivity;
import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.network.FetchMeetTheTeamTask;

/**
 * Created by liukwarm on 10/24/15.
 */
public class MTTListViewActivity extends ActionBarActivity {

    private SearchView searchView;
    private ArrayList<MTTListViewItem> list;
    public static String EXTRA_LIST = "list";
    public static String EXTRA_INDEX = "index";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mttlistview);

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

        lv.setAdapter(new MTTListViewAdapter(getApplicationContext(),
                list));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToLink(position);
            }
        });

        setupActionBar();
    }

    private void goToLink(int position) {
        Intent intent = new Intent(this, MTTWebViewActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_LIST, list);
        intent.putExtra(EXTRA_INDEX, position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_justlogo, menu);

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
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    void setupActionBar() {
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        this.getSupportActionBar().setCustomView(ib, params);

        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
