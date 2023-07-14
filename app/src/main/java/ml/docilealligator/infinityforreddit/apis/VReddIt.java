package ml.ino6962.postinfinityforreddit.apis;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface VReddIt {
    @GET()
    Call<String> getRedirectUrl(@Url String vReddItUrl);
}
