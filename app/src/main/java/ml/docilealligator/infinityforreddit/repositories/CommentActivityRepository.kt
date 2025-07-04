package ml.docilealligator.infinityforreddit.repositories

import androidx.lifecycle.LiveData
import ml.docilealligator.infinityforreddit.comment.CommentDraft
import ml.docilealligator.infinityforreddit.comment.CommentDraftDao

class CommentActivityRepository(
    private val commentDraftDao: CommentDraftDao
) {
    fun getCommentDraft(parentFullname: String): LiveData<CommentDraft> {
        return commentDraftDao.getCommentDraftLiveData(parentFullname)
    }

    suspend fun saveCommentDraft(parentFullname: String, content: String) {
        commentDraftDao.insert(CommentDraft(parentFullname, content, System.currentTimeMillis()))
    }

    suspend fun deleteCommentDraft(parentFullname: String) {
        commentDraftDao.delete(CommentDraft(parentFullname, "", 0))
    }
}