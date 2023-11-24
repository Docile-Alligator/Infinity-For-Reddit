package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.internal.Bracket;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

import io.noties.markwon.inlineparser.InlineProcessor;

public class EmoteInlineProcessor extends InlineProcessor {
    @Override
    public char specialCharacter() {
        return '!';
    }

    @Override
    protected Node parse() {
        int startIndex = index;
        index++;
        if (peek() == '[') {
            index++;

            Text node = text("![");

            // Add entry to stack for this opener
            addBracket(Bracket.image(node, startIndex + 1, lastBracket(), lastDelimiter()));

            return node;
        } else {
            return null;
        }
    }
}
