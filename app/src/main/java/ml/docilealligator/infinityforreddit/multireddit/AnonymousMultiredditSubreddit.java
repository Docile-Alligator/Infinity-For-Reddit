package ml.docilealligator.infinityforreddit.multireddit;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "anonymous_multireddit_subreddits", primaryKeys = {"path", "username", "subreddit_name"},
        foreignKeys = @ForeignKey(entity = MultiReddit.class, parentColumns = {"path", "username"},
                childColumns = {"path", "username"}, onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE))
public class AnonymousMultiredditSubreddit {
    @NonNull
    @ColumnInfo(name = "path")
    private String path;
    @NonNull
    @ColumnInfo(name = "username")
    public String username = "-";
    @NonNull
    @ColumnInfo(name = "subreddit_name")
    private String subredditName;

    public AnonymousMultiredditSubreddit(@NonNull String path, @NonNull String subredditName) {
        this.path = path;
        this.subredditName = subredditName;
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
}
