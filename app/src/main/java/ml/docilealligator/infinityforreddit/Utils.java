package ml.docilealligator.infinityforreddit;

public class Utils {
    public static String addSubredditAndUserLink(String markdown) {
        return markdown.replaceAll("(?<!\\s)/{0,1}u/\\w+/{0,1}(?<!\\s)$", "[$0]($0)")
                .replaceAll("(?<!\\s)/{0,1}r/\\w+/{0,1}(?<!\\s)$", "[$0]($0)");
    }
}
