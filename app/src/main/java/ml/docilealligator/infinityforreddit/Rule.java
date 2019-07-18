package ml.docilealligator.infinityforreddit;

class Rule {
    private String shortName;
    private String descriptionHtml;

    Rule(String shortName, String descriptionHtml) {
        this.shortName = shortName;
        this.descriptionHtml = descriptionHtml;
    }

    String getShortName() {
        return shortName;
    }

    String getDescriptionHtml() {
        return descriptionHtml;
    }
}
