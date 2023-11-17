package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomBlock;

import ml.docilealligator.infinityforreddit.post.Post;

public class ImageAndGifBlock extends CustomBlock {
    public Post.MediaMetadata mediaMetadata;

    public ImageAndGifBlock(Post.MediaMetadata mediaMetadata) {
        this.mediaMetadata = mediaMetadata;
    }
}
