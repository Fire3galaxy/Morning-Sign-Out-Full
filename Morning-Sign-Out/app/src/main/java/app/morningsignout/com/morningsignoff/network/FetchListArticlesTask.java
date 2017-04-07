package app.morningsignout.com.morningsignoff.network;

import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.category.CategoryAdapter;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;
import app.morningsignout.com.morningsignoff.category.CategoryActivity;
import app.morningsignout.com.morningsignoff.network.CheckConnection;

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
    private boolean isFirstLoad, isRefresh, isCancelled;

    private int adapterPageNum;

    // Called by onCreateView on first call
    public FetchListArticlesTask(CategoryFragment fragment,
                                 int pageNum,
                                 boolean isFirstLoad,
                                 boolean isRefresh) {
        this.fragmentRef = new WeakReference<>(fragment);   // ensure fragment still exists
        this.pageNum = pageNum;         // page of mso page called for task
        this.isFirstLoad = isFirstLoad;
        this.isCancelled = false;
    }

    @Override
    protected void onPreExecute() {
        if (fragmentRef.get() == null || !CheckConnection.isConnected(fragmentRef.get().getContext())) {
            isCancelled = true;
            return;
        }

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
        if (isCancelled)
            return null;

        // is valid request (next page only, not repeat or excess page)
        if (adapterPageNum == pageNum - 1) {
            try {
                return getArticlesJSON(params[0],pageNum);
//                return getArticles(params[0], pageNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // Articles retrived online are being sent here, and we pass the info to the CategoryAdapter
    protected void onPostExecute(final List<Article> articles) {
        if (isCancelled) {
            if (fragmentRef.get() != null)
                fragmentRef.get().getIsLoadingArticles().set(false);
            return;
        }

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
            if (articles != null)
                adapter.loadMoreItems(articles, pageNum);
            else if (adapter.isEmpty())
                Toast.makeText(fragmentRef.get().getContext(),
                        "We had trouble trying to connect", Toast.LENGTH_SHORT).show();
            fragmentRef.get().getIsLoadingArticles().set(false);
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

    List<Article> getArticlesJSON(String arg, int pageNum) { // FIXME: get this working
        StringBuilder builder = new StringBuilder();
        String urlPath = "";
        urlPath = "http://morningsignout.com/?json=get_category_posts&slug=" + arg + "&page=" + pageNum;
        HttpURLConnection connection = null;

        // opening URL connection, setup JSON
        try {
//            URL url = new URL("http://www.morningsignout.com/?json=get_category_posts&slug=featured&page=1&include=author,url,title,thumbnail");
            URL url = new URL(urlPath);
            connection = (HttpURLConnection) url.openConnection();
            InputStream response = connection.getInputStream();

            byte[] bytes = new byte[128];
            int bytesRead = 0;
            while ((bytesRead = response.read(bytes)) > 0) {
                if (bytesRead == bytes.length)
                    builder.append(new String(bytes));
                else
                    builder.append((new String(bytes)).substring(0, bytesRead));
            }
        } catch (MalformedURLException e) {
            Log.e("FetchListArticlesTask", "JSON: " + "MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("FetchListArticlesTask", "JSON: " + "ProtocolException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FetchListArticlesTask", "JSON: " + "IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("FetchListArticlesTask", "JSON: " + "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        String jsonStr = builder.toString();
//        Log.d("FetchListArticlesTask", json);
        int postCount = 0;
        List<Article> articlesList = new ArrayList<Article>();

        if (jsonStr != null)
        {
            try {

                // Get JSON object
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Get postcount
                postCount = jsonObj.getInt("count");
//                Log.d("FetchListArticlesTask", "JSON: " + "postCount: " + postCount);

                // create JSONArray of posts
                JSONArray posts = jsonObj.getJSONArray("posts");
//                Log.d("FetchListArticlesTask", "JSON: " + "length: " + posts.length());

                // going to steal Parser's replaceUnicode()
                Parser p = new Parser();

                for (int index = 0; index < postCount; index++)
                {
                    articlesList.add(new Article());
                    // create JSONObj of images
                    JSONObject imageObj = posts.getJSONObject(index).getJSONObject("thumbnail_images");

                    // Title
                    String title = p.replaceUnicode(posts.getJSONObject(index).getString("title"));
//                    Log.d("FetchListArticlesTask", "JSON: " + "title: " + title);
                    articlesList.get(index).setTitle(title);

                    // Link
                    String link = posts.getJSONObject(index).getString("url");
//                    Log.d("FetchListArticlesTask", "JSON: " + "url: " + link);
                    articlesList.get(index).setLink(link);

                    // ImageURL (full)
                    String fullURL = imageObj.getJSONObject("full").getString("url");
//                    Log.d("FetchListArticlesTask", "JSON: " + "fullURL: " + fullURL);
                    articlesList.get(index).setImageURL(fullURL);

                    // TODO: implement thumbnails for better performance
                    // Image_medURL
                    String medURL = imageObj.getJSONObject("medium").getString("url");
//                    Log.d("FetchListArticlesTask", "JSON: " + "medURL: " + medURL);
//                    articlesList.get(index).setImageURL(medURL);

                    // Author
                    JSONObject authorObject = posts.getJSONObject(index).getJSONObject("author");
                    String author = p.replaceUnicode(authorObject.getString("name"));
//                    Log.d("FetchListArticlesTask", "JSON: " + "author: " + author);
                    articlesList.get(index).setAuthor(author);

                    // Description
                }

            } catch (JSONException je) {
                Log.e("FetchListArticlesTask", "JSON: " + je.getMessage());
            }
        }
        // if no articles found, return nothing
        return articlesList.isEmpty() ? null : articlesList;
    }
}

