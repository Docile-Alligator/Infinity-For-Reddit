package ml.docilealligator.infinityforreddit;

public class SwitchAccountEvent {
    String excludeActivityClassName;
    public SwitchAccountEvent() {}
    public SwitchAccountEvent(String excludeActivityClassName) {
        this.excludeActivityClassName = excludeActivityClassName;
    }
}
