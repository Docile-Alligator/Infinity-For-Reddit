package ml.docilealligator.infinityforreddit.markdown.video

import ml.docilealligator.infinityforreddit.thing.MediaMetadata
import org.commonmark.node.Block
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import java.util.regex.Pattern

class VideoBlockParser(
    mediaMetadata: MediaMetadata
): AbstractBlockParser() {
    private val videoBlock: VideoBlock = VideoBlock(mediaMetadata)

    override fun getBlock(): Block {
        return videoBlock
    }

    override fun tryContinue(parserState: ParserState?): BlockContinue? {
        return null
    }

    companion object Factory : AbstractBlockParserFactory() {
        private val videoPattern: Pattern =
            Pattern.compile("!\\[.*]\\(https://reddit\\.com/link/([^/]+)/video/([^/]+)/player\\)")
        private var mediaMetadataMap: Map<String, MediaMetadata>? = null

        override fun tryStart(
            state: ParserState,
            matchedBlockParser: MatchedBlockParser?
        ): BlockStart? {
            return mediaMetadataMap?.let { mediaMetadataMap ->
                val line = state.line.toString()
                val matcher = videoPattern.matcher(line)
                if (matcher.find() && matcher.end() == line.length) {
                    val id = matcher.group(2)
                    mediaMetadataMap[id ?: ""]?.let {
                        BlockStart.of(VideoBlockParser(it))
                    } ?: BlockStart.none()
                } else BlockStart.none()
            } ?: BlockStart.none()
        }

        fun setMediaMetadataMap(mediaMetadataMap: Map<String, MediaMetadata>?) {
            this.mediaMetadataMap = mediaMetadataMap
        }
    }
}