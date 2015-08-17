package app.morningsignout.com.morningsignoff;

/**
 * Created by Daniel on 3/1/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import android.R.*;

/**
 * Creates the listview for all categories in the main page
 */
public class MainPageFragment extends Fragment {
    // Those are the categories shown on the buttons in the main page
    String[] categories_titles;

    public MainPageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories_titles = getResources().getStringArray(R.array.categories);
//        // changing menu through fragment
//        setHasOptionsMenu(true);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_main, menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Action bar (Home and Up button clicks have default to parent activity in Andr...Manifest
//        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> categories = new ArrayList<>(Arrays.asList(categories_titles));

        // Adapter that will send array's data to list view
        final ArrayAdapter<String> stringAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_items_mainpage,
                R.id.list_item_button,
                categories);

        ListView lv_frag = (ListView) rootView.findViewById(R.id.listview_articles);
        lv_frag.setAdapter(stringAdapter);

        // create the category page here
        lv_frag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Glorious toast example before learning how to launch CategoryActivity!
//            Toast toast = Toast.makeText(getActivity(), stringAdapter.getItem(position),
//                Toast.LENGTH_SHORT);
//            toast.show();

                // Give the categoryActivity a category title
                Log.e("MainPageFragment", "category_category");
                Intent categoryPageIntent = new Intent(getActivity(), CategoryActivity.class);
                //categoryPageIntent.putExtra(Intent.EXTRA_TITLE, stringAdapter.getItem(position));
                categoryPageIntent.putExtra(Intent.EXTRA_TITLE, position);
                startActivity(categoryPageIntent);
            }
        });

        return rootView;
    }
}
