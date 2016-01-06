package app.morningsignout.com.morningsignoff;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Daniel on 1/4/2016.
 */
public class DisqusDialog extends DialogFragment {
    private static final int REPLY = 0, SEE_USER = 1, DELETE = 2;
    private static final int[] optionIcons = {R.drawable.ic_action_reply,
                                 R.drawable.ic_action_person,
                                 R.drawable.ic_action_discard};

    public interface OnChangeCommentsListener {
        void onChangeComments();
    }

    String accessToken;
    Comments comment;
    OnChangeCommentsListener listener;

    LinearLayout optionsLayout;
    LinearLayout replyLayout;

    static public DisqusDialog createDisqusDialog(String accessToken, Comments comment) {
        DisqusDialog dialog = new DisqusDialog();
        dialog.accessToken = accessToken;
        dialog.comment = comment;

        return dialog;
    }

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        listener = (OnChangeCommentsListener) act;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialog = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_disqusdialog, null); // null passed because parent is dialog

        /* layout for options: first one visible. made gone when reply is clicked
         * layout for reply: gone until made visible by clicking reply in options */
        optionsLayout = (LinearLayout) dialog.findViewById(R.id.layout_options);
        replyLayout = (LinearLayout) dialog.findViewById(R.id.layout_reply);

        // List of options: reply, see user, delete (if applicable)
        final ListView options = (ListView) dialog.findViewById(R.id.listView_options);
        String[] optionStrings = getResources().getStringArray(R.array.disqus_options);

        // adapter for options. I can just use array of strings, but I want icons next to text.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.textview_disqusoptions, optionStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setCompoundDrawablesWithIntrinsicBounds(optionIcons[position], 0, 0, 0);
                return textView;
            }
        };

        /* optionStrings[0] - reply: change layout
         * optionStrings[1] - see user: intent to browser with user, close dialog
         * optionStrings[2] - delete: delete comment, refresh comments, close dialog */
        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case REPLY:
                        replyLayout.setVisibility(View.VISIBLE);
                        optionsLayout.setVisibility(View.GONE);
                        return;
                    case SEE_USER:
                        Uri profile = Uri.parse(comment.profile_url);
                        Intent visitProfile = new Intent(Intent.ACTION_VIEW, profile);
                        startActivity(visitProfile);
                        break;
                    case DELETE:
                        new DisqusDeleteComment(DisqusDialog.this.getActivity())
                                .execute(accessToken, comment.id);

                        listener.onChangeComments();
                        break;
                }

                dismiss();
            }
        });
        options.setAdapter(adapter);

        // Need to set horizontal scrolling manually to make this work
        EditText replyText = (EditText) dialog.findViewById(R.id.editText_reply);
        replyText.setHorizontallyScrolling(false);

        builder.setView(dialog);

        return builder.create();
    }


}
