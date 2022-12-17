package ml.docilealligator.infinityforreddit.events;

public class ChangeDisableCommentLoadingEvent {
    public boolean disableCommentLoading;

    public ChangeDisableCommentLoadingEvent(boolean disableCommentLoading) {
        this.disableCommentLoading = disableCommentLoading;
    }
}
