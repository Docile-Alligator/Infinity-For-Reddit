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

import static ml.docilealligator.infinityforreddit.videoautoplay.ToroExo.with;
import static ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil.checkNotNull;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

/**
 * Usage: use this as-it or inheritance.
 *
 * @author eneim (2018/02/04).
 * @since 3.4.0
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
public class DefaultExoCreator implements ExoCreator, MediaSourceEventListener {

  final ToroExo toro;  // per application
  final Config config;
  private final TrackSelector trackSelector;  // 'maybe' stateless
  private final LoadControl loadControl;  // stateless
  private final MediaSourceBuilder mediaSourceBuilder;  // stateless
  private final RenderersFactory renderersFactory;  // stateless
  private final DataSource.Factory mediaDataSourceFactory;  // stateless
  private final DataSource.Factory manifestDataSourceFactory; // stateless

  public DefaultExoCreator(@NonNull ToroExo toro, @NonNull Config config) {
    this.toro = checkNotNull(toro);
    this.config = checkNotNull(config);
    trackSelector = new DefaultTrackSelector();
    loadControl = config.loadControl;
    mediaSourceBuilder = config.mediaSourceBuilder;

    DefaultRenderersFactory tempFactory = new DefaultRenderersFactory(this.toro.context);
    tempFactory.setExtensionRendererMode(config.extensionMode);
    renderersFactory = tempFactory;

    DataSource.Factory baseFactory = config.dataSourceFactory;
    if (baseFactory == null) {
      baseFactory = new DefaultHttpDataSourceFactory(toro.appName, config.meter);
    }
    DataSource.Factory factory = new DefaultDataSourceFactory(this.toro.context,  //
        config.meter, baseFactory);
    if (config.cache != null) factory = new CacheDataSourceFactory(config.cache, factory);
    mediaDataSourceFactory = factory;
    manifestDataSourceFactory = new DefaultDataSourceFactory(this.toro.context, this.toro.appName);
  }

  public DefaultExoCreator(Context context, Config config) {
    this(with(context), config);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DefaultExoCreator that = (DefaultExoCreator) o;

    if (!toro.equals(that.toro)) return false;
    if (!trackSelector.equals(that.trackSelector)) return false;
    if (!loadControl.equals(that.loadControl)) return false;
    if (!mediaSourceBuilder.equals(that.mediaSourceBuilder)) return false;
    if (!renderersFactory.equals(that.renderersFactory)) return false;
    if (!mediaDataSourceFactory.equals(that.mediaDataSourceFactory)) return false;
    return manifestDataSourceFactory.equals(that.manifestDataSourceFactory);
  }

  @Override public int hashCode() {
    int result = toro.hashCode();
    result = 31 * result + trackSelector.hashCode();
    result = 31 * result + loadControl.hashCode();
    result = 31 * result + mediaSourceBuilder.hashCode();
    result = 31 * result + renderersFactory.hashCode();
    result = 31 * result + mediaDataSourceFactory.hashCode();
    result = 31 * result + manifestDataSourceFactory.hashCode();
    return result;
  }

  final TrackSelector getTrackSelector() {
    return trackSelector;
  }

  @Nullable @Override public Context getContext() {
    return toro.context;
  }

  @NonNull @Override public SimpleExoPlayer createPlayer() {
    return new ToroExoPlayer(toro.context, renderersFactory, trackSelector, loadControl,
        new DefaultBandwidthMeter.Builder(toro.context).build(), config.drmSessionManager,
        Util.getLooper());
  }

  @NonNull @Override public MediaSource createMediaSource(@NonNull Uri uri, String fileExt) {
    return mediaSourceBuilder.buildMediaSource(this.toro.context, uri, fileExt, new Handler(),
        manifestDataSourceFactory, mediaDataSourceFactory, this);
  }

  @NonNull @Override //
  public Playable createPlayable(@NonNull Uri uri, String fileExt) {
    return new PlayableImpl(this, uri, fileExt);
  }

  /// MediaSourceEventListener

  @Override
  public void onLoadStarted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    // no-ops
  }

  @Override
  public void onLoadCompleted(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    // no-ops
  }

  @Override
  public void onLoadCanceled(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
    // no-ops
  }

  @Override
  public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId,
      LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error,
      boolean wasCanceled) {
    // no-ops
  }

  @Override public void onReadingStarted(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
    // no-ops
  }

  @Override
  public void onUpstreamDiscarded(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId,
      MediaLoadData mediaLoadData) {
    // no-ops
  }

  @Override public void onDownstreamFormatChanged(int windowIndex,
      @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaLoadData mediaLoadData) {
    // no-ops
  }

  @Override
  public void onMediaPeriodCreated(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
    // no-ops
  }

  @Override
  public void onMediaPeriodReleased(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
    // no-ops
  }
}
