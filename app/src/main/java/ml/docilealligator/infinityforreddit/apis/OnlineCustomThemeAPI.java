package ml.docilealligator.infinityforreddit.apis;

import com.google.common.util.concurrent.ListenableFuture;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OnlineCustomThemeAPI {
    @GET("/themes/")
    ListenableFuture<Response<String>> getCustomThemesListenableFuture(@Query("page") String page);
}
