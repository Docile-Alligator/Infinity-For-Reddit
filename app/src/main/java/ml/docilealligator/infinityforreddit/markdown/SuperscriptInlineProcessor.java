package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.node.HtmlInline;
import org.commonmark.node.Node;

import java.util.regex.Pattern;

import io.noties.markwon.inlineparser.InlineProcessor;

public class SuperscriptInlineProcessor extends InlineProcessor {
    private static final Pattern HTML_TAG = Pattern.compile("^</?sup>", Pattern.CASE_INSENSITIVE);

    @Override
    public char specialCharacter() {
        return '<';
    }

    @Nullable
    @Override
    protected Node parse() {
        String m = match(HTML_TAG);
        if (m != null) {
            HtmlInline node = new HtmlInline();
            node.setLiteral(m);
            return node;
        } else {
            return null;
        }
    }
}
