package ml.docilealligator.infinityforreddit.events;

public class ChangeHideSubredditAndUserPrefixEvent {
    public boolean hideSubredditAndUserPrefix;

    public ChangeHideSubredditAndUserPrefixEvent(boolean hideSubredditAndUserPrefix) {
        this.hideSubredditAndUserPrefix = hideSubredditAndUserPrefix;
    }
}
