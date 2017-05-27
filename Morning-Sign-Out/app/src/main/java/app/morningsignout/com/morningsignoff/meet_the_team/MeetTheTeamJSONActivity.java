package app.morningsignout.com.morningsignoff.meet_the_team;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.network.FetchMeetTheTeamTask;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by maniknarang on 5/25/17.
 */

public class MeetTheTeamJSONActivity extends AppCompatActivity {
    private MeetTheTeamJSONAdapter meetTheTeamJSONAdapter;
    private Context context;
    private ListView listView;
    private ArrayList<MeetTheTeamAuthor> authorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet_the_team_json);

        context = this;

        String url = "http://morningsignout.com/?json=get_author_index" ;
        listView = (ListView) findViewById(R.id.meet_the_team_json_list);

        meetTheTeamJSONAdapter = new MeetTheTeamJSONAdapter(this,new ArrayList<MeetTheTeamAuthor>());
        listView.setAdapter(meetTheTeamJSONAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String url = "http://morningsignout.com/author/" + authorList.get(position).getSlug();

                WebView webView = new WebView(view.getContext());
                webView.setWebViewClient(new WebViewClient());

                // Load the url
                webView.loadUrl(url);


                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                startActivity(intent);

            }
        });

        new FetchMeetTheTeamJSONTask().execute(url);
    }

    public class FetchMeetTheTeamJSONTask extends AsyncTask<String,Void,ArrayList<MeetTheTeamAuthor>> {

        @Override
        protected ArrayList<MeetTheTeamAuthor> doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(params[0])
                    .build();
            authorList = new ArrayList<>();

            try {

                Response response = client.newCall(request).execute();
                JSONObject jsonObject = new JSONObject(response.body().string());
                JSONArray jsonArray = jsonObject.getJSONArray("authors");

                for(int i=0; i<jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    String desc = jsonArray.getJSONObject(i).getString("description");
                    String slug = jsonArray.getJSONObject(i).getString("slug");

                    authorList.add(new MeetTheTeamAuthor(name,desc,slug));
                }

                return authorList;
            } catch (IOException e) {
                // No need to operate upon catching
            } catch (JSONException e) {
                // No need to operate. JSON will get fetched
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MeetTheTeamAuthor> meetTheTeamAuthors) {
            MeetTheTeamJSONAdapter meetTheTeamJSONAdapter = new MeetTheTeamJSONAdapter(context,meetTheTeamAuthors);
            listView.setAdapter(meetTheTeamJSONAdapter);
        }
    }
}