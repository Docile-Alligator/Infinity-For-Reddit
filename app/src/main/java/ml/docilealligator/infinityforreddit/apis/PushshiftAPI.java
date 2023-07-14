package ml.docilealligator.infinityforreddit.apis;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PushshiftAPI {
    @GET("reddit/comment/search/")
    Call<String> getRemovedComment(@Query("ids") String commentId);

    @GET("reddit/submission/search/")
    Call<String> getRemovedPost(@Query("ids") String postId);

    @GET("reddit/comment/search/")
    Call<String> searchComments(@Query("link_id") String linkId,
                                @Query("limit") int limit,
                                @Query("sort") String sort,
                                @Query(value = "fields", encoded = true) String fields,
                                @Query("after") long after,
                                @Query("before") long before,
                                @Query("q") String query);
}
