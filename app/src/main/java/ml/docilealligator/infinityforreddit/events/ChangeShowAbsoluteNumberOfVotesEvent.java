package ml.ino6962.postinfinityforreddit.events;

public class ChangeShowAbsoluteNumberOfVotesEvent {
    public boolean showAbsoluteNumberOfVotes;

    public ChangeShowAbsoluteNumberOfVotesEvent(boolean showAbsoluteNumberOfVotes) {
        this.showAbsoluteNumberOfVotes = showAbsoluteNumberOfVotes;
    }
}
