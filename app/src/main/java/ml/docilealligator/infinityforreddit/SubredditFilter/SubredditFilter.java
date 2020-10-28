package ml.docilealligator.infinityforreddit.SubredditFilter;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "subreddit_filter", primaryKeys = {"subreddit_name", "type"})
public class SubredditFilter {
    public static int TYPE_POPULAR_AND_ALL = 0;
    @NonNull
    @ColumnInfo(name = "subreddit_name")
    private String subredditName;
    @ColumnInfo(name = "type")
    private int type;

    public SubredditFilter(@NonNull String subredditName, int type) {
        this.subredditName = subredditName;
        this.type = type;
    }

    @NonNull
    public String getSubredditName() {
        return subredditName;
    }

    public void setSubredditName(@NonNull String subredditName) {
        this.subredditName = subredditName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
