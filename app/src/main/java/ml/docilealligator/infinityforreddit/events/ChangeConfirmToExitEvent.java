package ml.docilealligator.infinityforreddit.events;

public class ChangeConfirmToExitEvent {
    public boolean confirmToExit;

    public ChangeConfirmToExitEvent(boolean confirmToExit) {
        this.confirmToExit = confirmToExit;
    }
}
