package ml.docilealligator.infinityforreddit.apis;

import com.google.common.util.concurrent.ListenableFuture;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OnlineCustomThemeAPI {
    @GET("/themes/")
    ListenableFuture<Response<String>> getCustomThemesListenableFuture(@Query("page") String page);

    @GET("/themes/theme")
    Call<String> getCustomTheme(@Query("name") String themeName, @Query("username") String username);

    @FormUrlEncoded
    @PATCH("/themes/modify")
    Call<String> modifyTheme(@Field("id") int id, @Field("name") String themeName,
                             @Field("data") String customThemeJson, @Field("primary_color") String primaryColor);

    @FormUrlEncoded
    @POST("/themes/upload")
    Call<String> uploadTheme(@Field("name") String themeName, @Field("username") String username,
                             @Field("data") String customThemeJson, @Field("primary_color") String primaryColor);
}
