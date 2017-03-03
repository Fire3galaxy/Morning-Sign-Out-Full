package app.morningsignout.com.morningsignoff.category;

import android.graphics.Bitmap;
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

    public static Bitmap getBitmap() {
//        if (!instance.bitmaps.isEmpty())
//            Log.d("CategoryBitmapPool", "reused bitmap" + ": " + (instance.bitmaps.size() - 1));
//        else
//            Log.d("CategoryBitmapPool", "made new bitmap" + ": " + instance.bitmaps.size());

        // FIXME: Debug why the bitmaps aren't immutable. Figure out if the new bitmaps are being recycled.
        // Still getting decoding problems
        synchronized(instance) {
            Bitmap b = (instance.bitmaps.isEmpty()) ?
                    //Bitmap.createBitmap(instance.unusedBitmapWidth, instance.unusedBitmapHeight, Bitmap.Config.ARGB_8888)
                    null : instance.findBitmapWithDimens
                    //instance.bitmaps.pop();
            return b;
        }
    }

    public static void recycle(Bitmap b) {
        synchronized(instance) {
            instance.bitmaps.push(b);
        }
//        Log.d("CategoryBitmapPool", "recycled bitmap of size " + b.getHeight() + ", " + b.getWidth() + ": " + instance.bitmaps.size());
    }

    private void findBitmapWithDimens(int width, int height) {

    }
}
