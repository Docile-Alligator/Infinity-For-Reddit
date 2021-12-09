package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.node.Node;

import io.noties.markwon.inlineparser.InlineProcessor;

public class SuperscriptParser extends InlineProcessor {

    private int level = 0;

    @Override
    public char specialCharacter() {
        return '^';
    }

    @Nullable
    @Override
    protected Node parse() {
        Node node = parseSuperscript(level);
        if (node != null) {
            return nestSuperscriptNodes(node);
        }
        level = 0;
        return null;
    }

    private Node nestSuperscriptNodes(Node node) {
        if (block.getLastChild() instanceof Superscript
                && peek() != '^') {
            var current = block.getLastChild();
            current.appendChild(node);
            Node tmp = null;
            while (true) {
                tmp = current;
                current = current.getPrevious();
                if (current instanceof Superscript) {
                    current.appendChild(tmp);
                } else {
                    break;
                }
            }
            level = 0;
            return tmp;
        } else {
            level++;
            return node;
        }
    }

    // Hopefully we've handled edge cases
    private Superscript parseSuperscript(int level) {
        int start = index;
        int length = input.length();
        int caret_pos = -1;
        int nCarets = 0;
        int new_lines = 0;
        boolean hasBracket = false;
        for (int i = start; i < length; i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '\n') {
                new_lines++;
                if (new_lines > 0 && nCarets > 0 || hasBracket) {
                    break;
                }
            } else if ((i + 1) < length
                    && nCarets == 0
                    && !hasBracket
                    && !(i > 0 && input.charAt(i - 1) == '\\')
                    && currentChar == '^'
                    && !Character.isWhitespace(input.charAt(i + 1))) {
                if (input.charAt(i + 1) == '(') {
                    hasBracket = true;
                }
                caret_pos = i;
                nCarets++;
            } else if (nCarets > 0) {
                if (hasBracket
                        && (i > 0)
                        && currentChar == ')'
                        && input.charAt(i - 1) != '\\') {
                    index = i + 1;
                    Superscript node = new Superscript();
                    node.setLiteral(input.substring(caret_pos + 2, i));
                    node.setLevel(level);
                    return node;
                } else if (!hasBracket && Character.isWhitespace(currentChar)) {
                    index = i;
                    Superscript node = new Superscript();
                    node.setLiteral(input.substring(caret_pos + 1, i));
                    node.setLevel(level);
                    return node;
                } else if (!hasBracket && (i == length - 1)) {
                    index = i + 1;
                    Superscript node = new Superscript();
                    node.setLiteral(input.substring(caret_pos + 1, i + 1));
                    node.setLevel(level);
                    return node;
                } else if ((i + 1) < length
                        && (i > 0)
                        && currentChar == '^'
                        && input.charAt(i - 1) != '\\'
                        && !Character.isWhitespace(input.charAt(i + 1))) {
                    index = i;
                    Superscript node = new Superscript();
                    node.setLiteral(input.substring(caret_pos + 1, i));
                    node.setLevel(level);
                    return node;
                }
            }
        }
        return null;
    }
}

