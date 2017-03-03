package app.morningsignout.com.morningsignoff.category;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by Daniel on 2/25/2017.
 */
public class CategoryBitmapPool {
    static public CategoryBitmapPool instance = new CategoryBitmapPool();

    private LinkedList<Bitmap> bitmaps;

    private CategoryBitmapPool() {
        bitmaps = new LinkedList<Bitmap>();
    }

    public static Bitmap getBitmap(BitmapFactory.Options targetOptions) {
        synchronized(instance) {
            return (instance.bitmaps.isEmpty()) ? null : instance.findSuitableBitmap(targetOptions);
        }
    }

    public static void recycle(Bitmap b) {
        synchronized(instance) {
            instance.bitmaps.add(b);
            Log.d("JustBitmap", Integer.toString(instance.bitmaps.size()));
        }
    }

    private Bitmap findSuitableBitmap(BitmapFactory.Options targetOptions) {
        for (int i = 0; i < bitmaps.size(); i++) {
            if (canUseInBitmap(bitmaps.get(i), targetOptions)) {
                 return bitmaps.remove(i);
            }
        }

        return null;
    }

    // Bitmap memory optimization:
    // https://developer.android.com/topic/performance/graphics/manage-memory.html#inBitmap
    private boolean canUseInBitmap(Bitmap bitmapToUse, BitmapFactory.Options targetOptions) {
        if (bitmapToUse == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * 4; // ARGB_8888 (max #) assumed

            return byteCount <= bitmapToUse.getAllocationByteCount();
        }

        // FIXME: only considering KitKat+ right now
        return false;
    }
}
