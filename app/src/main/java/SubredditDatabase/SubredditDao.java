package SubredditDatabase;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface SubredditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubredditData SubredditData);

    @Query("DELETE FROM subreddits")
    void deleteAllSubreddits();

    @Query("SELECT * from subreddits WHERE id = :id")
    LiveData<SubredditData> getSubredditLiveDataById(String id);

    @Query("SELECT * from subreddits WHERE name = :namePrefixed")
    LiveData<SubredditData> getSubredditLiveDataByNamePrefixed(String namePrefixed);

    @Query("SELECT * from subreddits WHERE name = :namePrefixed LIMIT 1")
    SubredditData getSubredditData(String namePrefixed);
}
