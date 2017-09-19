package app.morningsignout.com.morningsignoff.network;

/**
 * Created by Daniel on 9/16/2017.
 */

public class FetchError {
    public enum Error {DISCONNECTED, NO_RESULTS};

    public Error error;
    public String requestParam;
    public int pageNum;
}
