package ml.docilealligator.infinityforreddit;

class Utils {

  @SuppressWarnings("Annotator")
  static String addSubredditAndUserLink(String markdown) {
    return markdown.replaceAll("(?<!\\s)/{0,1}u/\\w+/{0,1}(?<!\\s)$", "[$0]($0)")
        .replaceAll("(?<!\\s)/{0,1}r/\\w+/{0,1}(?<!\\s)$", "[$0]($0)");
  }
}
