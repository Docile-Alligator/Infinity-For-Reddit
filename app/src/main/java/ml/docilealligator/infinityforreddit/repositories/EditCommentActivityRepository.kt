package ml.docilealligator.infinityforreddit.repositories

import androidx.lifecycle.LiveData
import ml.docilealligator.infinityforreddit.comment.CommentDraft
import ml.docilealligator.infinityforreddit.comment.CommentDraftDao
import ml.docilealligator.infinityforreddit.comment.DraftType

class EditCommentActivityRepository(
    private val commentDraftDao: CommentDraftDao
) {
    fun getCommentDraft(fullname: String): LiveData<CommentDraft> {
        return commentDraftDao.getCommentDraftLiveData(fullname, DraftType.EDIT)
    }

    suspend fun saveCommentDraft(fullname: String, content: String) {
        commentDraftDao.insert(CommentDraft(fullname, content, System.currentTimeMillis(), DraftType.EDIT))
    }

    suspend fun deleteCommentDraft(fullname: String) {
        commentDraftDao.delete(CommentDraft(fullname, "", 0, DraftType.EDIT))
    }
}