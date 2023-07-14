package ml.ino6962.postinfinityforreddit.events;

public class ChangeHideKarmaEvent {
    public boolean hideKarma;

    public ChangeHideKarmaEvent(boolean showKarma) {
        this.hideKarma = showKarma;
    }
}
