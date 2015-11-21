package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
        team.setAdapter(new TeamRowAdapter(null));
    }
}

class TeamRowAdapter extends BaseAdapter {
    List<String> hyperlinks;

    public TeamRowAdapter(List<String> hyperlinks) {
        this.hyperlinks = hyperlinks;
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
        // FIXME: Change this to load my team_row xml.
        return null;
    }
}
