package ml.docilealligator.infinityforreddit.utils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ShareLinkHandler {
    private final Retrofit retrofit;

    @Inject
    public ShareLinkHandler(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    private String fetchRedirectUrl(String urlString) throws IOException {
        okhttp3.Call call = retrofit.callFactory().newCall(new Request.Builder().url(urlString).build());
        try (okhttp3.Response response = call.execute()) {
            return response.request().url().toString();
        }
    }

    public CompletableFuture<String> handleUrlResolv(String urlString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchRedirectUrl(urlString);
            } catch (IOException e) {
                return null;
            }
        });
    }
}