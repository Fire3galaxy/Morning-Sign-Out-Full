package app.morningsignout.com.morningsignoff.about_mso;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import app.morningsignout.com.morningsignoff.R;


public class AboutMSOActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_mso);
        setupActionBar();

        // setting TextViews to the spannable strings
        TextView p1 = (TextView)findViewById(R.id.textView_aboutUsDesc1);
        TextView p2 = (TextView)findViewById(R.id.textView_aboutUsDesc2);

        p1.setText(Html.fromHtml(getString(R.string.sign_out_content)));
        p2.setText(Html.fromHtml(getString(R.string.morning_content)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_justlogo, menu);
        return true;
    }

    public void returnToParent(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void setupActionBar() {
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null);
        this.getSupportActionBar().setCustomView(ib, params);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement;
        //if (id == R.id.action_settings) {
           //return true;
       // }

        return super.onOptionsItemSelected(item);
    }*/
}
