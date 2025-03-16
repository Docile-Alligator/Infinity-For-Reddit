package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomBlock;

import ml.docilealligator.infinityforreddit.thing.GiphyGif;

public class GiphyGifBlock extends CustomBlock {
    public GiphyGif giphyGif;

    public GiphyGifBlock(GiphyGif giphyGif) {
        this.giphyGif = giphyGif;
    }
}
