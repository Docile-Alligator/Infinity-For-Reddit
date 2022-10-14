package ml.docilealligator.infinityforreddit.markdown;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;

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

    private static List<SpanInfo> getSpans(SpannableStringBuilder spannableStringBuilder, int start, int end) {
        var spanArray = spannableStringBuilder.getSpans(start, end, Object.class);
        List<SpanInfo> spanList = new ArrayList<>();
        for (Object span : spanArray) {
            int spanStart = spannableStringBuilder.getSpanStart(span);
            int spanEnd = spannableStringBuilder.getSpanEnd(span);
            int spanFlags = spannableStringBuilder.getSpanFlags(span);
            spanList.add(new SpanInfo(span, spanStart, spanEnd, spanFlags));
        }
        return spanList;
    }

    private static SpanInfo matchSpanAtPosition(List<SpanInfo> spans, int value, Object spanClass) {
        for (var span : spans)
            if (span.what.getClass() == spanClass && span.start <= value && value <= span.end)
                return span;
        return null;
    }

    private static SpanInfo matchNonTextSpanAtBoundary(List<SpanInfo> spans, int value) {
        for (var span : spans)
            if (span.what.getClass() != TextViewSpan.class && (span.end == value || span.start == value))
                return span;
        return null;
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
                    var spannableStringBuilder = visitor.builder().spannableStringBuilder();
                    var codeSpans = spannableStringBuilder.getSpans(start, end, CodeSpan.class);
                    if (codeSpans.length > 0) {
                        for (int i = codeSpans.length - 1; i >= 0; i--) {
                            var span = codeSpans[i];
                            var spanStart = spannableStringBuilder.getSpanStart(span);
                            var spanEnd = spannableStringBuilder.getSpanEnd(span);
                            var nextSpan = i - 1 >= 0 ? codeSpans[i - 1] : null;
                            var nextSpanStart = nextSpan == null ? null : spannableStringBuilder.getSpanStart(nextSpan);
                            if (spanEnd <= end) {
                                visitor.builder().setSpan(new SuperscriptSpan(true), start, spanStart);
                                visitor.builder().setSpan(new SuperscriptSpan(true), spanEnd, nextSpanStart == null ? end : nextSpanStart);
                                start = spanEnd;
                            }
                        }
                    } else {
                        visitor.setSpansForNode(superscript, start);
                    }
                }
            }
        });
    }

    @Override
    public void afterRender(@NonNull Node node, @NonNull MarkwonVisitor visitor) {
        superscriptOpeningBracketStorage.clear();
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
    public void afterSetText(@NonNull TextView textView) {
        if (superscriptOpeningList.size() == 0) {
            return;
        }

        CharSequence text = textView.getText();
        var spannableStringBuilder = new SpannableStringBuilder(text);
        var spans = getSpans(spannableStringBuilder, 0, spannableStringBuilder.length());

        outerLoop:
        for (int i = 0; i < superscriptOpeningList.size(); i++) {
            SuperscriptOpening opening = superscriptOpeningList.get(i);
            SuperscriptOpening nextOpening = i + 1 < superscriptOpeningList.size() ? superscriptOpeningList.get(i + 1) : null;

            // Workaround for Table Plugin
            var superscriptMarker = matchSpanAtPosition(spans, opening.start, SuperscriptSpan.class);
            if (superscriptMarker == null || ((SuperscriptSpan) superscriptMarker.what).isBracketed)
                return;
            spannableStringBuilder.removeSpan(superscriptMarker.what);
            spans.remove(superscriptMarker);

            if (opening.start >= text.length() || (matchSpanAtPosition(spans, opening.start, CodeSpan.class) == null && Character.isWhitespace(text.charAt(opening.start))) || (nextOpening != null && opening.start.equals(nextOpening.start))) {
                superscriptOpeningList.remove(i);
                i--;
                continue;
            }

            int openingStart = opening.start;
            for (int j = opening.start; j <= text.length(); j++) {
                char currentChar = peek(j, text);
                SpanInfo codeSpanAtPosition = matchSpanAtPosition(spans, j, CodeSpan.class);
                SpanInfo nonTextSpanAtBoundary = matchNonTextSpanAtBoundary(spans, j);
                boolean isChildOfDelimited = !(opening.node.getParent() == null || opening.node.getParent() instanceof Paragraph || opening.node.getParent() instanceof TableCell);
                boolean isInsideDelimited = nonTextSpanAtBoundary != null && openingStart != j && j == nonTextSpanAtBoundary.end && (openingStart > nonTextSpanAtBoundary.start || isChildOfDelimited);
                if (codeSpanAtPosition != null) {
                    spannableStringBuilder.setSpan(new SuperscriptSpan(), openingStart, j, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //Skip to end of CodeSpan
                    j = codeSpanAtPosition.end;
                    currentChar = peek(j, text);
                    if (currentChar == '\0' || Character.isWhitespace(currentChar) || (nextOpening != null && j == nextOpening.start)) {
                        superscriptOpeningList.remove(i);
                        i--;
                        continue outerLoop;
                    }
                    openingStart = j;
                } else if (currentChar == '\0' || Character.isWhitespace(currentChar) || (nextOpening != null && j == nextOpening.start) || isInsideDelimited) {
                    spannableStringBuilder.setSpan(new SuperscriptSpan(), openingStart, j, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    superscriptOpeningList.remove(i);
                    i--;
                    continue outerLoop;
                }
            }
        }

        textView.setText(spannableStringBuilder);
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
