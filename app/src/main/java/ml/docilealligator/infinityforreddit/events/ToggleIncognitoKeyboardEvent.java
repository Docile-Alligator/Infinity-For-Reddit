package ml.docilealligator.infinityforreddit.events;

public class ToggleIncognitoKeyboardEvent {
    public boolean isIncognitoKeyboard;

    public ToggleIncognitoKeyboardEvent(boolean isIncognitoKeyboard) {
        this.isIncognitoKeyboard = isIncognitoKeyboard;
    }
}
