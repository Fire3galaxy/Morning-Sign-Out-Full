package app.morningsignout.com.morningsignoff.image_loading;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.morningsignout.com.morningsignoff.article.Article;

/**
 * Created by Daniel on 8/1/2017.
 */

public abstract class ImageListAdapter extends BaseAdapter {
    private LayoutInflater inflater;

    private ArrayList<Article> articles;
    private Set<String> uniqueArticleNames; // FIXME: This was a temp fix a long time ago for repeats that somehow got in the list
    private int pageNum;

    private static int REQ_IMG_WIDTH = 0, REQ_IMG_HEIGHT = 0;
    private final int VIEW_HEIGHT_DP = 220; // From single_row_category's imageview

    ImageListAdapter(Activity activity, LayoutInflater inflater) {
        this.articles = new ArrayList<>();
        this.uniqueArticleNames = new HashSet<>();
        this.inflater = inflater;
        pageNum = 0;

        // Get width/height of the images we download for use in FetchImageRunnable
        // (hardcoded height of imageview in single_row_category)
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources r = activity.getResources();

        REQ_IMG_WIDTH = metrics.widthPixels;
        REQ_IMG_HEIGHT = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, VIEW_HEIGHT_DP, r.getDisplayMetrics());
    }

    public int getPageNum() {
        return pageNum;
    }

    // Get the number of row items
    @Override
    public int getCount(){
        return articles.size();
    }

    @Override
    public Object getItem(int i){
        return articles.get(i);
    }

    // Get the item id, since no database, the id is its assignment
    @Override
    public long getItemId(int i){
        return i;
    }

    // Return based on if list has items
    @Override
    public boolean isEmpty() {
        return articles.isEmpty();
    }

    // This function is called along with .notifyDataSetChanged() in Asynctask's onScrollListener function
    // when the viewers scroll to the bottom of the articles
    public void loadMoreItems(List<Article> moreArticles, int pageNum){
        // if prevent the late page from loading twice
        if(moreArticles != null && this.pageNum != pageNum){
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i) {
                String article = moreArticles.get(i).getTitle();

                // Hack-ish way of preventing the list from being populated with doubles
                // which happens if request occurs multiple times...
                if (!uniqueArticleNames.contains(article) && moreArticles.get(i).getImageURL() != null)
                    articles.add(moreArticles.get(i));
                uniqueArticleNames.add(article);
            }

            notifyDataSetChanged();
        }
    }
}
