package ml.docilealligator.infinityforreddit.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.AbstractMarkwonPlugin;

public class ProcessRedditSuperscript extends AbstractMarkwonPlugin {
    private static final Pattern re = Pattern.compile("(?:\\^\\((.+?)\\))|(?:\\^(\\S+))");

    @Override
    public String processMarkdown(String markdown) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher =  re.matcher(markdown);
        int start = 0;

        while (matcher.find()) {
            try {
                String match;
                if ((match = matcher.group(1)) != null || (match = matcher.group(2)) != null) {
                    builder.append(markdown.substring(start, matcher.start()))
                            .append("<sup>")
                            .append(match)
                            .append("</sup>");
                    start = matcher.end();
                } else {
                    throw new NullPointerException();
                }
            } catch(NullPointerException e) {
                e.printStackTrace();
            } finally {
                continue;
            }
        }

        if (start < markdown.length()) {
            builder.append(markdown.substring(start));
        }

        if(builder.length() > 0) {
            return builder.toString(); }
        else {
            return markdown;
        }
    }
}
