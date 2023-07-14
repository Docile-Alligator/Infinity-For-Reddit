package ml.docilealligator.infinityforreddit.apis;

import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ImgurAPI {
    @GET("gallery/{id}")
    Call<String> getGalleryImages(@Header(APIUtils.AUTHORIZATION_KEY) String clientId, @Path("id") String id);

    @GET("album/{id}")
    Call<String> getAlbumImages(@Header(APIUtils.AUTHORIZATION_KEY) String clientId, @Path("id") String id);

    @GET("image/{id}")
    Call<String> getImage(@Header(APIUtils.AUTHORIZATION_KEY) String clientId, @Path("id") String id);
}
