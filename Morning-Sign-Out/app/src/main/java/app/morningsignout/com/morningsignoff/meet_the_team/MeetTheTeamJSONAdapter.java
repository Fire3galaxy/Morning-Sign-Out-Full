package app.morningsignout.com.morningsignoff.meet_the_team;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import app.morningsignout.com.morningsignoff.R;

/**
 * Created by maniknarang on 5/25/17.
 */

public class MeetTheTeamJSONAdapter extends ArrayAdapter<MeetTheTeamAuthor> {

    private ArrayList<MeetTheTeamAuthor> meetTheTeamAuthors;

    public MeetTheTeamJSONAdapter(Context context, ArrayList<MeetTheTeamAuthor> meetTheTeamAuthors) {
        super(context, 0, meetTheTeamAuthors);
        this.meetTheTeamAuthors = meetTheTeamAuthors;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listView = convertView;
        listView = LayoutInflater.from(getContext()).inflate(R.layout.meet_the_team_author_list, parent, false);
        MeetTheTeamAuthor meetTheTeamAuthor = meetTheTeamAuthors.get(position);

        TextView nameView = (TextView) listView.findViewById(R.id.meet_the_team_json_name);
        TextView descView = (TextView) listView.findViewById(R.id.meet_the_team_json_desc);

        nameView.setText(meetTheTeamAuthor.getName());

        if (meetTheTeamAuthor.getDesc().equals("")) {
            descView.setHeight(3);
        } else {
            descView.setText(meetTheTeamAuthor.getDesc());
        }

        return listView;
    }
}
