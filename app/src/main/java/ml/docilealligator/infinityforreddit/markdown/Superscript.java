package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Visitor;

public class Superscript extends CustomNode {
    private boolean isBracketed;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public boolean isBracketed() {
        return isBracketed;
    }

    public void setBracketed(boolean bracketed) {
        isBracketed = bracketed;
    }
}
