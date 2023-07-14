package ml.docilealligator.infinityforreddit.events;

public class ChangeDefaultPostLayoutEvent {
    public int defaultPostLayout;

    public ChangeDefaultPostLayoutEvent(int defaultPostLayout) {
        this.defaultPostLayout = defaultPostLayout;
    }
}
