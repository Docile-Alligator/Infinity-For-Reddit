package ml.docilealligator.infinityforreddit.events;

public class ChangeShowAbsoluteNumberOfVotesEvent {
    public boolean showAbsoluteNumberOfVotes;

    public ChangeShowAbsoluteNumberOfVotesEvent(boolean showAbsoluteNumberOfVotes) {
        this.showAbsoluteNumberOfVotes = showAbsoluteNumberOfVotes;
    }
}
