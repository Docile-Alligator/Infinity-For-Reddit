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

import static android.text.TextUtils.isEmpty;

import static androidx.media3.common.util.Util.inferContentType;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.C.ContentType;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.dash.DefaultDashChunkSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.LoopingMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MediaSourceEventListener;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

/**
 * @author eneim (2018/01/24).
 * @since 3.4.0
 */

public interface MediaSourceBuilder {

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    MediaSource buildMediaSource(@NonNull Context context, @NonNull Uri uri,
                                 @Nullable String fileExt, @Nullable Handler handler,
                                 @NonNull DataSource.Factory manifestDataSourceFactory,
                                 @NonNull DataSource.Factory mediaDataSourceFactory,
                                 @Nullable MediaSourceEventListener listener);

    MediaSourceBuilder DEFAULT = new MediaSourceBuilder() {
        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public MediaSource buildMediaSource(@NonNull Context context, @NonNull Uri uri,
                                            @Nullable String ext, @Nullable Handler handler,
                                            @NonNull DataSource.Factory manifestDataSourceFactory,
                                            @NonNull DataSource.Factory mediaDataSourceFactory, MediaSourceEventListener listener) {
            @ContentType int type = isEmpty(ext) ? inferContentType(uri) : inferContentType(Uri.parse("." + ext));
            MediaSource result;
            switch (type) {
                case C.CONTENT_TYPE_SS:
                    result = new SsMediaSource.Factory(
                            new DefaultSsChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(uri));
                    break;
                case C.CONTENT_TYPE_DASH:
                    result = new DashMediaSource.Factory(
                            new DefaultDashChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(uri));
                    break;
                case C.CONTENT_TYPE_HLS:
                    result = new HlsMediaSource.Factory(mediaDataSourceFactory) //
                            .createMediaSource(MediaItem.fromUri(uri));
                    break;
                case C.CONTENT_TYPE_OTHER:
                    result = new ProgressiveMediaSource.Factory(mediaDataSourceFactory) //
                            .createMediaSource(MediaItem.fromUri(uri));
                    break;
                default:
                    throw new IllegalStateException("Unsupported type: " + type);
            }

            result.addEventListener(handler, listener);
            return result;
        }
    };

    MediaSourceBuilder LOOPING = new MediaSourceBuilder() {

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public MediaSource buildMediaSource(@NonNull Context context, @NonNull Uri uri,
                                            @Nullable String fileExt, @Nullable Handler handler,
                                            @NonNull DataSource.Factory manifestDataSourceFactory,
                                            @NonNull DataSource.Factory mediaDataSourceFactory,
                                            @Nullable MediaSourceEventListener listener) {
            return new LoopingMediaSource(
                    DEFAULT.buildMediaSource(context, uri, fileExt, handler, manifestDataSourceFactory,
                            mediaDataSourceFactory, listener));
        }
    };
}
