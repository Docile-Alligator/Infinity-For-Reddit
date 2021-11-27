package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

// Parse and consume a block quote except when it's a spoiler opening
public class BlockQuoteWithExceptionParser extends AbstractBlockParser {
    private final BlockQuote block = new BlockQuote();

    private static boolean isMarker(ParserState state, int index) {
        CharSequence line = state.getLine();
        return state.getIndent() < Parsing.CODE_BLOCK_INDENT && index < line.length() && line.charAt(index) == '>';
    }

    private static boolean isMarkerSpoiler(ParserState state, int index) {
        CharSequence line = state.getLine();
        int length = line.length();
        try {
            return state.getIndent() < Parsing.CODE_BLOCK_INDENT && index < length && line.charAt(index) == '>' && (index + 1) < length && line.charAt(index + 1) == '!';
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public BlockQuote getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        if (isMarker(state, nextNonSpace)) {
            int newColumn = state.getColumn() + state.getIndent() + 1;
            // optional following space or tab
            if (Parsing.isSpaceOrTab(state.getLine(), nextNonSpace + 1)) {
                newColumn++;
            }
            return BlockContinue.atColumn(newColumn);
        } else {
            return BlockContinue.none();
        }
    }

    public static class Factory extends AbstractBlockParserFactory {
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            // Potential for a spoiler opening
            // We don't check for spoiler closing, neither does Reddit
            if (isMarkerSpoiler(state, nextNonSpace)) {
                // It might be a spoiler, don't consume
                return BlockStart.none();
            }
            // Not a spoiler then
            else if (isMarker(state, nextNonSpace)) {
                int newColumn = state.getColumn() + state.getIndent() + 1;
                // optional following space or tab
                if (Parsing.isSpaceOrTab(state.getLine(), nextNonSpace + 1)) {
                    newColumn++;
                }
                return BlockStart.of(new BlockQuoteWithExceptionParser()).atColumn(newColumn);
            } else {
                return BlockStart.none();
            }
        }
    }
}
