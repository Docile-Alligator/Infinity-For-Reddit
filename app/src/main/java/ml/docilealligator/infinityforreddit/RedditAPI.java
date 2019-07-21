package ml.docilealligator.infinityforreddit;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedditAPI {
    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<String> getAccessToken(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("{subredditNamePrefixed}/comments/{article}.json?&raw_json=1")
    Call<String> getComments(@Path("subredditNamePrefixed") String subredditNamePrefixed,
                             @Path("article") String article);

    @GET("r/{subredditName}/about.json?raw_json=1")
    Call<String> getSubredditData(@Path("subredditName") String subredditName);

    @GET("subreddits/mine/subscriber?raw_json=1")
    Call<String> getSubscribedThing(@Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("api/v1/me?raw_json=1")
    Call<String> getMyInfo(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("api/vote")
    Call<String> voteThing(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("comments/{id}.json?raw_json=1")
    Call<String> getPost(@Path("id") String id, @HeaderMap Map<String, String> headers);

    @GET("best?raw_json=1")
    Call<String> getBestPosts(@Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}.json?raw_json=1&limit=25")
    Call<String> getSubredditBestPosts(@Path("subredditName") String subredditName, @Query("after") String lastItem,
                                       @HeaderMap Map<String, String> headers);

    @GET("user/{username}/submitted.json?raw_json=1&limit=25")
    Call<String> getUserBestPosts(@Path("username") String username, @Query("after") String lastItem,
                                  @HeaderMap Map<String, String> headers);

    @GET("user/{username}/about.json?raw_json=1")
    Call<String> getUserData(@Path("username") String username);

    @GET("user/{username}/comments.json?raw_json=1")
    Call<String> getUserComments(@Path("username") String username, @Query("after") String after);

    @FormUrlEncoded
    @POST("api/subscribe")
    Call<String> subredditSubscription(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("{subredditNamePrefixed}/api/info.json?raw_json=1")
    Call<String> getInfo(@Path("subredditNamePrefixed") String subredditNamePrefixed, @Query("id") String id);

    @GET("subreddits/search.json?raw_json=1&include_over_18=on")
    Call<String> searchSubreddits(@Query("q") String subredditName, @Query("after") String after);

    @GET("search.json?raw_json=1&type=user&include_over_18=on")
    Call<String> searchUsers(@Query("q") String profileName, @Query("after") String after);

    @GET("search.json?raw_json=1&type=link&include_over_18=on")
    Call<String> searchPosts(@Query("q") String query, @Query("after") String after,
                             @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}/search.json?raw_json=1&type=link&restrict_sr=true&include_over_18=on")
    Call<String> searchPostsInSpecificSubreddit(@Path("subredditName") String subredditName, @Query("q") String query,
                                                @Query("after") String after, @HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("api/comment")
    Call<String> sendComment(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

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

    @Multipart
    @POST(".")
    Call<String> uploadMediaToAWS(@PartMap()Map<String, RequestBody> params, @Part() MultipartBody.Part file);
}
