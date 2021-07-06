package ml.docilealligator.infinityforreddit;

public class RPANComment {
    public String author;
    public String authorIconImage;
    public String content;
    public long createdUTC;

    public RPANComment(String author, String authorIconImage, String content, long createdUTC) {
        this.author = author;
        this.authorIconImage = authorIconImage;
        this.content = content;
        this.createdUTC = createdUTC;
    }
}
