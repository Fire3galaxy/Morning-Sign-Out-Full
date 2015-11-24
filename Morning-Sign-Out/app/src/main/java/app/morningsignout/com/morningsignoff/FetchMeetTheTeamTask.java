package app.morningsignout.com.morningsignoff;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 10/30/2015.
 */
public class FetchMeetTheTeamTask extends AsyncTask<Void, Void, Map<String, ArrayList<ExecutiveListItem>>> {
    WeakReference<GetTeamAsyncActivity> refFromCallingActivity;

    // Temporary variables for testing
    WeakReference<Button> toKevinActivity;
    final String notReady = "Fetching team array now!",
                 ready = "Can now click to send array to listview.";

    final String team = "Category: Public Health";
    static final public String TEAM_KEY = "team";
    static final public String NAME_KEY = "teamname";

    // FIXME: Set the button from GetTeamAsyncActivity here to allow click ONLY when map is set.
    // Pass an intent to Kevin's activity with an arraylist from the FetchMeetTheTeamTask map.
    public FetchMeetTheTeamTask(GetTeamAsyncActivity refFromCallingActivity, Button toKevin) {
        this.refFromCallingActivity = new WeakReference<>(refFromCallingActivity);
        this.toKevinActivity = new WeakReference<>(toKevin);
    }

    @Override
    protected void onPreExecute() {
        if (toKevinActivity.get() != null)
            toKevinActivity.get().setText(notReady);
    }

    @Override
    protected Map<String, ArrayList<ExecutiveListItem>> doInBackground(Void... params) {
        return getMembers();
    }

    @Override
    protected void onPostExecute(final Map<String, ArrayList<ExecutiveListItem>> members) {
        // Assign map to variable of calling activity. ENSURE THAT THIS ASSIGNMENT IS SYNCED.
        /* FIXME: Replace with a synchronized function from calling activity. Also make that
         * FIXME: calling activity the argument for this asynctask, not a reference to the map, so
         * FIXME: you can call that function.
         * FIXME: And come up with something for if call fails
         */
        if (refFromCallingActivity.get() != null) {
            refFromCallingActivity.get().setTeamsMap(members);

            if (toKevinActivity.get() != null) {
                toKevinActivity.get().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(refFromCallingActivity.get(), ExecutiveActivity.class);
                        intent.putParcelableArrayListExtra(TEAM_KEY, members.get(team));
                        intent.putExtra(NAME_KEY, team);
                        refFromCallingActivity.get().startActivity(intent);
                    }
                });

                toKevinActivity.get().setText(ready);
            }
        }
    }

    private Map<String, ArrayList<ExecutiveListItem>> getMembers() {
        try {
            Map<String, ArrayList<ExecutiveListItem>> members = new HashMap<>();
            Document doc = Jsoup.connect("http://morningsignout.com/team/").get();
            Elements groups = doc.getElementsByClass("user-group");

            // iterates through category groups
            for (Element e : groups) {
                String category = e.select("h2").first().text();
                members.put(category, new ArrayList<ExecutiveListItem>()); // Each Team Category

                // efficiency, less calls to get(list)
                List<ExecutiveListItem> reference = members.get(category);

                // iterates through users in each category
                for (Element u : e.getElementsByClass("user")) {
                    Element aTag = u.select("a").first();
                    Element authorInfo = aTag.getElementsByClass("author-information").first();

                    ExecutiveListItem t = new ExecutiveListItem(authorInfo.select("h1").first().text(), // Name
                            authorInfo.select("h2").first().text(),                                     // Position
                            aTag.attr("href"));                                                         // Hyperlink

                    reference.add(t);
                }
            }

            return members;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
