package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Node;
import org.commonmark.node.Text;

import io.noties.markwon.inlineparser.InlineProcessor;

public class SuperscriptOpeningInlineProcessor extends InlineProcessor {
    @NonNull
    private final SuperscriptOpeningStorage superscriptOpeningStorage;

    public SuperscriptOpeningInlineProcessor(@NonNull SuperscriptOpeningStorage superscriptOpeningStorage) {
        this.superscriptOpeningStorage = superscriptOpeningStorage;
    }

    @Override
    public char specialCharacter() {
        return '^';
    }

    @Nullable
    @Override
    protected Node parse() {
        index++;
        char c = peek();
        if (c != '\0' && !Character.isWhitespace(c)) {
            if (c == '(') {
                index++;
                Text node = text("^(");
                superscriptOpeningStorage.add(block, node, lastDelimiter());
                return node;
            }

            if (lastDelimiter() != null && lastDelimiter().canOpen && block.getLastChild() != null) {
                if (lastDelimiter().node == this.block.getLastChild()) {
                    if (lastDelimiter().delimiterChar == peek()) {
                        index--;
                        return null;
                    }
                }
            }

            return new Superscript();
        }
        return null;
    }
}
