package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.account.Account
import ml.docilealligator.infinityforreddit.apis.RedditAPIKt
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.LoadingMorePostsStatus
import ml.docilealligator.infinityforreddit.post.ParsePost
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.post.PostType
import ml.docilealligator.infinityforreddit.postfilter.PostFilter
import ml.docilealligator.infinityforreddit.readpost.NullReadPostsList
import ml.docilealligator.infinityforreddit.readpost.ReadPost
import ml.docilealligator.infinityforreddit.readpost.ReadPostType
import ml.docilealligator.infinityforreddit.readpost.ReadPostsListInterface
import ml.docilealligator.infinityforreddit.thing.SortType
import ml.docilealligator.infinityforreddit.user.UserProfileImagesBatchLoader
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.JSONUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit

class ViewPostDetailActivityViewModel(
    private val retrofit: Retrofit,
    private val oauthRetrofit: Retrofit,
    private val redditDataRoomDatabase: RedditDataRoomDatabase,
    private val loader: UserProfileImagesBatchLoader
) : ViewModel() {
    var post: Post? = null

    var posts: ArrayList<Post>? = null

    private var _loadMorePostsState = MutableStateFlow(LoadMorePostsState(LoadingMorePostsStatus.NOT_LOADING, 0))
    val loadMorePostsState = _loadMorePostsState.asLiveData()

    data class LoadMorePostsState(
        val status: Int,
        val nNewPosts: Int = 0,
        val changePage: Boolean = false
    )

    fun getPost(index: Int): Post? {
        return posts?.getOrNull(index)
    }

    fun loadAuthorImages(comments: MutableList<Comment?>, loadIconListener: LoadIconListener) {
        loader.loadAuthorImages(comments, loadIconListener)
    }

    fun fetchMorePosts(
        accessToken: String?,
        accountName: String,
        changePage: Boolean,
        postType: Int,
        subredditName: String?,
        concatenatedSubredditNames: String?,
        username: String?,
        userWhere: String?,
        multiPath: String?,
        query: String?,
        sortType: SortType.Type,
        sortTime: SortType.Time?,
        postFilter: PostFilter?,
        @ReadPostType readPostType: Int,
        readPostsList: ReadPostsListInterface?
    ) {
        viewModelScope.launch {
            if (_loadMorePostsState.value.status == LoadingMorePostsStatus.LOADING
                || _loadMorePostsState.value.status == LoadingMorePostsStatus.NO_MORE_POSTS) {
                return@launch
            }

            _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.LOADING)

            if (postType != PostType.READ_POSTS) {
                try {
                    val api: RedditAPIKt =
                        (if (accountName == Account.ANONYMOUS_ACCOUNT) retrofit else oauthRetrofit).create(
                            RedditAPIKt::class.java
                        )
                    val response: Response<String>?
                    val afterKey = posts?.let {
                        it.lastOrNull()?.fullName
                    }
                    when (postType) {
                        PostType.SUBREDDIT -> response = subredditName?.let {
                            if (accountName == Account.ANONYMOUS_ACCOUNT) {
                                api.getSubredditBestPosts(
                                    subredditName, sortType, sortTime, afterKey,
                                    APIUtils.subredditAPICallLimit(subredditName)
                                )
                            } else {
                                api.getSubredditBestPostsOauth(
                                    subredditName, sortType,
                                    sortTime, afterKey, APIUtils.subredditAPICallLimit(subredditName),
                                    APIUtils.getOAuthHeader(accessToken)
                                )
                            }
                        }

                        PostType.USER -> response = username?.let {
                            if (accountName == Account.ANONYMOUS_ACCOUNT) {
                                api.getUserPosts(username, afterKey, sortType, sortTime)
                            } else {
                                userWhere?.let {
                                    api.getUserPostsOauth(
                                        username, userWhere, afterKey, sortType,
                                        sortTime, APIUtils.getOAuthHeader(accessToken)
                                    )
                                }
                            }
                        }

                        PostType.SEARCH -> response = if (subredditName == null) {
                            if (accountName == Account.ANONYMOUS_ACCOUNT) {
                                api.searchPosts(
                                    query, afterKey, sortType, sortTime
                                )
                            } else {
                                api.searchPostsOauth(
                                    query, afterKey, sortType,
                                    sortTime, APIUtils.getOAuthHeader(accessToken)
                                )
                            }
                        } else {
                            if (accountName == Account.ANONYMOUS_ACCOUNT) {
                                api.searchPostsInSpecificSubreddit(
                                    subredditName, query,
                                    sortType, sortTime, afterKey
                                )
                            } else {
                                api.searchPostsInSpecificSubredditOauth(
                                    subredditName, query,
                                    sortType, sortTime, afterKey,
                                    APIUtils.getOAuthHeader(accessToken)
                                )
                            }
                        }

                        PostType.MULTIREDDIT -> response = multiPath?.let {
                            if (accountName == Account.ANONYMOUS_ACCOUNT) {
                                api.getMultiRedditPosts(multiPath, afterKey, sortTime)
                            } else {
                                api.getMultiRedditPostsOauth(
                                    multiPath, afterKey,
                                    sortTime, APIUtils.getOAuthHeader(accessToken)
                                )
                            }
                        }

                        PostType.ANONYMOUS_FRONT_PAGE, PostType.ANONYMOUS_MULTIREDDIT -> response = concatenatedSubredditNames?.let {
                            api.getAnonymousFrontPageOrMultiredditPosts(
                                concatenatedSubredditNames, sortType,
                                sortTime, afterKey, APIUtils.subredditAPICallLimit(subredditName),
                                APIUtils.ANONYMOUS_USER_AGENT
                            )
                        }

                        else -> response = api.getBestPosts(
                            sortType, sortTime, afterKey,
                            APIUtils.getOAuthHeader(accessToken)
                        )
                    }

                    if (response?.isSuccessful == true) {
                        val newPosts = withContext(Dispatchers.Default) {
                            parsePostsSync(response.body(), -1, postFilter, readPostsList)
                        }
                        if (newPosts == null) {
                            _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.NO_MORE_POSTS)
                        } else {
                            val postLinkedHashSet = LinkedHashSet<Post?>(posts)
                            val currentPostsSize = postLinkedHashSet.size
                            postLinkedHashSet.addAll(newPosts)
                            if (currentPostsSize == postLinkedHashSet.size) {
                                _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.NO_MORE_POSTS)
                            } else {
                                posts = java.util.ArrayList<Post>(postLinkedHashSet)
                                _loadMorePostsState.value = LoadMorePostsState(
                                    LoadingMorePostsStatus.LOADED,
                                    postLinkedHashSet.size - currentPostsSize,
                                    changePage
                                )
                            }
                        }
                    } else {
                        _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.FAILED)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.FAILED)
                }
            } else {
                val lastItem: Long = posts?.let {
                    if (!it.isEmpty()) {
                        redditDataRoomDatabase.readPostDaoKt()
                            .getReadPost(it.lastOrNull()?.id ?: "")?.time
                    } else {
                        0
                    }
                } ?: 0
                val readPosts: MutableList<ReadPost> = redditDataRoomDatabase.readPostDaoKt()
                    .getAllReadPosts(accountName, lastItem, readPostType)
                val ids = StringBuilder()
                for (readPost in readPosts) {
                    ids.append("t3_").append(readPost.id).append(",")
                }
                if (ids.isNotEmpty()) {
                    ids.deleteCharAt(ids.length - 1)
                }

                try {
                    val response = if (accountName == Account.ANONYMOUS_ACCOUNT) {
                        oauthRetrofit.create(RedditAPIKt::class.java)
                            .getInfoOauth(ids.toString(), APIUtils.getOAuthHeader(accessToken))
                    } else {
                        retrofit.create(RedditAPIKt::class.java).getInfo(ids.toString())
                    }

                    if (response.isSuccessful) {
                        val responseString = response.body()
                        val newPosts = withContext(Dispatchers.Default) {
                            parsePostsSync(
                                responseString,
                                -1,
                                postFilter,
                                NullReadPostsList.getInstance()
                            )
                        }
                        if (newPosts.isNullOrEmpty()) {
                            _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.NO_MORE_POSTS)
                        } else {
                            val postLinkedHashSet = LinkedHashSet<Post?>(posts)
                            val currentPostsSize = postLinkedHashSet.size
                            postLinkedHashSet.addAll(newPosts)
                            if (currentPostsSize == postLinkedHashSet.size) {
                                _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.NO_MORE_POSTS)
                            } else {
                                posts = java.util.ArrayList<Post>(postLinkedHashSet)
                                _loadMorePostsState.value = LoadMorePostsState(
                                    LoadingMorePostsStatus.LOADED,
                                    postLinkedHashSet.size - currentPostsSize,
                                    changePage
                                )
                            }
                        }
                    } else {
                        _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.FAILED)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loadMorePostsState.value = LoadMorePostsState(LoadingMorePostsStatus.FAILED)
                }
            }
        }
    }

    fun parsePostsSync(
        response: String?,
        nPosts: Int,
        postFilter: PostFilter?,
        readPostsList: ReadPostsListInterface?
    ): java.util.LinkedHashSet<Post>? {
        val newPosts = java.util.LinkedHashSet<Post>()
        try {
            val jsonResponse = JSONObject(response ?: "")
            val allPostsData =
                jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY)

            //Posts listing
            val numberOfPosts =
                if (nPosts < 0 || nPosts > allPostsData.length()) allPostsData.length() else nPosts

            val newPostsIds = java.util.ArrayList<String?>()
            for (i in 0..<numberOfPosts) {
                try {
                    if (allPostsData.getJSONObject(i).getString(JSONUtils.KIND_KEY) != "t3") {
                        continue
                    }
                    val data = allPostsData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY)
                    val post = ParsePost.parseBasicData(data)
                    if (PostFilter.isPostAllowed(post, postFilter)) {
                        newPosts.add(post)
                        newPostsIds.add(post.getId())
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            if (readPostsList != null) {
                val readPostsIds = readPostsList.getReadPostsIdsByIds(newPostsIds)
                for (post in newPosts) {
                    if (readPostsIds.contains(post.id)) {
                        post.markAsRead()
                    }
                }
            }

            return newPosts
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    interface LoadIconListener {
        fun loadIconSuccess(authorFullName: String?, iconUrl: String?)
    }

    companion object {
        fun provideFactory(
            retrofit: Retrofit,
            oauthRetrofit: Retrofit,
            redditDataRoomDatabase: RedditDataRoomDatabase,
            loader: UserProfileImagesBatchLoader
        ): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewPostDetailActivityViewModel(retrofit, oauthRetrofit, redditDataRoomDatabase, loader) as T
                }
            }
        }
    }
}