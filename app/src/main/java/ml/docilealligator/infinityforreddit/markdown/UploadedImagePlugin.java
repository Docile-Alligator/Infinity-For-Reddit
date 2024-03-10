package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import java.util.Map;

import io.noties.markwon.AbstractMarkwonPlugin;
import ml.docilealligator.infinityforreddit.UploadedImage;

public class UploadedImagePlugin extends AbstractMarkwonPlugin {
    private final UploadedImageBlockParser.Factory factory;

    public UploadedImagePlugin() {
        this.factory = new UploadedImageBlockParser.Factory();
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

    public void setUploadedImageMap(Map<String, UploadedImage> uploadedImageMap) {
        factory.setUploadedImageMap(uploadedImageMap);
    }
}
