package ml.ino6962.postinfinityforreddit.events;

public class ChangePullToRefreshEvent {
    public boolean pullToRefresh;

    public ChangePullToRefreshEvent(boolean pullToRefresh) {
        this.pullToRefresh = pullToRefresh;
    }
}
