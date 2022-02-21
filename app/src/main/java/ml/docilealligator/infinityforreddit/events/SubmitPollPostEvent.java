package ml.docilealligator.infinityforreddit.events;

public class SubmitPollPostEvent {
    public boolean postSuccess;
    public String postUrl;
    public String errorMessage;

    public SubmitPollPostEvent(boolean postSuccess, String postUrl, String errorMessage) {
        this.postSuccess = postSuccess;
        this.postUrl = postUrl;
        this.errorMessage = errorMessage;
    }
}
