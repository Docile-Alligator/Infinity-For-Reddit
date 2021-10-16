package ml.docilealligator.infinityforreddit.multireddit;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MultiRedditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MultiReddit multiReddit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MultiReddit> multiReddits);

    @Query("SELECT * FROM multi_reddits WHERE username = :username ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<MultiReddit>> getAllMultiReddits(String username);

    @Query("SELECT * FROM multi_reddits WHERE username = :username ORDER BY name COLLATE NOCASE ASC")
    List<MultiReddit> getAllMultiRedditsList(String username);

    @Query("SELECT * FROM multi_reddits WHERE username = :username AND is_favorite ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<MultiReddit>> getAllFavoriteMultiReddits(String username);

    @Query("SELECT * FROM multi_reddits WHERE path = :path AND username = :username COLLATE NOCASE LIMIT 1")
    MultiReddit getMultiReddit(String path, String username);

    @Query("DELETE FROM multi_reddits WHERE name = :name AND username = :username")
    void deleteMultiReddit(String name, String username);

    @Query("DELETE FROM multi_reddits WHERE path = :path")
    void anonymousDeleteMultiReddit(String path);

    @Query("DELETE FROM multi_reddits WHERE username = :username")
    void deleteAllUserMultiReddits(String username);
}
