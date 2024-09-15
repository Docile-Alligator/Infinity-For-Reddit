package ml.docilealligator.infinityforreddit.recentsearchquery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
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
    @Nullable
    @ColumnInfo(name = "search_in_multireddit_path")
    private String multiRedditPath;
    @Nullable
    @ColumnInfo(name = "search_in_multireddit_display_name")
    private String multiRedditDisplayName;
    @SelectThingReturnKey.THING_TYPE
    @ColumnInfo(name = "search_in_thing_type")
    private int searchInThingType;
    @ColumnInfo(name = "time")
    private long time;

    public RecentSearchQuery(@NonNull String username, @NonNull String searchQuery,
                             @Nullable String searchInSubredditOrUserName, @Nullable String multiRedditPath,
                             @Nullable String multiRedditDisplayName,
                             @SelectThingReturnKey.THING_TYPE int searchInThingType) {
        this.username = username;
        this.searchQuery = searchQuery;
        this.searchInSubredditOrUserName = searchInSubredditOrUserName;
        this.searchInThingType = searchInThingType;
        this.multiRedditPath = multiRedditPath;
        this.multiRedditDisplayName = multiRedditDisplayName;
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

    @Nullable
    public String getMultiRedditPath() {
        return multiRedditPath;
    }

    public void setMultiRedditPath(@Nullable String multiRedditPath) {
        this.multiRedditPath = multiRedditPath;
    }

    @Nullable
    public String getMultiRedditDisplayName() {
        return multiRedditDisplayName;
    }

    public void setMultiRedditDisplayName(@Nullable String multiRedditDisplayName) {
        this.multiRedditDisplayName = multiRedditDisplayName;
    }

    @SelectThingReturnKey.THING_TYPE
    public int getSearchInThingType() {
        return searchInThingType;
    }

    public void setSearchInThingType(@SelectThingReturnKey.THING_TYPE int searchInThingType) {
        this.searchInThingType = searchInThingType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
