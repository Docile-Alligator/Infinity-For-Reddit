package ml.docilealligator.infinityforreddit.recentsearchquery;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecentSearchQueryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecentSearchQuery recentSearchQuery);

    @Query("SELECT * FROM recent_search_queries WHERE username = :username ORDER BY time DESC")
    LiveData<List<RecentSearchQuery>> getAllRecentSearchQueriesLiveData(String username);

    @Query("SELECT * FROM recent_search_queries WHERE username = :username ORDER BY time DESC")
    List<RecentSearchQuery> getAllRecentSearchQueries(String username);

    @Delete
    void deleteRecentSearchQueries(RecentSearchQuery recentSearchQuery);
}
