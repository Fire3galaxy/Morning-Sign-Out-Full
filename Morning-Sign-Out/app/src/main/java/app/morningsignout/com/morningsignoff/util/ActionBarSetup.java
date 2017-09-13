package app.morningsignout.com.morningsignoff.util;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import app.morningsignout.com.morningsignoff.R;

/**
 * Created by Daniel on 9/7/2017.
 */

/* For setting up the action bar if it has the mso logo (R.drawable.mso_square_white).
 * Call it like this:
 *
 * new ActionBarSetup(this).setupActionBar().set(UpToParent/OnClick)Listener();
 *
 * The last function call is optional, but if called, it must be the last function. Hence,
 * it does not return ActionBarSetup.
 */
public class ActionBarSetup {
    private AppCompatActivity activity;
    private ImageButton ib;

    public ActionBarSetup(AppCompatActivity activity) {
        this.activity = activity;
    }

    public ActionBarSetup setupActionBar() {
        if (activity.getSupportActionBar() != null) {
            ib = (ImageButton) activity.getLayoutInflater().inflate(R.layout.title_main, null);
            activity.getSupportActionBar().setCustomView(ib);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
        }

        return this;
    }

    // Emphasize: Listeners are the last function called in the chain
    public void setUpToParentListener() {
        ib.setOnClickListener(new UpToParentListener());
        ib = null;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        ib.setOnClickListener(listener);
        ib = null;
    }

    private class UpToParentListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            NavUtils.navigateUpFromSameTask(activity);
        }
    }

}
