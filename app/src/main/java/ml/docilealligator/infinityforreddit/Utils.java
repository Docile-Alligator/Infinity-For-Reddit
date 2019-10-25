package ml.docilealligator.infinityforreddit;

public class Utils {
    public static String addSubredditAndUserLink(String markdown) {
        return markdown.replaceAll("\n", "  ")
                .replaceAll("((?<=[\\s])|^)/{0,1}[ru]/\\w+/{0,1}", "[$0]($0)");
    }
}
