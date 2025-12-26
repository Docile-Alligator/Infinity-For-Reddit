package ml.docilealligator.infinityforreddit.multireddit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface AnonymousMultiredditSubredditDaoKt {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(anonymousMultiredditSubreddits: MutableList<AnonymousMultiredditSubreddit>)
}