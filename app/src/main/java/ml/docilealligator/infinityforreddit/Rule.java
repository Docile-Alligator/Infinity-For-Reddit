package ml.docilealligator.infinityforreddit;

public class Rule {
    private String shortName;
    private String descriptionHtml;

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
