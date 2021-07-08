package ml.docilealligator.infinityforreddit.apis;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Strapi {
    @GET("/broadcasts")
    Call<String> getAllBroadcasts();

    @GET("/videos/{rpan_id_or_fullname}")
    Call<String> getRPANBroadcast(@Path("rpan_id_or_fullname") String rpanIdOrFullname);
}
