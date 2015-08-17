package app.morningsignout.com.morningsignoff;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* Majorly reformed for the sake of just getting specific articles and
 * NOT a list of articles for the category page. The original code with parsing intended
 * for bigger lists of articles is in previous branch under different file name
 */
public class FetchHeadlineArticles {
    // Assigned value keeps logString in sync with class name if class name changed (Udacity)
    private static final String logString = FetchHeadlineArticles.class.getSimpleName();

    public static Article getArticles(String arg, int headlinePage) {
        Parser p = new Parser();
        String urlPath = "http://morningsignout.com/category/" + arg;

        BufferedReader in = null;
        HttpURLConnection c = null; // Done because of tutorial

        try {
            // Open connection to
            URL url = new URL(urlPath);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.connect();

            if (c.getInputStream() == null) return null; // Stream was null, why?

            in = new BufferedReader(new InputStreamReader(c.getInputStream() ) );
            String inputLine;

            // For parsing the html
            boolean inContent = false; // If in <h1> tags, need to wait 2 tags before
            int closeDiv = 0, ind = 0; // counts </div> tags, ind is index of articlesList

            Article article = new Article();

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("<div class=\"content__post__info\">")) inContent = true;

                if (inContent) {
                    if (ind == headlinePage) {
                        // Title & Link of article
                        if (inputLine.contains("<h1>")) {
                            String title = p.getTitle(inputLine),
                                    link = p.getLink(inputLine);

                            article.setTitle(title);
                            article.setLink(link);
                        }
                        // Image URL
                        else if (inputLine.trim().contains("<img")) {
                            String imageURL = p.getImageURL(inputLine);

                            article.setImageURL(imageURL);
                        }
                        // Description of article
                        else if (inputLine.contains("<p>")) {
                            String description = p.getDescription(inputLine);
                            article.setDescription(description);
                        }
                    }

                    if (inputLine.trim().equals("</div>")) closeDiv++;
                    if (closeDiv == 2) {
                        closeDiv = 0;
                        inContent = false;
                        ind++;

                        if (ind > headlinePage) return article;
                        // System.out.println();
                    }
                }
            }
            in.close();

            // If buffer was empty, no items in list, so website has no articles for some reason.
            return null;
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
}

