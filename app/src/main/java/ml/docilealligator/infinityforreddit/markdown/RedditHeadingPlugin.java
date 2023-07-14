package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import io.noties.markwon.AbstractMarkwonPlugin;

/**
 * Replaces CommonMark heading parsing with Reddit-style parsing that does not require space after #
 */
public class RedditHeadingPlugin extends AbstractMarkwonPlugin {

    @NonNull
    public static RedditHeadingPlugin create() {
        return new RedditHeadingPlugin();
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(new RedditHeadingParser.Factory());
    }
}
