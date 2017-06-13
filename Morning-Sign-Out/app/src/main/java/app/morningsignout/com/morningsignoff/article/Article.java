package app.morningsignout.com.morningsignoff.article;

public class Article {
    private String title;
    private String link;
    private String imageURL;
    private String mediumURL;
    private String author;
    private String content;
    private String dsq_thread_id;

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

    public String getDsqThreadId() {
        return dsq_thread_id;
    }

    public void setDsqThreadId(String dsq_thread_id) {
        this.dsq_thread_id = dsq_thread_id;
    }

    // From the link
    public String getSlug() {
        if (link == null)
            return null;

        // 26 -> Length of "http://morningsignout.com/", which is constant
        // length - 1 -> remove an extra '/' at the end
        return link.substring(26, link.length() - 1);
    }
}
