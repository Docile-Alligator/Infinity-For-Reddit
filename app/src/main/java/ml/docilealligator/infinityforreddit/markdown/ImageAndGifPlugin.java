package ml.docilealligator.infinityforreddit.markdown;

import android.util.Log;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import java.util.Map;

import io.noties.markwon.AbstractMarkwonPlugin;
import ml.docilealligator.infinityforreddit.post.Post;

public class ImageAndGifPlugin extends AbstractMarkwonPlugin {

    private ImageAndGifBlockParser.Factory factory;

    public ImageAndGifPlugin() {
        this.factory = new ImageAndGifBlockParser.Factory();
    }

    @NonNull
    @Override
    public String processMarkdown(@NonNull String markdown) {
        Log.i("asdfa", "imageandgifplugin " + markdown + "ooooo");
        return super.processMarkdown(markdown);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(factory);
    }

    public void setMediaMetadataMap(Map<String, Post.MediaMetadata> mediaMetadataMap) {
        factory.setMediaMetadataMap(mediaMetadataMap);
    }
}
