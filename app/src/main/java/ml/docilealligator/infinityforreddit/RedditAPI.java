package ml.docilealligator.infinityforreddit;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedditAPI {
    @FormUrlEncoded
    @POST("api/v1/access_token")
    Call<String> getAccessToken(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("{subredditNamePrefixed}/comments/{article}.json?raw_json=1")
    Call<String> getComments(@Path("subredditNamePrefixed") String subredditNamePrefixed,
                             @Path("article") String article, @Query("comment") String comment);

    @GET("r/{subredditName}/about.json?raw_json=1")
    Call<String> getSubredditData(@Path("subredditName") String subredditName);

    @GET("subreddits/mine/subscriber?raw_json=1")
    Call<String> getSubscribedThing(@Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("api/v1/me?raw_json=1")
    Call<String> getUserInfo(@HeaderMap Map<String, String> headers);

    @FormUrlEncoded
    @POST("api/vote")
    Call<String> voteThing(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @GET("best?raw_json=1")
    Call<String> getBestPost(@Query("after") String lastItem, @HeaderMap Map<String, String> headers);

    @GET("r/{subredditName}.json?raw_json=1&limit=25")
    Call<String> getPost(@Path("subredditName") String subredditName, @Query("after") String lastItem);

    @GET("user/{username}/about.json/raw_json=1")
    Call<String> getUserData(@Path("username") String username);
}
