package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.node.Node;

import io.noties.markwon.inlineparser.InlineProcessor;

public class SpoilerOpeningParser extends InlineProcessor {
    @Override
    public char specialCharacter() {
        return '>';
    }

    @Nullable
    @Override
    protected Node parse() {
        index++;
        if (peek() == '!') {
            index++;
            SpoilerOpening node = new SpoilerOpening();
            node.setLiteral(">!");
            return node;
        }
        return null;
    }
}
