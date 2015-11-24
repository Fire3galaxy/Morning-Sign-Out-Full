package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/16/2015.
 */
public class MTTWebViewActivity extends ActionBarActivity {
    // Need ExecutiveListItem list for previous/next buttons
    // Need index of which person on list is picked
    ArrayList<ExecutiveListItem> teamArray;
    int index;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mttwebview);

        // Setting up action bar with logo and up button
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        View title = getLayoutInflater().inflate(R.layout.title_main, null);
        actionbar.setCustomView(title);
        actionbar.setDisplayShowCustomEnabled(true);

        // Initialize Team Array and Index selected in previous list
        if (getIntent() != null) {
            Intent ref = getIntent();

            teamArray = ref.getParcelableArrayListExtra(ExecutiveActivity.EXTRA_LIST);
            index = ref.getIntExtra(ExecutiveActivity.EXTRA_INDEX, 0);
        }

        // Set up ViewPager for swiping left/right to other people
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager_mttwebview);
        MTTWebPagerAdapter adapter = new MTTWebPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(index, false); // Setting pager to selected person

        // Showing message for swiping left/right for 10 seconds
        final RelativeLayout swipeMessage = (RelativeLayout) findViewById(R.id.relativeLayout_mtt_swipe);
        swipeMessage.setVisibility(View.VISIBLE);

        Log.d("", "");  // just because studio didn't properly install debug apk
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                swipeMessage.setVisibility(View.GONE);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 7 * 1000);

        // FIXME: Test, Transition to a ViewPager and make the prev/next buttons scroll the page (not swipe). Think of way to hide buttons when not in use.
        // Semi-visible arrows could be good way of filling white space and indicating you can scroll sideways
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aboutmso, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MTTWebPagerAdapter extends FragmentStatePagerAdapter {
        public MTTWebPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            MTTWebPageFragment teamMember = new MTTWebPageFragment();
            Bundle args = new Bundle();
            args.putString(MTTWebPageFragment.MEMBER_URL, teamArray.get(position).hyperlink);
            teamMember.setArguments(args);

            return teamMember;
        }

        @Override
        public int getCount() {
            return teamArray.size();
        }
    }
}


