package ml.docilealligator.infinityforreddit.Event;

public class ChangeWifiStatusEvent {
    public boolean isConnectedToWifi;
    public ChangeWifiStatusEvent(boolean isConnectedToWifi) {
        this.isConnectedToWifi = isConnectedToWifi;
    }
}
