package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by liukwarm on 8/19/15.
 */
public class StartupActivity extends Activity {
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
