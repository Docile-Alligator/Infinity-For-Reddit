package ml.docilealligator.infinityforreddit.markdown;

import android.util.Log;

import org.commonmark.node.Block;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageAndGifBlockParser extends AbstractBlockParser {

    private ImageAndGifBlock imageAndGifBlock;

    ImageAndGifBlockParser() {
        this.imageAndGifBlock = new ImageAndGifBlock();
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
        private Pattern redditPreviewPattern =  Pattern.compile("!\\[img]\\(https://preview.redd.it/\\w+.(jpg|png)((\\?+[-a-zA-Z0-9()@:%_+.~#?&/=]*)|)\\)");

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            String line = state.getLine().toString();
            Log.i("asdfa", "s " + line + "fuck");
            Matcher matcher = redditPreviewPattern.matcher(line);
            if (matcher.find()) {
                if (matcher.end() == line.length()) {
                    return BlockStart.of(new ImageAndGifBlockParser());
                }
            }
            return BlockStart.none();
        }
    }
}
