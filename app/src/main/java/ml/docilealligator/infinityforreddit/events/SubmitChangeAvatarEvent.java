package ml.docilealligator.infinityforreddit.events;

public class SubmitChangeAvatarEvent {
    public final boolean isSuccess;
    public final String errorMessage;

    public SubmitChangeAvatarEvent(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
}
