package ml.docilealligator.infinityforreddit.Event;

public class DownloadImageOrGifEvent {
    public boolean isSuccessful;

    public DownloadImageOrGifEvent(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
}
