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

import static com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS;
import static ml.docilealligator.infinityforreddit.videoautoplay.ToroExo.toro;

import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.common.collect.ImmutableList;

import ml.docilealligator.infinityforreddit.R;

/**
 * Making {@link Playable} extensible. This can be used with custom {@link ExoCreator}. Extending
 * this class must make sure the re-usability of the implementation.
 *
 * @author eneim (2018/02/26).
 * @since 3.4.0
 */

@SuppressWarnings("WeakerAccess")
public class ExoPlayable extends PlayableImpl {

    @SuppressWarnings("unused")
    private static final String TAG = "ToroExo:Playable";

    private EventListener listener;

    // Adapt from ExoPlayer demo.
    protected boolean inErrorState = false;
    protected ImmutableList<Tracks.Group> lastSeenTrackGroupArray;

    /**
     * Construct an instance of {@link ExoPlayable} from an {@link ExoCreator} and {@link Uri}. The
     * {@link ExoCreator} is used to request {@link SimpleExoPlayer} instance, while {@link Uri}
     * defines the media to play.
     *
     * @param creator the {@link ExoCreator} instance.
     * @param uri     the {@link Uri} of the media.
     * @param fileExt the custom extension of the media Uri.
     */
    public ExoPlayable(ExoCreator creator, Uri uri, String fileExt) {
        super(creator, uri, fileExt);
    }

    @Override
    public void prepare(boolean prepareSource) {
        if (listener == null) {
            listener = new Listener();
            super.addEventListener(listener);
        }
        super.prepare(prepareSource);
        this.lastSeenTrackGroupArray = null;
        this.inErrorState = false;
    }

    @Override
    public void setPlayerView(@Nullable PlayerView playerView) {
        // This will also clear these flags
        if (playerView != this.playerView) {
            this.lastSeenTrackGroupArray = null;
            this.inErrorState = false;
        }
        super.setPlayerView(playerView);
    }

    @Override
    public void reset() {
        super.reset();
        this.lastSeenTrackGroupArray = null;
        this.inErrorState = false;
    }

    @Override
    public void release() {
        if (listener != null) {
            super.removeEventListener(listener);
            listener = null;
        }
        super.release();
        this.lastSeenTrackGroupArray = null;
        this.inErrorState = false;
    }

    @SuppressWarnings({"unused"}) //
    protected void onErrorMessage(@NonNull String message) {
        // Sub class can have custom reaction about the error here, including not to show this toast
        // (by not calling super.onErrorMessage(message)).
        if (this.errorListeners.size() > 0) {
            this.errorListeners.onError(new RuntimeException(message));
        } else if (playerView != null) {
            Toast.makeText(playerView.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    class Listener extends DefaultEventListener {
        @Override
        public void onTracksChanged(@NonNull Tracks tracks) {
            ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
            if (trackGroups == lastSeenTrackGroupArray) return;
            lastSeenTrackGroupArray = trackGroups;
            if (!(creator instanceof DefaultExoCreator)) return;
            TrackSelector selector = ((DefaultExoCreator) creator).getTrackSelector();
            if (selector instanceof DefaultTrackSelector) {
                MappedTrackInfo trackInfo = ((DefaultTrackSelector) selector).getCurrentMappedTrackInfo();
                if (trackInfo != null) {
                    if (trackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        onErrorMessage(toro.getString(R.string.error_unsupported_video));
                    }

                    if (trackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO) == RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        onErrorMessage(toro.getString(R.string.error_unsupported_audio));
                    }
                }
            }
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            inErrorState = true;
            if (isBehindLiveWindow(error)) {
                ExoPlayable.super.reset();
            } else {
                ExoPlayable.super.updatePlaybackInfo();
            }

            super.onPlayerError(error);
        }

        @Override
        public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
            if (inErrorState) {
                // Adapt from ExoPlayer demo.
                // "This will only occur if the user has performed a seek whilst in the error state. Update
                // the resume position so that if the user then retries, playback will resume from the
                // position to which they seek." - ExoPlayer
                ExoPlayable.super.updatePlaybackInfo();
            }
            super.onPositionDiscontinuity(oldPosition, newPosition, reason);
        }
    }

    static boolean isBehindLiveWindow(PlaybackException error) {
        if (error instanceof ExoPlaybackException && ((ExoPlaybackException) error).type != ExoPlaybackException.TYPE_SOURCE) return false;
        Throwable cause = error.getCause();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) return true;
            cause = cause.getCause();
        }
        return false;
    }
}
