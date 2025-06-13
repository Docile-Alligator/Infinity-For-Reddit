package ml.docilealligator.infinityforreddit.comment

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CommentDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commentDraft: CommentDraft)

    @Delete
    fun delete(commentDraft: CommentDraft)

    @Query("SELECT * FROM comment_draft WHERE parent_full_name = :parentFullName")
    fun getCommentDraftLiveData(parentFullName: String): LiveData<CommentDraft>
}