package ml.docilealligator.infinityforreddit.events;

public class SubmitSaveProfileEvent {
    public final boolean isSuccess;
    public final String errorMessage;

    public SubmitSaveProfileEvent(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
}
