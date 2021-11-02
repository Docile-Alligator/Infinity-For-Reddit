package ml.docilealligator.infinityforreddit.events;

public class SubmitChangeBannerEvent {
    public final boolean isSuccess;
    public final String errorMessage;

    public SubmitChangeBannerEvent(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
}
