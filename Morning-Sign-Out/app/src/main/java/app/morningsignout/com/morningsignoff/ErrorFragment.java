package app.morningsignout.com.morningsignoff;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// THE BAD FRAGMENT - use this for moments when something shouldn't happen
// e.g. an intent is null when it shouldn't have been
public class ErrorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_error, container, false);
    }
}
