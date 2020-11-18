package ml.docilealligator.infinityforreddit.subreddit;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SubredditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubredditData SubredditData);

    @Query("DELETE FROM subreddits")
    void deleteAllSubreddits();

    @Query("SELECT * from subreddits WHERE name = :namePrefixed COLLATE NOCASE LIMIT 1")
    LiveData<SubredditData> getSubredditLiveDataByName(String namePrefixed);

    @Query("SELECT * from subreddits WHERE name = :namePrefixed COLLATE NOCASE LIMIT 1")
    SubredditData getSubredditData(String namePrefixed);
}
