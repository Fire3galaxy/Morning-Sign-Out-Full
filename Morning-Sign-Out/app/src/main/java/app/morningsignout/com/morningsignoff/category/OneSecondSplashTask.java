package app.morningsignout.com.morningsignoff.category;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Daniel on 3/31/2017.
 */

public class OneSecondSplashTask extends AsyncTask<Void, Void, Void> {
    CategoryActivity categoryActivity;
    SplashFragment splashFragment;

    public OneSecondSplashTask(CategoryActivity categoryActivity, SplashFragment splashFragment) {
        this.categoryActivity = categoryActivity;
        this.splashFragment = splashFragment;
    }

    @Override
    protected void onPreExecute() {
        categoryActivity.startSplash();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ie) {
            Log.e("CategoryActivity", ie.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        splashFragment.fadeSplash();
    }
}
