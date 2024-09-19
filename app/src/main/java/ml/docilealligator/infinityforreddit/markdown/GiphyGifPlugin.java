package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.parser.Parser;

import io.noties.markwon.AbstractMarkwonPlugin;
import ml.docilealligator.infinityforreddit.thing.GiphyGif;

public class GiphyGifPlugin extends AbstractMarkwonPlugin {
    private final GiphyGifBlockParser.Factory factory;

    public GiphyGifPlugin(@Nullable GiphyGif giphyGif) {
        this.factory = new GiphyGifBlockParser.Factory(giphyGif);
    }

    @NonNull
    @Override
    public String processMarkdown(@NonNull String markdown) {
        return super.processMarkdown(markdown);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(factory);
    }
}
