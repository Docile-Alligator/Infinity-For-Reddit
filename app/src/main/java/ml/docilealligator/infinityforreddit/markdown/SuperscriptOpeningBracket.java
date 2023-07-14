package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.internal.Delimiter;
import org.commonmark.node.Node;

public class SuperscriptOpeningBracket {
    /**
     * Node that contains superscript opening bracket markdown ({@code ^(}).
     */
    public final Node node;

    /**
     * Previous superscript opening bracket.
     */
    public final SuperscriptOpeningBracket previous;

    /**
     * Previous delimiter (emphasis, etc) before this bracket.
     */
    public final Delimiter previousDelimiter;

    public final Integer start;

    public SuperscriptOpeningBracket(Node node, SuperscriptOpeningBracket previous, Delimiter previousDelimiter) {
        this.node = node;
        this.previous = previous;
        this.previousDelimiter = previousDelimiter;
        this.start = null;
    }
}

