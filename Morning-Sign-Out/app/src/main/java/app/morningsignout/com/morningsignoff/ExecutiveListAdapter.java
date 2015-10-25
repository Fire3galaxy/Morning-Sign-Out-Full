package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by liukwarm on 10/24/15.
 */
public class ExecutiveListAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<ExecutiveListItem> items;

    public ExecutiveListAdapter(Context context, ArrayList<ExecutiveListItem> navDrawerItems){
        this.context = context;
        this.items = navDrawerItems;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.executive_list_item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView pos = (TextView) convertView.findViewById(R.id.position);

        name.setText(items.get(position).name);
        pos.setText(items.get(position).position);

        return convertView;
    }


}
