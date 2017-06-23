package app.morningsignout.com.morningsignoff.meet_the_team;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.network.FetchMeetTheTeamTask;
import app.morningsignout.com.morningsignoff.network.Parser;
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
        setupActionBar();

        context = this;

        String url = "http://morningsignout.com/?json=get_author_index" ;
        listView = (ListView) findViewById(R.id.meet_the_team_json_list);

        meetTheTeamJSONAdapter = new MeetTheTeamJSONAdapter(this,new ArrayList<MeetTheTeamAuthor>());
        listView.setFastScrollEnabled(true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_justlogo, menu);
        return true;
    }

    public void setupActionBar() {
        ImageButton ib = (ImageButton) getLayoutInflater().inflate(R.layout.title_main, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        this.getSupportActionBar().setCustomView(ib, params);
        super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.getSupportActionBar().setDisplayShowCustomEnabled(true);
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

                // To sort the JSON array
                JSONArray sortedjsonArray = new JSONArray();

                List<JSONObject> list = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getJSONObject(i));
                }

                Collections.sort(list, new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject o1, JSONObject o2) {
                        String s1 = new String();
                        String s2 = new String();

                        try {
                            s1 = (String) o1.get("name");
                            s2 = (String) o2.get("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return s1.compareTo(s2);
                    }
                });

                for (int i = 0; i < jsonArray.length(); i++) {
                    sortedjsonArray.put(list.get(i));
                }

                for(int i = 0; i < sortedjsonArray.length(); i++) {
                    String name = sortedjsonArray.getJSONObject(i).getString("name");
                    String desc = Parser.replaceUnicode(sortedjsonArray.getJSONObject(i).getString("description"));
                    String slug = sortedjsonArray.getJSONObject(i).getString("slug");

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
            listView.setFastScrollEnabled(true);
            listView.setAdapter(meetTheTeamJSONAdapter);
        }
    }
}