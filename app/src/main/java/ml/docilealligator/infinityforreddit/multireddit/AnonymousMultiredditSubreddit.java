package ml.docilealligator.infinityforreddit.multireddit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import ml.docilealligator.infinityforreddit.account.Account;

@Entity(tableName = "anonymous_multireddit_subreddits", primaryKeys = {"path", "username", "subreddit_name"},
        foreignKeys = @ForeignKey(entity = MultiReddit.class, parentColumns = {"path", "username"},
                childColumns = {"path", "username"}, onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE))
public class AnonymousMultiredditSubreddit {
    @NonNull
    @ColumnInfo(name = "path")
    private String path;
    @NonNull
    @ColumnInfo(name = "username")
    public String username = Account.ANONYMOUS_ACCOUNT;
    @NonNull
    @ColumnInfo(name = "subreddit_name")
    private String subredditName;
    @Nullable
    @ColumnInfo(name = "icon_url")
    private String iconUrl;

    public AnonymousMultiredditSubreddit(@NonNull String path, @NonNull String subredditName, @Nullable String iconUrl) {
        this.path = path;
        this.subredditName = subredditName;
        this.iconUrl = iconUrl;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    @NonNull
    public String getSubredditName() {
        return subredditName;
    }

    public void setSubredditName(@NonNull String subredditName) {
        this.subredditName = subredditName;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(@Nullable String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
