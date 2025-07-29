package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.SingleLiveEvent
import ml.docilealligator.infinityforreddit.apis.RedditAPI
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.moderation.CommentModerationEvent
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.ApproveFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.Approved
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.DistinguishAsModFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.DistinguishedAsMod
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.LockFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.Locked
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.MarkAsSpamFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.MarkNSFWFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.MarkSpoilerFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.MarkedAsSpam
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.MarkedNSFW
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.MarkedSpoiler
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.RemoveFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.SetStickyPost
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.SetStickyPostFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UndistinguishAsModFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UndistinguishedAsMod
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnlockFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.Unlocked
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnmarkNSFWFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnmarkSpoilerFailed
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnmarkedNSFW
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnmarkedSpoiler
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnsetStickyPost
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent.UnsetStickyPostFailed
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.utils.APIUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ViewPostDetailFragmentViewModel(
    private val oauthRetrofit: Retrofit,
    private val accessToken: String?,
    private val accountName: String?
) : ViewModel() {
    val postModerationEventLiveData: SingleLiveEvent<PostModerationEvent> = SingleLiveEvent()
    val commentModerationEventLiveData: SingleLiveEvent<CommentModerationEvent> = SingleLiveEvent()

    fun approvePost(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        oauthRetrofit.create(RedditAPI::class.java)
            .approveThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.isApproved = true
                        post.approvedBy = accountName
                        post.approvedAtUTC = System.currentTimeMillis()
                        post.setRemoved(false, false)
                        postModerationEventLiveData.postValue(Approved(post, position))
                    } else {
                        postModerationEventLiveData.postValue(ApproveFailed(post, position))
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    postModerationEventLiveData.postValue(ApproveFailed(post, position))
                }
            })
    }

    fun removePost(post: Post, position: Int, isSpam: Boolean) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.SPAM_KEY] = isSpam.toString()
        oauthRetrofit.create(RedditAPI::class.java)
            .removeThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.isApproved = false
                        post.approvedBy = null
                        post.approvedAtUTC = 0
                        post.setRemoved(true, isSpam)
                        postModerationEventLiveData.postValue(
                            if (isSpam) MarkedAsSpam(
                                post,
                                position
                            ) else PostModerationEvent.Removed(post, position)
                        )
                    } else {
                        postModerationEventLiveData.postValue(
                            if (isSpam) MarkAsSpamFailed(
                                post,
                                position
                            ) else RemoveFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    postModerationEventLiveData.postValue(
                        if (isSpam) MarkAsSpamFailed(
                            post,
                            position
                        ) else RemoveFailed(post, position)
                    )
                }
            })
    }

    fun toggleSticky(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.STATE_KEY] = (!post.isStickied).toString()
        params[APIUtils.API_TYPE_KEY] = APIUtils.API_TYPE_JSON
        oauthRetrofit.create(RedditAPI::class.java)
            .toggleStickyPost(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.setIsStickied(!post.isStickied)
                        postModerationEventLiveData.postValue(
                            if (post.isStickied) SetStickyPost(
                                post,
                                position
                            ) else UnsetStickyPost(post, position)
                        )
                    } else {
                        postModerationEventLiveData.postValue(
                            if (post.isStickied) UnsetStickyPostFailed(
                                post,
                                position
                            ) else SetStickyPostFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    postModerationEventLiveData.postValue(
                        if (post.isStickied) UnsetStickyPostFailed(
                            post,
                            position
                        ) else SetStickyPostFailed(post, position)
                    )
                }
            })
    }

    fun toggleLock(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        val call: Call<String> = if (post.isLocked) oauthRetrofit.create(
            RedditAPI::class.java
        ).unLockThing(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
            RedditAPI::class.java
        ).lockThing(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    post.setIsLocked(!post.isLocked)
                    postModerationEventLiveData.postValue(
                        if (post.isLocked) Locked(
                            post,
                            position
                        ) else Unlocked(post, position)
                    )
                } else {
                    postModerationEventLiveData.postValue(
                        if (post.isLocked) UnlockFailed(
                            post,
                            position
                        ) else LockFailed(post, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                postModerationEventLiveData.postValue(
                    if (post.isLocked) UnlockFailed(
                        post,
                        position
                    ) else LockFailed(post, position)
                )
            }
        })
    }

    fun toggleNSFW(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        val call: Call<String> = if (post.isNSFW) oauthRetrofit.create(
            RedditAPI::class.java
        ).unmarkNSFW(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
            RedditAPI::class.java
        ).markNSFW(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    post.isNSFW = !post.isNSFW
                    postModerationEventLiveData.postValue(
                        if (post.isNSFW) MarkedNSFW(
                            post,
                            position
                        ) else UnmarkedNSFW(post, position)
                    )
                } else {
                    postModerationEventLiveData.postValue(
                        if (post.isNSFW) UnmarkNSFWFailed(
                            post,
                            position
                        ) else MarkNSFWFailed(post, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                postModerationEventLiveData.postValue(
                    if (post.isNSFW) UnmarkNSFWFailed(
                        post,
                        position
                    ) else MarkNSFWFailed(post, position)
                )
            }
        })
    }

    fun toggleSpoiler(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        val call: Call<String> = if (post.isSpoiler) oauthRetrofit.create(
            RedditAPI::class.java
        ).unmarkSpoiler(
            APIUtils.getOAuthHeader(accessToken),
            params
        ) else oauthRetrofit.create(
            RedditAPI::class.java
        ).markSpoiler(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    post.isSpoiler = !post.isSpoiler
                    postModerationEventLiveData.postValue(
                        if (post.isSpoiler) MarkedSpoiler(
                            post,
                            position
                        ) else UnmarkedSpoiler(post, position)
                    )
                } else {
                    postModerationEventLiveData.postValue(
                        if (post.isSpoiler) UnmarkSpoilerFailed(
                            post,
                            position
                        ) else MarkSpoilerFailed(post, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                postModerationEventLiveData.postValue(
                    if (post.isSpoiler) UnmarkSpoilerFailed(
                        post,
                        position
                    ) else MarkSpoilerFailed(post, position)
                )
            }
        })
    }

    fun toggleMod(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.HOW_KEY] = if (post.isModerator) APIUtils.HOW_NO else APIUtils.HOW_YES
        oauthRetrofit.create(RedditAPI::class.java)
            .toggleDistinguishedThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.setIsModerator(!post.isModerator)
                        postModerationEventLiveData.postValue(
                            if (post.isModerator) DistinguishedAsMod(
                                post,
                                position
                            ) else UndistinguishedAsMod(post, position)
                        )
                    } else {
                        postModerationEventLiveData.postValue(
                            if (post.isModerator) UndistinguishAsModFailed(
                                post,
                                position
                            ) else DistinguishAsModFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    postModerationEventLiveData.postValue(
                        if (post.isModerator) UndistinguishAsModFailed(
                            post,
                            position
                        ) else DistinguishAsModFailed(post, position)
                    )
                }
            })
    }

    fun approveComment(comment: Comment, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = comment.fullName
        oauthRetrofit.create(RedditAPI::class.java)
            .approveThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        comment.isApproved = true
                        comment.approvedBy = accountName
                        comment.approvedAtUTC = System.currentTimeMillis()
                        comment.setRemoved(false, false)
                        commentModerationEventLiveData.postValue(CommentModerationEvent.Approved(comment, position))
                    } else {
                        commentModerationEventLiveData.postValue(CommentModerationEvent.ApproveFailed(comment, position))
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    commentModerationEventLiveData.postValue(CommentModerationEvent.ApproveFailed(comment, position))
                }
            })
    }

    fun removeComment(comment: Comment, position: Int, isSpam: Boolean) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = comment.fullName
        params[APIUtils.SPAM_KEY] = isSpam.toString()
        oauthRetrofit.create(RedditAPI::class.java)
            .removeThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        comment.isApproved = false
                        comment.approvedBy = null
                        comment.approvedAtUTC = 0
                        comment.setRemoved(true, isSpam)
                        commentModerationEventLiveData.postValue(
                            if (isSpam) CommentModerationEvent.MarkedAsSpam(
                                comment,
                                position
                            ) else CommentModerationEvent.Removed(comment, position)
                        )
                    } else {
                        commentModerationEventLiveData.postValue(
                            if (isSpam) CommentModerationEvent.MarkAsSpamFailed(
                                comment,
                                position
                            ) else CommentModerationEvent.RemoveFailed(comment, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    commentModerationEventLiveData.postValue(
                        if (isSpam) CommentModerationEvent.MarkAsSpamFailed(
                            comment,
                            position
                        ) else CommentModerationEvent.RemoveFailed(comment, position)
                    )
                }
            })
    }

    fun toggleLock(comment: Comment, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = comment.fullName
        val call: Call<String> = if (comment.isLocked) oauthRetrofit.create(
            RedditAPI::class.java
        ).unLockThing(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
            RedditAPI::class.java
        ).lockThing(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    comment.isLocked = !comment.isLocked
                    commentModerationEventLiveData.postValue(
                        if (comment.isLocked) CommentModerationEvent.Locked(
                            comment,
                            position
                        ) else CommentModerationEvent.Unlocked(comment, position)
                    )
                } else {
                    commentModerationEventLiveData.postValue(
                        if (comment.isLocked) CommentModerationEvent.UnlockFailed(
                            comment,
                            position
                        ) else CommentModerationEvent.LockFailed(comment, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                commentModerationEventLiveData.postValue(
                    if (comment.isLocked) CommentModerationEvent.UnlockFailed(
                        comment,
                        position
                    ) else CommentModerationEvent.LockFailed(comment, position)
                )
            }
        })
    }

    companion object {
        fun provideFactory(oauthRetrofit: Retrofit, accessToken: String?, accountName: String?) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewPostDetailFragmentViewModel(
                        oauthRetrofit, accessToken, accountName
                    ) as T
                }
            }
        }
    }
}