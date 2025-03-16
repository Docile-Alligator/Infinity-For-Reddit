package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.node.Block;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.thing.GiphyGif;
import ml.docilealligator.infinityforreddit.thing.UploadedImage;

public class GiphyGifBlockParser extends AbstractBlockParser {
    private final GiphyGifBlock giphyGifBlock;

    GiphyGifBlockParser(GiphyGif giphyGif) {
        this.giphyGifBlock = new GiphyGifBlock(giphyGif);
    }

    @Override
    public Block getBlock() {
        return giphyGifBlock;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        return null;
    }

    public static class Factory extends AbstractBlockParserFactory {
        private final Pattern pattern =  Pattern.compile("!\\[gif]\\(giphy\\|\\w+\\|downsized\\)");
        @Nullable
        private GiphyGif giphyGif;

        // Only for editing comments with GiphyGif. No need to convert MediaMetadata to GiphyGif.
        @Nullable
        private Map<String, UploadedImage> uploadedImageMap;

        public Factory(@Nullable GiphyGif giphyGif, @Nullable List<UploadedImage> uploadedImages) {
            this.giphyGif = giphyGif;

            if (uploadedImages == null) {
                return;
            }

            uploadedImageMap = new HashMap<>();
            for (UploadedImage u : uploadedImages) {
                uploadedImageMap.put(u.imageName, u);
            }
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (giphyGif == null && (uploadedImageMap == null || uploadedImageMap.isEmpty())) {
                return BlockStart.none();
            }

            String line = state.getLine().toString();
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int startIndex = line.lastIndexOf('(');
                if (startIndex > 0) {
                    int endIndex = line.indexOf(')', startIndex);
                    String id = line.substring(startIndex + 1, endIndex);
                    if (giphyGif != null && giphyGif.id.equals(id)) {
                        return BlockStart.of(new GiphyGifBlockParser(giphyGif));
                    } else if (uploadedImageMap != null && uploadedImageMap.containsKey(id)) {
                        return BlockStart.of(new GiphyGifBlockParser(new GiphyGif(id, false)));
                    }
                }
            }
            return BlockStart.none();
        }
    }
}
