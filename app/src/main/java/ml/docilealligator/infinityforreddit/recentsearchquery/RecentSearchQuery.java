package ml.docilealligator.infinityforreddit.recentsearchquery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    @Nullable
    @ColumnInfo(name = "search_in_subreddit_or_user_name")
    private String searchInSubredditOrUserName;
    @ColumnInfo(name = "search_in_is_user")
    private boolean searchInIsUser;
    @ColumnInfo(name = "time")
    private long time;

    public RecentSearchQuery(@NonNull String username, @NonNull String searchQuery,
                             @Nullable String searchInSubredditOrUserName, boolean searchInIsUser) {
        this.username = username;
        this.searchQuery = searchQuery;
        this.searchInSubredditOrUserName = searchInSubredditOrUserName;
        this.searchInIsUser = searchInIsUser;
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

    @Nullable
    public String getSearchInSubredditOrUserName() {
        return searchInSubredditOrUserName;
    }

    public void setSearchInSubredditOrUserName(@Nullable String searchInSubredditOrUserName) {
        this.searchInSubredditOrUserName = searchInSubredditOrUserName;
    }

    public boolean isSearchInIsUser() {
        return searchInIsUser;
    }

    public void setSearchInIsUser(boolean searchInIsUser) {
        this.searchInIsUser = searchInIsUser;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
