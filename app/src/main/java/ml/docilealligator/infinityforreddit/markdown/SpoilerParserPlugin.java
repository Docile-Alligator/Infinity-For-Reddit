package ml.docilealligator.infinityforreddit.markdown;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.AbstractMarkwonPlugin;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class SpoilerParserPlugin extends AbstractMarkwonPlugin {
    private final int textColor;
    private final int backgroundColor;

    SpoilerParserPlugin(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public static SpoilerParserPlugin create(@NonNull int textColor, @NonNull int backgroundColor) {
        return new SpoilerParserPlugin(textColor, backgroundColor);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        textView.setHighlightColor(Color.TRANSPARENT);
        SpannableStringBuilder markdownStringBuilder = new SpannableStringBuilder(textView.getText());
        Pattern spoilerPattern = Pattern.compile(">!(\\n?(?:.+?(?:\\n?.+?)?)\\n?)!<");
        Matcher matcher = spoilerPattern.matcher(markdownStringBuilder);
        int start = 0;
        boolean find = false;
        while (matcher.find(start)) {
            if (markdownStringBuilder.length() < 4
                    || matcher.start() < 0
                    || matcher.end() > markdownStringBuilder.length()) {
                break;
            }
            find = true;
            markdownStringBuilder.delete(matcher.end() - 2, matcher.end());
            markdownStringBuilder.delete(matcher.start(), matcher.start() + 2);
            SpoilerSpan spoilerSpan = new SpoilerSpan(textColor, backgroundColor);
            markdownStringBuilder.setSpan(spoilerSpan, matcher.start(), matcher.end() - 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = matcher.end() - 4;
        }
        /*Pattern escapedSpoilerOpenerPattern = Pattern.compile("&gt;!");
        Matcher escapedSpoilerOpenerMatcher = escapedSpoilerOpenerPattern.matcher(markdownStringBuilder);
        ArrayList<Integer> matched = new ArrayList<>();
        while (escapedSpoilerOpenerMatcher.find()) {
            matched.add(escapedSpoilerOpenerMatcher.start());
            find = true;
        }
        for (int i = matched.size() - 1; i >= 0; i--) {
            markdownStringBuilder.replace(matched.get(i), matched.get(i) + 4, ">");
        }*/
        if (find) {
            textView.setText(markdownStringBuilder);
        }
    }

}
