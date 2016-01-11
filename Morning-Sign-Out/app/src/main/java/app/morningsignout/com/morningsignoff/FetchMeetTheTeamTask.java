package app.morningsignout.com.morningsignoff;

import android.os.AsyncTask;

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
public class FetchMeetTheTeamTask extends AsyncTask<Void, Void, Map<String, ArrayList<MTTListViewItem>>> {
    WeakReference<MeetTheTeamActivity> refFromCallingActivity;

    static final public String TEAM_KEY = "team";
    static final public String NAME_KEY = "teamname";

    // FIXME: Set the button from GetTeamAsyncActivity here to allow click ONLY when map is set.
    // Pass an intent to Kevin's activity with an arraylist from the FetchMeetTheTeamTask map.
    public FetchMeetTheTeamTask(MeetTheTeamActivity refFromCallingActivity) {
        this.refFromCallingActivity = new WeakReference<>(refFromCallingActivity);
    }

    @Override
    protected Map<String, ArrayList<MTTListViewItem>> doInBackground(Void... params) {
        return getMembers();
    }

    @Override
    protected void onPostExecute(final Map<String, ArrayList<MTTListViewItem>> members) {
        // Assign map to variable of calling activity. ENSURE THAT THIS ASSIGNMENT IS SYNCED.
        if (refFromCallingActivity.get() != null) {
            if (members != null) {
                refFromCallingActivity.get().setTeamsMap(members); // Give map and show activity!
                refFromCallingActivity.get().showActivity();
            } else
                refFromCallingActivity.get().cancel(); // Close activity!
        }
    }

    private Map<String, ArrayList<MTTListViewItem>> getMembers() {
        try {
            Map<String, ArrayList<MTTListViewItem>> members = new HashMap<>();
            Document doc = Jsoup.connect("http://morningsignout.com/team/").get();
            Elements groups = doc.getElementsByClass("user-group");

            // iterates through category groups
            for (Element e : groups) {
                String category = e.select("h2").first().text();
                members.put(category, new ArrayList<MTTListViewItem>()); // Each Team Category

                // efficiency, less calls to get(list)
                List<MTTListViewItem> reference = members.get(category);

                // iterates through users in each category
                for (Element u : e.getElementsByClass("user")) {
                    Element aTag = u.select("a").first();
                    Element authorInfo = aTag.getElementsByClass("author-information").first();

                    MTTListViewItem t = new MTTListViewItem(authorInfo.select("h1").first().text(), // Name
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
