package ml.docilealligator.infinityforreddit.utils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShareLinkHandler {
    private final OkHttpClient client = new OkHttpClient();

    private String fetchRedirectUrl(String urlString) throws IOException {
        Request request = new Request.Builder().url(urlString).build();
        try (Response response = client.newCall(request).execute()) {
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