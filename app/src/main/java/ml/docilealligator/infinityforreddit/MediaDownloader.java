package ml.docilealligator.infinityforreddit;

import android.content.Context;

public interface MediaDownloader {
    void download(String url, String fileName, Context ctx);
}
