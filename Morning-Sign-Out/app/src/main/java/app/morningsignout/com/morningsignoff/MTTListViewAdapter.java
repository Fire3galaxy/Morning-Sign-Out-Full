package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by liukwarm on 10/24/15.
 */
public class MTTListViewAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<MTTListViewItem> items;

    public MTTListViewAdapter(Context context, ArrayList<MTTListViewItem> navDrawerItems){
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
        MTTLV_ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.mtt_listview_item, parent, false);

            viewHolder = new MTTLV_ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.pos = (TextView) convertView.findViewById(R.id.position);

            convertView.setTag(viewHolder);
        } else
            viewHolder = (MTTLV_ViewHolder) convertView.getTag();

        viewHolder.name.setText(items.get(position).name);
        viewHolder.pos.setText(items.get(position).position);

        return convertView;
    }

    private class MTTLV_ViewHolder {
        TextView name;
        TextView pos;
    }
}
