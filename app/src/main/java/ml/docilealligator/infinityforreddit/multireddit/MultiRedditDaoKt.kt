package ml.docilealligator.infinityforreddit.multireddit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MultiRedditDaoKt {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(multiReddit: MultiReddit)

    @Query("SELECT * FROM multi_reddits WHERE path = :path AND username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getMultiReddit(path: String, username: String): MultiReddit?
}