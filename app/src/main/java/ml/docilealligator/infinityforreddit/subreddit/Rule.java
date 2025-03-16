package ml.docilealligator.infinityforreddit.subreddit;

public class Rule {
    private final String shortName;
    private final String descriptionHtml;

    public Rule(String shortName, String descriptionHtml) {
        this.shortName = shortName;
        this.descriptionHtml = descriptionHtml;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }
}
