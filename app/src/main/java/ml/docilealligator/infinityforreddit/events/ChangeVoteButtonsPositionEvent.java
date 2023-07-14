package ml.ino6962.postinfinityforreddit.events;

public class ChangeVoteButtonsPositionEvent {
    public boolean voteButtonsOnTheRight;

    public ChangeVoteButtonsPositionEvent(boolean voteButtonsOnTheRight) {
        this.voteButtonsOnTheRight = voteButtonsOnTheRight;
    }
}
