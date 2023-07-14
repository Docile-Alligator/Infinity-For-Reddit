package ml.ino6962.postinfinityforreddit.events;

public class ChangeNetworkStatusEvent {
    public int connectedNetwork;

    public ChangeNetworkStatusEvent(int connectedNetwork) {
        this.connectedNetwork = connectedNetwork;
    }
}
