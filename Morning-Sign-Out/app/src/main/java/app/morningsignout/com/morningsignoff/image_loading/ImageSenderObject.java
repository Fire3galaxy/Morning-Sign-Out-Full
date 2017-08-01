package app.morningsignout.com.morningsignoff.image_loading;

import android.graphics.Bitmap;
import android.widget.ImageView;

import app.morningsignout.com.morningsignoff.image_loading.FetchImageRunnable;

/**
 * Created by Daniel on 2/24/2017.
 */

class ImageSenderObject {
    String imageUrl;
    ImageView imageView;
    Bitmap downloadedImage;
    FetchImageRunnable task;

    ImageSenderObject(String imageUrl, ImageView imageView, Bitmap downloadedImage, FetchImageRunnable task) {
        this.imageUrl = imageUrl;
        this.imageView = imageView;
        this.downloadedImage = downloadedImage;
        this.task = task;
    }
}
