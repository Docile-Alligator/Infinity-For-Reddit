package ml.docilealligator.infinityforreddit.apis;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;

public interface Strapi {
    @GET("/broadcasts")
    Call<String> getAllBroadcasts(@HeaderMap Map<String ,String> headers);

    @GET("/videos/{rpan_id_or_fullname}")
    Call<String> getRPANBroadcast(@Path("rpan_id_or_fullname") String rpanIdOrFullname);
}
