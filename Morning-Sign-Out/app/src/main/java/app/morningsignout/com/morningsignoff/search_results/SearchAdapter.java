package app.morningsignout.com.morningsignoff.search_results;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.category.AdapterObject;

/**
 * Created by shinr on 6/1/2017.
 */

public class SearchAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private AdapterView adapterView = null;

    private ArrayList<Article> articles;
    private Set<String> uniqueArticleNames; // FIXME: see category\CategoryAdapter.java
    private int pageNum;
    private int firstVisibleItem, lastVisibleItem;

    // constructor
    SearchAdapter(Activity activity, LayoutInflater inflater) {
        this.articles = new ArrayList<>();
//        this.uniqueArticleNames = new HashSet<>();
        this.inflater = inflater;
        pageNum = 0;
        firstVisibleItem = 0;
        lastVisibleItem = 0;
    }

    public int getPageNum() {
        return pageNum;
    }

    // Get the number of row items
    @Override
    public int getCount() { return articles.size(); }

    @Override
    public Object getItem(int i) { return articles.get(i); }

    // Get the item id, since no database, the id is assignment
    @Override
    public long getItemId(int i) { return i; }

    static boolean isLandscape(Context con) {
        Display display = ((WindowManager) con.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        int orientation = display.getRotation();

        return (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270);
    }

    // Return based on if list has items
    @Override
    public boolean isEmpty() {
        return articles.isEmpty();
    }

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        // create a new rowItem object here
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row_search, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the author, imageViewReference and title of the row item
            viewHolder.title = (TextView) row.findViewById(R.id.textViewTitle_search);
            viewHolder.author = (TextView) row.findViewById(R.id.textViewAuthor_search);
            viewHolder.image = (ImageView) row.findViewById(R.id.imageView_search);
            viewHolder.excerpt = (TextView) row.findViewById(R.id.textViewExcerpt_search);
            row.setTag(viewHolder);
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }

        Article rowTemp = articles.get(i);

        // Set the values of the rowItem
        if(isLandscape(row.getContext()))
            viewHolder.title.setLines(3);
        viewHolder.title.setText(rowTemp.getTitle());
        viewHolder.author.setText(rowTemp.getAuthor());

        // add image stuff here

        return row;
    }

    public void setAdapterView(AdapterView adapterView) { this.adapterView = adapterView; }

    public void loadMoreItems(List<Article> moreArticles, int pageNum) {
        // check to prevent page loading twice
        if (moreArticles != null && this.pageNum != pageNum) {
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i) {
                String article = moreArticles.get(i).getTitle();

                // see category\CategoryAdapter.java
                // this is a hack
                if (!uniqueArticleNames.contains(article) && moreArticles.get(i).getImageURL() != null)
                    articles.add(moreArticles.get(i));
                uniqueArticleNames.add(article);
            }

            notifyDataSetChanged(); // update the fragment and force display
        }
    }
}
