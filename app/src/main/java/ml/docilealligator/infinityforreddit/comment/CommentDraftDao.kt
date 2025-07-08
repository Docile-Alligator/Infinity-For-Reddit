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
    suspend fun delete(commentDraft: CommentDraft)

    @Query("SELECT * FROM comment_draft WHERE full_name = :fullName AND draft_type = :draftType")
    fun getCommentDraftLiveData(fullName: String, draftType: DraftType): LiveData<CommentDraft>
}