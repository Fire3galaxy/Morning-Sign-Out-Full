package app.morningsignout.com.morningsignoff;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Daniel on 1/4/2016.
 */
public class DisqusDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View test = getActivity().getLayoutInflater().inflate(R.layout.fragment_disqusdialog, null);
        Button su = (Button) test.findViewById(R.id.button_su);
        final ImageView suImage = (ImageView) test.findViewById((R.id.imageView_su));
        su.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                suImage.setVisibility(View.VISIBLE);
            }
        });

        builder.setView(test);

        return builder.create();
    }
}
