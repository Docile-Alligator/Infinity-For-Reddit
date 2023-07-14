package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Node;
import org.commonmark.node.Text;

import io.noties.markwon.inlineparser.InlineProcessor;

/**
 * Parses spoiler opening markdown ({@code >!}). Relies on {@link SpoilerClosingInlineProcessor}
 * to handle closing and create {@link SpoilerNode SpoilerNodes}.
 */
public class SpoilerOpeningInlineProcessor extends InlineProcessor {
    @NonNull
    private final SpoilerOpeningBracketStorage spoilerOpeningBracketStorage;

    public SpoilerOpeningInlineProcessor(@NonNull SpoilerOpeningBracketStorage spoilerOpeningBracketStorage) {
        this.spoilerOpeningBracketStorage = spoilerOpeningBracketStorage;
    }

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
            Text node = text(">!");
            spoilerOpeningBracketStorage.add(block, node, lastDelimiter());
            return node;
        }
        return null;
    }
}
