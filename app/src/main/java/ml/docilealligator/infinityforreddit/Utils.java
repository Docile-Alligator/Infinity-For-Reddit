package ml.docilealligator.infinityforreddit;

class Utils {
    static String addSubredditAndUserLink(String markdown) {
        return markdown.replaceAll("(?<!\\w)/*u/\\w+/*", "[$0]($0)")
                .replaceAll("(?<!\\w)/*r/\\w+/*", "[$0]($0)");
    }
}
