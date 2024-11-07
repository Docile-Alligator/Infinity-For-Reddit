package ml.docilealligator.infinityforreddit.apis;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerAPI {
    @GET("/themes/")
    ListenableFuture<Response<String>> getCustomThemesListenableFuture(@Query("page") String page);

    @GET("/themes/theme")
    Call<String> getCustomTheme(@Query("name") String themeName, @Query("username") String username);

    @FormUrlEncoded
    @PATCH("/themes/modify")
    Call<String> modifyTheme(@HeaderMap Map<String, String> headers, @Field("id") int id,
                             @Field("name") String themeName,
                             @Field("data") String customThemeJson);

    @FormUrlEncoded
    @POST("/themes/create")
    Call<String> createTheme(@HeaderMap Map<String, String> headers, @Field("name") String themeName,
                             @Field("data") String customThemeJson);

    @FormUrlEncoded
    @POST("/redditUserAuth/refresh_access_token")
    Call<String> refreshAccessToken(@Field("username") String username, @Field("refresh_token") String refreshToken);
}
