package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.Block;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.post.Post;

public class ImageAndGifBlockParser extends AbstractBlockParser {

    private ImageAndGifBlock imageAndGifBlock;

    ImageAndGifBlockParser(Post.MediaMetadata mediaMetadata) {
        this.imageAndGifBlock = new ImageAndGifBlock(mediaMetadata);
    }

    @Override
    public Block getBlock() {
        return imageAndGifBlock;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        return null;
    }

    public static class Factory extends AbstractBlockParserFactory {
        private final Pattern redditPreviewPattern =  Pattern.compile("!\\[img]\\(https://preview.redd.it/\\w+.(jpg|png)((\\?+[-a-zA-Z0-9()@:%_+.~#?&/=]*)|)\\)");
        private Map<String, Post.MediaMetadata> mediaMetadataMap;
        private final int fromIndex = "![img](https://preview.redd.it/".length();

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            String line = state.getLine().toString();
            Matcher matcher = redditPreviewPattern.matcher(line);
            if (matcher.find()) {
                if (matcher.end() == line.length()) {
                    int endIndex = line.indexOf('.', fromIndex);
                    if (endIndex > 0) {
                        String id = line.substring(fromIndex, endIndex);
                        return mediaMetadataMap.containsKey(id) ? BlockStart.of(new ImageAndGifBlockParser(mediaMetadataMap.get(id))) : BlockStart.none();
                    }
                }
            }
            return BlockStart.none();
        }

        public void setMediaMetadataMap(Map<String, Post.MediaMetadata> mediaMetadataMap) {
            this.mediaMetadataMap = mediaMetadataMap;
        }
    }
}
