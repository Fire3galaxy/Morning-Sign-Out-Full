package app.morningsignout.com.morningsignoff.image_loading;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.morningsignout.com.morningsignoff.article.Article;

/**
 * Created by Daniel on 8/1/2017.
 */

public abstract class ArticleListAdapter extends BaseAdapter {
    protected LayoutInflater inflater;

    protected ArrayList<Article> articles;
    private Set<String> uniqueArticleNames; // FIXME: This was a temp fix a long time ago for repeats that somehow got in the list

    // For future reference, page number json requests START AT 1. The constructor here has pageNum
    // set to 0 and expects it to be incremented by a future request.
    private int pageNum;

    public ArticleListAdapter(LayoutInflater inflater) {
        this.articles = new ArrayList<>();
        this.uniqueArticleNames = new HashSet<>();
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
            Log.d("Adapter", "more articles added " + pageNum);
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i) {
                String article = moreArticles.get(i).getTitle();

                // Hack-ish way of preventing the list from being populated with doubles
                // which happens if request occurs multiple times...
                if (!uniqueArticleNames.contains(article) && moreArticles.get(i).getFullURL() != null)
                    articles.add(moreArticles.get(i));
                uniqueArticleNames.add(article);
            }

            notifyDataSetChanged();
        }
    }

    public void loadNewItems(List<Article> replacementArticles) {
        Log.d("Adapter", "articles added " + 1);
        pageNum = 1;
        articles = new ArrayList<>(replacementArticles);
        uniqueArticleNames.clear();
        notifyDataSetChanged();
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}
