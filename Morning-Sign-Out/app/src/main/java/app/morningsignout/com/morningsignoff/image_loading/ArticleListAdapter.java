package app.morningsignout.com.morningsignoff.image_loading;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;

/**
 * Created by Daniel on 8/1/2017.
 */

public abstract class ArticleListAdapter extends BaseAdapter {
    protected LayoutInflater inflater;
    protected ArrayList<Article> articles;

    // For future reference, page number json requests START AT 1. The constructor here has pageNum
    // set to 0 and expects it to be incremented by a future request.
    private int pageNum;

    public ArticleListAdapter(LayoutInflater inflater) {
        this.articles = new ArrayList<>();
        this.inflater = inflater;
        pageNum = 0;
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
        // it prevents the same page from loading twice
        if(moreArticles != null && this.pageNum != pageNum){
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i)
                articles.add(moreArticles.get(i));

            notifyDataSetChanged();
        }
    }

    public void loadNewItems(List<Article> replacementArticles) {
        pageNum = 1;
        articles = new ArrayList<>(replacementArticles);
        notifyDataSetChanged();
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}
