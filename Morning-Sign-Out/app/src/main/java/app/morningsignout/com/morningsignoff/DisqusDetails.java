package app.morningsignout.com.morningsignoff;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Daniel on 11/25/2015.
 */
public class DisqusDetails {
    static public final String PUBLIC_KEY = "W7S5K8Iad6l5z9pWLgdWMg58rVTmGtOPSBtx30eZcXBVaDB7gPYYv3XgztKtQDuS";
    static public final String SECRET_KEY = "P8QbTcCBz9lMn5Dw5sjBSnhB76VFrGfMR4Jb7el6qJmfQOm2CmdbvEjlKTpYbjFR";

    static public final String AUTHORIZE_URL = "https://disqus.com/api/oauth/2.0/authorize/?"
            + "client_id=" + PUBLIC_KEY + "&"
            + "scope=read,write&"
            + "response_type=code&"
            + "redirect_uri=http://www.morningsignout.com/";
    static public final String GET_ACCESS_TOKEN_URL = "https://disqus.com/api/oauth/2.0/access_token/";
    static public final String GET_ACCESS_TOKEN_DATA = "grant_type=authorization_code&"
            + "client_id=" + PUBLIC_KEY + "&"
            + "client_secret=" + SECRET_KEY + "&"
            + "redirect_uri=http://www.morningsignout.com&"
            + "code=";
    static public final String GET_REFRESH_TOKEN_URL = "https://disqus.com/api/oauth/2.0/access_token/";
    static public final String GET_REFRESH_TOKEN_DATA = "grant_type=refresh_token&"
            + "client_id=" + PUBLIC_KEY + "&"
            + "client_secret=" + SECRET_KEY + "&"
            + "refresh_token=";
    static public final String GET_LIST_POSTS_URL = "https://disqus.com/api/3.0/threads/listPosts.json?"
            + "api_key=" + PUBLIC_KEY + "&"
            + "order=asc&"
            + "forum=morningsignout&thread=";
    static public final String POST_COMMENT_URL = "https://disqus.com/api/3.0/posts/create.json";
    static public final String POST_COMMENT_DATA = "api_key=" + PUBLIC_KEY + "&"; // + ACCESS_TOKEN + THREAD_ID + MESSAGE

    // For use in getting code: DisqusLogin and LoginClient
    static public final String CODE_KEY = "code";

    HttpDetails httpMethods;
    DisqusMethods disqusMethods;

    DisqusDetails() {
        httpMethods = new HttpDetails();
        disqusMethods = new DisqusMethods();
    }

    // LIST_POSTS Disqus: two internet requests to load comments
    ArrayList<Comments> getComments(String slug) {
        String threadId = httpMethods.getMsoThreadId(slug);             // Request 1
        String commentsJson = disqusMethods.getCommentsJson(threadId);  // Request 2

        if (commentsJson != null)
            return Comments.parseCommentsArray(commentsJson);

        return null;
    }

    AccessToken getAccessToken(String code) {
        String tokenJson = disqusMethods.getAccessTokenFromCode(code);
        if (tokenJson != null)
            return AccessToken.parseAccessToken(tokenJson);

        return null;
    }

    // ------------------------------------------------------------------------------------

    private class DisqusMethods {
        String getCommentsJson(String threadId) {
            return httpMethods.getHttp(GET_LIST_POSTS_URL + threadId);
        }

        String getAccessTokenFromCode(String code) {
            return httpMethods.postHttp(GET_ACCESS_TOKEN_URL, code);
        }
    }

    private class HttpDetails {
        static final String dsqVar = "\"dsq_thread_id\":[\"";   // "dsq_thread_id":["
        static final int dsqVarLength = 18;
        static final String msoPost = "http://morningsignout.com/?json=get_post&slug="; // slug from ArticleWebViewClient

        String getMsoThreadId(String slug) {
            String threadMSO = getHttp(msoPost + slug);

            // Checks for null
            if (threadMSO == null) return null;
            int findDsqVar = threadMSO.indexOf(dsqVar);
            if (findDsqVar == -1) return null;

            int dsq_thread_id = findDsqVar + dsqVarLength;
            int end = threadMSO.indexOf("\"", dsq_thread_id);
            return threadMSO.substring(dsq_thread_id, end);
        }

        String getHttp(String _url)  {
            if (_url == null) return null;

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                StringBuilder response = new StringBuilder();
                BufferedReader buf = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;

                while ((line = buf.readLine()) != null)
                    response.append(line);
                buf.close();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        String postHttp(String _url, String data) {
            if (_url == null || data == null) return null;

            HttpURLConnection urlConnection = null;
            try {
                // Set up url
                URL url = new URL(_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(data.length()));

                // Put contents of POST response body in output stream
                urlConnection.setDoOutput(true);
                OutputStream os = urlConnection.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                // Get response code
                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();
                System.out.println("Disqus" + "POST Response Code :: " + responseCode + " - " + responseMessage);

                InputStream rStream = null;

                // Set rStream to returned json or error stream
                if (responseCode == HttpURLConnection.HTTP_OK) //success
                    rStream = urlConnection.getInputStream();
                else
                    rStream = urlConnection.getErrorStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        rStream));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print result
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

            return null;
        }
    }
}

class JsonDisqus {
    public static String parseJsonObject(JsonObject obj, String field) {
        JsonElement elem = obj.get(field);
        try {
            String str = "";
            if(!elem.isJsonNull()){
                str = elem.getAsString();
            }
            return str;
        } catch (NullPointerException e) {
            return null;
        }
    }
}

class AccessToken {
    String access_token,
            expires_in,
            username,
            refresh_token;

    public AccessToken() {

    }

    // add author param
    public AccessToken(String _access_token,
                       String _expires_in,
                       String _username,
                       String _refresh_token){
        access_token = _access_token;
        expires_in = _expires_in;
        username = _username;
        refresh_token = _refresh_token;
    }

    @Override
    public String toString(){
        return String.format(
                "access_token: %s\nexpires_in: %s\nusername: %s\nrefresh_token: %s\n",
                access_token, expires_in, username, refresh_token);
    }

    static AccessToken parseAccessToken(String jsonString){
        JsonParser parser = new JsonParser();
        JsonObject disqusJson = parser.parse(jsonString).getAsJsonObject();

        String access_token = JsonDisqus.parseJsonObject(disqusJson, "access_token");
        String expires_in = JsonDisqus.parseJsonObject(disqusJson, "expires_in");
        String username = JsonDisqus.parseJsonObject(disqusJson, "username");
        String refresh_token = JsonDisqus.parseJsonObject(disqusJson, "refresh_token");

        return new AccessToken(access_token, expires_in, username, refresh_token);
    }
}

class Comments {
    String username,
            name,
            profile_url,
            message,
            date_posted,
            id,
            parent;


    public Comments() {

    }

    // add author param
    public Comments(String _username,
                    String _name,
                    String _profile_url,
                    String _message,
                    String _date_posted,
                    String _id,
                    String _parent){
        username = _username;
        name = _name;
        profile_url = _profile_url;
        message = _message;
        date_posted = _date_posted;
        id = _id;
        parent = _parent;
    }

    @Override
    public String toString(){
        return String.format(
                "\nusername: %s\nname: %s\nprofile_url: %s\nmessage: %s\ndate_posted: %s\nid: %s\nparent: %s\n",
                username, name, profile_url, message, date_posted, id, parent);
    }

    private static Comments parseComment(JsonObject disqusJson){
        JsonObject author = disqusJson.get("author")
                .getAsJsonObject();

        String username = JsonDisqus.parseJsonObject(author, "username");
        String name = JsonDisqus.parseJsonObject(author, "name");
        String profile_url = JsonDisqus.parseJsonObject(author, "profileUrl");
        String message = JsonDisqus.parseJsonObject(disqusJson, "raw_message");
        String date_posted = JsonDisqus.parseJsonObject(disqusJson, "createdAt");
        String id = JsonDisqus.parseJsonObject(disqusJson, "id");
        String parent = JsonDisqus.parseJsonObject(disqusJson, "parent");


        //System.out.println(response);
        return new Comments(username, name, profile_url, message, date_posted, id, parent);
    }

    static ArrayList<Comments> parseCommentsArray(String jsonString) {
        if (jsonString == null)
            return null;

        JsonParser parser = new JsonParser();
        JsonObject disqusJson = parser.parse(jsonString).getAsJsonObject();

        JsonArray responses = disqusJson.get("response")
                .getAsJsonArray();

        ArrayList<Comments> comments = new ArrayList<Comments>();

        for (JsonElement postElem : responses) {
            JsonObject obj = postElem.getAsJsonObject();
            Comments comment = parseComment(obj);
            comments.add(comment);
        }
        return comments;
    }
}
