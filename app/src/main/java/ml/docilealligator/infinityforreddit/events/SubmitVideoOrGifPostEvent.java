package ml.docilealligator.infinityforreddit.events;

public class SubmitVideoOrGifPostEvent {
    public boolean postSuccess;
    public boolean errorProcessingVideoOrGif;
    public String errorMessage;

    public SubmitVideoOrGifPostEvent(boolean postSuccess, boolean errorProcessingVideoOrGif, String errorMessage) {
        this.postSuccess = postSuccess;
        this.errorProcessingVideoOrGif = errorProcessingVideoOrGif;
        this.errorMessage = errorMessage;
    }
}
