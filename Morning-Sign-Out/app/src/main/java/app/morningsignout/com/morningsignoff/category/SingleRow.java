package app.morningsignout.com.morningsignoff.category;

import app.morningsignout.com.morningsignoff.article.Article;

// Defines the row item xml
public class SingleRow{
    public String title;
    public String description;
    public String link;
    public String imageURL;

    SingleRow(String title, String description, String link) {
        this.title = title;
        this.description = description;
        this.link = link;

        this.imageURL = null;
    }

    // For fetch list articles, imageURL is set to download imageViewReference later (fetch cat. imageViewReference task)
    SingleRow(String title, String description, String imageURL, String link) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.link = link;
    }

    static SingleRow newInstance(Article article) {
        String mDescription;
        if (article.getAuthor() == null) mDescription = "";
        else mDescription = article.getAuthor();

        return new SingleRow(article.getTitle(),
                mDescription,
                article.getImageURL(),
                article.getLink());
    }
}