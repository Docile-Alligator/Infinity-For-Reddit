package ml.docilealligator.infinityforreddit.markdown;

import static io.noties.markwon.inlineparser.InlineParserUtils.mergeChildTextNodes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Node;

import io.noties.markwon.inlineparser.InlineProcessor;

public class SuperscriptClosingInlineProcessor extends InlineProcessor {
    @NonNull
    private final SuperscriptOpeningStorage superscriptOpeningStorage;

    public SuperscriptClosingInlineProcessor(@NonNull SuperscriptOpeningStorage superscriptOpeningStorage) {
        this.superscriptOpeningStorage = superscriptOpeningStorage;
    }

    @Override
    public char specialCharacter() {
        return ')';
    }

    @Nullable
    @Override
    protected Node parse() {
        SuperscriptOpeningBracket superscriptOpening = superscriptOpeningStorage.pop(block);
        if (superscriptOpening == null) {
            return null;
        }
        index++;

        Superscript superscript = new Superscript();
        superscript.setBracketed(true);
        Node node = superscriptOpening.node.getNext();
        while (node != null) {
            Node next = node.getNext();
            superscript.appendChild(node);
            node = next;
        }

        // Process delimiters such as emphasis inside spoiler
        processDelimiters(superscriptOpening.previousDelimiter);
        mergeChildTextNodes(superscript);
        // We don't need the corresponding text node anymore, we turned it into a spoiler node
        superscriptOpening.node.unlink();

        return superscript;
    }
}
