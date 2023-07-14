package ml.ino6962.postinfinityforreddit.events;

public class ChangeInboxCountEvent {
    public int inboxCount;

    public ChangeInboxCountEvent(int inboxCount) {
        this.inboxCount = inboxCount;
    }
}
