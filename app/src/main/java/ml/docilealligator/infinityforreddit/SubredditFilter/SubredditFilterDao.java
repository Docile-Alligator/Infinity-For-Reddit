package ml.docilealligator.infinityforreddit.SubredditFilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubredditFilterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubredditFilter subredditFilter);

    @Query("SELECT * FROM subreddit_filter")
    LiveData<List<SubredditFilter>> getAllSubredditFiltersLiveData();

    @Query("SELECT * FROM subreddit_filter")
    List<SubredditFilter> getAllSubredditFilters();

    @Delete
    void deleteSubredditFilter(SubredditFilter subredditFilter);
}
