package ml.docilealligator.infinityforreddit.markdown;

import static io.noties.markwon.inlineparser.InlineParserUtils.mergeChildTextNodes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Node;

import io.noties.markwon.inlineparser.InlineProcessor;

/**
 * Parses spoiler closing markdown ({@code !<}) and creates {@link SpoilerNode SpoilerNodes}.
 * Relies on {@link SpoilerOpeningInlineProcessor} to handle opening.
 *
 * Implementation inspired by {@link io.noties.markwon.inlineparser.CloseBracketInlineProcessor}
 */
public class SpoilerClosingInlineProcessor extends InlineProcessor {
    @NonNull
    private final SpoilerOpeningBracketStorage spoilerOpeningBracketStorage;

    public SpoilerClosingInlineProcessor(@NonNull SpoilerOpeningBracketStorage spoilerOpeningBracketStorage) {
        this.spoilerOpeningBracketStorage = spoilerOpeningBracketStorage;
    }

    @Override
    public char specialCharacter() {
        return '!';
    }

    @Nullable
    @Override
    protected Node parse() {
        index++;
        if (peek() != '<') {
            return null;
        }
        index++;

        SpoilerOpeningBracket spoilerOpeningBracket = spoilerOpeningBracketStorage.pop(block);
        if (spoilerOpeningBracket == null) {
            return null;
        }

        SpoilerNode spoilerNode = new SpoilerNode();
        Node node = spoilerOpeningBracket.node.getNext();
        while (node != null) {
            Node next = node.getNext();
            spoilerNode.appendChild(node);
            node = next;
        }

        // Process delimiters such as emphasis inside spoiler
        processDelimiters(spoilerOpeningBracket.previousDelimiter);
        mergeChildTextNodes(spoilerNode);
        // We don't need the corresponding text node anymore, we turned it into a spoiler node
        spoilerOpeningBracket.node.unlink();

        return spoilerNode;
    }
}
