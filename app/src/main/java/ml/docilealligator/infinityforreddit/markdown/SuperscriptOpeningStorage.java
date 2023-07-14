package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.internal.Delimiter;
import org.commonmark.node.Node;

public class SuperscriptOpeningStorage {
    @Nullable
    private SuperscriptOpeningBracket lastBracket;
    private Node currentBlock;

    public void clear() {
        lastBracket = null;
    }

    public void add(Node block, Node node, Delimiter lastDelimiter) {
        updateBlock(block);
        lastBracket = new SuperscriptOpeningBracket(node, lastBracket, lastDelimiter);
    }

    @Nullable
    public SuperscriptOpeningBracket pop(Node block) {
        updateBlock(block);
        SuperscriptOpeningBracket opening = lastBracket;
        if (opening != null) {
            lastBracket = opening.previous;
        }
        return opening;
    }

    private void updateBlock(Node block) {
        if (block != currentBlock) {
            clear();
        }
        currentBlock = block;
    }
}
