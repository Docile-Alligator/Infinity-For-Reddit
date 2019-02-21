package SubscribedSubredditDatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubscribedSubredditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubscribedSubredditData subscribedSubredditData);

    @Query("DELETE FROM subscribed_subreddits")
    void deleteAllSubscribedSubreddits();

    @Query("SELECT * from subscribed_subreddits ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits();

    @Query("SELECT * from subscribed_subreddits WHERE name = :subredditName COLLATE NOCASE LIMIT 1")
    SubscribedSubredditData getSubscribedSubreddit(String subredditName);

    @Query("DELETE FROM subscribed_subreddits WHERE name = :subredditName")
    void deleteSubscribedSubreddit(String subredditName);
}
