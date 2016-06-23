package app.morningsignout.com.morningsignoff.category;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.network.FetchCategoryImageTask;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in the FetchListArticlesTask which is called in CategoryActivity
public class CategoryAdapter extends BaseAdapter {
    ArrayList<SingleRow> articles;
    Set<String> uniqueArticleNames;

    CategoryFragment categoryFragment;
    LayoutInflater inflater;
    Boolean canLoadMore;
    int pageNum;
    GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter;

    CategoryAdapter(CategoryFragment categoryFragment, LayoutInflater inflater, GridViewWithHeaderAndFooter gridViewWithHeaderAndFooter) {
        this.categoryFragment = categoryFragment;
        this.articles = new ArrayList<SingleRow>();
        this.uniqueArticleNames = new HashSet<String>();
        this.inflater = inflater;
        canLoadMore = true;
        pageNum = 0;
        this.gridViewWithHeaderAndFooter = gridViewWithHeaderAndFooter;
    }

    public synchronized int getPageNum() {
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

    public static boolean isLandscape(Context con) {
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
    public View getView(int i, View view, final ViewGroup viewGroup){
        // crate a new rowItem object here
        View row;
        AdapterObject viewHolder;

        if (view == null) {
            row = inflater.inflate(R.layout.single_row, viewGroup, false);
            viewHolder = new AdapterObject();

            // Get the description, imageViewReference and title of the row item
            viewHolder.title = (TextView) row.findViewById(R.id.textView);
            viewHolder.description = (TextView) row.findViewById(R.id.textView2);
            viewHolder.image = (ImageView) row.findViewById(R.id.imageView);
            viewHolder.pb = (ProgressBar) row.findViewById(R.id.progressBarSingleRow);
            row.setTag(viewHolder);
        }
        else {
            row = view;
            viewHolder = (AdapterObject) row.getTag();
        }

        // Set the values of the rowItem
        SingleRow rowTemp = articles.get(i);
        if(isLandscape(row.getContext())){
            viewHolder.title.setLines(3);
        }
        viewHolder.title.setText(rowTemp.title);
        viewHolder.description.setText(rowTemp.description);

        final Bitmap b = categoryFragment.getBitmapFromMemCache(rowTemp.title);

        // Load imageViewReference into row element
        if (b == null) {    // download
            if (cancelPotentialWork(rowTemp.imageURL, viewHolder.image)) {
                FetchCategoryImageTask task = new FetchCategoryImageTask(categoryFragment,
                        rowTemp, viewHolder.image);
                CategoryImageTaskDrawable taskWrapper = new CategoryImageTaskDrawable(task);

                viewHolder.image.setImageDrawable(taskWrapper);
                task.execute();
            }
        } else {            // set saved imageViewReference
            // Cropping imageViewReference to preserve aspect ratio
            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.image.setCropToPadding(true);
            viewHolder.image.setImageBitmap(b);
        }

        return row;
    }



    // This function is called along with .notifyDataSetChanged() in Asynctask's onScrollListener function
    // when the viewers scroll to the bottom of the articles
    public synchronized void loadMoreItems(List<Article> moreArticles, int pageNum){
        // if prevent the late page from loading twice
        if(moreArticles != null && this.pageNum != pageNum){
            this.pageNum = pageNum;

            for (int i = 0; i < moreArticles.size(); ++i) {
                String article = moreArticles.get(i).getTitle();

                // Hack-ish way of preventing the list from being populated with doubles
                // which happens if request occurs multiple times...
                if (!uniqueArticleNames.contains(article) && moreArticles.get(i).getImageURL() != null)
                    articles.add(SingleRow.newInstance(moreArticles.get(i)));
                uniqueArticleNames.add(article);
//                notifyDataSetChanged();
            }

            notifyDataSetChanged();
        }
    }

    private static boolean cancelPotentialWork(String url, ImageView imageView) {
        FetchCategoryImageTask task = getFetchCategoryImageTask(imageView);

        if (task != null) {
            String imageViewUrl = task.getUrl();

            if (imageViewUrl == null || !imageViewUrl.equals(url))
                task.cancel(true);
            else
                return false;
        }
        return true;
    }

    public static FetchCategoryImageTask getFetchCategoryImageTask(ImageView imageView) {
        if (imageView != null) {
            if (imageView.getDrawable() instanceof CategoryImageTaskDrawable) {
                CategoryImageTaskDrawable taskDrawable = (CategoryImageTaskDrawable) imageView.getDrawable();
                return taskDrawable.getFetchCategoryImageTask();
            }
        }
        return null;
    }


}
