package app.morningsignout.com.morningsignoff.network;

import android.text.Html;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;

/**
 * Created by shinr on 5/25/2017.
 */


public class FetchJSON {
    // Tells the search class what type of request to make
    // JSEARCH sends a search request
    // JCATLIST fetches articles in a category (arg)
    public enum SearchType {
        JSEARCH, JCATLIST, JLATEST
    }
    // Returns a list of Articles from the website using the WordPress JSON API.
    // Requires a SearchType, argument, page number.
    public static List<Article> getResultsJSON(SearchType searchType, String arg, int pageNum)
    {
        StringBuilder builder = new StringBuilder(); // Temporary, builds the response string.
        List<Article> articleList = new ArrayList<Article>(); // to be returned

        // this should fill in the argument to JSON's GET method.
        String queryType = "";
        switch (searchType) {
            case JSEARCH:
                // when searching, the argument is passed as the "search" parameter
                queryType = "get_search_results&search=";
                break;
            case JCATLIST:
                // when selecting a category, the argument is passed as the "slug" parameter
                queryType = "get_category_posts&slug=";
                break;
            case JLATEST:
                //
                queryType = "get_recent_posts";
                // no argument necessary
//                arg = ""; // hopefully this doesn't change the value of the original arg (params[0])
                break;
            default:
                Log.e("FetchJSON", "unknown JSON method");
                queryType = "get_recent_posts";
                break;
        }
        //posts per page and/or request: &count=20 by default.
        String urlPath = "http://morningsignout.com/?json="
                + queryType // get_<json get method> &[search|slug]
                + arg // searchparams | categoryslug
                + "&page=" + pageNum
                + "&include=author,url,title,thumbnail,content,excerpt,tags";
        HttpURLConnection connection = null;

        // open http connection
        try {
            URL url = new URL(urlPath);
            connection = (HttpURLConnection) url.openConnection();
            InputStream response = connection.getInputStream();

            byte[] bytes = new byte[128];
            int bytesRead = 0;
            while ((bytesRead = response.read(bytes)) > 0) {
                if (bytesRead == bytes.length)
                    builder.append(new String(bytes));
                else
                    builder.append(new String(bytes).substring(0, bytesRead));
            }
        } catch (MalformedURLException e) {
            Log.e("FetchJSON", "MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FetchJSON", "IOException: " + e.getMessage());
            e.printStackTrace();
        } catch(Exception e) {
            Log.e("FetchJSON", "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the connection
            if (connection != null) {
                connection.disconnect();
            }
        }

        String jsonStr = builder.toString(); // Converts response to String.
        int postCount = 0; // Keeps track of how many posts were returned by the query.

        if (jsonStr != null) {
            try { // note: the prefix "opt" indicates optional, such that the method won't fail
                  //                                            if something unexpected happens.

                // Get JSON object. This contains the entire response.
                JSONObject jsonObject = new JSONObject(jsonStr);
                // Get post count.
                postCount = jsonObject.optInt("count");
                // Generate an array of returned posts
                JSONArray posts = jsonObject.optJSONArray("posts");

                // Loop through the posts, generate Articles for each, and populate articlesList.
                for (int index = 0; index < postCount; index++) {
                    // The current post. Start the process. New Article.
                    JSONObject currPost = posts.optJSONObject(index);
                    articleList.add(new Article());

                    // Title.
                    String title = stripHtml(currPost.optString("title"));
                    articleList.get(index).setTitle(title);

                    // Link.
                    String link = currPost.optString("url");
                    articleList.get(index).setLink(link);

                    // Images.
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


                    articleList.get(index).setCategoryURL(mediumURL);
                    articleList.get(index).setImageURL(fullURL);

                    // TODO: implement thumbnails for better performance
                    String medURL = "";

                    // Author
                    JSONObject authorObject = currPost.optJSONObject("author");
                    String author = Parser.replaceUnicode(authorObject.optString("name"));
                    articleList.get(index).setAuthor(author);

                    // Content
                    articleList.get(index).setContent(currPost.optString("content"));

                    // Excerpt
                    articleList.get(index).setExcerpt(stripHtml(currPost.optString("excerpt")));

                    // FIXME: no tags found
                    // Tags.
                    JSONArray tags = currPost.optJSONArray("tags");
                    if (currPost.has("tags"))
                    {
                        int tagCount = tags.length();
//                        Log.d("FetchJSON","tags found: " + tagCount);
                        for (int tagIndex = 0; tagIndex < tagCount; tagIndex++)
                        {
                            JSONObject tagObject = null;
                            if (tags.optJSONObject(tagIndex) != null)
                            {
                                String single_tag = "";
                                tagObject = tags.getJSONObject(tagIndex);
                                if (tagObject.has("title"))
                                {
                                    single_tag = tagObject.optString("title");
//                                    Log.d("FetchJSON","tag_title: " + single_tag);
                                }
                                else if (tagObject.has("slug")) // if tag has no title, try slug
                                {
                                    single_tag = tagObject.optString("slug");
//                                    Log.d("FetchJSON","tag_slug: " + single_tag);
                                } // else do nothing.

                                if (single_tag != null && !single_tag.isEmpty())
                                {
//                                    Log.d("FetchJSON","post: " + title + " tag: "+ single_tag);
                                    articleList.get(index).setTags(single_tag); // push a tag onto list
                                }
                            }
                            else {
                                Log.d("FetchJSON","post " + title + " has no tags");
                            }
                        }
                    } else
                    {
                        Log.d("FetchJSON","no tags");
                    }
                }
            } catch (JSONException je) {
                Log.e("FetchJSON", je.getMessage());
                je.printStackTrace();
            }
        }

        // if no articles found, return nothing
        return articleList.isEmpty() ? null : articleList;
    }

    public static String stripHtml(String html) {
        return Jsoup.parse(html).text();
    }
}
