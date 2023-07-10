package ml.docilealligator.infinityforreddit.markdown;

import android.text.Spannable;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.core.spans.CodeSpan;
import io.noties.markwon.core.spans.TextViewSpan;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;

public class SuperscriptPlugin extends AbstractMarkwonPlugin {
    private final SuperscriptOpeningStorage superscriptOpeningBracketStorage;
    private final List<SuperscriptOpening> superscriptOpeningList;

    SuperscriptPlugin() {
        this.superscriptOpeningBracketStorage = new SuperscriptOpeningStorage();
        this.superscriptOpeningList = new ArrayList<>();
    }

    @NonNull
    public static SuperscriptPlugin create() {
        return new SuperscriptPlugin();
    }

    private static char peek(int index, CharSequence input) {
        return index >= 0 && index < input.length() ? input.charAt(index) : '\0';
    }

    private static List<SpanInfo> getSpans(Spannable spannable, int start, int end) {
        var spanArray = spannable.getSpans(start, end, Object.class);
        List<SpanInfo> spanList = new ArrayList<>();
        for (int i = spanArray.length - 1; i >= 0; i--) {
            Object span = spanArray[i];
            int spanStart = spannable.getSpanStart(span);
            int spanEnd = spannable.getSpanEnd(span);
            int spanFlags = spannable.getSpanFlags(span);
            spanList.add(new SpanInfo(span, spanStart, spanEnd, spanFlags));
        }
        return spanList;
    }

    private static SpanInfo matchSuperscriptAtPosition(List<SpanInfo> spans, int value) {
        for (var span : spans)
            if (span.what.getClass() == SuperscriptSpan.class && !((SuperscriptSpan) span.what).isBracketed && span.start <= value && value <= span.end)
                return span;
        return null;
    }

    private static SpanInfo matchSpanAtPosition(List<SpanInfo> spans, int value, Object spanClass) {
        for (var span : spans)
            if (span.what.getClass() == spanClass && span.start <= value && value <= span.end)
                return span;
        return null;
    }

    private static SpanInfo matchNonTextSpanAtBoundary(List<SpanInfo> spans, int value) {
        for (var span : spans)
            if ((span.end == value || span.start == value) && span.what.getClass() != CodeSpan.class && span.what.getClass() != SuperscriptSpan.class && span.what.getClass() != TextViewSpan.class)
                return span;
        return null;
    }

    @Override
    public void configure(@NonNull Registry registry) {
        registry.require(MarkwonInlineParserPlugin.class, plugin -> {
                    plugin.factoryBuilder().addInlineProcessor(new SuperscriptOpeningInlineProcessor(superscriptOpeningBracketStorage));
                    plugin.factoryBuilder().addInlineProcessor(new SuperscriptClosingInlineProcessor(superscriptOpeningBracketStorage));
                }
        );
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(Superscript.class, (config, renderProps) -> new SuperscriptSpan(true));
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(Superscript.class, new MarkwonVisitor.NodeVisitor<>() {
            int depth = 0;

            @Override
            public void visit(@NonNull MarkwonVisitor visitor, @NonNull Superscript superscript) {
                int start = visitor.length();

                if (!notEmptySuperscript(superscript)) {
                    return;
                }

                if (!superscript.isBracketed()) {
                    visitor.builder().setSpan(new SuperscriptSpan(false), start, start + 1); // Workaround for Table Plugin
                    superscriptOpeningList.add(new SuperscriptOpening(superscript, start));
                    return;
                }

                depth++;
                visitor.visitChildren(superscript);
                depth--;
                if (depth == 0) {
                    int end = visitor.builder().length();
                    var spans = visitor.builder().getSpans(start, end);
                    for (var span : spans) {
                        if (span.what instanceof CodeSpan) {
                            if (span.end <= end) {
                                visitor.builder().setSpan(new SuperscriptSpan(true), start, span.start);
                            }
                            start = span.end;
                        }
                    }
                    if (start < end) {
                        visitor.setSpansForNode(superscript, start);
                    }
                }
            }
        });
    }

    private boolean notEmptyLink(Link link) {
        Node next = link.getFirstChild();
        while (next != null) {
            if (next instanceof Text) {
                return true;
            } else if (next instanceof Superscript) {
                if (notEmptySuperscript((Superscript) next)) {
                    return true;
                }
            } else if (next instanceof Link) {
                if (notEmptyLink((Link) next)) {
                    return true;
                }
            } else if (next instanceof SpoilerNode) {
                if (notEmptySpoilerNode((SpoilerNode) next)) {
                    return true;
                }
            } else {
                return true;
            }
            next = next.getNext();
        }

        return false;
    }

    private boolean notEmptySpoilerNode(SpoilerNode spoilerNode) {
        Node next = spoilerNode.getFirstChild();
        while (next != null) {
            if (next instanceof Text) {
                return true;
            } else if (next instanceof Superscript) {
                if (notEmptySuperscript((Superscript) next)) {
                    return true;
                }
            } else if (next instanceof Link) {
                if (notEmptyLink((Link) next)) {
                    return true;
                }
            } else if (next instanceof SpoilerNode) {
                if (notEmptySpoilerNode((SpoilerNode) next)) {
                    return true;
                }
            } else {
                return true;
            }
            next = next.getNext();
        }

        return false;
    }

    private boolean notEmptySuperscript(Superscript superscript) {
        Node next;
        if (superscript.isBracketed()) {
            next = superscript.getFirstChild();
        } else {
            next = superscript.getNext();
        }

        while (next != null) {
            if (next instanceof Link) {
                if (notEmptyLink((Link) next)) {
                    return true;
                }
            } else if (next instanceof SpoilerNode) {
                if (notEmptySpoilerNode((SpoilerNode) next)) {
                    return true;
                }
            } else if (!(next instanceof Superscript)) {
                return true;
            } else {
                if (notEmptySuperscript((Superscript) next)) {
                    return true;
                }
            }
            next = next.getNext();
        }

        return false;
    }

    @Override
    public void afterRender(@NonNull Node node, @NonNull MarkwonVisitor visitor) {
        superscriptOpeningBracketStorage.clear();
    }

    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        if (superscriptOpeningList.size() == 0 || !(markdown instanceof Spannable)) {
            return;
        }

        var spannable = (Spannable) markdown;
        var spans = getSpans(spannable, 0, spannable.length());
        final String text = spannable.toString();

        outerLoop:
        for (int i = 0; i < superscriptOpeningList.size(); i++) {
            SuperscriptOpening opening = superscriptOpeningList.get(i);
            SuperscriptOpening nextOpening = i + 1 < superscriptOpeningList.size() ? superscriptOpeningList.get(i + 1) : null;

            // Workaround for Table Plugin
            var superscriptMarker = matchSuperscriptAtPosition(spans, opening.start);
            if (superscriptMarker == null)
                return;
            spannable.removeSpan(superscriptMarker.what);
            spans.remove(superscriptMarker);

            boolean isNextOpeningOfLocalNode = nextOpening != null && opening.node.getParent().equals(nextOpening.node.getParent());
            if (opening.start >= text.length() || (matchSpanAtPosition(spans, opening.start, CodeSpan.class) == null && Character.isWhitespace(text.charAt(opening.start))) || (isNextOpeningOfLocalNode && opening.start.equals(nextOpening.start))) {
                superscriptOpeningList.remove(i);
                i--;
                continue;
            }

            boolean isChildOfDelimited = !(opening.node.getParent() == null || opening.node.getParent() instanceof Paragraph || opening.node.getParent() instanceof TableCell);
            int openingStart = opening.start;
            for (int j = opening.start; j <= text.length(); j++) {
                char currentChar = peek(j, text);
                SpanInfo codeSpanAtPosition = matchSpanAtPosition(spans, j, CodeSpan.class);
                SpanInfo nonTextSpanAtBoundary = matchNonTextSpanAtBoundary(spans, j);
                // When we reach the end position of, for example, an Emphasis
                // Check whether the superscript originated from inside this Emphasis
                // If so, stop further spanning of the current Superscript
                boolean isInsideDelimited = nonTextSpanAtBoundary != null && openingStart != j && j == nonTextSpanAtBoundary.end && (openingStart > nonTextSpanAtBoundary.start || isChildOfDelimited);
                if (codeSpanAtPosition != null) {
                    if (openingStart < j) {
                        spannable.setSpan(new SuperscriptSpan(false), openingStart, j, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    //Skip to end of CodeSpan
                    j = codeSpanAtPosition.end;
                    currentChar = peek(j, text);
                    if (currentChar == '\0' || Character.isWhitespace(currentChar) || (isNextOpeningOfLocalNode && j == nextOpening.start) || isInsideDelimited) {
                        superscriptOpeningList.remove(i);
                        i--;
                        continue outerLoop;
                    }
                    openingStart = j;
                } else if (currentChar == '\0' || Character.isWhitespace(currentChar) || isInsideDelimited) {
                    spannable.setSpan(new SuperscriptSpan(false), openingStart, j, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    superscriptOpeningList.remove(i);
                    i--;
                    continue outerLoop;
                }
            }
        }
    }

    private static class SpanInfo {
        public final Object what;
        public final int start;
        public final int end;
        public final int flags;

        private SpanInfo(Object what, int start, int end, int flags) {
            this.what = what;
            this.start = start;
            this.end = end;
            this.flags = flags;
        }
    }
}
