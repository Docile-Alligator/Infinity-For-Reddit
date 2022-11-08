package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.internal.Delimiter;
import org.commonmark.node.Node;

public class SpoilerOpeningBracketStorage {
    @Nullable
    private SpoilerOpeningBracket lastBracket;
    private Node currentBlock;

    public void clear() {
        lastBracket = null;
    }

    public void add(Node block, Node node, Delimiter lastDelimiter) {
        updateBlock(block);
        lastBracket = new SpoilerOpeningBracket(node, lastBracket, lastDelimiter);
    }

    @Nullable
    public SpoilerOpeningBracket pop(Node block) {
        updateBlock(block);
        SpoilerOpeningBracket bracket = lastBracket;
        if (bracket != null) {
            lastBracket = bracket.previous;
        }
        return bracket;
    }

    private void updateBlock(Node block) {
        if (block != currentBlock) {
            clear();
        }
        currentBlock = block;
    }
}
