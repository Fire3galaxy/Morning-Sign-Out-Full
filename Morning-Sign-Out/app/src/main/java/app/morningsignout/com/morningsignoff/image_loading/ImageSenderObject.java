package app.morningsignout.com.morningsignoff.image_loading;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Daniel on 2/24/2017.
 */

class ImageSenderObject {
    FragmentWithCache fwc;
    ImageView imageView;
    Bitmap downloadedImage;
    FetchImageRunnable task;
    String imageUrl;

    ImageSenderObject(FragmentWithCache fwc,
                      ImageView imageView,
                      Bitmap downloadedImage,
                      FetchImageRunnable task,
                      String imageUrl) {
        this.fwc = fwc;
        this.imageView = imageView;
        this.downloadedImage = downloadedImage;
        this.task = task;
        this.imageUrl = imageUrl;
    }
}
