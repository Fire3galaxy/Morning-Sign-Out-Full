package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

// THE BAD FRAGMENT - use this for moments when something shouldn't happen
// e.g. an intent is null when it shouldn't have been
public class ErrorFragment extends Fragment {
    static final String ERROR_CODE = "ERROR_CODE";
    static final String NO_INTERNET = "NO_INTERNET";

    CategoryActivity myActivity;
    String error;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        myActivity = (CategoryActivity) getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            switch (getArguments().getString(ERROR_CODE)) {
                case NO_INTERNET:
                    View rootView = inflater.inflate(R.layout.fragment_error_no_internet, container, false);
                    error = NO_INTERNET;

                    Button refresh = (Button) rootView.findViewById(R.id.button_error_no_internet);
                    refresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (myActivity.checkForInternet()) {
                                CategoryFragment fragment = new CategoryFragment();
                                Bundle args = new Bundle();
                                args.putString(CategoryFragment.EXTRA_TITLE, myActivity.getCurrentCategoryUrl());
                                fragment.setArguments(args);

                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container_category, fragment)
                                        .commit();
                            } else {
                                Toast.makeText(myActivity, "Unable to connect", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    return rootView;
            }
        }

        return inflater.inflate(R.layout.fragment_error, container, false);
    }
}
