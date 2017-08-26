package app.morningsignout.com.morningsignoff.util;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by Daniel on 8/1/2017.
 */

public class PhoneOrientation {
    static public boolean isPortrait(Context con) {
        return con.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    static public boolean isLandscape(Context con) {
        return con.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
