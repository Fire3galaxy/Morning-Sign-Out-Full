/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// To add to actionbar, check here: http://developer.android.com/guide/topics/ui/actionbar.html
public class MainActivity extends ActionBarActivity {
    private static final int NUM_PAGES = 5;

    /**
     * A simple pager adapter that represents 5 HeadlineFragment objects, in
     * sequence. Copied from http://developer.android.com/training/animation/screen-slide.html
     * Each image button will be a page instantiated by the pager adapter
     */
    private class HeadlinePagerAdapter extends FragmentPagerAdapter {
        public HeadlinePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.e("getItem()", "Creating Headline Fragment~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            return HeadlineFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "at onCreate");

        Log.e("MainPageFragment", "category_category");
        Intent categoryPageIntent = new Intent(this, CategoryActivity.class);
        //categoryPageIntent.putExtra(Intent.EXTRA_TITLE, stringAdapter.getItem(position));

        categoryPageIntent.putExtra(Intent.EXTRA_TITLE, 0);
        startActivity(categoryPageIntent);

        setContentView(R.layout.activity_main);

        // The pager for list of headline images (each is own page)
        ViewPager headlinePager = (ViewPager) findViewById(R.id.container_headline);
        headlinePager.setAdapter(new HeadlinePagerAdapter(getSupportFragmentManager()));

        // Bottom half of screen: The list of category buttons
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainPageFragment())
                    .commit();
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Not important
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "at onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "at onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "at onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "at onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "at onDestroy");
    }
}
