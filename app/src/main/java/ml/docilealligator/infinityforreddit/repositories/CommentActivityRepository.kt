package ml.docilealligator.infinityforreddit.repositories

import androidx.lifecycle.LiveData
import ml.docilealligator.infinityforreddit.comment.CommentDraft
import ml.docilealligator.infinityforreddit.comment.CommentDraftDao

class CommentActivityRepository(
    private val commentDraftDao: CommentDraftDao
) {
    public fun getCommentDraft(parentFullname: String): LiveData<CommentDraft> {
        return commentDraftDao.getCommentDraftLiveData(parentFullname)
    }

    public suspend fun saveCommentDraft(parentFullname: String, content: String) {
        commentDraftDao.insert(CommentDraft(parentFullname, content, System.currentTimeMillis()))
    }
}