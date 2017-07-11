package app.morningsignout.com.morningsignoff.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.article.Article;
import app.morningsignout.com.morningsignoff.category.CategoryAdapter;
import app.morningsignout.com.morningsignoff.category.CategoryFragment;

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
                fragmentRef.get().getProgressBar().setVisibility(View.GONE);
            }

            // hide how-to-refresh textView
            if (articles != null)
                fragmentRef.get().getRefreshTextView().setVisibility(View.GONE);
        } else
            fragmentRef.get().getFooterProgressBar().setVisibility(View.GONE);

        // If result and adapter are not null and fragment still exists, load items
        if (adapter != null && fragmentRef.get() != null) {
            if (articles != null)
                adapter.loadMoreItems(articles, pageNum);
            else if (adapter.isEmpty())
                Toast.makeText(fragmentRef.get().getContext(), R.string.error_fail_to_connect,
                        Toast.LENGTH_SHORT).show();
            fragmentRef.get().getIsLoadingArticles().set(false);
        }
    }

    List<Article> getArticlesJSON(String arg, int pageNum) {
        StringBuilder builder = new StringBuilder();
        String urlPath = "http://morningsignout.com/?json=get_category_posts"
                + "&slug=" + arg
                + "&page=" + pageNum
                + "&include=author,url,title,thumbnail,content";
        HttpURLConnection connection = null;

        // For wordpress JSON code. Use this because there is no "latest" category.
        if (arg.equals("latest")) {
            urlPath = "http://morningsignout.com/?json=get_recent_posts"
                    + "&page=" + pageNum
                    + "&include=author,url,title,thumbnail,content";
        }

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
        int postCount = 0;
        List<Article> articlesList = new ArrayList<Article>();

        if (jsonStr != null)
        {
            try {

                // Get JSON object
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Get postcount
                postCount = jsonObj.optInt("count");

                // create JSONArray of posts
                JSONArray posts = jsonObj.optJSONArray("posts");

                for (int index = 0; index < postCount; index++)
                {
                    JSONObject currPost = posts.optJSONObject(index);
                    articlesList.add(new Article());

                    // Title
                    String title = Parser.replaceUnicode(currPost.optString("title"));  // going to steal Parser's replaceUnicode()
                    articlesList.get(index).setTitle(title);

                    // Link
                    String link = currPost.optString("url");
                    articlesList.get(index).setLink(link);

                    String mediumURL = FetchCategoryImageRunnable.NO_IMAGE;
                    String fullURL = FetchCategoryImageRunnable.NO_IMAGE;
                    // create JSONObj of images
                    if (currPost.has("thumbnail_images")){

                        JSONObject imageObj = currPost.optJSONObject("thumbnail_images");
                        // ImageURL (full)
                        if (imageObj == null)
                        {
                            if (currPost.has("thumbnail")) {
                                mediumURL = currPost.optString("thumbnail");
                                if (!URLUtil.isValidUrl(mediumURL))
                                {
                                    mediumURL = FetchCategoryImageRunnable.NO_IMAGE;
                                }
                            }
                        }
                        else if (imageObj.has("medium"))
                        {
                            mediumURL = imageObj.optJSONObject("medium").optString("url");
                        }
                        else if (imageObj.has("medium_large"))
                        {
                            mediumURL = imageObj.optJSONObject("medium_large").optString("url");
                        }
                        else if (imageObj.has("large"))
                        {
                            mediumURL = imageObj.optJSONObject("large").optString("url");
                        }
                        else if (imageObj.has("full"))
                        {
                            mediumURL = imageObj.optJSONObject("full").optString("url");
                        }
                        else {
                            if(imageObj.length() != 0)
                            {
                                // check for a list of available thumbnail images
                                List<String> imgList = new ArrayList<String>();
                                Iterator<?> keys = imageObj.keys();
                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    imgList.add(key);
                                }
                                String randomImage = imgList.get(0); // let's just grab the first image we find
                                mediumURL = imageObj.optJSONObject(randomImage).optString("url");
                            }
                            else
                            {
                                Log.e("FetchListArticlesTask","JSON: "+ title + ": no images found!");
                            }
                        }

                        // Full size URL
                        if (imageObj.has("full"))
                        {
                            fullURL = imageObj.optJSONObject("full").optString("url");
                        }
                    }
                    else if (currPost.has("thumbnail"))
                    {
                        mediumURL = currPost.optString("thumbnail");
                    }
                    else
                    {
                        Log.e("FetchListArticlesTask","JSON: "+ title + ": no images found!");
                    }


                    articlesList.get(index).setCategoryURL(mediumURL);
                    articlesList.get(index).setImageURL(fullURL);

                    // TODO: implement thumbnails for better performance
                    String medURL = "";

                    // Author
                    JSONObject authorObject = currPost.optJSONObject("author");
                    String author = Parser.replaceUnicode(authorObject.optString("name"));
                    articlesList.get(index).setAuthor(author);

                    // Content
                    articlesList.get(index).setContent(currPost.optString("content"));
                }

            } catch (JSONException je) {
                Log.e("FetchListArticlesTask", "JSON: " + je.getMessage());
                je.printStackTrace();
            }
        }
        // if no articles found, return nothing
        return articlesList.isEmpty() ? null : articlesList;
    }
}

