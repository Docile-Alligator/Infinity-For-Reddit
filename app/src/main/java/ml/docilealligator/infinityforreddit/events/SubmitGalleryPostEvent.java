package ml.docilealligator.infinityforreddit.events;

public class SubmitGalleryPostEvent {
    public boolean postSuccess;
    public String postUrl;
    public String errorMessage;

    public SubmitGalleryPostEvent(boolean postSuccess, String postUrl, String errorMessage) {
        this.postSuccess = postSuccess;
        this.postUrl = postUrl;
        this.errorMessage = errorMessage;
    }
}
