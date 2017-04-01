package app.morningsignout.com.morningsignoff.category;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.morningsignout.com.morningsignoff.R;
import app.morningsignout.com.morningsignoff.category.CategoryActivity;

/**
 * Created by Daniel on 3/31/2017.
 */

public class SplashFragment extends Fragment {
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_splash, container, false);
        (new OneSecondSplashTask((CategoryActivity) getActivity(), this)).execute();

        return rootView;
    }

    void fadeSplash() {
        rootView.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                CategoryActivity activity = (CategoryActivity) getActivity();

                rootView.setVisibility(View.GONE);
                activity.removeSplashFragment(SplashFragment.this);
                activity.endSplash();
            }
        });
    }
}
