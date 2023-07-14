package ml.ino6962.postinfinityforreddit.events;

import ml.ino6962.postinfinityforreddit.Flair;

public class FlairSelectedEvent {
    public long viewPostDetailFragmentId;
    public Flair flair;

    public FlairSelectedEvent(long viewPostDetailFragmentId, Flair flair) {
        this.viewPostDetailFragmentId = viewPostDetailFragmentId;
        this.flair = flair;
    }
}
