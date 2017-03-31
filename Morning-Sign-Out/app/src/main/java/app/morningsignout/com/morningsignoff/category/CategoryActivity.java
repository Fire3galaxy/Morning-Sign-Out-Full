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

// Category page activity
public class CategoryActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private SearchView searchView;
    private ImageView splashScreenView;

    private String mTitle;              // Current Title
    private String[] categories_urls,   // category strings for url usage
            categories_titles;          // ... for Title usage
    private int position;               // position in category array

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private class OneSecondSplashTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            enableSplash();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Log.e("CategoryActivity", ie.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            fadeSplash();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categories_titles = getResources().getStringArray(R.array.categories);
        categories_urls = getResources().getStringArray(R.array.categories_for_url);
        position = 0;

        // nav drawer icons from resources
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.categories_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.listView_slide);
        splashScreenView = (ImageView) findViewById(R.id.imageView_splashScreen);

        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        for (int i = 0; i <= 8; i++)
            navDrawerItems.add(new NavDrawerItem(categories_titles[i], navMenuIcons.getResourceId(i, -1)));

        navMenuIcons.recycle();
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Change up button of actionbar to 3 horizontal bars for slide
        setUpButtonToTripleBar();
        // Set up button to open/close drawer and change title of current activity
        setDrawerListenerToActionBarToggle();

        // For CategoryFragment
        if (savedInstanceState == null) {
            CategoryFragment fragment = CategoryFragment.findOrCreateRetainFragment(getSupportFragmentManager());
            Bundle args = new Bundle();
            args.putString(CategoryFragment.EXTRA_TITLE, categories_titles[position]);
            args.putString(CategoryFragment.EXTRA_URL, categories_urls[position]);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_category, fragment)
                    .commit();
        }

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

        // Splash screen (only on first time opening)
        if (savedInstanceState == null)
            (new OneSecondSplashTask()).execute();
        else
            splashScreenView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        // Go back to home screen
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

	    // Messing with layout or gravity of searchview here if needed

        ComponentName componentName = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d("CategoryActivity","iconified" + String.valueOf(searchView.isIconified()));
            Log.d("","");
            if (!searchView.isIconified()) {    // Check if searchView is expanded
                searchView.setIconified(true);
                return true;
            }
        }

        // If it wasn't the Back key or none of the conditions are met, use default system behavior
        return super.onKeyDown(keyCode, event);
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
            // Enter code here! categories_urls.length -> About MSO
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

    private void enableSplash() {
        getSupportActionBar().hide();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        splashScreenView.setVisibility(View.VISIBLE);
    }

    public void fadeSplash() {
        getSupportActionBar().show();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        splashScreenView.animate().alpha(0f).setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                splashScreenView.setVisibility(View.GONE);
            }
        });
    }

    public void debugAllBitmapSources(View v) {
        String TAG = "CategoryActivity";
        CategoryFragment.instance.debugCache();
        CategoryBitmapPool.debugPool();
        FetchCategoryImageRunnable.debugAllHashes();
    }
}
