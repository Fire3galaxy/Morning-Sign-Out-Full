package app.morningsignout.com.morningsignoff.search_results;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.category.AdapterObject;

/**
 * Created by shinr on 6/1/2017.
 */

public class SearchAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    private ArrayList<Article> articles;

    // Get the number of row items
    @Override
    public int getCount() { return articles.size(); }

    @Override
    public Object getItem(int i) { return articles.get(i); }

    // Get the item id, since no database, the id is assignment
    @Override
    public long getItemId(int i) { return i; }

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        // create a new rowItem object here
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row, viewGroup, false);
            viewHolder = new AdapterObject();
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }
        return row;
    }
}
