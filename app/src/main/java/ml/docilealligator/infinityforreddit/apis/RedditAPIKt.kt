package ml.docilealligator.infinityforreddit.apis

import ml.docilealligator.infinityforreddit.thing.SortType
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditAPIKt {
    @GET("/api/multi/multipath?expand_srs=true&raw_json=1")
    suspend fun getMultiRedditInfo(
        @HeaderMap headers: Map<String, String>,
        @Query("multipath") multipath: String
    ): String

    @FormUrlEncoded
    @POST("/api/multi/copy?expand_srs=true")
    suspend fun copyMultiReddit(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): String

    @FormUrlEncoded
    @POST("/api/v1/access_token")
    suspend fun getAccessToken(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): String

    @FormUrlEncoded
    @POST("/api/v1/access_token")
    fun getAnonymousAccessToken(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Call<String>

    @GET("/api/v1/me?raw_json=1")
    suspend fun getMyInfo(@HeaderMap headers: Map<String, String>): String

    @GET("comments/{id}.json?raw_json=1")
    suspend fun getPostOauth(
        @Path("id") id: String,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("comments/{id}.json?raw_json=1")
    suspend fun getPost(@Path("id") id: String): Response<String>

    @GET("/comments/{id}/placeholder/{singleCommentId}.json?raw_json=1")
    suspend fun getPostAndCommentsSingleThreadByIdOauth(
        @Path("id") id: String, @Path("singleCommentId") singleCommentId: String,
        @Query("sort") sortType: SortType.Type?, @Query("context") contextNumber: String?,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("/comments/{id}.json?raw_json=1")
    suspend fun getPostAndCommentsByIdOauth(
        @Path("id") id: String, @Query("sort") sortType: SortType.Type?,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("/comments/{id}/placeholder/{singleCommentId}.json?raw_json=1")
    suspend fun getPostAndCommentsSingleThreadById(
        @Path("id") id: String, @Path("singleCommentId") singleCommentId: String,
        @Query("sort") sortType: SortType.Type?, @Query("context") contextNumber: String?
    ): Response<String>

    @GET("/comments/{id}.json?raw_json=1")
    suspend fun getPostAndCommentsById(
        @Path("id") id: String,
        @Query("sort") sortType: SortType.Type?
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/morechildren.json?raw_json=1&api_type=json")
    suspend fun moreChildren(
        @Field("link_id") linkId: String?,
        @Field("children") children: String?,
        @Field("sort") sort: SortType.Type?
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/morechildren.json?raw_json=1&api_type=json")
    suspend fun moreChildrenOauth(
        @Field("link_id") linkId: String?,
        @Field("children") children: String?,
        @Field("sort") sort: SortType.Type?,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("r/{subredditName}/about.json?raw_json=1")
    suspend fun getSubredditData(@Path("subredditName") subredditName: String): Response<String>

    @GET("r/{subredditName}/about.json?raw_json=1")
    suspend fun getSubredditDataOauth(
        @Path("subredditName") subredditName: String,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/approve")
    suspend fun approveThing(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/remove")
    suspend fun removeThing(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/set_subreddit_sticky")
    suspend fun toggleStickyPost(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/lock")
    suspend fun lockThing(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/unlock")
    suspend fun unLockThing(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/distinguish")
    suspend fun toggleDistinguishedThing(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/marknsfw")
    suspend fun markNSFW(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/unmarknsfw")
    suspend fun unmarkNSFW(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/spoiler")
    suspend fun markSpoiler(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/unspoiler")
    suspend fun unmarkSpoiler(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/sendreplies")
    suspend fun toggleRepliesNotification(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("{subredditNamePrefixed}/api/selectflair")
    suspend fun selectFlair(
        @Path("subredditNamePrefixed") subredditName: String,
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/save")
    suspend fun save(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/unsave")
    suspend fun unsave(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/hide")
    suspend fun hide(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("/api/unhide")
    suspend fun unhide(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @FormUrlEncoded
    @POST("api/del")
    suspend fun delete(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<String>

    @GET("{sortType}?raw_json=1&limit=100")
    suspend fun getBestPosts(
        @Path("sortType") sortType: SortType.Type, @Query("t") sortTime: SortType.Time?,
        @Query("after") lastItem: String?, @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&always_show_media=1")
    suspend fun getSubredditBestPostsOauth(
        @Path("subredditName") subredditName: String, @Path("sortType") sortType: SortType.Type,
        @Query("t") sortTime: SortType.Time?, @Query("after") lastItem: String?,
        @Query("limit") limit: Int,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&always_show_media=1")
    suspend fun getSubredditBestPosts(
        @Path("subredditName") subredditName: String, @Path("sortType") sortType: SortType.Type,
        @Query("t") sortTime: SortType.Time?, @Query("after") lastItem: String?,
        @Query("limit") limit: Int
    ): Response<String>

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&always_show_media=1")
    suspend fun getAnonymousFrontPageOrMultiredditPosts(
        @Path("subredditName") subredditName: String,
        @Path("sortType") sortType: SortType.Type,
        @Query("t") sortTime: SortType.Time?,
        @Query("after") lastItem: String?,
        @Query("limit") limit: Int,
        @Header("User-Agent") userAgent: String?
    ): Response<String>

    @GET("user/{username}/{where}.json?&type=links&raw_json=1&limit=100")
    suspend fun getUserPostsOauth(
        @Path("username") username: String, @Path("where") where: String,
        @Query("after") lastItem: String?, @Query("sort") sortType: SortType.Type?,
        @Query("t") sortTime: SortType.Time?, @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("user/{username}/submitted.json?raw_json=1&limit=100")
    suspend fun getUserPosts(
        @Path("username") username: String, @Query("after") lastItem: String?,
        @Query("sort") sortType: SortType.Type?, @Query("t") sortTime: SortType.Time?
    ): Response<String>

    @GET("search.json?include_over_18=1&raw_json=1&limit=100&type=link")
    suspend fun searchPostsOauth(
        @Query("q") query: String?, @Query("after") after: String?,
        @Query("sort") sort: SortType.Type?, @Query("t") sortTime: SortType.Time?,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("search.json?include_over_18=1&raw_json=1&limit=100&type=link")
    suspend fun searchPosts(
        @Query("q") query: String?, @Query("after") after: String?,
        @Query("sort") sort: SortType.Type?, @Query("t") sortTime: SortType.Time?
    ): Response<String>

    @GET("r/{subredditName}/search.json?include_over_18=1&raw_json=1&limit=100&type=link&restrict_sr=true")
    suspend fun searchPostsInSpecificSubredditOauth(
        @Path("subredditName") subredditName: String,
        @Query("q") query: String?, @Query("sort") sort: SortType.Type?,
        @Query("t") sortTime: SortType.Time?, @Query("after") after: String?,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("r/{subredditName}/search.json?include_over_18=1&raw_json=1&limit=100&type=link&restrict_sr=true")
    suspend fun searchPostsInSpecificSubreddit(
        @Path("subredditName") subredditName: String,
        @Query("q") query: String?, @Query("sort") sort: SortType.Type?,
        @Query("t") sortTime: SortType.Time?, @Query("after") after: String?
    ): Response<String>

    @GET("{multipath}?raw_json=1&limit=100")
    suspend fun getMultiRedditPosts(
        @Path(value = "multipath", encoded = true) multiPath: String,
        @Query("after") after: String?, @Query("t") sortTime: SortType.Time?
    ): Response<String>

    @GET("{multipath}.json?raw_json=1&limit=100")
    suspend fun getMultiRedditPostsOauth(
        @Path(value = "multipath", encoded = true) multiPath: String,
        @Query("after") after: String?, @Query("t") sortTime: SortType.Time?,
        @HeaderMap headers: Map<String, String>
    ): Response<String>

    @GET("/api/info.json?raw_json=1")
    suspend fun getInfo(@Query("id") id: String): Response<String>

    @GET("/api/info.json?raw_json=1")
    suspend fun getInfoOauth(
        @Query("id") id: String,
        @HeaderMap headers: Map<String, String>
    ): Response<String>
}