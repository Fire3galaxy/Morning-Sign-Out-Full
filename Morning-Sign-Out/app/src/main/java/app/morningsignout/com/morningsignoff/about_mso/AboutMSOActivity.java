package app.morningsignout.com.morningsignoff.about_mso;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.text.Html;
import android.widget.TextView;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.util.ActionBarSetup;


public class AboutMSOActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_mso);
        new ActionBarSetup(this).setupActionBar().setUpToParentListener();

        // setting TextViews to the spannable strings
        TextView p1 = (TextView)findViewById(R.id.textView_aboutUsDesc1);
        TextView p2 = (TextView)findViewById(R.id.textView_aboutUsDesc2);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            //noinspection deprecation
            p1.setText(Html.fromHtml(getString(R.string.sign_out_content)));
            //noinspection deprecation
            p2.setText(Html.fromHtml(getString(R.string.morning_content)));
        } else {
            p1.setText(Html.fromHtml(getString(R.string.sign_out_content), Html.FROM_HTML_MODE_LEGACY));
            p2.setText(Html.fromHtml(getString(R.string.morning_content), Html.FROM_HTML_MODE_LEGACY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_justlogo, menu);
        return true;
    }
}
