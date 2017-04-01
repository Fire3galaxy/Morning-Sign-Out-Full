package app.morningsignout.com.morningsignoff.category;

import android.animation.Animator;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ImageView;
import android.animation.Animator.AnimatorListener;

import java.util.ArrayList;
import java.lang.InterruptedException;
import java.util.Map;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.search_results.SearchResultsActivity;
import app.morningsignout.com.morningsignoff.meet_the_team.MeetTheTeamActivity;
import app.morningsignout.com.morningsignoff.network.FetchCategoryImageRunnable;

// Category page categoryActivity
public class CategoryActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private SearchView searchView;

    private String mTitle;              // Current Title
    private String[] categories_urls,   // category strings for url usage
            categories_titles;          // ... for Title usage
    private int position;               // position in category array

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Log.d("CategoryActivity", "Clicked");
            selectItem(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // XML resources
        categories_titles = getResources().getStringArray(R.array.categories);
        categories_urls = getResources().getStringArray(R.array.categories_for_url);
        position = 0;

        // Views
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.listView_slide);

        // contents of nav drawer
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();
        // nav drawer icons from resources
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.categories_icons);

        // adding nav drawer items to array
        for (int i = 0; i < navMenuIcons.length(); i++)
            navDrawerItems.add(new NavDrawerItem(categories_titles[i], navMenuIcons.getResourceId(i, -1)));

        navMenuIcons.recycle();
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        Log.d("CategoryActivity", "HERE");

        // Change up button of actionbar to 3 horizontal bars for slide
        setUpButtonToTripleBar();
        // Set up button to open/close drawer and change title of current categoryActivity
        setDrawerListenerToActionBarToggle();

        // Adding MSO Logo to center of action bar
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(0);
            }
        });
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        this.getSupportActionBar().setCustomView(ib, params);
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);


        // Fragments added to activity
        if (savedInstanceState == null) {
            CategoryFragment fragment = CategoryFragment.findOrCreateRetainFragment(getSupportFragmentManager());
            Bundle args = new Bundle();
            args.putString(CategoryFragment.EXTRA_TITLE, categories_titles[position]);
            args.putString(CategoryFragment.EXTRA_URL, categories_urls[position]);
            fragment.setArguments(args);

            SplashFragment splashScreenFragment = new SplashFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_category, splashScreenFragment)
                    .add(R.id.container_category, fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("CategoryActivity","iconified" + String.valueOf(searchView.isIconified()));
        if (!searchView.isIconified())  // Check if searchView is expanded
            searchView.setIconified(true);
        else
            super.onBackPressed();
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

	    // Messing with layout or gravity of searchview here if needed

        ComponentName componentName = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
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
           as you specify a parent categoryActivity in AndroidManifest.xml. */
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        return id == R.id.title || super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title.toString();
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
                invalidateOptionsMenu(); // Show actionbar icons
            }

            public void onDrawerOpened(View view) {
                invalidateOptionsMenu(); // Hide actionbar icons
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    void selectItem(int position) {
        Log.d("CategoryActivity", Integer.toString(position));
        if (this.position != position && position < categories_urls.length) {
            this.position = position;

            CategoryFragment fragment = new CategoryFragment();
            Bundle args = new Bundle();
            args.putString(CategoryFragment.EXTRA_TITLE, categories_titles[position]);
            args.putString(CategoryFragment.EXTRA_URL, categories_urls[position]);
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_category, fragment)
                    .commit();
        } else if (position == categories_urls.length){
            // categories_urls.length -> About MSO
            Intent AboutMSOActivity = new Intent(this, app.morningsignout.com.morningsignoff.about_mso.AboutMSOActivity.class);
            startActivity(AboutMSOActivity);
        } else if (position == categories_urls.length + 1) {
            // categories_urls.length + 1 -> Meet the Team
            Intent intent = new Intent(this, MeetTheTeamActivity.class);
            startActivity(intent);
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(categories_titles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    void startSplash() {
        getSupportActionBar().hide();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    void endSplash() {
        getSupportActionBar().show();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    void removeSplashFragment(Fragment splashFragment) {
        getSupportFragmentManager().beginTransaction()
                .remove(splashFragment)
                .commit();
    }

    public void debugAllBitmapSources(View v) {
//        String TAG = "CategoryActivity";
//        CategoryFragment.instance.debugCache();
//        CategoryBitmapPool.debugPool();
//        FetchCategoryImageRunnable.debugAllHashes();
    }
}
