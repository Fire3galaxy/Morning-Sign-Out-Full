package app.morningsignout.com.morningsignoff.about_mso;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.util.ActionBarSetup;


public class AboutMSOActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_mso);
        ActionBarSetup.setupActionBar(this);

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
}
