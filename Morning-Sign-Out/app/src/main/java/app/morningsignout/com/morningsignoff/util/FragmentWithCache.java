package app.morningsignout.com.morningsignoff.util;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;

import java.util.Map;

/**
 * Created by Daniel on 8/1/2017.
 */

public abstract class FragmentWithCache extends Fragment {
    final static String LOG_NAME = "FragmentWithCache";
    private LruCache<String, Bitmap> memoryCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpCache();
    }

    private void setUpCache() {
         /* Thanks to http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
         * for caching code */
        // max memory of hdpi ~32 MB
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // memory for images ~4.5 MB = 7-8 images
        final int cacheSize;

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if (width <= 320) {
            cacheSize = maxMemory / 3;
        } else
            cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    // FIXME: Debug the crashing issues next time before trying an external cache
    public boolean addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
            return true;
        }

        return false;
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    public void debugCache() {
        Map<String, Bitmap> cacheSnapshot = memoryCache.snapshot();
        if (cacheSnapshot.isEmpty())
            Log.d(LOG_NAME, "No entries");
        else
            for (Map.Entry<String, Bitmap> e : cacheSnapshot.entrySet())
                Log.d(LOG_NAME, Integer.toString(e.getValue().hashCode())
                        + ": " + e.getKey() + ", " + e.getKey().length());
    }
}
