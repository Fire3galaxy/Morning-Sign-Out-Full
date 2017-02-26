package app.morningsignout.com.morningsignoff.category;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Stack;

/**
 * Created by Daniel on 2/25/2017.
 */
public class CategoryBitmapPool {
    private Stack<Bitmap> bitmaps;
    private int reqWidth, reqHeight;

    public CategoryBitmapPool(int reqWidth, int reqHeight) {
        bitmaps = new Stack<Bitmap>();
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

//    static public void push(Bitmap b) {
//        if (b != null) {
//            instance.bitmaps.push(b);
//            Log.d("CategoryBitmapPool", "Pushed. Size: " + Integer.toString(b.getWidth()) + ", " + Integer.toString(b.getHeight()));
//        }
//    }

    public Bitmap getBitmap() {

    }
}
