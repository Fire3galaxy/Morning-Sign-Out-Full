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

    private ListView listView;
    private ProgressBar progressBar;
    private Context c;
    private String category;

    private List<Article> articlesList;
    private int pageNum;

    private int adapterPageNum;

    public FetchListArticlesTask(Context c, ListView listView, ProgressBar progressBar,
                                 int pageNum) {
        this.c = c;
        this.listView = listView;
        this.progressBar = progressBar;
        this.pageNum = pageNum;
    }

    @Override
    protected void onPreExecute() {
        // Make access to the asyncTask synchronized to prevent excess loading
        final CategoryAdapter adapter = (CategoryAdapter) listView.getAdapter();
        adapter.disableLoading();
        adapterPageNum = adapter.getPageNum();
    }

    // takes in the category name as a sufix to the URL, ex. healthcare/  and call getArticles()
    @Override
    protected List<Article> doInBackground(String... params) {
        if (adapterPageNum == pageNum - 1) {
            try {
                // pass the category name as a string
                category = params[0];
                return getArticles(params[0], pageNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // else, invalid request

        return null;
    }

    // Articles retrived online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        final CategoryAdapter adapter = (CategoryAdapter) listView.getAdapter();

        // Avoids redundancy if bar is gone
        if (progressBar.getVisibility() != ProgressBar.GONE)
            progressBar.setVisibility(ProgressBar.GONE);

        // If result is not null, then add more articles to the list and notify the data change
        if (articles != null) {
            Log.d("FetchListArticlesTask", Integer.toString(pageNum));

            adapter.loadMoreItems(articles, pageNum);

            Log.d("FetchListArticlesTask", "Calling loadMoreItems " + Integer.toString(pageNum));
        }

        // if there are no more articles available, indicated by a successful request but null
        // return/less than 12 results, do not enable loading for the adapter.
        if (!(adapterPageNum == pageNum - 1 && (articlesList == null || articlesList.size() < 12))) {
            adapter.enableLoading();
        }
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

