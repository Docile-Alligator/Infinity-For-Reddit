package ml.docilealligator.infinityforreddit.events;

public class ChangeDisableSwipingBetweenTabsEvent {
    public boolean disableSwipingBetweenTabs;

    public ChangeDisableSwipingBetweenTabsEvent(boolean disableSwipingBetweenTabs) {
        this.disableSwipingBetweenTabs = disableSwipingBetweenTabs;
    }
}
