package ml.docilealligator.infinityforreddit;

public class ChangeSpoilerBlurEvent {
    public boolean needBlurSpoiler;
    public ChangeSpoilerBlurEvent(boolean needBlurSpoiler) {
        this.needBlurSpoiler = needBlurSpoiler;
    }
}
