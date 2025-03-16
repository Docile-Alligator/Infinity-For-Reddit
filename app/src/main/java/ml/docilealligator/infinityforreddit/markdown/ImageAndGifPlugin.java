package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import java.util.Map;

import io.noties.markwon.AbstractMarkwonPlugin;
import ml.docilealligator.infinityforreddit.thing.MediaMetadata;

public class ImageAndGifPlugin extends AbstractMarkwonPlugin {

    private final ImageAndGifBlockParser.Factory factory;

    public ImageAndGifPlugin() {
        this.factory = new ImageAndGifBlockParser.Factory();
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

    public void setMediaMetadataMap(Map<String, MediaMetadata> mediaMetadataMap) {
        factory.setMediaMetadataMap(mediaMetadataMap);
    }
}
