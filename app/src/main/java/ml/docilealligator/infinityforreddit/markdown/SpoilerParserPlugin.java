package ml.docilealligator.infinityforreddit.markdown;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.parser.Parser;

import java.util.LinkedHashMap;
import java.util.Map;
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

        if(textView.getText().length() < 5) {
            return;
        }

        SpannableStringBuilder markdownStringBuilder = new SpannableStringBuilder(textView.getText());

        LinkedHashMap<Integer, Integer> spoilers = parse(markdownStringBuilder);
        if(spoilers.size() == 0) {
            return;
        }
        int offset = 2;

        for (Map.Entry<Integer, Integer> entry : spoilers.entrySet()) {
            int spoilerStart = entry.getKey() - offset;
            int spoilerEnd = entry.getValue() - offset;

            // Try not to set a spoiler span if it's inside a CodeSpan
            CodeSpan[] codeSpans = markdownStringBuilder.getSpans(spoilerStart, spoilerEnd, CodeSpan.class);
            CodeBlockSpan[] codeBlockSpans = markdownStringBuilder.getSpans(spoilerStart, spoilerEnd, CodeBlockSpan.class);

            if (codeSpans.length == 0 && codeBlockSpans.length == 0) {
                markdownStringBuilder.delete(spoilerStart, spoilerStart + 2);
                markdownStringBuilder.delete(spoilerEnd, spoilerEnd + 2);
                SpoilerSpan spoilerSpan = new SpoilerSpan(textColor, backgroundColor);
                markdownStringBuilder.setSpan(spoilerSpan, spoilerStart, spoilerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                offset += 4;
            }

            for (CodeSpan codeSpan : codeSpans) {
                int spanBeginning = markdownStringBuilder.getSpanStart(codeSpan);
                int spanEnd = markdownStringBuilder.getSpanEnd(codeSpan);
                if (spoilerStart < spanBeginning && spanEnd < spoilerEnd) {
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
                if (spoilerStart < spanBeginning && spanEnd < spoilerEnd) {
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
        if(offset > 2) {
            textView.setText(markdownStringBuilder);
        }
    }

    // Very naive implementation, needs to be improved for efficiency and edge cases
    // Don't allow more than one new line after every non-blank line
    // Try not to care about recursing spoilers, we just want the outermost spoiler because
    // spoiler revealing-hiding breaks with recursing spoilers
    private LinkedHashMap<Integer, Integer> parse(SpannableStringBuilder markdown) {
        final int MAX_NEW_LINE = 1;
        var openSpoilerStack = new Stack<Integer>();
        var closedSpoilerMap = new LinkedHashMap<Integer, Integer>();
        int variable_max_depth = calculateBalance(0, markdown) + 1;
        int new_lines = 0;
        int depth = 0;
        for (int i = 0; i < markdown.length(); i++) {
            if (markdown.charAt(i) == '\u2000' || markdown.charAt(i) == '\t') {
                continue;
            } else if (markdown.charAt(i) == '>' && (i + 1) < markdown.length() && markdown.charAt(i + 1) == '!') {
                openSpoilerStack.push(i + 1);
                depth++;
            } else if (openSpoilerStack.size() > 0
                    && markdown.charAt(i) == '!' && (i + 1) < markdown.length()
                    && markdown.charAt(i + 1) == '<') {
                var pos = i + 1;
                for (int j = 0; j < depth; j++) {
                    if (!openSpoilerStack.isEmpty()) pos = openSpoilerStack.peek();
                    if (pos + 1 <= i) {
                        if (!openSpoilerStack.isEmpty()) pos = openSpoilerStack.peek();
                        break;
                    } else {
                        if (!openSpoilerStack.isEmpty()) pos = openSpoilerStack.pop();
                    }
                }
                if (depth <= variable_max_depth && pos + 1 <= i) //Spoiler content cannot be zero or less length
                {
                    openSpoilerStack.clear();
                    closedSpoilerMap.put(pos + 1, i);
                }
                depth--;
            } else if (markdown.charAt(i) == '\n') {
                new_lines++;
                if (openSpoilerStack.size() >= 1 && new_lines > MAX_NEW_LINE) {
                    openSpoilerStack.clear();
                    new_lines = 0;
                    depth = 0;
                    variable_max_depth = calculateBalance(i, markdown) + 1;
                }
            } else {
                new_lines = 0;
            }

            if (openSpoilerStack.size() >= 32) // No
            {
                openSpoilerStack.clear();
                closedSpoilerMap.clear();
                continue;
            }
        }
        return closedSpoilerMap;
    }

    private int calculateBalance(int index, SpannableStringBuilder line) {
        final int MAX_NEW_LINE = 1;
        int new_lines = 0;
        int opening = 0;
        int closing = 0;
        for (int i = index; i < line.length(); i++) {
            if (line.charAt(i) == '\u0020' || line.charAt(i) == '\t') {
                continue;
            } else if (line.charAt(i) == '>'
                    && (i + 1) < line.length()
                    && line.charAt(i + 1) == '!') {
                opening++;
            } else if (line.charAt(i) == '!' && (i + 1) < line.length()
                    && line.charAt(i + 1) == '<') {
                closing++;
            } else if (line.charAt(i) == '\n') {
                new_lines++;
                if (new_lines > MAX_NEW_LINE) {
                    break;
                }
            } else {
                new_lines = 0;
                continue;
            }
        }
        return Math.abs(opening - closing);
    }

}
