package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Visitor;

class SpoilerOpening extends CustomNode {
    private String literal;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }
}
