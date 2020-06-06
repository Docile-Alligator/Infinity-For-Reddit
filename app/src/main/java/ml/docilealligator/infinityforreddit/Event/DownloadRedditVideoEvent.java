package ml.docilealligator.infinityforreddit.Event;

public class DownloadRedditVideoEvent {
    public boolean isSuccessful;

    public DownloadRedditVideoEvent(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
}
