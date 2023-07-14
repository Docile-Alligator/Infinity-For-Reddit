/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ml.docilealligator.infinityforreddit.videoautoplay;

import static java.lang.Runtime.getRuntime;
import static ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil.checkNotNull;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.core.util.Pools;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;

/**
 * Global helper class to manage {@link ExoCreator} and {@link SimpleExoPlayer} instances.
 * In this setup, {@link ExoCreator} and SimpleExoPlayer pools are cached. A {@link Config}
 * is a key for each {@link ExoCreator}.
 * <p>
 * A suggested usage is as below:
 * <pre><code>
 * ExoCreator creator = ToroExo.with(this).getDefaultCreator();
 * Playable playable = creator.createPlayable(uri);
 * playable.prepare();
 * // next: setup PlayerView and start the playback.
 * </code></pre>
 *
 * @author eneim (2018/01/26).
 * @since 3.4.0
 */

public final class ToroExo {

    private static final String TAG = "ToroExo";

    // Magic number: Build.VERSION.SDK_INT / 6 --> API 16 ~ 18 will set pool size to 2, etc.
    @SuppressWarnings("WeakerAccess") //
    static final int MAX_POOL_SIZE = Math.max(Util.SDK_INT / 6, getRuntime().availableProcessors());
    @SuppressLint("StaticFieldLeak")  //
    static volatile ToroExo toro;

    public static ToroExo with(Context context) {
        if (toro == null) {
            synchronized (ToroExo.class) {
                if (toro == null) toro = new ToroExo(context.getApplicationContext());
            }
        }
        return toro;
    }

    @NonNull
    final String appName;
    @NonNull
    final Context context;  // Application context
    @NonNull
    private final Map<Config, ExoCreator> creators;
    @NonNull
    private final Map<ExoCreator, Pools.Pool<ExoPlayer>> playerPools;

    private Config defaultConfig; // will be created on the first time it is used.

    private ToroExo(@NonNull Context context /* Application context */) {
        this.context = context;
        this.appName = getUserAgent();
        this.playerPools = new HashMap<>();
        this.creators = new HashMap<>();

        // Adapt from ExoPlayer demo app. Start this on demand.
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        if (CookieHandler.getDefault() != cookieManager) {
            CookieHandler.setDefault(cookieManager);
        }
    }

    /**
     * Utility method to produce {@link ExoCreator} instance from a {@link Config}.
     */
    public final ExoCreator getCreator(Config config) {
        ExoCreator creator = this.creators.get(config);
        if (creator == null) {
            creator = new DefaultExoCreator(this, config);
            this.creators.put(config, creator);
        }

        return creator;
    }

    @SuppressWarnings("WeakerAccess")
    public final Config getDefaultConfig() {
        if (defaultConfig == null) defaultConfig = new Config.Builder(context).build();
        return defaultConfig;
    }

    /**
     * Get the default {@link ExoCreator}. This ExoCreator is configured by {@link #defaultConfig}.
     */
    public final ExoCreator getDefaultCreator() {
        return getCreator(getDefaultConfig());
    }

    /**
     * Request an instance of {@link SimpleExoPlayer}. It can be an existing instance cached by Pool
     * or new one.
     * <p>
     * The creator may or may not be the one created by either {@link #getCreator(Config)} or
     * {@link #getDefaultCreator()}.
     *
     * @param creator the {@link ExoCreator} that is scoped to the {@link SimpleExoPlayer} config.
     * @return an usable {@link SimpleExoPlayer} instance.
     */
    @NonNull  //
    public final ToroExoPlayer requestPlayer(@NonNull ExoCreator creator) {
        ExoPlayer player = getPool(checkNotNull(creator)).acquire();
        if (player == null) player = creator.createPlayer();
        return new ToroExoPlayer(player);
    }

    /**
     * Release player to Pool attached to the creator.
     *
     * @param creator the {@link ExoCreator} that created the player.
     * @param player  the {@link SimpleExoPlayer} to be released back to the Pool
     * @return true if player is released to relevant Pool, false otherwise.
     */
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"}) //
    public final boolean releasePlayer(@NonNull ExoCreator creator, @NonNull ExoPlayer player) {
        return getPool(checkNotNull(creator)).release(player);
    }

    /**
     * Release and clear all current cached ExoPlayer instances. This should be called when
     * client Application runs out of memory ({@link Application#onTrimMemory(int)} for example).
     */
    public final void cleanUp() {
        // TODO [2018/03/07] Test this. Ref: https://stackoverflow.com/a/1884916/1553254
        for (Iterator<Map.Entry<ExoCreator, Pools.Pool<ExoPlayer>>> it =
             playerPools.entrySet().iterator(); it.hasNext(); ) {
            Pools.Pool<ExoPlayer> pool = it.next().getValue();
            ExoPlayer item;
            while ((item = pool.acquire()) != null) item.release();
            it.remove();
        }
    }

    /// internal APIs
    private Pools.Pool<ExoPlayer> getPool(ExoCreator creator) {
        Pools.Pool<ExoPlayer> pool = playerPools.get(creator);
        if (pool == null) {
            pool = new Pools.SimplePool<>(MAX_POOL_SIZE);
            playerPools.put(creator, pool);
        }

        return pool;
    }

    /**
     * Get a possibly-non-localized String from existing resourceId.
     */
    /* pkg */ String getString(@StringRes int resId, @Nullable Object... params) {
        return params == null || params.length < 1 ?  //
                this.context.getString(resId) : this.context.getString(resId, params);
    }

    // Share the code of setting Volume. For use inside library only.
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) //
    public static void setVolumeInfo(@NonNull ToroExoPlayer player,
                                     @NonNull VolumeInfo volumeInfo) {
        player.setVolumeInfo(volumeInfo);
    }

    @SuppressWarnings("WeakerAccess")
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) //
    public static VolumeInfo getVolumeInfo(ToroExoPlayer player) {
        return new VolumeInfo(player.getVolumeInfo());
    }

    @SuppressWarnings("SameParameterValue")
    private static String getUserAgent() {
        return APIUtils.USER_AGENT;
    }
}