package app.morningsignout.com.morningsignoff.meet_the_team;

/**
 * Created by maniknarang on 5/25/17.
 */

public class MeetTheTeamAuthor {
    private String name;
    private String desc;
    private String slug;

    public MeetTheTeamAuthor(String name, String desc, String slug) {
        this.name = name;
        this.desc= desc;
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSlug() {
        return slug;
    }

}
