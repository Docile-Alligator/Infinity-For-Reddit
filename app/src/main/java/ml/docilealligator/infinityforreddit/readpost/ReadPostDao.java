package ml.docilealligator.infinityforreddit.readpost;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface ReadPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadPost readPost);

    @Query("SELECT * FROM read_posts WHERE username = :username AND (:before IS NULL OR time < :before) ORDER BY time DESC LIMIT 25")
    ListenableFuture<List<ReadPost>> getAllReadPostsListenableFuture(String username, Long before);

    @Query("SELECT * FROM read_posts WHERE username = :username AND (:before IS NULL OR time < :before) ORDER BY time DESC LIMIT 25")
    List<ReadPost> getAllReadPosts(String username, Long before);

    @Query("SELECT * FROM read_posts WHERE id = :id LIMIT 1")
    ReadPost getReadPost(String id);

    @Query("SELECT COUNT(id) FROM read_posts WHERE username = :username")
    int getReadPostsCount(String username);

    @Query("DELETE FROM read_posts WHERE rowid IN (SELECT rowid FROM read_posts WHERE username = :username ORDER BY time ASC LIMIT 100)")
    void deleteOldestReadPosts(String username);

    @Query("DELETE FROM read_posts")
    void deleteAllReadPosts();

    @Query("SELECT id FROM read_posts WHERE id IN (:ids) AND username = :username")
    List<String> getReadPostsIdsByIds(List<String> ids, String username);

    default int getMaxReadPostEntrySize() { // in bytes
        return  20 + // max username size
                10 + // id size
                8;   // time size
    }
}
