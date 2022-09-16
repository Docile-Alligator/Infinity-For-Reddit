package ml.docilealligator.infinityforreddit.markdown;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.HtmlBlock;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.spans.CodeBlockSpan;
import io.noties.markwon.core.spans.CodeSpan;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;

public class SpoilerParserPlugin extends AbstractMarkwonPlugin {
    private final int textColor;
    private final int backgroundColor;
    private boolean textHasSpoiler = false;
    private int firstSpoilerStart = -1;

    SpoilerParserPlugin(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public static SpoilerParserPlugin create(int textColor, int backgroundColor) {
        return new SpoilerParserPlugin(textColor, backgroundColor);
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(SpoilerOpening.class, (visitor, opening) -> {
            textHasSpoiler = true;
            if (firstSpoilerStart == -1) {
                firstSpoilerStart = visitor.length();
            }
            visitor.builder().append(opening.getLiteral());
        });
    }

    @Override
    public void configure(@NonNull Registry registry) {
        registry.require(MarkwonInlineParserPlugin.class, plugin ->
                plugin.factoryBuilder().addInlineProcessor(new SpoilerOpeningParser())
        );
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(new BlockQuoteWithExceptionParser.Factory());

        Set<Class<? extends Block>> blocks = CorePlugin.enabledBlockTypes();
        blocks.remove(HtmlBlock.class);
        blocks.remove(BlockQuote.class);

        builder.enabledBlockTypes(blocks);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        textView.setHighlightColor(Color.TRANSPARENT);

        if (!textHasSpoiler || textView.getText().length() < 5) {
            firstSpoilerStart = 0;
            return;
        }

        SpannableStringBuilder markdownStringBuilder = new SpannableStringBuilder(textView.getText());

        ArrayList<SpoilerRange> spoilers = parse(markdownStringBuilder, firstSpoilerStart);
        firstSpoilerStart = 0;
        textHasSpoiler = false; // Since PostDetail can contain multiple TextViews, we do this here
        if (spoilers.size() == 0) {
            return;
        }

        // Process all the found spoilers. We always want to delete the brackets
        // because they are in matching pairs. But we want to apply SpoilerSpan
        // only to the outermost spoilers because nested spans break revealing-hiding
        int openingPosition = -1;
        ArrayList<SpoilerBracket> brackets = new ArrayList<>();
        for (SpoilerRange range : spoilers) {
            brackets.add(new SpoilerBracket(range.start, true, range.nested));
            brackets.add(new SpoilerBracket(range.end, false, range.nested));
        }
        //noinspection ComparatorCombinators as it requires api 24+
        Collections.sort(brackets, (lhs, rhs) -> Integer.compare(lhs.position, rhs.position));

        int offset = 2;
        for (SpoilerBracket bracket: brackets) {
            if (bracket.opening) {
                int spoilerStart = bracket.position - offset;
                if (!bracket.nested) {
                    openingPosition = spoilerStart;
                }
                markdownStringBuilder.delete(spoilerStart, spoilerStart + 2);
            } else {
                int spoilerEnd = bracket.position - offset + 2;
                markdownStringBuilder.delete(spoilerEnd, spoilerEnd + 2);
                if (!bracket.nested) {
                    SpoilerSpan spoilerSpan = new SpoilerSpan(textColor, backgroundColor);
                    markdownStringBuilder.setSpan(spoilerSpan, openingPosition, spoilerEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            offset += 2;
        }

        if (offset > 2) {
            textView.setText(markdownStringBuilder);
        }
    }

    private boolean noCodeIntersection(SpannableStringBuilder markdown, int position) {
        return markdown.getSpans(position, position + 2, CodeSpan.class).length == 0
                && markdown.getSpans(position, position + 2, CodeBlockSpan.class).length == 0;
    }

    /** Parse spoilers in the string starting from {@code start}.
     *
     * Returns all spoilers, spoilers that are nested inside other spoilers will have
     * {@code nested} set to {@code true}.
     * Doesn't allow more than one new line after every non-blank line.
     *
     * NB: could be optimized to reduce the number of calls to {@link #noCodeIntersection(SpannableStringBuilder, int)}
     */
    private ArrayList<SpoilerRange> parse(SpannableStringBuilder markdown, int start) {
        final int MAX_NEW_LINE = 1;
        int length = markdown.length();
        Stack<Integer> openSpoilerStack = new Stack<>();
        Stack<SpoilerRange> spoilersStack = new Stack<>();
        ArrayList<SpoilerRange> closedSpoilers = new ArrayList<>();
        int new_lines = 0;
        for (int i = start; i < length; i++) {
            char currentChar = markdown.charAt(i);
            if (currentChar == '\n') {
                new_lines++;
                if (new_lines > MAX_NEW_LINE) {
                    openSpoilerStack.clear();
                    new_lines = 0;
                }
            } else if ((currentChar != '>')
                    && (currentChar != '<')
                    && (currentChar != '!')) {
                new_lines = 0;
            } else if (currentChar == '>'
                    && i + 1 < length
                    && markdown.charAt(i + 1) == '!'
                    && noCodeIntersection(markdown, i)) {
                openSpoilerStack.push(i + 2);
                i++; // skip '!'
            } else if (openSpoilerStack.size() > 0
                    && currentChar == '!'
                    && i + 1 < length
                    && markdown.charAt(i + 1) == '<'
                    && noCodeIntersection(markdown, i)) {
                var pos = openSpoilerStack.pop();
                while (!spoilersStack.isEmpty()
                        && spoilersStack.peek().start > pos) {
                    SpoilerRange nestedRange = spoilersStack.pop();
                    nestedRange.nested = true;
                    closedSpoilers.add(nestedRange);
                }
                SpoilerRange range = new SpoilerRange(pos, i);
                spoilersStack.push(range);
                i++; // skip '<'
            } else {
                new_lines = 0;
            }
        }

        closedSpoilers.addAll(spoilersStack);
        return closedSpoilers;
    }

    private static class SpoilerBracket {
        final int position;
        final boolean opening;
        final boolean nested;


        private SpoilerBracket(int position, boolean opening, boolean nested) {
            this.position = position;
            this.opening = opening;
            this.nested = nested;
        }
    }

    private static class SpoilerRange {
        final int start;
        final int end;
        boolean nested;

        SpoilerRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
