package app.morningsignout.com.morningsignoff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by tanvikamath on 12/26/15.
 */
public class MeetTheTeamAdapter extends BaseAdapter {
    Context context;
    private String[] captions = {
            "Executive", "General Administration", "Research",
            "Wellness", "Medicine", "Public Health",
            "Healthcare", "Web Team", "Visuals Team",
            "Marketing Team", "Finance Team", "Operations and Admin"
    };

    private Integer[] images = {
            R.drawable.category_featured, R.drawable.category_humanities, R.drawable.category_research,
            R.drawable.category_wellness, R.drawable.category_medicine, R.drawable.category_public_health,
            R.drawable.category_healthcare, R.drawable.category_web_team, R.drawable.category_visuals_team,
            R.drawable.category_marketing, R.drawable.category_finance, R.drawable.category_humanities
    };

    public MeetTheTeamAdapter(Context c) {
        this.context = c;
    }

    public int getCount() {
        return images.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((MeetTheTeamActivity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.meet_the_team_row, parent, false);

            TextView textViewTitle = (TextView) row.findViewById(R.id.textView);
            ImageView imageView = (ImageView) row.findViewById(R.id.imageView);

            textViewTitle.setText(this.captions[position]);
            imageView.setImageResource(this.images[position]);
        }
        return row;
    }

}