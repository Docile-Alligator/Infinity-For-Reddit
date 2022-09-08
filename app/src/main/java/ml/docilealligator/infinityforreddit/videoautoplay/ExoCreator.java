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

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;

/**
 * A simple interface whose implementation helps Client to easily create {@link SimpleExoPlayer}
 * instance, {@link MediaSource} instance or specifically a {@link Playable} instance.
 *
 * Most of the time, Client just needs to request for a {@link Playable} for a specific Uri.
 *
 * @author eneim (2018/02/04).
 * @since 3.4.0
 */

public interface ExoCreator {

  String TAG = "ToroExo:Creator";

  /**
   * Return current Application context used in {@link ToroExo}. An {@link ExoCreator} must be used
   * within Application scope.
   */
  @Nullable Context getContext();

  /**
   * Create a new {@link SimpleExoPlayer} instance. This method should always create new instance of
   * {@link SimpleExoPlayer}, but client should use {@link ExoCreator} indirectly via
   * {@link ToroExo}.
   *
   * @return a new {@link SimpleExoPlayer} instance.
   */
  @NonNull
  ExoPlayer createPlayer();

  /**
   * Create a {@link MediaSource} from media {@link Uri}.
   *
   * @param uri the media {@link Uri}.
   * @param fileExt the optional (File) extension of the media Uri.
   * @return a {@link MediaSource} for media {@link Uri}.
   */
  @NonNull MediaSource createMediaSource(@NonNull Uri uri, @Nullable String fileExt);

  // Client just needs the method below to work with Toro, but I prepare both 2 above for custom use-cases.

  /**
   * Create a {@link Playable} for a media {@link Uri}. Client should always use this method for
   * quick and simple setup. Only use {@link #createMediaSource(Uri, String)} and/or
   * {@link #createPlayer()} when necessary.
   *
   * @param uri the media {@link Uri}.
   * @param fileExt the optional (File) extension of the media Uri.
   * @return the {@link Playable} to manage the media {@link Uri}.
   */
  @NonNull
  Playable createPlayable(@NonNull Uri uri, @Nullable String fileExt);
}
