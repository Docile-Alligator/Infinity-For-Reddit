package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.OkHttpClient;

@GlideModule
public class ProxyEnabledGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);
        SharedPreferences mProxySharedPreferences = context.getSharedPreferences(SharedPreferencesUtils.PROXY_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        boolean proxyEnabled = mProxySharedPreferences.getBoolean(SharedPreferencesUtils.PROXY_ENABLED, false);
        if (proxyEnabled) {
            Proxy.Type proxyType = Proxy.Type.valueOf(mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_TYPE, "HTTP"));
            if (proxyType != Proxy.Type.DIRECT) {
                String proxyHost = mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_HOSTNAME, "127.0.0.1");
                int proxyPort = Integer.parseInt(mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_PORT, "1080"));

                InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
                Proxy proxy = new Proxy(proxyType, proxyAddr);
                builder.proxy(proxy);
            }
        }

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(builder.build());

        registry.replace(GlideUrl.class, InputStream.class, factory);
    }
}
