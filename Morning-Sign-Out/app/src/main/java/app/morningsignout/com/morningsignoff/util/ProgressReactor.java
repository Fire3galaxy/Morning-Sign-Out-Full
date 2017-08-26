package app.morningsignout.com.morningsignoff.util;

/**
 * Created by Daniel on 8/2/2017.
 */

public class ProgressReactor {
    private ProgressIndicator indicator;
    private ProgressIndicator.Type type;

    public ProgressReactor(ProgressIndicator indicator, ProgressIndicator.Type type) {
        this.indicator = indicator;
        this.type = type;
    }

    public void reactToProgress(boolean start) {
        switch (type) {
            case Loading:
                if (start) indicator.loadingStart();
                else indicator.loadingEnd();
                break;
            case Refresh:
                if (start) indicator.refreshStart();
                else indicator.refreshEnd();
                break;
            case LoadingMore:
                if (start) indicator.loadingMoreStart();
                else indicator.loadingMoreEnd();
                break;
        }
    }
}
