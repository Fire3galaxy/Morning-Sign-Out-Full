package app.morningsignout.com.morningsignoff.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.category.CategoryAdapter;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import app.morningsignout.com.morningsignoff.category.CategoryActivity;

import static android.view.View.GONE;

// This class is called in Category Activity to fetch articles online and feed it to CategoryAdapter
// do in background gets the article objects from morningsignout, and converts imageURL to bitmap
// getArticles() is the method that connects to the website and html
// articles are sent to onPostExecute, where I set arrayAdapter.
public class FetchListArticlesTask extends AsyncTask<String, Void, List<Article>> {
    // Assigned value keeps logString in sync with class name if class name changed (Udacity)
    private final String logString = FetchListArticlesTask.class.getSimpleName();

    private WeakReference<CategoryFragment> fragmentRef;
    private int pageNum;
    private boolean isFirstLoad, isRefresh;

    private int adapterPageNum;

    // Called by onCreateView on first call
    public FetchListArticlesTask(CategoryFragment fragment,
                                 int pageNum,
                                 boolean isFirstLoad,
                                 boolean isRefresh) {
        this.fragmentRef = new WeakReference<>(fragment);   // ensure fragment still exists
        this.pageNum = pageNum;         // page of mso page called for task
        this.isFirstLoad = isFirstLoad;
    }

    @Override
    protected void onPreExecute() {
        if (fragmentRef.get() == null)
            return;

        // Load correct progressbar based on state of fragment
        // first load & refresh: used refresh layout
        // just first load: actually first time fragment is created
        // neither: scrolled to bottom of list
        if (isFirstLoad) {
            if (isRefresh)
                fragmentRef.get().getSwipeRefreshLayout().setRefreshing(true);
            else
                fragmentRef.get().getProgressBar().setVisibility(View.VISIBLE);
        } else
            fragmentRef.get().getFooterProgressBar().setVisibility(View.VISIBLE);

        // Get page number for list
        WrapperListAdapter wrappedAdapter =
                (WrapperListAdapter) fragmentRef.get().getGridViewWithHeaderAndFooter().getAdapter();
        CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();
        adapterPageNum = adapter.getPageNum();
    }

    // takes in the category name as a sufix to the URL, ex. healthcare/  and call getArticles()
    @Override
    protected List<Article> doInBackground(String... params) {
        // is valid request (next page only, not repeat or excess page)
        if (adapterPageNum == pageNum - 1) {
            try {
                return getArticles(params[0], pageNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Articles retrived online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        if (fragmentRef.get() == null)
            return;

        WrapperListAdapter wrappedAdapter =
                (WrapperListAdapter) fragmentRef.get().getGridViewWithHeaderAndFooter().getAdapter();
        CategoryAdapter adapter = (CategoryAdapter) wrappedAdapter.getWrappedAdapter();;

        // Loading should only show on first loading list
        // hide progressbar, refresh message, and refresh icon (if loading is successful)
        if (isFirstLoad) {
            if (isRefresh) {
                fragmentRef.get().getSwipeRefreshLayout().setRefreshing(false);
            } else {
                fragmentRef.get().getProgressBar().setVisibility(GONE);
//                fragmentRef.get().getSplashScreenView().setVisibility(GONE);
            }

            // hide how-to-refresh textView
            if (articles != null)
                fragmentRef.get().getRefreshTextView().setVisibility(GONE);
        } else
            fragmentRef.get().getFooterProgressBar().setVisibility(GONE);

        // If result and adapter are not null and fragment still exists, load items
        if (adapter != null && fragmentRef.get() != null) {
            fragmentRef.get().getIsLoadingArticles().set(false);
            if (articles != null)
                adapter.loadMoreItems(articles, pageNum);
            else if (adapter.isEmpty())
                Toast.makeText(fragmentRef.get().getContext(),
                        "We had trouble trying to connect", Toast.LENGTH_SHORT).show();
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
            // Open connection to list article url
            URL url = new URL(urlPath);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.connect();

            // Return if failed
            int statusCode = c.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            // Stream was null, Possibly a timeout issue or something wrong.
            if (c.getInputStream() == null) return null;

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
                    }
                }
            }
            in.close();

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
}

