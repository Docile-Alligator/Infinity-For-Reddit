package ml.ino6962.postinfinityforreddit.events;

public class ChangePostFeedMaxResolutionEvent {
    public int postFeedMaxResolution;

    public ChangePostFeedMaxResolutionEvent(int postFeedMaxResolution) {
        this.postFeedMaxResolution = postFeedMaxResolution;
    }
}
