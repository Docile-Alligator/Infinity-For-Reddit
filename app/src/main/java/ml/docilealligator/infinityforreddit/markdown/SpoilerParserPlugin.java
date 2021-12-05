package ml.docilealligator.infinityforreddit.markdown;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Pair;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.spans.CodeBlockSpan;
import io.noties.markwon.core.spans.CodeSpan;

public class SpoilerParserPlugin extends AbstractMarkwonPlugin {
    private final int textColor;
    private final int backgroundColor;

    SpoilerParserPlugin(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public static SpoilerParserPlugin create(@NonNull int textColor, @NonNull int backgroundColor) {
        return new SpoilerParserPlugin(textColor, backgroundColor);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(new BlockQuoteWithExceptionParser.Factory());

        Set<Class<? extends Block>> blocks = CorePlugin.enabledBlockTypes();
        blocks.remove(HtmlBlock.class);
        blocks.remove(HtmlInline.class);
        blocks.remove(BlockQuote.class);

        builder.enabledBlockTypes(blocks);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        textView.setHighlightColor(Color.TRANSPARENT);

        if (textView.getText().length() < 5) {
            return;
        }

        SpannableStringBuilder markdownStringBuilder = new SpannableStringBuilder(textView.getText());

        ArrayList<Pair<Integer, Integer>> spoilers = parse(markdownStringBuilder);
        if (spoilers.size() == 0) {
            return;
        }

        int offset = 2;
        for (Pair<Integer, Integer> spoiler : spoilers) {
            int spoilerStart = spoiler.first - offset;
            int spoilerEnd = spoiler.second - offset;

            // Try not to set a spoiler span if it's inside a CodeSpan
            CodeSpan[] codeSpans = markdownStringBuilder.getSpans(spoilerStart, spoilerEnd, CodeSpan.class);
            CodeBlockSpan[] codeBlockSpans = markdownStringBuilder.getSpans(spoilerStart, spoilerEnd, CodeBlockSpan.class);

            if (codeSpans.length == 0 && codeBlockSpans.length == 0) {
                markdownStringBuilder.delete(spoilerStart, spoilerStart + 2);
                markdownStringBuilder.delete(spoilerEnd, spoilerEnd + 2);
                SpoilerSpan spoilerSpan = new SpoilerSpan(textColor, backgroundColor);
                markdownStringBuilder.setSpan(spoilerSpan, spoilerStart, spoilerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                offset += 4;
                continue;
            }

            for (CodeSpan codeSpan : codeSpans) {
                int spanBeginning = markdownStringBuilder.getSpanStart(codeSpan);
                int spanEnd = markdownStringBuilder.getSpanEnd(codeSpan);
                if (spoilerStart + 2 <= spanBeginning && spanEnd <= spoilerEnd + 2) {
                    markdownStringBuilder.delete(spoilerStart, spoilerStart + 2);
                    markdownStringBuilder.delete(spoilerEnd, spoilerEnd + 2);
                    SpoilerSpan spoilerSpan = new SpoilerSpan(textColor, backgroundColor);
                    markdownStringBuilder.setSpan(spoilerSpan, spoilerStart, spoilerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    offset += 4;
                } else {
                    break;
                }
            }

            for (CodeBlockSpan codeBlockSpan : codeBlockSpans) {
                int spanBeginning = markdownStringBuilder.getSpanStart(codeBlockSpan);
                int spanEnd = markdownStringBuilder.getSpanEnd(codeBlockSpan);
                if (spoilerStart + 2 <= spanBeginning && spanEnd <= spoilerEnd + 2) {
                    markdownStringBuilder.delete(spoilerStart, spoilerStart + 2);
                    markdownStringBuilder.delete(spoilerEnd, spoilerEnd + 2);
                    SpoilerSpan spoilerSpan = new SpoilerSpan(textColor, backgroundColor);
                    markdownStringBuilder.setSpan(spoilerSpan, spoilerStart, spoilerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    offset += 4;
                } else {
                    break;
                }
            }
        }
        if (offset > 2) {
            textView.setText(markdownStringBuilder);
        }
    }

    // Very naive implementation, needs to be improved for efficiency and edge cases
    // Don't allow more than one new line after every non-blank line
    // Try not to care about recursing spoilers, we just want the outermost spoiler because
    // spoiler revealing-hiding breaks with recursing spoilers
    private ArrayList<Pair<Integer, Integer>> parse(SpannableStringBuilder markdown) {
        final int MAX_NEW_LINE = 1;
        int length = markdown.length();
        Stack<Integer> openSpoilerStack = new Stack<>();
        ArrayList<Pair<Integer, Integer>> closedSpoilers = new ArrayList<>();
        int new_lines = 0;
        for (int i = 0; i < length; i++) {
            if (markdown.charAt(i) == '\n') {
                new_lines++;
                if (openSpoilerStack.size() >= 1 && new_lines > MAX_NEW_LINE) {
                    openSpoilerStack.clear();
                    new_lines = 0;
                }
                continue;
            }
            if ((markdown.charAt(i) != '>') && (markdown.charAt(i) != '<') && (markdown.charAt(i) != '!')) {
                continue;
            }
            if ((i + 1 < length)
                    && markdown.charAt(i) == '>'
                    && markdown.charAt(i + 1) == '!') {
                openSpoilerStack.push(i + 2);
                continue;
            }
            if ((i + 1 < length) && (i - 1 >= 0)
                    && openSpoilerStack.size() > 0
                    && markdown.charAt(i - 1) != '>'
                    && markdown.charAt(i) == '!'
                    && markdown.charAt(i + 1) == '<') {
                var pos = openSpoilerStack.pop();
                if (!closedSpoilers.isEmpty()
                        && closedSpoilers.get(closedSpoilers.size() - 1).first > pos
                        && closedSpoilers.get(closedSpoilers.size() - 1).second < i) {
                    closedSpoilers.remove(closedSpoilers.size() - 1);
                }
                closedSpoilers.add(Pair.create(pos, i));
            }
        }
        return closedSpoilers;
    }
}
