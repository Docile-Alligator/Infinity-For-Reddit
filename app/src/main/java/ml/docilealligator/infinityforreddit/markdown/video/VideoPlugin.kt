package ml.docilealligator.infinityforreddit.markdown.video

import io.noties.markwon.AbstractMarkwonPlugin
import ml.docilealligator.infinityforreddit.markdown.imageandgif.ImageAndGifBlockParser
import ml.docilealligator.infinityforreddit.thing.MediaMetadata
import org.commonmark.parser.Parser

class VideoPlugin: AbstractMarkwonPlugin() {
    private val factory = VideoBlockParser.Factory

    override fun processMarkdown(markdown: String): String {
        return super.processMarkdown(markdown)
    }

    override fun configureParser(builder: Parser.Builder) {
        builder.customBlockParserFactory(factory)
    }

    fun setMediaMetadataMap(mediaMetadataMap: Map<String, MediaMetadata>?) {
        factory.setMediaMetadataMap(mediaMetadataMap)
    }
}