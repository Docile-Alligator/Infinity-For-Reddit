package ml.ino6962.postinfinityforreddit.events;

public class ChangeDisableSwipingBetweenTabsEvent {
    public boolean disableSwipingBetweenTabs;

    public ChangeDisableSwipingBetweenTabsEvent(boolean disableSwipingBetweenTabs) {
        this.disableSwipingBetweenTabs = disableSwipingBetweenTabs;
    }
}
