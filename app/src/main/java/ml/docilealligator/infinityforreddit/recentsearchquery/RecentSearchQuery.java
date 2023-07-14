package ml.docilealligator.infinityforreddit.recentsearchquery;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import ml.docilealligator.infinityforreddit.account.Account;

@Entity(tableName = "recent_search_queries", primaryKeys = {"username", "search_query"},
        foreignKeys = @ForeignKey(entity = Account.class, parentColumns = "username",
                childColumns = "username", onDelete = ForeignKey.CASCADE))
public class RecentSearchQuery {
    @NonNull
    @ColumnInfo(name = "username")
    private String username;
    @NonNull
    @ColumnInfo(name = "search_query")
    private String searchQuery;
    @ColumnInfo(name = "time")
    private long time;

    public RecentSearchQuery(@NonNull String username, @NonNull String searchQuery) {
        this.username = username;
        this.searchQuery = searchQuery;
        this.time = System.currentTimeMillis();
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(@NonNull String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
