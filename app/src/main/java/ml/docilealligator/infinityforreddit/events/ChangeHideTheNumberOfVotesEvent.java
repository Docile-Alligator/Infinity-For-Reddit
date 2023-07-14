package ml.ino6962.postinfinityforreddit.events;

public class ChangeHideTheNumberOfVotesEvent {
    public boolean hideTheNumberOfVotes;

    public ChangeHideTheNumberOfVotesEvent(boolean hideTheNumberOfVotes) {
        this.hideTheNumberOfVotes = hideTheNumberOfVotes;
    }
}
