package ml.docilealligator.infinityforreddit.markdown;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;

public class SpoilerParserPlugin extends AbstractMarkwonPlugin {
    private final int textColor;
    private final int backgroundColor;

    private final SpoilerOpeningBracketStorage spoilerOpeningBracketStorage = new SpoilerOpeningBracketStorage();

    SpoilerParserPlugin(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public static SpoilerParserPlugin create(int textColor, int backgroundColor) {
        return new SpoilerParserPlugin(textColor, backgroundColor);
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(SpoilerNode.class, (config, renderProps) ->
                new SpoilerSpan(textColor, backgroundColor));
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(SpoilerNode.class, new MarkwonVisitor.NodeVisitor<>() {
            int depth = 0;

            @Override
            public void visit(@NonNull MarkwonVisitor visitor, @NonNull SpoilerNode spoilerNode) {
                int start = visitor.length();
                depth++;
                visitor.visitChildren(spoilerNode);
                depth--;
                if (depth == 0) {
                    // don't add SpoilerSpans inside other SpoilerSpans
                    visitor.setSpansForNode(spoilerNode, start);
                }
            }
        });
    }

    @Override
    public void afterRender(@NonNull Node node, @NonNull MarkwonVisitor visitor) {
        spoilerOpeningBracketStorage.clear();
    }

    @Override
    public void configure(@NonNull Registry registry) {
        registry.require(MarkwonInlineParserPlugin.class, plugin -> {
                    plugin.factoryBuilder()
                            .addInlineProcessor(new SpoilerOpeningInlineProcessor(spoilerOpeningBracketStorage));
                    plugin.factoryBuilder()
                            .addInlineProcessor(new SpoilerClosingInlineProcessor(spoilerOpeningBracketStorage));
                }
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
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            Spanned spannedText = (Spanned) text;
            SpoilerSpan[] spans = spannedText.getSpans(0, text.length(), SpoilerSpan.class);
            if (spans.length == 0) {
                return;
            }

            // This is a workaround for Markwon's behavior.
            // Markwon adds spans in reversed order so SpoilerSpan is applied first
            // and other things (i.e. links, code, etc.) get drawn over it.
            // We fix it by removing all SpoilerSpans and adding them again
            // so they are applied last.
            List<SpanInfo> spanInfo = new ArrayList<>(spans.length);
            for (SpoilerSpan span : spans) {
                spanInfo.add(new SpanInfo(
                        span,
                        spannedText.getSpanStart(span),
                        spannedText.getSpanEnd(span),
                        spannedText.getSpanFlags(span)
                ));
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            for (SpanInfo info : spanInfo) {
                spannableStringBuilder.removeSpan(info.span);
                spannableStringBuilder.setSpan(info.span, info.start, info.end, info.flags);
            }
            textView.setText(spannableStringBuilder);
        }
    }

    private static class SpanInfo {
        public final SpoilerSpan span;
        public final int start;
        public final int end;
        public final int flags;

        private SpanInfo(SpoilerSpan span, int start, int end, int flags) {
            this.span = span;
            this.start = start;
            this.end = end;
            this.flags = flags;
        }
    }
}
