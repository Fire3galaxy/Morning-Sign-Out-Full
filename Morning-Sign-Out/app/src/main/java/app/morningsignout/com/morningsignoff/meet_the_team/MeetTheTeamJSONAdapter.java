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
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import app.morningsignout.com.morningsignoff.R;

/**
 * Created by maniknarang on 5/25/17.
 */

public class MeetTheTeamJSONAdapter extends ArrayAdapter<MeetTheTeamAuthor> implements SectionIndexer {

    private ArrayList<MeetTheTeamAuthor> meetTheTeamAuthors;
    private HashMap<String, Integer> mapIndex;
    private String[] sections;

    public MeetTheTeamJSONAdapter(Context context, ArrayList<MeetTheTeamAuthor> meetTheTeamAuthors) {
        super(context, 0, meetTheTeamAuthors);
        this.meetTheTeamAuthors = meetTheTeamAuthors;

        mapIndex = new LinkedHashMap<String, Integer>();

        for (int i = 0; i < meetTheTeamAuthors.size(); i++) {
            MeetTheTeamAuthor author = meetTheTeamAuthors.get(i);
            String ch = author.getName().substring(0,1);
            ch = ch.toUpperCase(Locale.US);

            // HashMap will prevent duplicates
            mapIndex.put(ch, i);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);
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

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mapIndex.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}
