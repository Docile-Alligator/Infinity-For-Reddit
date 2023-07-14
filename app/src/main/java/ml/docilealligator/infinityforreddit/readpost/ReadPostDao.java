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

    @Query("SELECT * FROM read_posts WHERE username = :username AND time > :after ORDER BY time LIMIT 25")
    ListenableFuture<List<ReadPost>> getAllReadPostsListenableFuture(String username, long after);

    @Query("SELECT * FROM read_posts WHERE username = :username AND time > :after ORDER BY time LIMIT 25")
    List<ReadPost> getAllReadPosts(String username, long after);

    @Query("SELECT * FROM read_posts WHERE username = :username")
    List<ReadPost> getAllReadPosts(String username);

    @Query("SELECT * FROM read_posts WHERE id = :id LIMIT 1")
    ReadPost getReadPost(String id);

    @Query("SELECT COUNT(id) FROM read_posts")
    int getReadPostsCount();

    @Query("DELETE FROM read_posts WHERE rowid IN (SELECT rowid FROM read_posts LIMIT 100) AND username = :username")
    void deleteOldestReadPosts(String username);

    @Query("DELETE FROM read_posts")
    void deleteAllReadPosts();
}
