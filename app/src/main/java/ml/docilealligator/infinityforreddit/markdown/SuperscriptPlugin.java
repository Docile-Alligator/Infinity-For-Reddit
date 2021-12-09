package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;

public class SuperscriptPlugin extends AbstractMarkwonPlugin {

    SuperscriptPlugin() {

    }

    public static SuperscriptPlugin create() {
        return new SuperscriptPlugin();
    }

    @Override
    public void configure(@NonNull Registry registry) {
        registry.require(MarkwonInlineParserPlugin.class, plugin ->
                plugin.factoryBuilder().addInlineProcessor(new SuperscriptParser()));
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(Superscript.class, (visitor, superscript) -> {
            if (superscript.getLevel() < 29) { // Arbitrary nesting limit
                final int start = visitor.length();
                visitor.builder().append(superscript.getLiteral());
                visitor.visitChildren(superscript);
                visitor.setSpans(start, new SuperScriptSpan());
            } else {
                visitor.clear();
            }
        });
    }
}
