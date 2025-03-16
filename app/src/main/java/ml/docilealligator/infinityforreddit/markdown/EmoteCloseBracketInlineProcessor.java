package ml.docilealligator.infinityforreddit.markdown;

import static io.noties.markwon.inlineparser.InlineParserUtils.mergeChildTextNodes;

import androidx.annotation.Nullable;

import org.commonmark.internal.Bracket;
import org.commonmark.internal.util.Escaping;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Node;

import java.util.Map;
import java.util.regex.Pattern;

import io.noties.markwon.inlineparser.InlineProcessor;
import ml.docilealligator.infinityforreddit.thing.MediaMetadata;

public class EmoteCloseBracketInlineProcessor extends InlineProcessor {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    @Nullable
    private Map<String, MediaMetadata> mediaMetadataMap;

    @Override
    public char specialCharacter() {
        return ']';
    }

    @Override
    protected Node parse() {
        index++;
        int startIndex = index;

        // Get previous `[` or `![`
        Bracket opener = lastBracket();
        if (opener == null) {
            // No matching opener, just return a literal.
            return text("]");
        }

        if (!opener.allowed) {
            // Matching opener but it's not allowed, just return a literal.
            removeLastBracket();
            return text("]");
        }

        // Check to see if we have a link/image

        String dest = null;
        String title = null;
        boolean isLinkOrImage = false;

        // Maybe a inline link like `[foo](/uri "title")`
        if (peek() == '(') {
            index++;
            spnl();
            if ((dest = parseLinkDestination()) != null) {
                spnl();
                // title needs a whitespace before
                if (WHITESPACE.matcher(input.substring(index - 1, index)).matches()) {
                    title = parseLinkTitle();
                    spnl();
                }
                if (peek() == ')') {
                    index++;
                    isLinkOrImage = true;
                } else {
                    index = startIndex;
                }
            }
        }

        // Maybe a reference link like `[foo][bar]`, `[foo][]` or `[foo]`
        if (!isLinkOrImage) {

            // See if there's a link label like `[bar]` or `[]`
            int beforeLabel = index;
            parseLinkLabel();
            int labelLength = index - beforeLabel;
            String ref = null;
            if (labelLength > 2) {
                ref = input.substring(beforeLabel, beforeLabel + labelLength);
            } else if (!opener.bracketAfter) {
                // If the second label is empty `[foo][]` or missing `[foo]`, then the first label is the reference.
                // But it can only be a reference when there's no (unescaped) bracket in it.
                // If there is, we don't even need to try to look up the reference. This is an optimization.
                ref = input.substring(opener.index, startIndex);
            }

            if (ref != null) {
                String label = Escaping.normalizeReference(ref);
                LinkReferenceDefinition definition = context.getLinkReferenceDefinition(label);
                if (definition != null) {
                    dest = definition.getDestination();
                    title = definition.getTitle();
                    isLinkOrImage = true;
                }
            }
        }

        if (isLinkOrImage) {
            // If we got here, open is a potential opener
            Node linkOrImage;
            if (opener.image) {
                if (mediaMetadataMap == null) {
                    index = startIndex;
                    removeLastBracket();

                    return text("]");
                }
                MediaMetadata mediaMetadata = mediaMetadataMap.get(dest);
                if (mediaMetadata == null) {
                    index = startIndex;
                    removeLastBracket();

                    return text("]");
                }

                linkOrImage = new Emote(mediaMetadata, title);
            } else {
                linkOrImage = new Link(dest, title);
            }

            Node node = opener.node.getNext();
            while (node != null) {
                Node next = node.getNext();
                linkOrImage.appendChild(node);
                node = next;
            }

            // Process delimiters such as emphasis inside link/image
            processDelimiters(opener.previousDelimiter);
            mergeChildTextNodes(linkOrImage);
            // We don't need the corresponding text node anymore, we turned it into a link/image node
            opener.node.unlink();
            removeLastBracket();

            // Links within links are not allowed. We found this link, so there can be no other link around it.
            if (!opener.image) {
                Bracket bracket = lastBracket();
                while (bracket != null) {
                    if (!bracket.image) {
                        // Disallow link opener. It will still get matched, but will not result in a link.
                        bracket.allowed = false;
                    }
                    bracket = bracket.previous;
                }
            }

            return linkOrImage;

        } else { // no link or image
            index = startIndex;
            removeLastBracket();

            return text("]");
        }
    }

    public void setMediaMetadataMap(@Nullable Map<String, MediaMetadata> mediaMetadataMap) {
        this.mediaMetadataMap = mediaMetadataMap;
    }
}
