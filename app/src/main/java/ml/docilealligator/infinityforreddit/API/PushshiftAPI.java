package ml.docilealligator.infinityforreddit.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PushshiftAPI {
    @GET("reddit/comment/search/")
    Call<String> getRemovedComment(@Query("ids") String commentId);
}
