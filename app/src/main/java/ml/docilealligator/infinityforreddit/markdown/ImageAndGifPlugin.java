package ml.docilealligator.infinityforreddit.markdown;

import android.util.Log;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import io.noties.markwon.AbstractMarkwonPlugin;

public class ImageAndGifPlugin extends AbstractMarkwonPlugin {
    @NonNull
    @Override
    public String processMarkdown(@NonNull String markdown) {
        Log.i("asdfa", "imageandgifplugin " + markdown + "fuck");
        return super.processMarkdown(markdown);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(new ImageAndGifBlockParser.Factory());
    }
}
