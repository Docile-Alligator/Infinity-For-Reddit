package ml.docilealligator.infinityforreddit.markdown.imageandgif;

import org.commonmark.node.CustomBlock;

import ml.docilealligator.infinityforreddit.thing.MediaMetadata;

public class ImageAndGifBlock extends CustomBlock {
    public MediaMetadata mediaMetadata;

    public ImageAndGifBlock(MediaMetadata mediaMetadata) {
        this.mediaMetadata = mediaMetadata;
    }
}
