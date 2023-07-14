package ml.ino6962.postinfinityforreddit.events;

public class NeedForPostListFromPostFragmentEvent {
    public long postFragmentTimeId;

    public NeedForPostListFromPostFragmentEvent(long postFragmentId) {
        this.postFragmentTimeId = postFragmentId;
    }
}
