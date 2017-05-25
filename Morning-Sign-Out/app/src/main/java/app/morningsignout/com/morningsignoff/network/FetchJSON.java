package app.morningsignout.com.morningsignoff.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import app.morningsignout.com.morningsignoff.article.Article;

/**
 * Created by shinr on 5/25/2017.
 */

public class FetchJSON {
    public static List<Article> getResultsJSON(String arg, int pageNum)
    {
        StringBuilder builder = new StringBuilder();
        List<Article> articleList = new ArrayList<Article>();
        String urlPath = "http://morningsignout.com/?json=get_search_results&search=" + arg
                + "&count=5&page=" + pageNum + "&include=author,url,title,thumbnail,content";
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
            Log.e("FetchListSearchTask", "MalformedURLException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FetchListSearchTask", "IOException: " + e.getMessage());
            e.printStackTrace();
        } catch(Exception e) {
            Log.e("FetchListSearchTask", "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the connection
            if (connection != null) {
                connection.disconnect();
            }
        }

        // if no articles found, return nothing
        return articleList.isEmpty() ? null : articleList;
    }
}
