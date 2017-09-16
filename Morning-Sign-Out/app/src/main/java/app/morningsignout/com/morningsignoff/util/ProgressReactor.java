package app.morningsignout.com.morningsignoff.util;

/**
 * Created by Daniel on 8/2/2017.
 */

public class ProgressReactor {
    public enum State {START, END};
    private ProgressIndicator indicator;
    private ProgressIndicator.Type type;

    public ProgressReactor(ProgressIndicator indicator, ProgressIndicator.Type type) {
        this.indicator = indicator;
        this.type = type;
    }

    // State is either Start or End. Success is always false for Start.
    // Success is true for End if the network request for articles succeeded and false otherwise.
    public void reactToProgress(State state, boolean success) {
        switch (type) {
            case Loading:
                if (state.equals(State.START)) indicator.loadingStart();
                else indicator.loadingEnd(success);
                break;
            case Refresh:
                if (state.equals(State.START)) indicator.refreshStart();
                else indicator.refreshEnd(success);
                break;
            case LoadingMore:
                if (state.equals(State.START)) indicator.loadingMoreStart();
                else indicator.loadingMoreEnd();
                break;
        }
    }
}
