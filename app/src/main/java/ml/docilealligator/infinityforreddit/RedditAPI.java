package ml.docilealligator.infinityforreddit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditAPI {
    @GET("{subredditNamePrefixed}/comments/{article}.json?raw_json=1")
    Call<String> getComments(@Path("subredditNamePrefixed") String subredditNamePrefixed, @Path("article") String article);
}
