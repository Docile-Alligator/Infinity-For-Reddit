package ml.docilealligator.infinityforreddit.network

import android.content.Context
import android.content.SharedPreferences
import ml.docilealligator.infinityforreddit.utils.APIUtils
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * The one OkHttpClient every Glide image load is supposed to go through: app timeouts, the app's
 * User-Agent, the API/media statistics listener, and the user's proxy setting.
 *
 * It lives here rather than inside [ProxyEnabledGlideModule] because that module is not the only
 * thing that configures Glide's networking. `BigImageViewer.initialize(GlideImageLoader.with(ctx))`
 * reaches `GlideProgressSupport.init(glide, okHttpClient)`, which does:
 *
 * ```
 * builder = okHttpClient != null ? okHttpClient.newBuilder() : new OkHttpClient.Builder();
 * builder.addNetworkInterceptor(progressInterceptor);
 * glide.getRegistry().replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(builder.build()));
 * ```
 *
 * That `replace` is global to the Glide singleton and permanent for the process. Called through the
 * one-argument `GlideImageLoader.with(context)`, okHttpClient is null, so it installs a **bare**
 * client and silently throws away everything [ProxyEnabledGlideModule] registered -- for every Glide
 * load in the app, not just the image viewer, from the moment the viewer is first opened. That is
 * why the proxy setting stopped applying to images, why image loads went missing from the API
 * monitor, and why issue #412's catbox.moe fix did nothing: files.catbox.moe resets the HTTP/2
 * stream for any User-Agent containing "dalvik" (case-insensitive), Glide's default User-Agent is
 * the platform's `System.getProperty("http.agent")` -- literally
 * `Dalvik/2.1.0 (Linux; U; Android <ver>; <model> Build/<id>)` -- and the interceptor meant to
 * replace it had been discarded before the request was ever made.
 *
 * Passing this client to the two-argument `GlideImageLoader.with(context, okHttpClient)` instead
 * makes `GlideProgressSupport` derive from it (`newBuilder()` keeps the interceptors, the event
 * listener and the proxy) and only add its own progress network interceptor on top.
 *
 * Every caller must therefore use the two-argument overload; the one-argument one is a trap.
 */
object ImageOkHttpClient {
    @Volatile
    private var instance: OkHttpClient? = null

    /**
     * The shared client. Built once: OkHttp expects a single instance per app so the connection and
     * thread pools are reused, and `newBuilder()`-derived copies keep sharing them.
     */
    @JvmStatic
    fun get(context: Context): OkHttpClient {
        return instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }
    }

    private fun build(applicationContext: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Unconditional replace, not the "only if absent" form in
            // NetworkModule.provideBaseOkhttp: Glide always populates User-Agent from
            // http.agent, so a conditional would never fire. APIUtils.USER_AGENT is read per
            // request rather than captured, so it cannot be sampled before Infinity.onCreate
            // assigns it.
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", APIUtils.USER_AGENT)
                        .build()
                )
            }
            // Gallery items Reddit's listing reports as failed carry a guessed extension.
            .addInterceptor(RedditMediaExtensionInterceptor)

        // Read per connection, as for the API clients: this one is built once for the process.
        val mProxySharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
            SharedPreferencesUtils.PROXY_SHARED_PREFERENCES_FILE,
            Context.MODE_PRIVATE
        )
        val proxyEnabled =
            mProxySharedPreferences.getBoolean(SharedPreferencesUtils.PROXY_ENABLED, false)
        if (proxyEnabled) {
            val proxyType = Proxy.Type.valueOf(
                mProxySharedPreferences.getString(
                    SharedPreferencesUtils.PROXY_TYPE,
                    "HTTP"
                )!!
            )
            if (proxyType != Proxy.Type.DIRECT) {
                val proxyHost: String = mProxySharedPreferences.getString(
                    SharedPreferencesUtils.PROXY_HOSTNAME,
                    "127.0.0.1"
                )!!
                val proxyPort =
                    mProxySharedPreferences.getString(SharedPreferencesUtils.PROXY_PORT, "1080")!!
                        .toInt()

                val proxyAddr = InetSocketAddress.createUnresolved(proxyHost, proxyPort)
                val proxy = Proxy(proxyType, proxyAddr)
                builder.proxy(proxy)
            }
        }

        return builder.build()
    }
}
