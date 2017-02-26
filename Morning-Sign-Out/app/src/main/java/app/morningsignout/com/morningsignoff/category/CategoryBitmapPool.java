package app.morningsignout.com.morningsignoff.category;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Stack;

/**
 * Created by Daniel on 2/25/2017.
 */
public class CategoryBitmapPool {
    private Stack<Bitmap> bitmaps;
    private int unusedBitmapWidth, unusedBitmapHeight;

    public CategoryBitmapPool(int reqWidth, int reqHeight) {
        bitmaps = new Stack<Bitmap>();
        this.unusedBitmapWidth = reqWidth + reqWidth;
        this.unusedBitmapHeight = reqHeight + reqHeight;
    }

//    static public void push(Bitmap b) {
//        if (b != null) {
//            instance.bitmaps.push(b);
//            Log.d("CategoryBitmapPool", "Pushed. Size: " + Integer.toString(b.getWidth()) + ", " + Integer.toString(b.getHeight()));
//        }
//    }

    public Bitmap getBitmap() {
        if (!bitmaps.isEmpty())
            Log.d("CategoryBitmapPool", "reused bitmap" + ": " + (bitmaps.size() - 1));
        else
            Log.d("CategoryBitmapPool", "made new bitmap" + ": " + bitmaps.size());
        return (bitmaps.isEmpty()) ?
                null : bitmaps.pop();
    }

    public void recycle(Bitmap b) {
        bitmaps.push(b);
        Log.d("CategoryBitmapPool", "recycled bitmap of size " + b.getHeight() + ", " + b.getWidth() + ": " + bitmaps.size());
    }
}
