package ml.ino6962.postinfinityforreddit.events;

public class ChangeHideTheNumberOfCommentsEvent {
    public boolean hideTheNumberOfComments;

    public ChangeHideTheNumberOfCommentsEvent(boolean hideTheNumberOfComments) {
        this.hideTheNumberOfComments = hideTheNumberOfComments;
    }
}
