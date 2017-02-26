package app.morningsignout.com.morningsignoff.category;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Stack;

/**
 * Created by Daniel on 2/25/2017.
 */
public class CategoryBitmapPool {
    static public CategoryBitmapPool instance;

    private Stack<Bitmap> bitmaps;
    private int unusedBitmapWidth, unusedBitmapHeight;

    public CategoryBitmapPool(int reqWidth, int reqHeight) {
        bitmaps = new Stack<Bitmap>();
        this.unusedBitmapWidth = reqWidth + reqWidth;
        this.unusedBitmapHeight = reqHeight + reqHeight;
    }

    public static Bitmap getBitmap() {
//        if (!instance.bitmaps.isEmpty())
//            Log.d("CategoryBitmapPool", "reused bitmap" + ": " + (instance.bitmaps.size() - 1));
//        else
//            Log.d("CategoryBitmapPool", "made new bitmap" + ": " + instance.bitmaps.size());
        return (instance.bitmaps.isEmpty()) ? null : instance.bitmaps.pop();
    }

    public static void recycle(Bitmap b) {
        instance.bitmaps.push(b);
//        Log.d("CategoryBitmapPool", "recycled bitmap of size " + b.getHeight() + ", " + b.getWidth() + ": " + instance.bitmaps.size());
    }
}
