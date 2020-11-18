package ml.docilealligator.infinityforreddit.events;

public class ChangeLockBottomAppBarEvent {
    public boolean lockBottomAppBar;

    public ChangeLockBottomAppBarEvent(boolean lockBottomAppBar) {
        this.lockBottomAppBar = lockBottomAppBar;
    }
}
