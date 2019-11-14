package ml.docilealligator.infinityforreddit.Utils;

public class Utils {
    public static String addSubredditAndUserLink(String markdown) {
        return markdown.replaceAll("((?<=[\\s])|^)/{0,1}[rRuU]/\\w+/{0,1}", "[$0]($0)");
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.subSequence(0, i+1);
    }
}
