package ml.docilealligator.infinityforreddit;

public class SubmitVideoPostEvent {
    public boolean postSuccess;
    public boolean errorProcessingVideo;
    public String errorMessage;

    public SubmitVideoPostEvent(boolean postSuccess, boolean errorProcessingVideo, String errorMessage) {
        this.postSuccess = postSuccess;
        this.errorProcessingVideo = errorProcessingVideo;
        this.errorMessage = errorMessage;
    }
}
