package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Daniel on 3/2/2015.
 */
public class HeadlineFragment extends Fragment {
    final static String IMAGE_NUMBER = "image number";
    private int page_number;
    private boolean created;
    private Article article;

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public HeadlineFragment() {
        Log.e("HeadlineFragment", "Constructor!~~~~~~~~~~~~~~~~~~");
        created = false;
    }

    // A "Factory" method that creates different images when scrolled through
    public static HeadlineFragment create(int pageNumber) {
        HeadlineFragment newImage = new HeadlineFragment();
        Bundle args = new Bundle();
        args.putInt(IMAGE_NUMBER, pageNumber);
        newImage.setArguments(args);
        return newImage;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page_number = getArguments() != null ? getArguments().getInt(IMAGE_NUMBER) : -1;
        if (page_number == -1) Log.e("HeadlineFragment", "bundle is null");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up view with layout item
        View rootView = inflater.inflate(R.layout.fragment_headline, container, false);

        // If fragment not created, load images from internet
        if (!created) {
            // set up imageButton
            ImageButton ib = (ImageButton) rootView.findViewById(R.id.imageButton_headline);

            // Decide image to show based on index in headline images
            new DownloadImageTask(this, getActivity(), rootView, ib).execute(page_number);

            created = true;
        }
        // If fragment already created before, do nothing on onCreateView
        else {
            // debugging
            if (getArticle() == null) Log.e("onCreateView", "PROBLEM: Headline Fragment created " +
                    "but no article has been set!");
            // restoring headline fragment
            else {
                TextView tv = (TextView) rootView.findViewById(R.id.textView_headline);
                ImageButton ib = (ImageButton) rootView.findViewById(R.id.imageButton_headline);

                // Set title to previously saved title and make textview visible
                tv.setText(getArticle().getTitle());
                tv.setVisibility(TextView.VISIBLE);

                // Set imagebutton to saved image, with cropping properties
                ib.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ib.setCropToPadding(true);
                ib.setImageBitmap(getArticle().getBitmap());

                // Set imagebutton link
                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent articlePageIntent = new Intent(getActivity(), ArticleActivity.class)
                                .putExtra(Intent.EXTRA_HTML_TEXT, getArticle().getLink())
                                .putExtra(Intent.EXTRA_SHORTCUT_NAME, getArticle().getTitle());
                        startActivity(articlePageIntent);
                    }
                });
            }
        }

        return rootView;
    }
}
