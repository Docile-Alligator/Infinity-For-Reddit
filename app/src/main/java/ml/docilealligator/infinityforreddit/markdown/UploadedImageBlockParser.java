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

import ml.docilealligator.infinityforreddit.UploadedImage;

public class UploadedImageBlockParser extends AbstractBlockParser {
    private final UploadedImageBlock uploadedImageBlock;

    UploadedImageBlockParser(UploadedImage uploadedImage) {
        this.uploadedImageBlock = new UploadedImageBlock(uploadedImage);
    }

    @Override
    public Block getBlock() {
        return uploadedImageBlock;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        return null;
    }

    public static class Factory extends AbstractBlockParserFactory {
        private final Pattern pattern =  Pattern.compile("!\\[.*]\\(\\w+\\)");
        @Nullable
        private Map<String, UploadedImage> uploadedImageMap;

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (uploadedImageMap == null || uploadedImageMap.isEmpty()) {
                return BlockStart.none();
            }

            String line = state.getLine().toString();
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int startIndex = line.lastIndexOf('(');
                if (startIndex > 0) {
                    int endIndex = line.indexOf(')', startIndex);
                    String id = line.substring(startIndex + 1, endIndex);
                    UploadedImage uploadedImage = uploadedImageMap.get(id);
                    if (uploadedImage != null) {
                        //![caption](id)
                        String caption = line.substring(matcher.start() + 2, startIndex - 1);
                        uploadedImage.setCaption(caption);
                        return BlockStart.of(new UploadedImageBlockParser(uploadedImage));
                    }
                }
            }
            return BlockStart.none();
        }

        public void setUploadedImages(@Nullable List<UploadedImage> uploadedImages) {
            if (uploadedImages == null) {
                return;
            }

            uploadedImageMap = new HashMap<>();
            for (UploadedImage u : uploadedImages) {
                uploadedImageMap.put(u.imageUrlOrKey, u);
            }
        }
    }
}
