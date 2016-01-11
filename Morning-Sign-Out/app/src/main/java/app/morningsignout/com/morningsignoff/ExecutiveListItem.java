package app.morningsignout.com.morningsignoff;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liukwarm on 10/24/15.
 */
public class ExecutiveListItem implements Parcelable {
    public String name;
    public String position;
    public String hyperlink;

    public ExecutiveListItem (String name, String position, String hyperlink) {
        this.name = name;
        this.position = position;
        this.hyperlink = hyperlink;
    }

    private ExecutiveListItem(Parcel in) {
        this.name = in.readString();
        this.position = in.readString();
        this.hyperlink = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(position);
        out.writeString(hyperlink);
    }

    public static final Parcelable.Creator<ExecutiveListItem> CREATOR
            = new Parcelable.Creator<ExecutiveListItem>() {
        public ExecutiveListItem createFromParcel(Parcel in) {
            return new ExecutiveListItem(in);
        }

        public ExecutiveListItem[] newArray(int size) {
            return new ExecutiveListItem[size];
        }
    };
}
