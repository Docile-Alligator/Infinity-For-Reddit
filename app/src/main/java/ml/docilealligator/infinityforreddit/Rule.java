package ml.docilealligator.infinityforreddit;

class Rule {

  private final String shortName;
  private final String descriptionHtml;

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
