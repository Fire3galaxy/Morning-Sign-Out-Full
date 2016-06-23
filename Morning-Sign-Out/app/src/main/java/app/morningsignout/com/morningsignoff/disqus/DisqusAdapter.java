package app.morningsignout.com.morningsignoff.disqus;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import app.morningsignout.com.morningsignoff.R;

/**
 * Created by pokeforce on 6/22/16.
 */
public class DisqusAdapter extends BaseAdapter {
    static final int INDENT = 20;
    static final String EMPTY = "No comments here yet. Be the first!\n\n" +
            "Swipe down here or hit refresh in the menu to check for more comments.";

    DisqusMainActivity act;
    ArrayList<Comments> commentsList;
    Resources resources;

    DisqusAdapter(DisqusMainActivity act, ArrayList<Comments> commentsList) {
        this.act = act;
        this.commentsList = commentsList;

        resources = act.getResources();
    }

    public void switchList(ArrayList<Comments> commentsList) {
        if (!this.commentsList.equals(commentsList)) {
            this.commentsList = commentsList;
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return !(commentsList.isEmpty());
    }

    @Override
    public int getCount() {
        if (commentsList.isEmpty()) return 1;

        return commentsList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentsList.get(position);
    }

    // Based on index in comments list or which extra view it is
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // No comments
        if (commentsList.isEmpty()) {
            // Is already an empty comments view.
            if (convertView != null && convertView instanceof TextView) return convertView;

            // Create new textview
            TextView noComments = new TextView(act);
            noComments.setText(EMPTY);
            noComments.setPadding(12, 8, 12, 0);
            noComments.setTypeface(Typeface.DEFAULT);
            noComments.setTextColor(Color.BLACK);
            return noComments;
        }

        DsqViewHolder viewHolder;   // To hold two textviews
        LayoutInflater inflater = act.getLayoutInflater();

        // Comment row
        if (convertView == null || convertView instanceof TextView) {
            convertView = inflater.inflate(R.layout.comment_row, parent, false);
            viewHolder = new DsqViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView_userDsq);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.textView_commentDsq);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DsqViewHolder) convertView.getTag();
        }

        // Padding will change for views based on where extra views are
        int padding = getPxFromDp(INDENT * commentsList.get(position).indent);

        if (convertView.getPaddingLeft() != padding)                // A subcomment
            convertView.setPadding(padding, 0, 0, 0);
        viewHolder.name.setText(commentsList.get(position).name);          // username
        viewHolder.comment.setText(commentsList.get(position).message);    // comment

        return convertView;
    }

    int getPxFromDp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    class DsqViewHolder {
        TextView name;
        TextView comment;
    }
}
