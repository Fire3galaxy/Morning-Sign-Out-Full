package app.morningsignout.com.morningsignoff;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
    ListView list;
    ProgressBar progressBar, progressBar2;
    String category = "";
    final static String EXTRA_TITLE = "EXTRA_TITLE";

    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        category = getArguments() != null ? getArguments().getString(EXTRA_TITLE) + "/" : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_main, container, false);

        list = (ListView) rootView.findViewById(R.id.listView);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        // Use Asynctask to fetch article from the given category
        new FetchListArticlesTask(getActivity(), list, progressBar, 1).execute(category);

        // Setup the click listener for the listView
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryAdapter categoryAdapter = (CategoryAdapter) parent.getAdapter();

                // test show its clicked
                Log.e("position: " + position, "cccccccccccccccccc");
                Log.e("Article size" + categoryAdapter.getItem(position), "cccccccccccccccccc");
                SingleRow rowTemp= (SingleRow) categoryAdapter.getItem(position);
                String articleTitle = rowTemp.title;

//                Toast toast = Toast.makeText(c.getApplicationContext(),
//                        "Loading Article: " + articleTitle, Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
//                toast.show();

                // Create new activity for the article here
                // feed the new activity with the URL of the page
                String articleLink = rowTemp.link;
                Intent articleActivity = new Intent(list.getContext(), ArticleActivity.class);
                // EXTRA_HTML_TEXT holds the html link for the article
                articleActivity.putExtra(Intent.EXTRA_HTML_TEXT, articleLink);
                // EXTRA_SHORTCUT_NAME holds the name of the article, e.g. "what life sucks in hell"
                articleActivity.putExtra(Intent.EXTRA_SHORTCUT_NAME, articleTitle);
                // EXTRA_TITLE holds the category name, e.g. "wellness/"
                articleActivity.putExtra(Intent.EXTRA_TITLE, category);

                list.getContext().startActivity(articleActivity);
            }
        });

        return rootView;
    }
}

// CategoryAdapter takes in a list of Articles and displays the titles, descriptions, images
// of those articles in the category page as row items
// It is created in the FetchListArticlesTask which is called in CategoryActivity
class CategoryAdapter extends BaseAdapter {
    ArrayList<SingleRow> articles;
    Context context;
    private int pageNum;

    CategoryAdapter(Context c, List<Article> articles){
        this.articles = new ArrayList<SingleRow>();
        // the context is needed for creating LayoutInflater
        context = c;
//        Resources res = c.getResources();
        pageNum = 0;

        loadMoreItems(articles, 1);
        Log.d("CategoryAdapter", "First time calling loadMoreItems");

        // FIXME: Actually, just post a "no articles" thing or a "is your internet on?" or something
        if (articles.isEmpty()) {
            Log.e("CategoryAdapter", "Error: empty articles in initialization!");
            System.exit(-1);
        }
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

    // Get the View route of a single row by id
    @Override
    public View getView(int i, View view, final ViewGroup viewGroup){
        // crate a new rowItem object here
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row;

        if (view == null) row = inflater.inflate(R.layout.single_row, viewGroup, false);
        else row = view;

        // Get the description, image and title of the row item
        TextView title = (TextView) row.findViewById(R.id.textView);
        TextView description = (TextView) row.findViewById(R.id.textView2);
        ImageView image = (ImageView) row.findViewById(R.id.imageView);
        ProgressBar pb = (ProgressBar) row.findViewById(R.id.progressBarSingleRow);

        /* On getting view, set this to invisible until loaded. (issue before: old image seen
           before new image on fast scroll) Mostly fixed by this, but on fast scroll down, still
           shows a little */
        image.setImageDrawable(null);

        // Set the values of the rowItem
        SingleRow rowTemp = articles.get(i);
        title.setText(rowTemp.title);
        description.setText(rowTemp.description);

        String s = "null";
        if (rowTemp.image != null) s = "not null";
        Log.e("ImageLog", "Item " + Integer.toString(i) + ", is " + s);

        // Load image into row element
        if (rowTemp.image == null) {    // download
            // Prepare prepped row objects in single holder object for fetchCategoryImageTask
            AdapterObject holder = new AdapterObject();
            holder.title = title;
            holder.description = description;
            holder.image = image;
            holder.pb = pb;

            new FetchCategoryImageTask(rowTemp, holder).execute();
        }
        else {                          // set saved image
            // Cropping image to preserve aspect ratio
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setCropToPadding(true);
            image.setImageBitmap(rowTemp.image);
        }

        return row;
    }

    // This function is called along with .notifyDataSetChange() in Asynctask's onScrollListener function
    // when the viewers scroll to the bottom of the articles
    public void loadMoreItems(List<Article> moreArticles, int pageNum){
        // If there are more than 24 articles, empty first 12 first then add 12 more articles in
//        int numOfArticles = articles.size();
//        if(numOfArticles >= maxStoredArticles){
//            for(int i = 0; i < moreArticles.size(); i++){
//                articles.remove(i);
//            }
//        }

        // if prevent the late page from loading twice
        if(moreArticles != null && this.pageNum != pageNum){
            this.pageNum = pageNum;

            // Testing CategoryAdapter
            Log.e("CategoryAdapter", "loading more" + this.pageNum);
            Log.e("CategoryAdapter", "Moresize" + moreArticles.size());
            Log.e("CategoryAdapter", "row" + articles.size());

            for (int i = 0; i < moreArticles.size(); ++i) {
                articles.add(SingleRow.newInstance(moreArticles.get(i)));
                notifyDataSetChanged();
            }
        }
    }

    public ArrayList<SingleRow> getArticles() {
        return articles;
    }
}

