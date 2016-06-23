package app.morningsignout.com.morningsignoff.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.category.CategoryActivity;

/**
 * Created by liukwarm on 8/19/15.
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(2000);
                } catch (Exception e) {

                } finally {

                    Intent categoryPageIntent = new Intent(getApplicationContext(), CategoryActivity.class);
                    categoryPageIntent.putExtra(Intent.EXTRA_TITLE, 0);
                    finish();
                    startActivity(categoryPageIntent);
                }
            }
        };
        welcomeThread.start();
    }
}
