package app.morningsignout.com.morningsignoff;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
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

    private WeakReference<CategoryFragment> fragmentRef;
    private WeakReference<ListView> listViewWeakRef;
    private CategoryFragment.CategoryViews loadingViews;
    private int pageNum;

    private int adapterPageNum;

    public FetchListArticlesTask(CategoryFragment fragment,
                                 ListView listView,
                                 CategoryFragment.CategoryViews loadingViews,
                                 int pageNum) {
        this.fragmentRef = new WeakReference<CategoryFragment>(fragment);
        this.listViewWeakRef = new WeakReference<ListView>(listView);
        this.loadingViews = loadingViews;
        this.pageNum = pageNum;
    }

    public FetchListArticlesTask(CategoryFragment fragment,
                                 ListView listView,
                                 int pageNum) {
        this.fragmentRef = new WeakReference<CategoryFragment>(fragment);
        this.listViewWeakRef = new WeakReference<ListView>(listView);
        this.loadingViews = null;
        this.pageNum = pageNum;
    }

    @Override
    protected void onPreExecute() {
        // Loading views
        if (loadingViews != null) {
            if (loadingViews.refresh && loadingViews.swipeRefresh.get() != null)
                loadingViews.swipeRefresh.get().setRefreshing(true);
            else if (loadingViews.progressBar.get() != null)
                loadingViews.progressBar.get().setVisibility(View.VISIBLE);
        }

        // initialize variables
        if (listViewWeakRef.get() != null) {
            // Make access to the asyncTask synchronized to prevent excess loading
            final CategoryAdapter adapter = (CategoryAdapter) listViewWeakRef.get().getAdapter();
            adapterPageNum = adapter.getPageNum();
        }
    }

    // takes in the category name as a sufix to the URL, ex. healthcare/  and call getArticles()
    @Override
    protected List<Article> doInBackground(String... params) {
        if (adapterPageNum == pageNum - 1) {
            try {
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
        CategoryAdapter adapter = null;

        if (listViewWeakRef.get() != null)
            adapter = (CategoryAdapter) listViewWeakRef.get().getAdapter();

        boolean loadingAnim = false;
        if (loadingViews != null) {
            if (loadingViews.refresh)
                loadingAnim = loadingViews.swipeRefresh.get() != null &&
                        loadingViews.swipeRefresh.get().isRefreshing();
            else
                loadingAnim = loadingViews.progressBar.get() != null &&
                        loadingViews.progressBar.get().getVisibility() != ProgressBar.GONE;
        }

        // Loading should only show on first loading list
        // hide progressbar, refresh message, and refresh icon (if loading is done/successful)
        if (loadingViews != null && loadingAnim) {
            if (loadingViews.refresh) loadingViews.swipeRefresh.get().setRefreshing(false);
            else loadingViews.progressBar.get().setVisibility(ProgressBar.GONE);

            if (articles != null) {
                TextView txtv = loadingViews.refreshTextView.get();

                if (txtv != null) txtv.setVisibility(View.GONE);
            }
        }

        // If result and adapter are not null and fragment still exists, load items
        if (articles != null && adapter != null && fragmentRef.get() != null) {
            fragmentRef.get().isLoadingArticles.set(false);
            adapter.loadMoreItems(articles, pageNum);

            Log.d("FetchListArticlesTask", "Calling loadMoreItems " + Integer.toString(pageNum));
        }
    }

    // Go to MorningSignOut.com and get a list of articles
    // get 12 articles and sent to onPostExecute()
    List<Article> getArticles(String arg, int pageNum) {
        // String arg is "research", "wellness", "humanities", etc.
        // For getting article titles, descriptions, and images. See class Article
        Parser p = new Parser();
        String urlPath = "";
        if (arg.equals("latest")) urlPath = "http://morningsignout.com/" + arg + "/page/" + pageNum;
        else urlPath = "http://morningsignout.com/category/" + arg + "/page/" + pageNum;
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

            List<Article> articlesList = new ArrayList<Article>();

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
//                        Bitmap imageViewReference = downloadBitmap(imageURL);
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

