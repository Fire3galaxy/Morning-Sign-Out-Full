package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Daniel on 12/14/2015.
 */

// When user is logged in, the button at the bottom will launch this activity.
// onclicklistener set up by DisqusGetAccessToken
public class DisqusCommentActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsqcomments);
    }
}
