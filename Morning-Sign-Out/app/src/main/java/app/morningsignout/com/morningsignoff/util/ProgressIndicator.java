package app.morningsignout.com.morningsignoff.util;

/**
 * Created by Daniel on 8/2/2017.
 */

// When using this class, you must specify which of the three types of "loading" animation you need
public interface ProgressIndicator {
    enum Type {Loading, Refresh, LoadingMore};

    // For adding the first set of articles (uses center progress bar)
    void loadingStart();
    void loadingEnd();

    // For refreshing the list with a new first set of articles (uses swipe refresh layout)
    void refreshStart();
    void refreshEnd();

    // For adding more articles to a list (uses footer progressbar)
    void loadingMoreStart();
    void loadingMoreEnd();
}
