package app.morningsignout.com.morningsignoff.network;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Daniel on 2/24/2017.
 */

public class CategoryImageSenderObject {
    public String imageUrl;
    public ImageView imageView;
    public Bitmap downloadedImage;
    public FetchCategoryImageRunnable task;

    public CategoryImageSenderObject(String imageUrl, ImageView imageView, Bitmap downloadedImage, FetchCategoryImageRunnable task) {
        this.imageUrl = imageUrl;
        this.imageView = imageView;
        this.downloadedImage = downloadedImage;
        this.task = task;
    }
}
