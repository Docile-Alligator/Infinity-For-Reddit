package ml.docilealligator.infinityforreddit.apis;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface DownloadFile {
    @Streaming
    @GET()
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
