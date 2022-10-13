package ml.docilealligator.infinityforreddit.markdown;

import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.SpannableBuilder;
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

    private static SpannableBuilder.Span matchSpanAtPosition(List<SpannableBuilder.Span> list, int value, Class spanClass) {
        for (var span : list)
            if (span.what.getClass() == spanClass && span.start <= value && value <= span.end)
                return span;
        return null;
    }

    private static SpannableBuilder.Span matchNonTextSpanAtBoundary(List<SpannableBuilder.Span> list, int value) {
        for (var span : list)
            if (span.what.getClass() != TextViewSpan.class && (span.end == value || span.start == value))
                return span;
        return null;
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(Superscript.class, (config, renderProps) -> new SuperscriptSpan());
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(Superscript.class, new MarkwonVisitor.NodeVisitor<>() {
            int depth = 0;

            @Override
            public void visit(@NonNull MarkwonVisitor visitor, @NonNull Superscript superscript) {
                int start = visitor.length();

                if (!superscript.isBracketed()) {
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
                        for (CodeSpan span : codeSpans) {
                            if (spannableStringBuilder.getSpanEnd(span) <= end) {
                                visitor.builder().setSpan(new SuperscriptSpan(), start, spannableStringBuilder.getSpanStart(span));
                                visitor.builder().setSpan(new SuperscriptSpan(), spannableStringBuilder.getSpanEnd(span), end);
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
        var spannableBuilder = new SpannableBuilder(text);
        List<SpannableBuilder.Span> spans = spannableBuilder.getSpans(0, spannableBuilder.length());

        outerLoop:
        for (int i = 0; i < superscriptOpeningList.size(); i++) {
            SuperscriptOpening opening = superscriptOpeningList.get(i);
            SuperscriptOpening nextOpening = i + 1 < superscriptOpeningList.size() ? superscriptOpeningList.get(i + 1) : null;

            if (opening.start >= text.length() || (matchSpanAtPosition(spans, opening.start, CodeSpan.class) == null && Character.isWhitespace(text.charAt(opening.start))) || (nextOpening != null && opening.start.equals(nextOpening.start))) {
                superscriptOpeningList.remove(i);
                i--;
                continue;
            }

            int openingStart = opening.start;
            for (int j = opening.start; j <= text.length(); j++) {
                char currentChar = peek(j, text);
                SpannableBuilder.Span codeSpanAtPosition = matchSpanAtPosition(spans, j, CodeSpan.class);
                SpannableBuilder.Span nonTextSpanAtBoundary = matchNonTextSpanAtBoundary(spans, j);
                boolean isChildOfDelimited = !(opening.node.getParent() == null || opening.node.getParent() instanceof Paragraph);
                boolean isInsideDelimited = nonTextSpanAtBoundary != null && openingStart != j && j == nonTextSpanAtBoundary.end && (openingStart > nonTextSpanAtBoundary.start || isChildOfDelimited);
                if (codeSpanAtPosition != null) {
                    spannableBuilder.setSpan(new SuperscriptSpan(), openingStart, j, 0);
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
                    spannableBuilder.setSpan(new SuperscriptSpan(), openingStart, j, 0);
                    superscriptOpeningList.remove(i);
                    i--;
                    continue outerLoop;
                }
            }
        }

        textView.setText(spannableBuilder.text());
    }
}
