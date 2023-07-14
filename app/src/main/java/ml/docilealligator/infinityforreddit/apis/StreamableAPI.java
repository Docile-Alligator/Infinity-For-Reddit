package ml.ino6962.postinfinityforreddit.apis;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StreamableAPI {
    @GET("videos/{shortcode}")
    Call<String> getStreamableData(@Path("shortcode") String shortCode);
}
