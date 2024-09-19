package ml.docilealligator.infinityforreddit.markdown;

import androidx.annotation.Nullable;

import org.commonmark.node.Block;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.thing.GiphyGif;

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

        public Factory(@Nullable GiphyGif giphyGif) {
            this.giphyGif = giphyGif;
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (giphyGif == null) {
                return BlockStart.none();
            }

            String line = state.getLine().toString();
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int startIndex = line.lastIndexOf('(');
                if (startIndex > 0) {
                    int endIndex = line.indexOf(')', startIndex);
                    String id = line.substring(startIndex + 1, endIndex);
                    if (giphyGif.id.equals(id)) {
                        return BlockStart.of(new GiphyGifBlockParser(giphyGif));
                    }
                }
            }
            return BlockStart.none();
        }
    }
}
