package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.Node;

public class SuperscriptOpening {
    /**
     * Node that contains non-bracketed superscript opening markdown ({@code ^}).
     */
    public final Node node;

    public final Integer start;

    public SuperscriptOpening(Node node, int start) {
        this.node = node;
        this.start = start;
    }
}
