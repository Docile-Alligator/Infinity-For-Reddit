package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Visitor;

public class Superscript extends CustomNode {
    private String literal;
    private int level;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}
