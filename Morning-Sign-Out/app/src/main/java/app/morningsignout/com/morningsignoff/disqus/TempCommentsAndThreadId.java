package app.morningsignout.com.morningsignoff.disqus;

import java.util.ArrayList;

/* Pair codes to indicate different situations with getComments()
 * 0 - Comments and ID return successfully
 * 1 - ID does not exist through getMsoThreadId()
 * 2 - Network issue with getMsoThreadId()
 * 3 - ID succeeded but network issue with getCommentsJson()
 */
public class TempCommentsAndThreadId {
    ArrayList<Comments> comments;
    String dsq_thread_id;
    int code;

    public TempCommentsAndThreadId(ArrayList<Comments> comments, String dsq_thread_id, int code) {
        this.comments = comments;
        this.dsq_thread_id = dsq_thread_id;
        this.code = code;
    }
}
