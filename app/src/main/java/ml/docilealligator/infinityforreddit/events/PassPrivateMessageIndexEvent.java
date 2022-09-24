package ml.docilealligator.infinityforreddit.events;

public class PassPrivateMessageIndexEvent {
    public int privateMessageIndex;

    public PassPrivateMessageIndexEvent(int privateMessageIndex) {
        this.privateMessageIndex = privateMessageIndex;
    }
}
