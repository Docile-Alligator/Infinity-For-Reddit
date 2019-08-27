package ml.docilealligator.infinityforreddit;

class Utils {
    static String addSubredditAndUserLink(String markdown) {
        return markdown.replaceAll("(?<!\\w)/{0,1}u/\\w+/{0,1}", "[$0]($0)")
                .replaceAll("(?<!\\w)/{0,1}r/\\w+/{0,1}", "[$0]($0)");
    }
}
