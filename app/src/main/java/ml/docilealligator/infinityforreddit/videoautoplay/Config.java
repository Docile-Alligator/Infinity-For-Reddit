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

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
import static ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil.checkNotNull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.cache.Cache;

/**
 * Necessary configuration for {@link ExoCreator} to produces {@link SimpleExoPlayer} and
 * {@link MediaSource}. Instance of this class must be construct using {@link Builder}.
 *
 * @author eneim (2018/01/23).
 * @since 3.4.0
 */

@SuppressWarnings("SimplifiableIfStatement")  //
public final class Config {

  @Nullable
  private final Context context;

  // primitive flags
  @ExtensionRendererMode final int extensionMode;

  // NonNull options
  @NonNull final BaseMeter meter;
  @NonNull final LoadControl loadControl;
  @NonNull final MediaSourceBuilder mediaSourceBuilder;

  // Nullable options
  @Nullable final Cache cache; // null by default
  // If null, ExoCreator must come up with a default one.
  // This is to help customizing the Data source, for example using OkHttp extension.
  @Nullable final DataSource.Factory dataSourceFactory;

  @SuppressWarnings("WeakerAccess") //
  Config(@Nullable Context context, int extensionMode, @NonNull BaseMeter meter,
         @NonNull LoadControl loadControl,
         @Nullable DataSource.Factory dataSourceFactory,
         @NonNull MediaSourceBuilder mediaSourceBuilder,
         @Nullable Cache cache) {
    this.context = context != null ? context.getApplicationContext() : null;
    this.extensionMode = extensionMode;
    this.meter = meter;
    this.loadControl = loadControl;
    this.dataSourceFactory = dataSourceFactory;
    this.mediaSourceBuilder = mediaSourceBuilder;
    this.cache = cache;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Config config = (Config) o;

    if (extensionMode != config.extensionMode) return false;
    if (!meter.equals(config.meter)) return false;
    if (!loadControl.equals(config.loadControl)) return false;
    if (!mediaSourceBuilder.equals(config.mediaSourceBuilder)) return false;
    if (!ObjectsCompat.equals(cache, config.cache)) return false;
    return ObjectsCompat.equals(dataSourceFactory, config.dataSourceFactory);
  }

  @Override public int hashCode() {
    int result = extensionMode;
    result = 31 * result + meter.hashCode();
    result = 31 * result + loadControl.hashCode();
    result = 31 * result + mediaSourceBuilder.hashCode();
    result = 31 * result + (cache != null ? cache.hashCode() : 0);
    result = 31 * result + (dataSourceFactory != null ? dataSourceFactory.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("unused") public Builder newBuilder() {
    return new Builder(context).setCache(this.cache)
        .setExtensionMode(this.extensionMode)
        .setLoadControl(this.loadControl)
        .setMediaSourceBuilder(this.mediaSourceBuilder)
        .setMeter(this.meter);
  }

  /// Builder
  @SuppressWarnings({ "unused", "WeakerAccess" }) //
  public static final class Builder {

    @Nullable // only for backward compatibility
    final Context context;

    /**
     * @deprecated Use the constructor with nonnull {@link Context} instead.
     */
    @Deprecated
    public Builder() {
      this(null);
    }

    public Builder(@Nullable Context context) {
      this.context = context != null ? context.getApplicationContext() : null;
      DefaultBandwidthMeter bandwidthMeter =
          new DefaultBandwidthMeter.Builder(this.context).build();
      meter = new BaseMeter<>(bandwidthMeter);
    }

    @ExtensionRendererMode private int extensionMode = EXTENSION_RENDERER_MODE_OFF;
    private BaseMeter meter;
    private LoadControl loadControl = new DefaultLoadControl();
    private DataSource.Factory dataSourceFactory = null;
    private MediaSourceBuilder mediaSourceBuilder = MediaSourceBuilder.DEFAULT;
    private Cache cache = null;

    public Builder setExtensionMode(@ExtensionRendererMode int extensionMode) {
      this.extensionMode = extensionMode;
      return this;
    }

    public Builder setMeter(@NonNull BaseMeter meter) {
      this.meter = checkNotNull(meter, "Need non-null BaseMeter");
      return this;
    }

    public Builder setLoadControl(@NonNull LoadControl loadControl) {
      this.loadControl = checkNotNull(loadControl, "Need non-null LoadControl");
      return this;
    }

    // Option is Nullable, but if user customize this, it must be a Nonnull one.
    public Builder setDataSourceFactory(@NonNull DataSource.Factory dataSourceFactory) {
      this.dataSourceFactory = checkNotNull(dataSourceFactory);
      return this;
    }

    public Builder setMediaSourceBuilder(@NonNull MediaSourceBuilder mediaSourceBuilder) {
      this.mediaSourceBuilder =
          checkNotNull(mediaSourceBuilder, "Need non-null MediaSourceBuilder");
      return this;
    }

    public Builder setCache(@Nullable Cache cache) {
      this.cache = cache;
      return this;
    }

    public Config build() {
      return new Config(context, extensionMode, meter, loadControl, dataSourceFactory,
          mediaSourceBuilder, cache);
    }
  }
}
