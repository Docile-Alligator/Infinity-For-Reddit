package ml.docilealligator.infinityforreddit.events;

public class ChangeDefaultLinkPostLayoutEvent {
    public int defaultLinkPostLayout;

    public ChangeDefaultLinkPostLayoutEvent(int defaultLinkPostLayout) {
        this.defaultLinkPostLayout = defaultLinkPostLayout;
    }
}
