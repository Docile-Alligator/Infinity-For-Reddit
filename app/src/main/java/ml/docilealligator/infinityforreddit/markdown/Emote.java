package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Visitor;

import ml.docilealligator.infinityforreddit.thing.MediaMetadata;

public class Emote extends CustomNode {
    private final MediaMetadata mediaMetadata;
    private final String title;

    public Emote(MediaMetadata mediaMetadata, String title) {
        this.mediaMetadata = mediaMetadata;
        this.title = title;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public MediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public String getTitle() {
        return title;
    }

    @Override
    protected String toStringAttributes() {
        return "destination=" + mediaMetadata.original.url + ", title=" + title;
    }
}
