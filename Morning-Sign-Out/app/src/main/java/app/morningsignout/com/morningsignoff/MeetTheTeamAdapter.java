package app.morningsignout.com.morningsignoff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by tanvikamath on 12/26/15. Puts the categories and icons into the gridview.
 */
public class MeetTheTeamAdapter extends BaseAdapter {
    Context context;
    private final String[] realNames = {
            "Executive", "General Administration", "Category: Research",
            "Category: Wellness", "Category: Medicine", "Category: Public Health",
            "Category: Healthcare", "Web Team", "Visuals Team",
            "Marketing Team", "Finance Team", "Operations & Admin"
    };

    private final String[] captions = {
            "Executive", "General Administration", "Research",
            "Wellness", "Medicine", "Public Health",
            "Healthcare", "Web Team", "Visuals Team",
            "Marketing Team", "Finance Team", "Operations & Admin"
    };

    private final Integer[] images = {
            R.drawable.executive_team, R.drawable.gen_admin_team, R.drawable.research_team,
            R.drawable.wellness_team, R.drawable.medicine_team, R.drawable.public_health_team,
            R.drawable.healthcare_team, R.drawable.web_team, R.drawable.visuals_team,
            R.drawable.marketing_team, R.drawable.finance_team, R.drawable.op_admin_team
    };

    public MeetTheTeamAdapter(Context c) {
        this.context = c;
    }

    public int getCount() {
        return images.length;
    }

    public Object getItem(int position) {
        return new TeamTitle(realNames[position], captions[position]);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MTTViewHolder viewHolder;

        if (row == null) {
            viewHolder = new MTTViewHolder();

            LayoutInflater inflater = ((MeetTheTeamActivity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.meet_the_team_row, parent, false);

            viewHolder.textViewTitle = (TextView) row.findViewById(R.id.textView_mtt);
            viewHolder.imageView = (ImageView) row.findViewById(R.id.imageView_mtt);
            row.setTag(viewHolder);
        } else {
            viewHolder = (MTTViewHolder) row.getTag();
        }

        viewHolder.textViewTitle.setText(this.captions[position]);
        viewHolder.imageView.setImageResource(this.images[position]);

        return row;
    }

    private class MTTViewHolder {
        TextView textViewTitle;
        ImageView imageView;
    }

    public class TeamTitle {
        String realName;
        String caption;

        public TeamTitle(String realName, String caption) {
            this.realName = realName;
            this.caption = caption;
        }
    }
}