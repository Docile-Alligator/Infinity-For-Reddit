package ml.docilealligator.infinityforreddit.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GfycatAPI {
    @GET("{gfyid}")
    Call<String> getGfycatData(@Path("gfyid") String gfyId);
}
