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
    static public CategoryBitmapPool instance;

    private LinkedList<Bitmap> bitmaps;
    private int unusedBitmapWidth, unusedBitmapHeight;

    public CategoryBitmapPool(int reqWidth, int reqHeight) {
        bitmaps = new LinkedList<Bitmap>();
        this.unusedBitmapWidth = reqWidth + reqWidth;
        this.unusedBitmapHeight = reqHeight + reqHeight;
    }

    public static Bitmap getBitmap(BitmapFactory.Options targetOptions) {
        // FIXME: Debug why the bitmaps aren't immutable. Figure out if the new bitmaps are being recycled.
        // Still getting decoding problems
        synchronized(instance) {
            Bitmap b = (instance.bitmaps.isEmpty()) ?
                    null : instance.findSuitableBitmap(targetOptions);
            Log.d("JustBitmap", Boolean.toString(instance.bitmaps.isEmpty()));
            return b;
        }
    }

    public static void recycle(Bitmap b) {
        synchronized(instance) {
            instance.bitmaps.add(b);
        }
//        Log.d("CategoryBitmapPool", "recycled bitmap of size " + b.getHeight() + ", " + b.getWidth() + ": " + instance.bitmaps.size());
    }

    private Bitmap findSuitableBitmap(BitmapFactory.Options targetOptions) {
        for (Bitmap b : bitmaps)
            if (canUseInBitmap(b, targetOptions)) {
                Log.d("JustBitmap", "return: " + b.getWidth() + ", " + b.getHeight());
                return b;
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

            boolean result = byteCount <= bitmapToUse.getAllocationByteCount();
            if (result) {
                Log.d("JustBitmap", "needed: " + width + ", " + height);
//                Log.d("JustBitmap", (byteCount / 1024) + ", " + (bitmapToUse.getAllocationByteCount() / 1024));
                Log.d("JustBitmap", "giving: " + bitmapToUse.getWidth() + ", " + bitmapToUse.getHeight());
            }
            return result;
        }

        // FIXME: only considering KitKat+ right now
        return false;
    }
}
