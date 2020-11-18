package ml.docilealligator.infinityforreddit.events;

public class ChangeNSFWBlurEvent {
    public boolean needBlurNSFW;

    public ChangeNSFWBlurEvent(boolean needBlurNSFW) {
        this.needBlurNSFW = needBlurNSFW;
    }
}
