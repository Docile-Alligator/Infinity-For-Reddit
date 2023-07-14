package ml.ino6962.postinfinityforreddit.events;

public class SubmitChangeAvatarEvent {
    public final boolean isSuccess;
    public final String errorMessage;

    public SubmitChangeAvatarEvent(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
}
