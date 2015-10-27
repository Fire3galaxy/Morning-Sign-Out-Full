package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.text.Html;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


public class AboutMSOActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.about_mso);

        // setting TextViews to the spannable strings
        TextView p1 = (TextView)findViewById(R.id.textView_aboutUsDesc1);
        TextView p2 = (TextView)findViewById(R.id.textView_aboutUsDesc2);

        p1.setText(Html.fromHtml(getString(R.string.sign_out_content)));
        p2.setText(Html.fromHtml(getString(R.string.morning_content)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
