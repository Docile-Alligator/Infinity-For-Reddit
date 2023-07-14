package ml.ino6962.postinfinityforreddit.events;

public class ToggleSecureModeEvent {
    public boolean isSecureMode;

    public ToggleSecureModeEvent(boolean isSecureMode) {
        this.isSecureMode = isSecureMode;
    }
}
