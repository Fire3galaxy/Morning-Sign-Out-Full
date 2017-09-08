package app.morningsignout.com.morningsignoff.util;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import app.morningsignout.com.morningsignoff.R;

/**
 * Created by Daniel on 9/7/2017.
 */

public class ActionBarSetup {
    public static void setupActionBar(AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) {
            ImageButton ib = (ImageButton) activity.getLayoutInflater().inflate(R.layout.title_main, null);
            activity.getSupportActionBar().setCustomView(ib);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
    }
}
