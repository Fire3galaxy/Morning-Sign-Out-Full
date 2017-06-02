package app.morningsignout.com.morningsignoff.article;

import java.util.HashSet;
import java.util.Set;

public class Article {
    private String title;
    private String link;
    private String imageURL;
    private String mediumURL;
    private String author;
    private String content;
    private String excerpt;
    private Set<String> tags = new HashSet<String>();

    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // If we decide not to use medium url, we can change it here
    public String getCategoryURL() {
        return mediumURL;
    }
    public void setCategoryURL(String mediumURL) {
        this.mediumURL = mediumURL;
    }

    public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public Set<String> getTags() { return tags; }
    public void setTags(String tag) { this.tags.add(tag); }
}
