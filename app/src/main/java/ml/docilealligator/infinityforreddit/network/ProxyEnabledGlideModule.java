package ml.docilealligator.infinityforreddit.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

@GlideModule
public class ProxyEnabledGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // The client itself is built by ImageOkHttpClient so that BigImageViewer's
        // GlideProgressSupport can derive from the same one. Registering it here covers every load
        // that happens before the image viewer is first opened; see ImageOkHttpClient for why that
        // is not the whole story.
        OkHttpUrlLoader.Factory factory =
                new OkHttpUrlLoader.Factory(ImageOkHttpClient.get(context));

        registry.replace(GlideUrl.class, InputStream.class, factory);
    }
}
