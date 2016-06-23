package app.morningsignout.com.morningsignoff.disqus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

import app.morningsignout.com.morningsignoff.R;

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

    AccessToken accessToken;
    Comments comment;
    String threadId;
    OnChangeCommentsListener listener;

    LinearLayout optionsLayout;
    LinearLayout replyLayout;

    static public DisqusDialog createDisqusDialog(AccessToken accessToken, Comments comment, String threadId) {
        DisqusDialog dialog = new DisqusDialog();
        dialog.accessToken = accessToken;
        dialog.comment = comment;
        dialog.threadId = threadId;

        return dialog;
    }

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        listener = (OnChangeCommentsListener) act;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /* View is composed of two layouts, one for options buttons like replying or deleting post,
         * and one for replying if that option is clicked. The first half of the function sets up
         * the adapter and onClickListners for the listview of the first layout. The second half
         * sets up the second layout's textview, edittext, and post button.
         */
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

        // Remove delete if user does not have authority to delete comment.
        if (!comment.username.equals(accessToken.username))
            optionStrings = Arrays.copyOf(optionStrings, 2);

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
                                .execute(accessToken.access_token, comment.id);

                        listener.onChangeComments();
                        break;
                }

                dismiss();
            }
        });
        options.setAdapter(adapter);

        // "Reply to <Name of commenter>"
        String replied = getResources().getString(R.string.disqus_dialog2);
        if (comment.username.equals(accessToken.username)) replied += " yourself!";
        else replied += " " + comment.name;

        TextView replyTo = (TextView) dialog.findViewById(R.id.textView_dialog2);
        replyTo.setText(replied);

        // Need to set horizontal scrolling manually to make this work
        final EditText replyText = (EditText) dialog.findViewById(R.id.editText_reply);
        replyText.setHorizontallyScrolling(false);
        // Post comment & dismiss dialog
        replyText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text = v.getText().toString();

                switch(actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        if (!text.isEmpty()) {
                            new DisqusPostComment((DisqusMainActivity) getActivity())
                                    .execute(threadId, comment.id, text);
                            listener.onChangeComments();
                            dismiss();
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            }
        });

        Button postButton = (Button) dialog.findViewById(R.id.button_replyDialog);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = replyText.getText().toString();

                if (!text.isEmpty()) {
                    new DisqusPostComment((DisqusMainActivity) getActivity())
                            .execute(threadId, comment.id, text);
                    listener.onChangeComments();
                    dismiss();
                }
            }
        });

        builder.setView(dialog);

        return builder.create();
    }


}
