package app.morningsignout.com.morningsignoff;

import java.util.concurrent.ExecutionException;

/**
 * Created by liukwarm on 10/24/15.
 */
public class ExecutiveListItem {

    public String name;
    public String position;
    public String hyperlink;

    public ExecutiveListItem (String name, String position, String hyperlink) {
        this.name = name;
        this.position = position;
        this.hyperlink = hyperlink;
    }
}
