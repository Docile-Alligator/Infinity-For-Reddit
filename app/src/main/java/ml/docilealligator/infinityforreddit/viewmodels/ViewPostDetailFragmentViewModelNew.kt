package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.AppResult
import ml.docilealligator.infinityforreddit.PostDetailCommentsCache
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.SingleLiveEvent
import ml.docilealligator.infinityforreddit.account.Account
import ml.docilealligator.infinityforreddit.apis.RedditAPI
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.comment.ParseComment
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterUsage
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
import ml.docilealligator.infinityforreddit.post.ParsePost
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData
import ml.docilealligator.infinityforreddit.subreddit.SubredditData
import ml.docilealligator.infinityforreddit.thing.SortType
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ViewPostDetailFragmentViewModelNew(
    private val retrofit: Retrofit,
    private val oauthRetrofit: Retrofit,
    private val redditDataRoomDatabase: RedditDataRoomDatabase,
    private val accessToken: String?,
    private val accountName: String?,
    private var post: Post?,
    private var postId: String?,
    private var commentId: String?,
    private val sortType: SortType.Type?,
    private val sortTypeSharedPreferences: SharedPreferences,
    var respectSubredditRecommendedSortType: Boolean,
    private val expandChildren: Boolean,
    private val contextNumber: String
) : ViewModel() {
    data class UiState(
        val sortType: SortType.Type?,
        val isInitialLoading: Boolean,
        val isInitialLoadingFailed: Boolean,
        val fetchPostFailed: Boolean,
        val isFetchingComments: Boolean,
        val isRefreshing: Boolean,
        val isLoadingMoreChildren: Boolean,
        val loadMoreChildrenSuccess: Boolean,
        val isSingleCommentThreadMode: Boolean,
        val shouldShowErrorView: Boolean,
        val singleCommentId: String?
    )

    data class DataState(
        val post: Post?,
        val comments: ArrayList<Comment>?,
        val children: ArrayList<String>?,
    ) {
        val hasMoreChildren: Boolean
            get() = children?.isEmpty() == false
    }

    data class ParseCommentsResult(
        val comments: ArrayList<Comment>,
        val topLevelComments: ArrayList<Comment>,
        val children: ArrayList<String>
    )

    sealed class SubredditError {
        data class Network(val e: Exception?) : SubredditError()
        data class Response(val code: Int, val message: String?) : SubredditError()
        object Quarantined : SubredditError()
        data class Json(val e: JSONException?) : SubredditError()
    }

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(
            sortType,
            isInitialLoading = false,
            isInitialLoadingFailed = false,
            fetchPostFailed = false,
            isFetchingComments = false,
            isRefreshing = false,
            isLoadingMoreChildren = false,
            loadMoreChildrenSuccess = true,
            isSingleCommentThreadMode = false,
            shouldShowErrorView = false,
            singleCommentId = null
        )
    )
    val uiState: LiveData<UiState> = _uiState.asLiveData()

    private val _dataState: MutableStateFlow<DataState> = MutableStateFlow(DataState(
        post, null, null
    ))
    val dataState: LiveData<DataState> = _dataState.asLiveData()

    var commentFilter: CommentFilter? = null

    val postModerationEventLiveData: SingleLiveEvent<PostModerationEvent> = SingleLiveEvent()
    val commentModerationEventLiveData: SingleLiveEvent<CommentModerationEvent> = SingleLiveEvent()

    private var fetchCommentJob: Job? = null

    val derivedPostId: String?
        get() = _dataState.value.post?.id ?: postId

    fun setPost(post: Post) {
        _dataState.value = _dataState.value.copy(
            post = post
        )
    }

    fun getPost(): Post? {
        return _dataState.value.post
    }

    fun setPostId(postId: String?) {
        this.postId = postId
    }

    fun fetchCommentsRespectRecommendedSort(
        changeRefreshState: Boolean
    ) {
        viewModelScope.launch {
            fetchCommentsRespectRecommendedSortSync(
                _uiState.value.sortType ?: updateSortType(loadSortType()), changeRefreshState,
            )
        }
    }

    fun fetchCommentsRespectRecommendedSort(
        sortType: SortType.Type,
        changeRefreshState: Boolean
    ) {
        viewModelScope.launch {
            fetchCommentsRespectRecommendedSortSync(
                sortType, changeRefreshState,
            )
        }
    }

    suspend fun fetchCommentsRespectRecommendedSortSync(
        changeRefreshState: Boolean
    ) {
        fetchCommentsRespectRecommendedSortSync(
            _uiState.value.sortType ?: updateSortType(loadSortType()), changeRefreshState,
        )
    }

    /***
     * @param sortType this is only used when failed to get the recommended sort
     */
    suspend fun fetchCommentsRespectRecommendedSortSync(
        sortType: SortType.Type,
        changeRefreshState: Boolean,
    ) {
        if (respectSubredditRecommendedSortType) {
            _dataState.value.post?.let {
                if (it.suggestedSort != null && it.suggestedSort.equals("null")) {
                    try {
                        val sortType = SortType.Type.valueOf(it.suggestedSort.uppercase())
                        updateSortType(sortType)
                        fetchComments(sortType, changeRefreshState)
                        return
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                }

                when (val subredditResult = fetchSubredditData(it.subredditName)) {
                    is AppResult.Success<*> -> {
                        when (subredditResult.data) {
                            is SubredditData -> {
                                val suggestedCommentSort: String? = subredditResult.data.suggestedCommentSort
                                if (suggestedCommentSort == null || suggestedCommentSort == "null" || suggestedCommentSort.isEmpty()) {
                                    respectSubredditRecommendedSortType = false
                                    updateSortType(loadSortType())
                                } else {
                                    try {
                                        updateSortType(SortType.Type.valueOf(suggestedCommentSort.uppercase()))
                                    } catch (e: IllegalArgumentException) {
                                        e.printStackTrace()
                                        updateSortType(loadSortType())
                                    }
                                }

                                fetchComments(changeRefreshState)
                            }
                            else -> {
                                // Null SubredditData!
                                respectSubredditRecommendedSortType = false
                                updateSortType(loadSortType())
                                fetchComments(changeRefreshState)
                            }
                        }
                    }

                    is AppResult.Error<*> -> {
                        respectSubredditRecommendedSortType = false
                        updateSortType(loadSortType())
                        fetchComments(changeRefreshState)
                    }
                }
            }
        } else {
            fetchComments(sortType, changeRefreshState)
        }
    }

    suspend fun fetchComments(
        changeRefreshState: Boolean
    ) {
        fetchComments(_uiState.value.sortType ?: updateSortType(loadSortType()), changeRefreshState)
    }

    suspend fun fetchComments(
        sortType: SortType.Type,
        changeRefreshState: Boolean
    ) {
        /*fetchCommentJob?.cancel()
        fetchCommentJob = viewModelScope.launch {



            fetchCommentJob = null
        }*/

        _uiState.value = _uiState.value.copy(
            isInitialLoading = true,
            isInitialLoadingFailed = false,
            isFetchingComments = true
        )

        val derivedPostId = derivedPostId
        if (derivedPostId == null) {
            _uiState.value = _uiState.value.copy(
                isInitialLoading = false,
                isInitialLoadingFailed = true,
                isFetchingComments = false,
                isRefreshing = if (changeRefreshState) false else _uiState.value.isRefreshing
            )
            return
        }

        val retrofit: Retrofit = if (accountName == Account.ANONYMOUS_ACCOUNT) retrofit else oauthRetrofit
        val api: RedditAPIKt = retrofit.create(RedditAPIKt::class.java)
        val response: Response<String>
        try {
            if (accountName == Account.ANONYMOUS_ACCOUNT) {
                response = commentId?.let { commentId ->
                    api.getPostAndCommentsSingleThreadById(
                        derivedPostId,
                        commentId,
                        sortType,
                        contextNumber
                    )
                } ?: api.getPostAndCommentsById(derivedPostId, sortType)
            } else {
                response = commentId?.let { commentId ->
                    api.getPostAndCommentsSingleThreadByIdOauth(
                        derivedPostId, commentId, sortType, contextNumber,
                        APIUtils.getOAuthHeader(accessToken)
                    )
                } ?: api.getPostAndCommentsByIdOauth(
                    derivedPostId,
                    sortType,
                    APIUtils.getOAuthHeader(accessToken)
                )
            }

            if (response.isSuccessful) {
                commentFilter = fetchCommentFilter(_dataState.value.post?.subredditName ?: "")
                val parseCommentsResult = parseComments(response.body(), commentFilter!!, expandChildren)
                when (parseCommentsResult) {
                    is AppResult.Success<ParseCommentsResult> -> {
                        _uiState.value = _uiState.value.copy(
                            isInitialLoading = false,
                            isInitialLoadingFailed = false,
                            isFetchingComments = false,
                            isRefreshing = if (changeRefreshState) false else _uiState.value.isRefreshing
                        )
                        _dataState.value = _dataState.value.copy(
                            comments = parseCommentsResult.data.comments,
                            children =  parseCommentsResult.data.children
                        )
                    }
                    is AppResult.Error<*> -> {
                        _uiState.value = _uiState.value.copy(
                            isInitialLoading = false,
                            isInitialLoadingFailed = true,
                            isFetchingComments = false,
                            isRefreshing = if (changeRefreshState) false else _uiState.value.isRefreshing
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isInitialLoading = false,
                    isInitialLoadingFailed = true,
                    isFetchingComments = false,
                    isRefreshing = if (changeRefreshState) false else _uiState.value.isRefreshing
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                isInitialLoading = false,
                isInitialLoadingFailed = true,
                isFetchingComments = false,
                isRefreshing = if (changeRefreshState) false else _uiState.value.isRefreshing
            )
        }
    }

    fun fetchPostAndCommentsById(
        postId: String?
    ) {
        viewModelScope.launch {
            postId?.let { postId ->
                try {
                    _uiState.value = _uiState.value.copy(
                        isInitialLoading = true,
                        isInitialLoadingFailed = false,
                        isFetchingComments = true
                    )

                    val response: Response<String>
                    if (accountName == Account.ANONYMOUS_ACCOUNT) {
                        response = _uiState.value.singleCommentId?.let { singleCommentId ->
                            if (_uiState.value.isSingleCommentThreadMode) {
                                retrofit.create(RedditAPIKt::class.java)
                                    .getPostAndCommentsSingleThreadById(
                                        postId, singleCommentId, getSortType(), contextNumber
                                    )
                            } else {
                                retrofit.create(RedditAPIKt::class.java).getPostAndCommentsById(
                                    postId,
                                    getSortType()
                                )
                            }
                        } ?: run {
                            retrofit.create(RedditAPIKt::class.java).getPostAndCommentsById(
                                postId,
                                getSortType()
                            )
                        }
                    } else {
                        response = _uiState.value.singleCommentId?.let { singleCommentId ->
                            if (_uiState.value.isSingleCommentThreadMode) {
                                oauthRetrofit.create(RedditAPIKt::class.java)
                                    .getPostAndCommentsSingleThreadByIdOauth(
                                        postId,
                                        singleCommentId,
                                        getSortType(),
                                        contextNumber,
                                        APIUtils.getOAuthHeader(accessToken)
                                    )
                            } else {
                                oauthRetrofit.create(RedditAPIKt::class.java)
                                    .getPostAndCommentsByIdOauth(
                                        postId, getSortType(), APIUtils.getOAuthHeader(accessToken)
                                    )
                            }
                        } ?: run {
                            oauthRetrofit.create(RedditAPIKt::class.java)
                                .getPostAndCommentsByIdOauth(
                                    postId, getSortType(), APIUtils.getOAuthHeader(accessToken)
                                )
                        }
                    }

                    if (response.isSuccessful) {
                        val post = parsePost(response.body())
                        post?.let { post ->
                            /*mPost = post

                            if (!renderContent()) {
                                return
                            }

                            tryMarkingPostAsRead()*/

                            _dataState.value = _dataState.value.copy(
                                post = post
                            )

                            commentFilter = fetchCommentFilter(post.subredditName)

                            if (respectSubredditRecommendedSortType) {
                                fetchCommentsRespectRecommendedSortSync(false)
                            } else {
                                val parseCommentsResult = parseComments(response.body(), commentFilter!!, expandChildren)
                                when (parseCommentsResult) {
                                    is AppResult.Success<ParseCommentsResult> -> {
                                        _dataState.value = _dataState.value.copy(
                                            comments = parseCommentsResult.data.comments,
                                            children = parseCommentsResult.data.children
                                        )
                                        _uiState.value = _uiState.value.copy(
                                            isInitialLoading = false,
                                            isInitialLoadingFailed = false,
                                            isFetchingComments = false
                                        )
                                    }
                                    is AppResult.Error<*> -> {
                                        _uiState.value = _uiState.value.copy(
                                            isInitialLoading = false,
                                            isInitialLoadingFailed = true
                                        )
                                    }
                                }
                            }
                        } ?: run {
                            _uiState.value = _uiState.value.copy(
                                shouldShowErrorView = true
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            shouldShowErrorView = true
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.value = _uiState.value.copy(
                        shouldShowErrorView = true
                    )
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    shouldShowErrorView = true
                )
            }
        }
    }

    fun fetchMoreComments() {
        viewModelScope.launch {
            if (_uiState.value.isFetchingComments || _uiState.value.isLoadingMoreChildren || !_uiState.value.loadMoreChildrenSuccess) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoadingMoreChildren = true,
                loadMoreChildrenSuccess = true
            )

            val childrenIds: String = _dataState.value.children?.let {
                java.lang.String.join(",", it)
            } ?: ""

            if (childrenIds.isEmpty()) {
                return@launch
            }

            try {
                val response: Response<String> = if (accountName == Account.ANONYMOUS_ACCOUNT) {
                    retrofit.create(RedditAPIKt::class.java).moreChildren(_dataState.value.post?.fullName, childrenIds, _uiState.value.sortType)
                } else {
                    oauthRetrofit.create(RedditAPIKt::class.java).moreChildrenOauth(
                        _dataState.value.post?.fullName, childrenIds,
                        _uiState.value.sortType, APIUtils.getOAuthHeader(accessToken)
                    )
                }

                if (response.isSuccessful) {
                    val parseCommentResult = parseMoreComments(response.body(), expandChildren)
                    when (parseCommentResult) {
                        is AppResult.Success<ParseCommentsResult> -> {
                            val updatedComments = _dataState.value.comments?.let {
                                ArrayList(it)
                            } ?: run {
                                ArrayList()
                            }
                            updatedComments.addAll(parseCommentResult.data.comments)
                            _dataState.value = _dataState.value.copy(
                                comments = updatedComments,
                                children = parseCommentResult.data.children
                            )
                            _uiState.value = _uiState.value.copy(
                                isLoadingMoreChildren = false,
                                loadMoreChildrenSuccess = true
                            )
                        }

                        is AppResult.Error<*> -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingMoreChildren = false,
                                loadMoreChildrenSuccess = false
                            )
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMoreChildren = false,
                        loadMoreChildrenSuccess = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreChildren = false,
                    loadMoreChildrenSuccess = false
                )
            }
        }
    }

    /**
     * @param position Placeholder position
     */
    fun fetchMoreChildComments(position: Int) {
        viewModelScope.launch {
            _dataState.value.comments?.getOrNull(position)?.let {
                val parentPosition = getParentPosition(position)
                _dataState.value.comments?.getOrNull(parentPosition)?.let { parentComment ->
                    if (it.isLoadingMoreChildren) {
                        return@launch
                    }

                    it.isLoadingMoreChildren = true
                    it.isLoadMoreChildrenFailed = false

                    val childrenIds: String = parentComment.moreChildrenIds?.let {
                        java.lang.String.join(",", it)
                    } ?: ""

                    if (childrenIds.isEmpty()) {
                        return@launch
                    }

                    try {
                        val response: Response<String> = if (accountName == Account.ANONYMOUS_ACCOUNT) {
                            retrofit.create(RedditAPIKt::class.java).moreChildren(_dataState.value.post?.fullName, childrenIds, _uiState.value.sortType)
                        } else {
                            oauthRetrofit.create(RedditAPIKt::class.java).moreChildrenOauth(
                                _dataState.value.post?.fullName, childrenIds,
                                _uiState.value.sortType, APIUtils.getOAuthHeader(accessToken)
                            )
                        }

                        if (response.isSuccessful) {
                            val parseCommentResult = parseMoreComments(response.body(), expandChildren)
                            val currentParentCommentPosition = findCommentPosition(parentComment.fullName, parentPosition)

                            when (parseCommentResult) {
                                is AppResult.Success<ParseCommentsResult> -> {
                                    _dataState.value.comments?.getOrNull(currentParentCommentPosition)?.let { currentParentComment ->
                                        if (currentParentComment.isExpanded) {
                                            _dataState.value.comments?.let { comments ->
                                                val copiedParentComment = Comment(currentParentComment)
                                                val updatedComments = ArrayList(comments)
                                                updatedComments[currentParentCommentPosition] = copiedParentComment

                                                if (!parseCommentResult.data.children.isEmpty()) {
                                                    copiedParentComment.moreChildrenIds = parseCommentResult.data.children
                                                    copiedParentComment.children[copiedParentComment.children.size - 1].isLoadingMoreChildren = false
                                                    copiedParentComment.children[copiedParentComment.children.size - 1].isLoadMoreChildrenFailed = false

                                                    val placeholderPosition: Int =
                                                        findLoadMoreCommentsPlaceholderPosition(
                                                            copiedParentComment.fullName,
                                                            position
                                                        )

                                                    if (placeholderPosition != -1) {
                                                        updatedComments[placeholderPosition].isLoadingMoreChildren = false
                                                        updatedComments[placeholderPosition].isLoadMoreChildrenFailed = false

                                                        updatedComments.addAll(placeholderPosition, parseCommentResult.data.comments)
                                                    }

                                                    _dataState.value = _dataState.value.copy(
                                                        comments = updatedComments
                                                    )
                                                } else {
                                                    copiedParentComment.children.removeAt(copiedParentComment.children.size - 1)
                                                    copiedParentComment.removeMoreChildrenIds()
                                                    copiedParentComment.addChildren(parseCommentResult.data.topLevelComments)

                                                    val placeholderPosition: Int =
                                                        findLoadMoreCommentsPlaceholderPosition(
                                                            copiedParentComment.fullName,
                                                            position
                                                        )

                                                    if (placeholderPosition != -1) {
                                                        updatedComments.removeAt(placeholderPosition)
                                                        updatedComments.addAll(placeholderPosition, parseCommentResult.data.comments)
                                                    }

                                                    _dataState.value = _dataState.value.copy(
                                                        comments = updatedComments
                                                    )

                                                    return@launch
                                                }
                                            }
                                        } else {
                                            if (currentParentComment.hasReply() && parseCommentResult.data.children.isEmpty()) {
                                                currentParentComment.children.removeAt(currentParentComment.children.size - 1)
                                                currentParentComment.removeMoreChildrenIds()
                                            }
                                        }

                                        currentParentComment.addChildren(parseCommentResult.data.topLevelComments)
                                    }
                                }

                                is AppResult.Error<*> -> {
                                    fetchMoreChildCommentsFailed(parentComment, currentParentCommentPosition)
                                }
                            }
                        } else {
                            fetchMoreChildCommentsFailed(parentComment, findCommentPosition(parentComment.fullName, parentPosition))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fetchMoreChildCommentsFailed(parentComment, findCommentPosition(parentComment.fullName, parentPosition))
                    }
                } ?: run {
                    _dataState.value.comments?.let { currentComments ->
                        val updatedComments = ArrayList(currentComments)
                        val copiedPlaceholder = Comment(currentComments[position])
                        copiedPlaceholder.isLoadingMoreChildren = false
                        copiedPlaceholder.isLoadMoreChildrenFailed = true
                        currentComments[position] = copiedPlaceholder
                        _dataState.value = _dataState.value.copy(
                            comments = updatedComments
                        )
                    }
                }
            }
        }
    }

    private fun fetchMoreChildCommentsFailed(parentComment: Comment, parentPosition: Int) {
        _dataState.value.comments?.let { comments ->
            if (parentPosition == -1) {
                // note: returning here is probably a mistake, because
                // parent is just not visible, but it can still exist in the comments tree.
                return
            }
            val currentParentComment: Comment = comments[parentPosition]

            val updatedComments = ArrayList(comments)

            if (currentParentComment.isExpanded) {
                val placeholderPositionHint =
                    parentPosition + currentParentComment.children.size
                val placeholderPosition =
                    findLoadMoreCommentsPlaceholderPosition(
                        parentComment.fullName,
                        placeholderPositionHint
                    )

                if (placeholderPosition != -1) {
                    val copiedPlaceholder = Comment(comments[placeholderPosition])
                    copiedPlaceholder.isLoadingMoreChildren = false
                    copiedPlaceholder.isLoadMoreChildrenFailed = true

                    updatedComments[placeholderPosition] = copiedPlaceholder
                    currentParentComment.children[currentParentComment.children.size - 1] = copiedPlaceholder
                }
            }
            currentParentComment.children[currentParentComment.children.size - 1].isLoadingMoreChildren = false
            currentParentComment.children[currentParentComment.children.size - 1].isLoadMoreChildrenFailed = true

            _dataState.value = _dataState.value.copy(
                comments = updatedComments
            )
        }
    }

    private fun getParentPosition(position: Int): Int {
        _dataState.value.comments?.let {
            if (position >= 0 && position < it.size) {
                val childDepth: Int = it[position].depth
                for (i in position downTo 0) {
                    if (it[i].depth < childDepth) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    private fun findCommentPosition(fullName: String?, positionHint: Int): Int {
        return findCommentPosition(fullName, positionHint, Comment.NOT_PLACEHOLDER)
    }

    private fun findCommentPosition(
        fullName: String?,
        positionHint: Int,
        placeholderType: Int
    ): Int {
        _dataState.value.comments?.let {
            if (0 <= positionHint && positionHint < it.size && it.get(
                    positionHint
                ).fullName == fullName
                && it[positionHint].placeholderType == placeholderType
            ) {
                return positionHint
            }

            for (i in it.indices) {
                val comment: Comment = it[i]
                if (comment.fullName == fullName && comment.placeholderType == placeholderType) {
                    return i
                }
            }
        }
        return -1
    }

    private fun findLoadMoreCommentsPlaceholderPosition(fullName: String?, positionHint: Int): Int {
        return findCommentPosition(fullName, positionHint, Comment.PLACEHOLDER_LOAD_MORE_COMMENTS)
    }

    suspend fun fetchCommentFilter(subredditName: String): CommentFilter {
        val commentFilterList: List<CommentFilter> =
            redditDataRoomDatabase.commentFilterDaoKt()
                .getValidCommentFilters(CommentFilterUsage.SUBREDDIT_TYPE, subredditName)
        return CommentFilter.mergeCommentFilter(commentFilterList)
    }

    suspend fun fetchSubredditData(subredditName: String): AppResult<SubredditData?, SubredditError> {
        try {
            val response: Response<String> = if (accountName == Account.ANONYMOUS_ACCOUNT) {
                retrofit.create(RedditAPIKt::class.java).getSubredditData(subredditName)
            } else {
                oauthRetrofit.create(RedditAPIKt::class.java).getSubredditDataOauth(
                    subredditName, APIUtils.getOAuthHeader(accessToken)
                )
            }
            if (response.isSuccessful) {
                return withContext(Dispatchers.Default) {
                    try {
                        val data: JSONObject = JSONObject(response.body()).getJSONObject(JSONUtils.DATA_KEY)
                        AppResult.Success(ParseSubredditData.parseSubredditDataSync(data, true))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        AppResult.Error(SubredditError.Json(e))
                    }
                }
            } else {
                return if (response.code() == 403) {
                    AppResult.Error(SubredditError.Quarantined)
                } else {
                    AppResult.Error(SubredditError.Response(response.code(), response.errorBody()?.string()))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return AppResult.Error(SubredditError.Network(e))
        }
    }

    fun refresh(fetchPost: Boolean, fetchComments: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = true
                )

                if (!fetchPost && fetchComments) {
                    fetchCommentsRespectRecommendedSortSync(true)
                }

                if (fetchPost) {
                    derivedPostId?.let {
                        try {
                            val response: Response<String> = if (accountName == Account.ANONYMOUS_ACCOUNT) {
                                retrofit.create(RedditAPIKt::class.java).getPost(it)
                            } else {
                                oauthRetrofit.create(RedditAPIKt::class.java)
                                    .getPostOauth(it, APIUtils.getOAuthHeader(accessToken))
                            }

                            if (response.isSuccessful) {
                                val post = withContext(Dispatchers.Default) {
                                    ParsePost.parsePostSync(response.body())
                                }
                                post?.let {
                                    _dataState.value = _dataState.value.copy(
                                        post = it
                                    )

                                    if (fetchComments) {
                                        fetchCommentsRespectRecommendedSortSync(true)
                                    } else {
                                        _uiState.value = _uiState.value.copy(
                                            isRefreshing = false
                                        )
                                    }
                                } ?: run {
                                    _uiState.value = _uiState.value.copy(
                                        fetchPostFailed = true,
                                        isRefreshing = false
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    fetchPostFailed = true,
                                    isRefreshing = false
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            _uiState.value = _uiState.value.copy(
                                fetchPostFailed = true,
                                isRefreshing = false
                            )
                        }
                    } ?: run {
                        _uiState.value = _uiState.value.copy(
                            fetchPostFailed = true,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun parseComments(
        response: String?,
        commentFilter: CommentFilter,
        expandChildren: Boolean
    ): AppResult<ParseCommentsResult, JSONException> {
        return withContext(Dispatchers.Default) {
            try {
                var childrenArray = JSONArray(response)
                val parentId = childrenArray.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY)
                    .getJSONArray(JSONUtils.CHILDREN_KEY)
                    .getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.NAME_KEY)
                childrenArray = childrenArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY)
                    .getJSONArray(JSONUtils.CHILDREN_KEY)

                val expandedNewComments = ArrayList<Comment>()
                val moreChildrenIds = ArrayList<String>()
                val newComments = ArrayList<Comment>()

                ParseComment.parseCommentRecursion(
                    childrenArray,
                    newComments,
                    moreChildrenIds,
                    0,
                    commentFilter
                )
                ParseComment.expandChildren(newComments, expandedNewComments, expandChildren)
                val commentData = if (expandChildren) {
                    expandedNewComments
                } else {
                    newComments
                }

                AppResult.Success(ParseCommentsResult(
                    commentData,
                    newComments,
                    moreChildrenIds
                ))
            } catch (e: JSONException) {
                e.printStackTrace()
                AppResult.Error(e)
            }
        }
    }

    private suspend fun parseMoreComments(
        response: String?,
        expandChildren: Boolean
    ): AppResult<ParseCommentsResult, JSONException> {
        return withContext(Dispatchers.Default) {
            try {
                val childrenArray: JSONArray =
                    JSONObject(response).getJSONObject(JSONUtils.JSON_KEY)
                        .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.THINGS_KEY)

                val newComments = ArrayList<Comment>()
                val expandedNewComments = ArrayList<Comment>()
                val moreChildrenIds = ArrayList<String>()

                // api response is a flat list of comments tree
                // process it in order and rebuild the tree
                for (i in 0..<childrenArray.length()) {
                    val child = childrenArray.getJSONObject(i)
                    val childData = child.getJSONObject(JSONUtils.DATA_KEY)
                    if (child.getString(JSONUtils.KIND_KEY) == JSONUtils.KIND_VALUE_MORE) {
                        val parentFullName = childData.getString(JSONUtils.PARENT_ID_KEY)
                        val childrenIds = childData.getJSONArray(JSONUtils.CHILDREN_KEY)

                        if (childrenIds.length() != 0) {
                            val localMoreChildrenIds =
                                ArrayList<String>(childrenIds.length())
                            for (j in 0..<childrenIds.length()) {
                                localMoreChildrenIds.add(childrenIds.getString(j))
                            }

                            val parentComment =
                                ParseComment.findCommentByFullName(newComments, parentFullName)
                            if (parentComment != null) {
                                parentComment.setHasReply(true)
                                parentComment.moreChildrenIds = localMoreChildrenIds
                                parentComment.addChildren(java.util.ArrayList<Comment>()) // ensure children list is not null
                            } else {
                                // assume that it is parent of this call
                                moreChildrenIds.addAll(localMoreChildrenIds)
                            }
                        } else {
                            val continueThreadPlaceholder = Comment(
                                parentFullName,
                                childData.getInt(JSONUtils.DEPTH_KEY),
                                Comment.PLACEHOLDER_CONTINUE_THREAD
                            )

                            val parentComment =
                                ParseComment.findCommentByFullName(newComments, parentFullName)
                            if (parentComment != null) {
                                parentComment.setHasReply(true)
                                parentComment.addChild(
                                    continueThreadPlaceholder,
                                    parentComment.getChildCount()
                                )
                                parentComment.setChildCount(parentComment.getChildCount() + 1)
                            } else {
                                // assume that it is parent of this call
                                newComments.add(continueThreadPlaceholder)
                            }
                        }
                    } else {
                        try {
                            val comment = ParseComment.parseSingleComment(childData, 0)
                            val parentFullName = comment.getParentId()

                            val parentComment =
                                ParseComment.findCommentByFullName(newComments, parentFullName)
                            if (parentComment != null) {
                                parentComment.setHasReply(true)
                                parentComment.addChild(comment, parentComment.getChildCount())
                                parentComment.setChildCount(parentComment.getChildCount() + 1)
                            } else {
                                // assume that it is parent of this call
                                newComments.add(comment)
                            }
                        } catch (e: JSONException) {
                            // Well we need to catch and ignore the exception to not show "error loading comments" to users
                            e.printStackTrace()
                        }
                    }
                }

                ParseComment.updateChildrenCount(newComments)
                ParseComment.expandChildren(newComments, expandedNewComments, expandChildren)
                val commentData = if (expandChildren) {
                    expandedNewComments
                } else {
                    newComments
                }

                AppResult.Success(
                    ParseCommentsResult(commentData, newComments, moreChildrenIds)
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                AppResult.Error(e)
            }
        }
    }

    @Nullable
    private suspend fun parsePost(response: String?): Post? {
        return withContext(Dispatchers.Default) {
            val allData: JSONArray =
                JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY)
                    .getJSONArray(JSONUtils.CHILDREN_KEY)
            if (allData.length() == 0) {
                null
            } else {
                val data = allData.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY)
                ParsePost.parseBasicData(data)
            }
        }
    }

    private fun loadSortType(): SortType.Type {
        val sortTypeName: String = sortTypeSharedPreferences.getString(
            SharedPreferencesUtils.SORT_TYPE_POST_COMMENT,
            SortType.Type.CONFIDENCE.name
        )!!
        if (SortType.Type.BEST.name == sortTypeName) {
            // migrate from BEST to CONFIDENCE
            // key guaranteed to exist because got non-default value
            sortTypeSharedPreferences.edit {
                putString(
                    SharedPreferencesUtils.SORT_TYPE_POST_COMMENT,
                    SortType.Type.CONFIDENCE.name
                )
            }
            return SortType.Type.CONFIDENCE
        }
        return SortType.Type.valueOf(sortTypeName)
    }

    fun updateSortType(sortType: SortType.Type): SortType.Type {
        _uiState.value = _uiState.value.copy(
            sortType = sortType
        )
        return sortType
    }

    fun getSortType(): SortType.Type {
        return _uiState.value.sortType ?: updateSortType(loadSortType())
    }

    fun expandComment(position: Int) {
        _dataState.value.comments?.let { comments ->
            val comment = comments.getOrNull(position)
            comment?.let {
                if (it.isExpanded) {
                    collapseComment(position)
                } else {
                    val updatedComment = Comment(it)
                    updatedComment.setExpanded(true)

                    val newList = ArrayList<Comment>()
                    expandComment(it.children, newList)

                    val updatedComments = ArrayList(comments)
                    updatedComments[position] = updatedComment
                    updatedComments.addAll(position + 1, newList)

                    _dataState.value = _dataState.value.copy(
                        comments = updatedComments
                    )
                }
            }
        }
    }

    private fun expandComment(
        comments: ArrayList<Comment>?,
        newList: ArrayList<Comment>
    ) {
        if (!comments.isNullOrEmpty()) {
            for (comment in comments) {
                newList.add(comment)
                expandComment(comment.children, newList)
                comment.setExpanded(true)
            }
        }
    }

    fun collapseComment(position: Int) {
        _dataState.value.comments?.let { comments ->
            val comment = comments.getOrNull(position)
            comment?.let {
                val updatedComment = Comment(it)
                updatedComment.setExpanded(false)

                val depth: Int = it.depth
                var allChildrenSize = 0
                for (i in position + 1..<comments.size) {
                    if (comments[i].depth > depth) {
                        allChildrenSize++
                    } else {
                        break
                    }
                }

                val updatedComments = ArrayList(comments)
                updatedComments[position] = updatedComment

                if (allChildrenSize > 0) {
                    updatedComments.subList(position + 1, position + 1 + allChildrenSize).clear()
                }

                _dataState.value = _dataState.value.copy(
                    comments = updatedComments
                )
            }
        }
    }

    fun addComment(comment: Comment) {
        _dataState.value.comments?.let {
            val updatedComments = ArrayList(it)
            updatedComments.add(0, comment)
            _dataState.value = _dataState.value.copy(
                comments = updatedComments
            )
        } ?: run {
            val updatedComments = ArrayList<Comment>()
            updatedComments.add(comment)
            _dataState.value = _dataState.value.copy(
                comments = updatedComments
            )
        }
    }

    fun addChildComment(comment: Comment, parentFullname: String?, parentPosition: Int) {
        _dataState.value.comments?.let {
            var finalParentPosition = parentPosition
            var parentComment = it[finalParentPosition]
            if (parentFullname != parentComment.fullName) {
                for (i in it.indices) {
                    if (parentFullname == it[i].fullName) {
                        parentComment = it[i]
                        finalParentPosition = i
                        break
                    }
                }
            }

            val updatedParentComment = Comment(parentComment)
            updatedParentComment.addChild(comment)
            updatedParentComment.setHasReply(true)

            val updatedComments = ArrayList(it)
            updatedComments[finalParentPosition] = updatedParentComment
            if (!updatedParentComment.isExpanded) {
                val newList = ArrayList<Comment>()

                expandComment(updatedParentComment.children, newList)

                updatedParentComment.setExpanded(true)
                updatedComments.addAll(finalParentPosition + 1, newList)
            } else {
                updatedComments.add(finalParentPosition + 1, comment)
            }

            _dataState.value = _dataState.value.copy(
                comments = updatedComments
            )
        }
    }

    fun restoreCache(cache: PostDetailCommentsCache) {
        if (_dataState.value.post == null) {
            _dataState.value = _dataState.value.copy(
                post = cache.post,
                comments = cache.visibleComments,
                children = cache.children
            )

            return
        }
        _dataState.value = _dataState.value.copy(
            comments = cache.visibleComments,
            children = cache.children
        )
    }

    fun editComment(comment: Comment, position: Int) {
        _dataState.value.comments?.let {
            val updatedComments = ArrayList(it)

            if (position < it.size && position >= 0) {
                val oldComment: Comment = it[position]
                if (oldComment.id.equals(comment.id)) {
                    val updatedComment = Comment(oldComment)
                    updatedComment.commentMarkdown = comment.commentMarkdown
                    updatedComment.mediaMetadataMap = comment.mediaMetadataMap

                    updatedComments[position] = updatedComment

                    _dataState.value = _dataState.value.copy(
                        comments = updatedComments
                    )
                } else {
                    val currentPosition = findCommentPosition(comment.fullName, position)
                    if (currentPosition >= 0 && currentPosition < it.size) {
                        val updatedComment = Comment(it[currentPosition])
                        updatedComment.commentMarkdown = comment.commentMarkdown
                        updatedComment.mediaMetadataMap = comment.mediaMetadataMap

                        updatedComments[currentPosition] = updatedComment

                        _dataState.value = _dataState.value.copy(
                            comments = updatedComments
                        )
                    }
                }
            }
        }
    }

    fun editComment(commentContentMarkdown: String?, position: Int) {
        _dataState.value.comments?.let {
            if (position < it.size && position >= 0) {
                val updatedComments = ArrayList(it)
                val updatedComment = Comment(it[position])
                updatedComment.commentMarkdown = commentContentMarkdown

                updatedComments[position] = updatedComment

                _dataState.value = _dataState.value.copy(
                    comments = updatedComments
                )
            }
        }
    }

    fun deleteComment(position: Int) {
        _dataState.value.comments?.let {
            if (position >= 0 && position < it.size) {
                val updatedComments = ArrayList(it)
                if (it[position].hasReply()) {
                    val updatedComment = Comment(it[position])
                    updatedComment.author = "[deleted]"
                    updatedComment.commentMarkdown = "[deleted]"

                    updatedComments[position] = updatedComment
                } else {
                    updatedComments.removeAt(position)
                }

                _dataState.value = _dataState.value.copy(
                    comments = updatedComments
                )
            }
        }
    }

    fun approvePost(post: Post, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = post.fullName
            try {
                val response = oauthRetrofit.create(RedditAPIKt::class.java)
                    .approveThing(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    post.isApproved = true
                    post.approvedBy = accountName
                    post.approvedAtUTC = System.currentTimeMillis()
                    post.setRemoved(false, false)

                    setPost(post)

                    postModerationEventLiveData.postValue(Approved(post, position))
                } else {
                    postModerationEventLiveData.postValue(ApproveFailed(post, position))
                }
            } catch (e: Exception) {
                e.printStackTrace()

                postModerationEventLiveData.postValue(ApproveFailed(post, position))
            }
        }
    }

    fun removePost(post: Post, position: Int, isSpam: Boolean) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = post.fullName
            params[APIUtils.SPAM_KEY] = isSpam.toString()
            try {
                val response = oauthRetrofit.create(RedditAPIKt::class.java)
                    .removeThing(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    post.isApproved = false
                    post.approvedBy = null
                    post.approvedAtUTC = 0
                    post.setRemoved(true, isSpam)

                    setPost(post)

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
            } catch (e: Exception) {
                e.printStackTrace()

                postModerationEventLiveData.postValue(
                    if (isSpam) MarkAsSpamFailed(
                        post,
                        position
                    ) else RemoveFailed(post, position)
                )
            }
        }
    }

    fun toggleSticky(post: Post, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = post.fullName
            params[APIUtils.STATE_KEY] = (!post.isStickied).toString()
            params[APIUtils.API_TYPE_KEY] = APIUtils.API_TYPE_JSON
            try {
                val response = oauthRetrofit.create(RedditAPIKt::class.java)
                    .toggleStickyPost(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    post.setIsStickied(!post.isStickied)

                    setPost(post)

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
            } catch (e: Exception) {
                e.printStackTrace()
                postModerationEventLiveData.postValue(
                    if (post.isStickied) UnsetStickyPostFailed(
                        post,
                        position
                    ) else SetStickyPostFailed(post, position)
                )
            }
        }
    }

    fun toggleLock(post: Post, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = post.fullName
            try {
                val response = if (post.isLocked) oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).unLockThing(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).lockThing(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    post.setIsLocked(!post.isLocked)

                    setPost(post)

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
            } catch (e: Exception) {
                e.printStackTrace()
                postModerationEventLiveData.postValue(
                    if (post.isLocked) UnlockFailed(
                        post,
                        position
                    ) else LockFailed(post, position)
                )
            }
        }
    }

    fun toggleNSFW(post: Post, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = post.fullName
            try {
                val response = if (post.isNSFW) oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).unmarkNSFW(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).markNSFW(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    post.isNSFW = !post.isNSFW

                    setPost(post)

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
            } catch (e: Exception) {
                e.printStackTrace()
                postModerationEventLiveData.postValue(
                    if (post.isNSFW) UnmarkNSFWFailed(
                        post,
                        position
                    ) else MarkNSFWFailed(post, position)
                )
            }
        }
    }

    fun toggleSpoiler(post: Post, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = post.fullName
            try {
                val response = if (post.isSpoiler) oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).unmarkSpoiler(
                    APIUtils.getOAuthHeader(accessToken),
                    params
                ) else oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).markSpoiler(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    post.isSpoiler = !post.isSpoiler

                    setPost(post)

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
            } catch (e: Exception) {
                e.printStackTrace()
                postModerationEventLiveData.postValue(
                    if (post.isSpoiler) UnmarkSpoilerFailed(
                        post,
                        position
                    ) else MarkSpoilerFailed(post, position)
                )
            }
        }
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

    fun toggleNotification(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.STATE_KEY] = (!post.isSendReplies).toString()
        oauthRetrofit.create(RedditAPI::class.java)
            .toggleRepliesNotification(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.isSendReplies = !post.isSendReplies
                        postModerationEventLiveData.postValue(
                            if (post.isSendReplies) PostModerationEvent.SetReceiveNotification(
                                post,
                                position
                            ) else PostModerationEvent.UnsetReceiveNotification(post, position)
                        )
                    } else {
                        postModerationEventLiveData.postValue(
                            if (post.isSendReplies) PostModerationEvent.UnsetReceiveNotificationFailed(
                                post,
                                position
                            ) else PostModerationEvent.SetReceiveNotificationFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    postModerationEventLiveData.postValue(
                        if (post.isSendReplies) PostModerationEvent.UnsetReceiveNotificationFailed(
                            post,
                            position
                        ) else PostModerationEvent.SetReceiveNotificationFailed(post, position)
                    )
                }
            })
    }

    fun approveComment(comment: Comment, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = comment.fullName
            try {
                val response = oauthRetrofit.create(RedditAPIKt::class.java)
                    .approveThing(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    comment.isApproved = true
                    comment.approvedBy = accountName
                    comment.approvedAtUTC = System.currentTimeMillis()
                    comment.setRemoved(false, false)

                    updateModdedStatus(comment, position)

                    commentModerationEventLiveData.postValue(CommentModerationEvent.Approved(comment, position))
                } else {
                    commentModerationEventLiveData.postValue(CommentModerationEvent.ApproveFailed(comment, position))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                commentModerationEventLiveData.postValue(CommentModerationEvent.ApproveFailed(comment, position))
            }
        }
    }

    fun removeComment(comment: Comment, position: Int, isSpam: Boolean) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = comment.fullName
            params[APIUtils.SPAM_KEY] = isSpam.toString()
            try {
                val response = oauthRetrofit.create(RedditAPIKt::class.java)
                    .removeThing(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    comment.isApproved = false
                    comment.approvedBy = null
                    comment.approvedAtUTC = 0
                    comment.setRemoved(true, isSpam)

                    updateModdedStatus(comment, position)

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
            } catch (e: Exception) {
                e.printStackTrace()
                commentModerationEventLiveData.postValue(
                    if (isSpam) CommentModerationEvent.MarkAsSpamFailed(
                        comment,
                        position
                    ) else CommentModerationEvent.RemoveFailed(comment, position)
                )
            }
        }
    }

    fun toggleLock(comment: Comment, position: Int) {
        viewModelScope.launch {
            val params: MutableMap<String, String> = HashMap()
            params[APIUtils.ID_KEY] = comment.fullName
            try {
                val response = if (comment.isLocked) oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).unLockThing(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
                    RedditAPIKt::class.java
                ).lockThing(APIUtils.getOAuthHeader(accessToken), params)

                if (response.isSuccessful) {
                    comment.isLocked = !comment.isLocked

                    updateModdedStatus(comment, position)

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
            } catch (e: Exception) {
                e.printStackTrace()
                commentModerationEventLiveData.postValue(
                    if (comment.isLocked) CommentModerationEvent.UnlockFailed(
                        comment,
                        position
                    ) else CommentModerationEvent.LockFailed(comment, position)
                )
            }
        }
    }

    fun updateModdedStatus(comment: Comment, position: Int) {
        _dataState.value.comments?.let { comments ->
            comments.getOrNull(position)?.let {
                val updatedComment = Comment(it)
                if (updatedComment.fullName == comment.fullName) {
                    updatedComment.isApproved = comment.isApproved
                    updatedComment.approvedAtUTC = comment.approvedAtUTC
                    updatedComment.approvedBy = comment.approvedBy
                    updatedComment.setRemoved(comment.isRemoved, comment.isSpam)
                    updatedComment.isLocked = comment.isLocked

                    val updatedComments = ArrayList(comments)
                    updatedComments[position] = updatedComment

                    _dataState.value = _dataState.value.copy(
                        comments = updatedComments
                    )
                    return
                }
            }

            val correctPosition = findCommentPosition(comment.fullName, position)
            comments.getOrNull(correctPosition)?.let {
                val updatedComment = Comment(it)
                updatedComment.isApproved = comment.isApproved
                updatedComment.approvedAtUTC = comment.approvedAtUTC
                updatedComment.approvedBy = comment.approvedBy
                updatedComment.setRemoved(comment.isRemoved, comment.isSpam)
                updatedComment.isLocked = comment.isLocked

                val updatedComments = ArrayList(comments)
                updatedComments[correctPosition] = updatedComment

                _dataState.value = _dataState.value.copy(
                    comments = updatedComments
                )
            }
        }
    }

    companion object {
        fun provideFactory(retrofit: Retrofit, oauthRetrofit: Retrofit,
                           redditDataRoomDatabase: RedditDataRoomDatabase,
                           accessToken: String?, accountName: String?,
                           post: Post?, postId: String?, commentId: String?,
                           sortTypeSharedPreferences: SharedPreferences,
                           respectSubredditRecommendedSortType: Boolean,
                           expandChildren: Boolean, contextNumber: String) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewPostDetailFragmentViewModelNew(
                        retrofit, oauthRetrofit, redditDataRoomDatabase, accessToken, accountName,
                        post, postId, commentId, null, sortTypeSharedPreferences,
                        respectSubredditRecommendedSortType,
                        expandChildren, contextNumber
                    ) as T
                }
            }
        }
    }
}