package app.morningsignout.com.morningsignoff;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.content.res.TypedArray;

import java.util.ArrayList;

// Category page activity
public class CategoryActivity extends ActionBarActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String mDrawerTitle;        // Title of activity when drawer swipe menu is visible
    private String mTitle;              // Current Title
    private String[] categories_urls,   // category strings for url usage
            categories_titles;          // ... for Title usage
    private int position;               // position in category array

    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the ListView layout from fragment_category_main.xml,
        setContentView(R.layout.activity_category);

        categories_titles = getResources().getStringArray(R.array.categories);
        categories_urls = getResources().getStringArray(R.array.categories_for_url);
        position = -1;

        // Set up title
        if (getIntent() != null)
            position = getIntent().getIntExtra(Intent.EXTRA_TITLE, -1);
//        setupActivityTitle();



//        // For DrawerLayout (no fragment)
//        mDrawerList = (ListView) findViewById(R.id.listView_slide);
//        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this,
//                R.layout.list_items_slide,
//                categories_titles);
//
//        mDrawerList.setAdapter(mAdapter); // Set up adapter for listview
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.categories_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.listView_slide);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        navDrawerItems.add(new NavDrawerItem(categories_titles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[6], navMenuIcons.getResourceId(6, -1)));
        navDrawerItems.add(new NavDrawerItem(categories_titles[7], navMenuIcons.getResourceId(7, -1)));

        navMenuIcons.recycle();
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Change up button of actionbar to 3 horizontal bars for slide
        setUpButtonToTripleBar();
        // Set up button to open/close drawer and change title of current activity
        setDrawerListenerToActionBarToggle();

//        Log.d("CategoryActivity", "mTitle = " + mTitle.toLowerCase());

        // For CategoryFragment
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(CategoryFragment.EXTRA_TITLE, categories_urls[position]);
        fragment.setArguments(args);

        // Set fragment's listview
        if (getIntent() != null && savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_category, fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_category, new ErrorFragment())
                    .commit();
        }

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
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_category, menu);
        getMenuInflater().inflate(R.menu.menu_category, menu);
//        return super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if drawer is open, hide action items (if using a menu xml)
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
           automatically handle clicks on the Home/Up button, so long
           as you specify a parent activity in AndroidManifest.xml. */
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.title) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title.toString();

//        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState(); // sync toggle state after onRestoreInstanceState
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig); // pass it on to toggle
    }

    void setupActivityTitle() {
        if (position == -1) { // Error
            mTitle = "";
            mDrawerTitle = mTitle;
            setTitle("");
            Log.e("CategoryActivity", "Titles are blank. Check that intent is not null and " +
                    "has int position");
        } else {
            mTitle = categories_titles[position];
            mDrawerTitle = "Categories Menu";
            setTitle(mTitle);
        }
    }

    void setUpButtonToTripleBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.slide_bars);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    void setDrawerListenerToActionBarToggle() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.title_slide_menu,
                R.string.title_activity_category) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // Show actionbar icons
            }

            public void onDrawerOpened(View view) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // Hide actionbar icons
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    void selectItem(int position) {
        if (this.position != position) {
            this.position = position;

            CategoryFragment fragment = new CategoryFragment();
            Bundle args = new Bundle();
            args.putString(CategoryFragment.EXTRA_TITLE, categories_urls[position]);
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_category, fragment)
                    .commit();
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(categories_titles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    // courtesy of http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
    public boolean checkForInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public String getCurrentCategoryUrl() {
        return categories_urls[position];
    }
}