package ml.docilealligator.infinityforreddit.apis;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;

public interface Strapi {
    @GET("/broadcasts")
    Call<String> getAllBroadcasts(@HeaderMap Map<String ,String> headers);
}
