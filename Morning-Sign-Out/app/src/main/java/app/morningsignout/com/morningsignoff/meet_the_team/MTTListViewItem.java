package app.morningsignout.com.morningsignoff.meet_the_team;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liukwarm on 10/24/15.
 */
public class MTTListViewItem implements Parcelable {
    public String name;
    public String position;
    public String hyperlink;

    public MTTListViewItem(String name, String position, String hyperlink) {
        this.name = name;
        this.position = position;
        this.hyperlink = hyperlink;
    }

    private MTTListViewItem(Parcel in) {
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

    public static final Parcelable.Creator<MTTListViewItem> CREATOR
            = new Parcelable.Creator<MTTListViewItem>() {
        public MTTListViewItem createFromParcel(Parcel in) {
            return new MTTListViewItem(in);
        }

        public MTTListViewItem[] newArray(int size) {
            return new MTTListViewItem[size];
        }
    };
}
