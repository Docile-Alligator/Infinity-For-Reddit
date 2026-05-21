package ml.docilealligator.infinityforreddit.readpost

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadPostDaoKt {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(readPost: ReadPost)

    @Query("SELECT * FROM read_posts WHERE id = :id AND read_post_type = 0 LIMIT 1")
    suspend fun getReadPost(id: String?): ReadPost?

    @Query("SELECT * FROM read_posts WHERE username = :username AND (:before IS NULL OR time < :before) AND read_post_type = :readPostType ORDER BY time DESC LIMIT 25")
    suspend fun getAllReadPosts(
        username: String,
        before: Long,
        @ReadPostType readPostType: Int
    ): MutableList<ReadPost>

    @Query("SELECT COUNT(id) FROM read_posts WHERE username = :username AND read_post_type = :readPostType")
    suspend fun getReadPostsCount(username: String, @ReadPostType readPostType: Int): Int

    @Query("DELETE FROM read_posts WHERE username = :username AND id = :postId AND read_post_type = :readPostType")
    suspend fun deleteReadPost(username: String, postId: String, @ReadPostType readPostType: Int)

    @Query("DELETE FROM read_posts WHERE rowid IN (SELECT rowid FROM read_posts WHERE username = :username AND read_post_type = :readPostType ORDER BY time ASC LIMIT 100)")
    suspend fun deleteOldestReadPosts(username: String, @ReadPostType readPostType: Int)
}