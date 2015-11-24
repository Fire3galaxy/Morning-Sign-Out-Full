package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Daniel on 11/18/2015.
 */
public class GetTeamAsyncActivity extends ActionBarActivity {
    /* FIXME: Changes I did: FetchMeetTheTeamTask uses this, not Tanvi's activity
        The listview in the category view has a test button, set in FetchListArticlesTask, onPostExecute.
        There is a layout file for this file in the res folder.
        FetchMeetTheTeamTask changes the button in this activity, is given argument in constructor for this button.
      */
    Map<String, ArrayList<ExecutiveListItem>> teams;

    // set content view in this activity, register new activities in manifest, fixed constructor issue in executiveActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_team_async);
        Button toKevinActivity = (Button) findViewById(R.id.button_test);

        new FetchMeetTheTeamTask(this, toKevinActivity).execute();
    }

    public void setTeamsMap(Map<String, ArrayList<ExecutiveListItem>> fromTask) {
        teams = fromTask;
    }
}
