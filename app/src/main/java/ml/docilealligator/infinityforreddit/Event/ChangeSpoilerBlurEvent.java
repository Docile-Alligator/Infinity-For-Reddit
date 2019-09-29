package ml.docilealligator.infinityforreddit.Event;

public class ChangeSpoilerBlurEvent {
    public boolean needBlurSpoiler;
    public ChangeSpoilerBlurEvent(boolean needBlurSpoiler) {
        this.needBlurSpoiler = needBlurSpoiler;
    }
}
