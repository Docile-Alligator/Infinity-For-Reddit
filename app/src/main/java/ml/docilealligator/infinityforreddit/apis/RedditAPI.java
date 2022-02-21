package ml.docilealligator.infinityforreddit.apis;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedditAPI {
    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<String> getAccessToken(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("r/{subredditName}/about.json?raw_json=1")
    Call<String> getSubredditData(@Path("subredditName") String subredditName);

    @GET("r/{subredditName}/about.json?raw_json=1")
    Call<String> getSubredditDataOauth(@Path("subredditName") String subredditName, @HeaderMap Map<String, String> headers);

    @GET("subreddits/mine/subscriber?raw_json=1")
    Call<String> getSubscribedThing(@Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("api/v1/me?raw_json=1")
    Call<String> getMyInfo(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("api/vote")
    Call<String> voteThing(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("comments/{id}.json?raw_json=1")
    Call<String> getPostOauth(@Path("id") String id, @HeaderMap Map<String, String> headers);

    @GET("comments/{id}.json?raw_json=1")
    Call<String> getPost(@Path("id") String id);

    @GET("user/{username}/submitted.json?raw_json=1&limit=25")
    Call<String> getUserPosts(@Path("username") String username, @Query("after") String lastItem,
                              @Query("sort") String sortType);

    @GET("user/{username}/submitted.json?raw_json=1&limit=25")
    Call<String> getUserPosts(@Path("username") String username, @Query("after") String lastItem,
                              @Query("sort") String sortType, @Query("t") String sortTime);

    @GET("user/{username}/about.json?raw_json=1")
    Call<String> getUserData(@Path("username") String username);

    @GET("user/{username}/about.json?raw_json=1")
    Call<String> getUserDataOauth(@HeaderMap Map<String, String> headers, @Path("username") String username);

    @GET("user/{username}/comments.json?raw_json=1")
    Call<String> getUserComments(@Path("username") String username, @Query("after") String after,
                                 @Query("sort") String sortType);

    @GET("user/{username}/comments.json?raw_json=1")
    Call<String> getUserComments(@Path("username") String username, @Query("after") String after,
                                 @Query("sort") String sortType, @Query("t") String sortTime);

    @GET("user/{username}/comments.json?raw_json=1")
    Call<String> getUserCommentsOauth(@HeaderMap Map<String, String> headers, @Path("username") String username,
                                      @Query("after") String after, @Query("sort") String sortType);

    @GET("user/{username}/comments.json?raw_json=1")
    Call<String> getUserCommentsOauth(@HeaderMap Map<String, String> headers, @Path("username") String username,
                                      @Query("after") String after, @Query("sort") String sortType,
                                      @Query("t") String sortTime);

    @GET("user/{username}/{where}.json?&type=comments&raw_json=1&limit=25")
    Call<String> getUserSavedCommentsOauth(@Path("username") String username, @Path("where") String where,
                                           @Query("after") String lastItem, @Query("sort") String sortType,
                                           @HeaderMap Map<String, String> headers);

    @GET("user/{username}/{where}.json?&type=comments&raw_json=1&limit=25")
    Call<String> getUserSavedCommentsOauth(@Path("username") String username, @Path("where") String where,
                                           @Query("after") String lastItem, @Query("sort") String sortType,
                                           @Query("t") String sortTime, @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("api/subscribe")
    Call<String> subredditSubscription(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("/api/info.json?raw_json=1")
    Call<String> getInfo(@Query("id") String id);

    @GET("/api/info.json?raw_json=1")
    Call<String> getInfoOauth(@Query("id") String id, @HeaderMap Map<String, String> headers);

    @GET("subreddits/search.json?raw_json=1")
    Call<String> searchSubreddits(@Query("q") String subredditName, @Query("after") String after,
                                  @Query("sort") String sort, @Query("include_over_18") int nsfw,
                                  @HeaderMap Map<String, String> headers);

    @GET("search.json?raw_json=1&type=user")
    Call<String> searchUsers(@Query("q") String profileName, @Query("after") String after,
                             @Query("sort") String sort, @Query("include_over_18") int nsfw);

    @FormUrlEncoded
    @POST("api/comment")
    Call<String> sendCommentOrReplyToMessage(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("api/del")
    Call<String> delete(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("api/submit")
    Call<String> submit(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("api/media/asset.json?raw_json=1&gilding_detail=1")
    Call<String> uploadImage(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("r/{subredditName}/api/link_flair.json?raw_json=1")
    Call<String> getFlairs(@HeaderMap Map<String, String> headers, @Path("subredditName") String subredditName);

    @GET("/r/{subredditName}/about/rules.json?raw_json=1")
    Call<String> getRules(@Path("subredditName") String subredditName);

    @GET("/comments/{id}/placeholder/{singleCommentId}.json?raw_json=1")
    Call<String> getPostAndCommentsSingleThreadByIdOauth(@Path("id") String id, @Path("singleCommentId") String singleCommentId,
                                                         @Query("sort") String sortType, @Query("context") String contextNumber,
                                                         @HeaderMap Map<String, String> headers);

    @GET("/comments/{id}.json?raw_json=1")
    Call<String> getPostAndCommentsByIdOauth(@Path("id") String id, @Query("sort") String sortType,
                                             @HeaderMap Map<String, String> headers);

    @GET("/comments/{id}/placeholder/{singleCommentId}.json?raw_json=1")
    Call<String> getPostAndCommentsSingleThreadById(@Path("id") String id, @Path("singleCommentId") String singleCommentId,
                                                    @Query("sort") String sortType, @Query("context") String contextNumber);

    @GET("/comments/{id}.json?raw_json=1")
    Call<String> getPostAndCommentsById(@Path("id") String id, @Query("sort") String sortType);

    @Multipart
    @POST(".")
    Call<String> uploadMediaToAWS(@PartMap() Map<String, RequestBody> params, @Part() MultipartBody.Part file);

    @FormUrlEncoded
    @POST("/api/editusertext")
    Call<String> editPostOrComment(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/marknsfw")
    Call<String> markNSFW(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/unmarknsfw")
    Call<String> unmarkNSFW(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/spoiler")
    Call<String> markSpoiler(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/unspoiler")
    Call<String> unmarkSpoiler(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("{subredditNamePrefixed}/api/selectflair")
    Call<String> selectFlair(@Path("subredditNamePrefixed") String subredditName, @HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("/message/{where}.json?raw_json=1")
    Call<String> getMessages(@HeaderMap Map<String, String> headers, @Path("where") String where, @Query("after") String after);

    @FormUrlEncoded
    @POST("/api/read_message")
    Call<String> readMessage(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> ids);

    @FormUrlEncoded
    @POST("/api/save")
    Call<String> save(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/unsave")
    Call<String> unsave(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/hide")
    Call<String> hide(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/unhide")
    Call<String> unhide(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/favorite")
    Call<String> favoriteThing(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("/api/multi/mine")
    Call<String> getMyMultiReddits(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("/api/multi/favorite?raw_json=1&gilding_detail=1")
    Call<String> favoriteMultiReddit(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("/api/multi/user/{username}")
    Call<String> getUserMultiReddits(@Path("username") String username);

    @GET("{multipath}?raw_json=1")
    Call<String> getMultiRedditPosts(@Path(value = "multipath", encoded = true) String multiPath,
                                     @Query("after") String after);

    @GET("{multipath}?raw_json=1")
    Call<String> getMultiRedditPosts(@Path(value = "multipath", encoded = true) String multiPath,
                                     @Query("after") String after, @Query("t") String sortTime);

    @GET("{multipath}.json?raw_json=1")
    Call<String> getMultiRedditPostsOauth(@Path(value = "multipath", encoded = true) String multiPath,
                                          @Query("after") String after, @Query("t") String sortTime,
                                          @HeaderMap Map<String, String> headers);

    @GET("{multipath}.json?raw_json=1")
    Call<String> getMultiRedditPostsOauth(@Path(value = "multipath", encoded = true) String multiPath,
                                          @Query("after") String after, @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("/api/multi/multipath")
    Call<String> createMultiReddit(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @PUT("/api/multi/multipath")
    Call<String> updateMultiReddit(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @DELETE("/api/multi/multipath")
    Call<String> deleteMultiReddit(@HeaderMap Map<String, String> headers, @Query("multipath") String multipath);

    @GET("/api/multi/multipath/")
    Call<String> getMultiRedditInfo(@HeaderMap Map<String, String> headers, @Query("multipath") String multipath);

    @FormUrlEncoded
    @POST("/api/report")
    Call<String> report(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/api/compose")
    Call<String> composePrivateMessage(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);


    @FormUrlEncoded
    @POST("api/block_user")
    Call<String> blockUser(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("r/{subredditName}/api/user_flair_v2.json?raw_json=1")
    Call<String> getUserFlairs(@HeaderMap Map<String, String> headers, @Path("subredditName") String subredditName);

    @FormUrlEncoded
    @POST("/r/{subredditName}/api/selectflair?raw_json=1")
    Call<String> selectUserFlair(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params,
                                 @Path("subredditName") String subredditName);

    @FormUrlEncoded
    @POST("api/v2/gold/gild")
    Call<String> awardThing(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("/r/random/comments.json?limit=1&raw_json=1")
    Call<String> getRandomPost();

    @GET("/r/randnsfw/new.json?sort=new&t=all&limit=1&raw_json=1")
    Call<String> getRandomNSFWPost();

    @POST("/api/read_all_messages")
    Call<String> readAllMessages(@HeaderMap Map<String, String> headers);


    @FormUrlEncoded
    @PUT("/api/multi{multipath}/r/{subredditName}")
    Call<String> addSubredditToMultiReddit(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params,
                                           @Path(value = "multipath", encoded = true) String multipath, @Path("subredditName") String subredditName);

    @FormUrlEncoded
    @POST("/api/quarantine_option?raw_json=1")
    Call<String> optInQuarantinedSubreddit(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("/api/subreddit_autocomplete_v2?typeahead_active=true&include_profiles=false&raw_json=1")
    Call<String> subredditAutocomplete(@HeaderMap Map<String ,String> headers, @Query("query") String query,
                                       @Query("include_over_18") boolean nsfw);

    @POST("/api/submit_gallery_post.json?resubmit=true&raw_json=1")
    Call<String> submitGalleryPost(@HeaderMap Map<String, String> headers, @Body String body);

    @POST("/api/submit_poll_post.json?resubmit=true&raw_json=1&gilding_detail=1")
    Call<String> submitPollPost(@HeaderMap Map<String, String> headers, @Body String body);

    @GET("/api/trending_searches_v1.json?withAds=0&raw_json=1&gilding_detail=1")
    Call<String> getTrendingSearches();

    @GET("/api/trending_searches_v1.json?withAds=0&raw_json=1&gilding_detail=1")
    Call<String> getTrendingSearchesOauth(@HeaderMap Map<String, String> headers);

    default Call<String> getWiki(@Path("subredditName") String subredditName) {
        return getWikiPage(subredditName, "index");
    };

    @GET("/r/{subredditName}/wiki/{wikiPage}.json?raw_json=1")
    Call<String> getWikiPage(@Path("subredditName") String subredditName, @Path("wikiPage") String wikiPage);

    @GET("{sortType}?raw_json=1")
    ListenableFuture<Response<String>> getBestPostsListenableFuture(@Path("sortType") String sortType, @Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("{sortType}?raw_json=1")
    ListenableFuture<Response<String>> getBestPostsListenableFuture(@Path("sortType") String sortType, @Query("t") String sortTime,
                                                                   @Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&limit=25&always_show_media=1")
    ListenableFuture<Response<String>> getSubredditBestPostsOauthListenableFuture(@Path("subredditName") String subredditName, @Path("sortType") String sortType,
                                            @Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&limit=25&always_show_media=1")
    ListenableFuture<Response<String>> getSubredditBestPostsOauthListenableFuture(@Path("subredditName") String subredditName, @Path("sortType") String sortType,
                                            @Query("t") String sortTime, @Query("after") String lastItem,
                                            @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&limit=25&always_show_media=1")
    ListenableFuture<Response<String>> getSubredditBestPostsListenableFuture(@Path("subredditName") String subredditName, @Path("sortType") String sortType,
                                       @Query("after") String lastItem);

    @GET("r/{subredditName}/{sortType}.json?raw_json=1&limit=25&always_show_media=1")
    ListenableFuture<Response<String>> getSubredditBestPostsListenableFuture(@Path("subredditName") String subredditName, @Path("sortType") String sortType,
                                       @Query("t") String sortTime, @Query("after") String lastItem);

    @GET("user/{username}/{where}.json?&type=links&raw_json=1&limit=25")
    ListenableFuture<Response<String>> getUserPostsOauthListenableFuture(@Path("username") String username, @Path("where") String where,
                                   @Query("after") String lastItem, @Query("sort") String sortType, @HeaderMap Map<String, String> headers);

    @GET("user/{username}/{where}.json?&type=links&raw_json=1&limit=25")
    ListenableFuture<Response<String>> getUserPostsOauthListenableFuture(@Path("username") String username, @Path("where") String where,
                                   @Query("after") String lastItem, @Query("sort") String sortType,
                                   @Query("t") String sortTime, @HeaderMap Map<String, String> headers);

    @GET("user/{username}/submitted.json?raw_json=1&limit=25")
    ListenableFuture<Response<String>> getUserPostsListenableFuture(@Path("username") String username, @Query("after") String lastItem,
                              @Query("sort") String sortType);

    @GET("user/{username}/submitted.json?raw_json=1&limit=25")
    ListenableFuture<Response<String>> getUserPostsListenableFuture(@Path("username") String username, @Query("after") String lastItem,
                              @Query("sort") String sortType, @Query("t") String sortTime);

    @GET("search.json?include_over_18=1&raw_json=1&type=link")
    ListenableFuture<Response<String>> searchPostsOauthListenableFuture(@Query("q") String query, @Query("after") String after,
                                  @Query("sort") String sort, @Query("source") String source,
                                  @HeaderMap Map<String, String> headers);

    @GET("search.json?include_over_18=1&raw_json=1&type=link")
    ListenableFuture<Response<String>> searchPostsOauthListenableFuture(@Query("q") String query, @Query("after") String after,
                                  @Query("sort") String sort, @Query("t") String sortTime,
                                  @Query("source") String source,
                                  @HeaderMap Map<String, String> headers);

    @GET("search.json?include_over_18=1&raw_json=1&type=link")
    ListenableFuture<Response<String>> searchPostsListenableFuture(@Query("q") String query, @Query("after") String after,
                             @Query("sort") String sort, @Query("source") String source);

    @GET("search.json?include_over_18=1&raw_json=1&type=link")
    ListenableFuture<Response<String>> searchPostsListenableFuture(@Query("q") String query, @Query("after") String after,
                             @Query("sort") String sort, @Query("t") String sortTime,
                             @Query("source") String source);

    @GET("r/{subredditName}/search.json?include_over_18=1&raw_json=1&type=link&restrict_sr=true")
    ListenableFuture<Response<String>> searchPostsInSpecificSubredditOauthListenableFuture(@Path("subredditName") String subredditName,
                                                     @Query("q") String query, @Query("sort") String sort,
                                                     @Query("after") String after,
                                                     @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}/search.json?include_over_18=1&raw_json=1&type=link&restrict_sr=true")
    ListenableFuture<Response<String>> searchPostsInSpecificSubredditOauthListenableFuture(@Path("subredditName") String subredditName,
                                                     @Query("q") String query, @Query("sort") String sort,
                                                     @Query("t") String sortTime, @Query("after") String after,
                                                     @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}/search.json?include_over_18=1&raw_json=1&type=link&restrict_sr=true")
    ListenableFuture<Response<String>> searchPostsInSpecificSubredditListenableFuture(@Path("subredditName") String subredditName,
                                                @Query("q") String query, @Query("sort") String sort,
                                                @Query("after") String after);

    @GET("r/{subredditName}/search.json?include_over_18=1&raw_json=1&type=link&restrict_sr=true")
    ListenableFuture<Response<String>> searchPostsInSpecificSubredditListenableFuture(@Path("subredditName") String subredditName,
                                                @Query("q") String query, @Query("sort") String sort,
                                                @Query("t") String sortTime, @Query("after") String after);

    @GET("{multipath}?raw_json=1")
    ListenableFuture<Response<String>> getMultiRedditPostsListenableFuture(@Path(value = "multipath", encoded = true) String multiPath,
                                     @Query("after") String after);

    @GET("{multipath}?raw_json=1")
    ListenableFuture<Response<String>> getMultiRedditPostsListenableFuture(@Path(value = "multipath", encoded = true) String multiPath,
                                     @Query("after") String after, @Query("t") String sortTime);

    @GET("{multipath}.json?raw_json=1")
    ListenableFuture<Response<String>> getMultiRedditPostsOauthListenableFuture(@Path(value = "multipath", encoded = true) String multiPath,
                                          @Query("after") String after, @Query("t") String sortTime,
                                          @HeaderMap Map<String, String> headers);

    @GET("{multipath}.json?raw_json=1")
    ListenableFuture<Response<String>> getMultiRedditPostsOauthListenableFuture(@Path(value = "multipath", encoded = true) String multiPath,
                                          @Query("after") String after, @HeaderMap Map<String, String> headers);

    @POST("r/{subredditName}/api/delete_sr_icon")
    Call<String> deleteSrIcon(@HeaderMap Map<String, String> headers, @Path("subredditName") String subredditName);

    @POST("r/{subredditName}/api/delete_sr_banner")
    Call<String> deleteSrBanner(@HeaderMap Map<String, String> headers, @Path("subredditName") String subredditName);

    @Multipart
    @POST("r/{subredditName}/api/upload_sr_img")
    Call<String> uploadSrImg(@HeaderMap Map<String, String> headers,
            @Path("subredditName") String subredditName,
            @PartMap Map<String, RequestBody> params,
            @Part MultipartBody.Part file);

    @GET("r/{subredditName}/about/edit?raw_json=1")
    Call<String> getSubredditSetting(@HeaderMap Map<String, String> headers, @Path("subredditName") String subredditName);

    @FormUrlEncoded
    @POST("/api/site_admin")
    Call<String> postSiteAdmin(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);
}
