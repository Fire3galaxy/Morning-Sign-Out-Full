package app.morningsignout.com.morningsignoff;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// This class is called in Category Activity to fetch articles online and feed it to CategoryAdapter
// do in background gets the article objects from morningsignout, and converts imageURL to bitmap
// getArticles() is the method that connects to the website and html
// articles are sent to onPostExecute, where I set arrayAdapter.
public class FetchListArticlesTask extends AsyncTask<String, Void, List<Article>> {
    // Assigned value keeps logString in sync with class name if class name changed (Udacity)
    private final String logString = FetchListArticlesTask.class.getSimpleName();

    // Used for creating CategoryAdapter and onItemClick listener
    private ListView listView;
    private ProgressBar progressBar;
    private Context c;
    private String category;

    private List<Article> articlesList;
    private int pageNum;

    public FetchListArticlesTask(Context c, ListView listView, ProgressBar progressBar,
                                 int pageNum) {
        this.c = c;
        this.listView = listView;
        this.progressBar = progressBar;
        this.pageNum = pageNum;
    }

    @Override
    protected void onPreExecute() {
    }

    // takes in the category name as a sufix to the URL, ex. healthcare/  and call getArticles()
    @Override
    protected List<Article> doInBackground(String... params) {
        try {
            // pass the category name as a string
            category = params[0];
            return getArticles(params[0], pageNum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Articles retrived online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        // Avoids redundancy if bar is gone
        if (progressBar.getVisibility() != ProgressBar.GONE)
            progressBar.setVisibility(ProgressBar.GONE);

        // Setup the adapter using the CategoryAdapter class
        // If the adapter is not set, then create the adapter and add the articles
        // If the adapter is set, then add more articles to the list then notify the data change
        if(listView.getAdapter() == null) {
            CategoryAdapter categoryAdapter = new CategoryAdapter(c, articles);
            listView.setAdapter(categoryAdapter);
        } else {
            CategoryAdapter categoryAdapter = (CategoryAdapter) listView.getAdapter();
            categoryAdapter.loadMoreItems(articles, pageNum);
            Log.d("FetchListArticlesTask", "Calling loadMoreItems " + Integer.toString(pageNum));
        }

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean isScrolling = false;
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0) {
                    boolean atStart = true;
                    boolean atEnd = true;

                    View firstView = view.getChildAt(0);
                    if (firstVisibleItem > 0) {
                        // not at start
                        atStart = false;
                    }

                    int lastVisibleItem = firstVisibleItem + visibleItemCount;
                    View lastView = view.getChildAt(visibleItemCount - 1);
                    if (lastVisibleItem < totalItemCount) {
                        // not at end
                        atEnd = false;
                    }

                    // now use atStart and atEnd to do whatever you need to do
                    if(atEnd && isScrolling) {
                        // The articltList only returns null if the last page has 12 items
                        // otherwise it returns random articles
                        if(articlesList == null || articlesList.size() < 12){
                            Toast toast = Toast.makeText(c.getApplicationContext(),
                                    "No other articles", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }else {
                            Toast toast = Toast.makeText(c.getApplicationContext(),
                                    "Loading........", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();

                            /* FIXME: Replace toast with loading bar, placed as "item" in listview
                             * with its own space. Disappears on items appearance (on post execute)
                             */
                            //loadingMoreArticles.setVisibility(ProgressBar.VISIBLE);

                            new FetchListArticlesTask(c, listView, progressBar, ++pageNum).execute(category);
                            Log.d("FetchListArticlesTask", "Loading more articles: page " + Integer.toString(pageNum));
                        }
                        isScrolling = false;
                    }

                }
            }
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                isScrolling = true;
            }
        });

    }

    // Go to MorningSignOut.com and get a list of articles
    // get 12 articles and sent to onPostExecute()
    List<Article> getArticles(String arg, int pageNum) {
        // String arg is "research", "wellness", "humanities", etc.
        // For getting article titles, descriptions, and images. See class Article
        Parser p = new Parser();
        String urlPath = "";
        if (arg.equals("latest/")) urlPath = "http://morningsignout.com/" + arg + "page/" + pageNum;
        else urlPath = "http://morningsignout.com/category/" + arg + "page/" + pageNum;
        Log.d("FetchListArticlesTask", "loading " + urlPath);

        BufferedReader in = null;
        HttpURLConnection c = null; // Done because of tutorial

        try {
            // Open connection to
            URL url = new URL(urlPath);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.connect();

            if (c.getInputStream() == null) return null; // Stream was null, why?

            in = new BufferedReader(new InputStreamReader(c.getInputStream()) );
            String inputLine;

            // For parsing the html
            boolean inContent = false; // If in <h1> tags, need to wait 2 tags before
            int closeDiv = 0, ind = 0; // counts </div> tags, ind is index of articlesList

            articlesList = new ArrayList<Article>();

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("<div class=\"content__post__info\">")) {
                    articlesList.add(new Article());
                    inContent = true;
                }

                if (inContent) {
                    // Title & Link of article
                    if (inputLine.contains("<h1>")) {
                        String title = p.getTitle(inputLine),
                                link = p.getLink(inputLine);

                        articlesList.get(ind).setTitle(title);
                        articlesList.get(ind).setLink(link);
                    } // Image URL and/or Author
                    else if (inputLine.trim().contains("<img") || inputLine.trim().contains("<h2>")) {
                        if (inputLine.trim().contains("<img")) {
                            String imageURL = p.getImageURL(inputLine);
                            articlesList.get(ind).setImageURL(imageURL);
                        }

                        if (inputLine.trim().contains("<h2>")) {
                            String author = p.getAuthor(inputLine);
                            articlesList.get(ind).setAuthor(author);
                        }

                        // convert string to bitmap then feed to each article
//                        Bitmap image = downloadBitmap(imageURL);
                    }
                    // Description of article
                    else if (inputLine.contains("<p>")) {
                        String description = p.getDescription(inputLine);
                        articlesList.get(ind).setDescription(description);
                    }

                    if (inputLine.trim().equals("</div>")) closeDiv++;
                    if (closeDiv == 2) {
                        closeDiv = 0;
                        inContent = false;
                        ind++;
                        // System.out.println();
                    }
                }

                //        		System.out.println(inputLine.trim());

            }
            in.close();
//	        for (int i = 0; i < articlesList.size(); i++)
//	        	Log.e("FetchListArticlesTask", articlesList.get(i).getDescription());

            // If buffer was empty, no items in list, so website has no articles for some reason.
            return articlesList.isEmpty() ? null : articlesList;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(logString, "error closing stream", e);
                }
            }
        }

        return null; // Exiting try/catch likely means error occurred.
    }

//    static String toUTF16(String s) {
//        try {
//            return new String(s.getBytes("UTF-8"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            Log.e("FetchListArticlesTask", e.getMessage());
//        }
//        return null;
//    }
}

