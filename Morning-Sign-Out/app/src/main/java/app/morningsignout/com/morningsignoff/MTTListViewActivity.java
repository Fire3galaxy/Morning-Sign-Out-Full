package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Daniel on 11/20/2015.
 */
public class MTTListViewActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mttwebview);

        ListView team = (ListView) findViewById(R.id.listView_mtt);
        team.setAdapter(new TeamRowAdapter(this, null));
    }
}

class TeamRowAdapter extends BaseAdapter {
    Context c;

    public TeamRowAdapter(Context c) {
        this.c = c;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.team_row, parent, false);
        }

        // FIXME: make asynctask that loads all of data and places them into array of TeamRowObjects
        // to update the adapter.
        // Adapter should only be in charge of putting data into views. (Remember to store views in viewHolder pattern
        // The asynctask should be responsible for getting all data and should be called in onCreate() once for simplicity.
        // It should also be decided that if the initially called user has articles, the webview should be used, not this
        // listview. Possibility: this activity could really be a fragment. The activity makes a first call, and decides
        // which fragment to load. Issue: Internet to decide. Slow. Could use progressbar

        // Idea 2: Scrap this idea with JSON extra requests. Use just the webview. Add buttons to fill in whitespace (vist url, email)
        // issue: lots. Eg. what if he doesn't have email/url, what if he has articles (do buttons still show at bottom)>
    }
}
