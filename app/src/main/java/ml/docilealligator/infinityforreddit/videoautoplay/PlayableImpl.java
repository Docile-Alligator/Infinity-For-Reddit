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
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.INDEX_UNSET;
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.TIME_UNSET;

import android.net.Uri;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;

/**
 * [20180225]
 * <p>
 * Default implementation of {@link Playable}.
 * <p>
 * Instance of {@link Playable} should be reusable. Retaining instance of Playable across config
 * change must guarantee that all {@link EventListener} are cleaned up on config change.
 *
 * @author eneim (2018/02/25).
 */
@SuppressWarnings("WeakerAccess") //
class PlayableImpl implements Playable {

    private final PlaybackInfo playbackInfo = new PlaybackInfo(); // never expose to outside.

    protected final EventListeners listeners = new EventListeners();  // original listener.
    protected final ToroPlayer.VolumeChangeListeners volumeChangeListeners = new ToroPlayer.VolumeChangeListeners();
    protected final ToroPlayer.ErrorListeners errorListeners = new ToroPlayer.ErrorListeners();

    protected final Uri mediaUri; // immutable, parcelable
    protected final String fileExt;
    protected final ExoCreator creator; // required, cached

    protected ToroExoPlayer player; // on-demand, cached
    protected MediaSource mediaSource;  // on-demand, since we do not reuse MediaSource now.
    protected PlayerView playerView; // on-demand, not always required.

    private boolean sourcePrepared = false;
    private boolean listenerApplied = false;

    PlayableImpl(ExoCreator creator, Uri uri, String fileExt) {
        this.creator = creator;
        this.mediaUri = uri;
        this.fileExt = fileExt;
    }

    @CallSuper
    @Override
    public void prepare(boolean prepareSource) {
        if (prepareSource) {
            ensureMediaSource();
            ensurePlayerView();
        }
    }

    @CallSuper
    @Override
    public void setPlayerView(@Nullable PlayerView playerView) {
        if (this.playerView == playerView) return;
        if (playerView == null) {
            this.playerView.setPlayer(null);
        } else {
            if (this.player != null) {
                PlayerView.switchTargetView(this.player.getPlayer(), this.playerView, playerView);
            }
        }

        this.playerView = playerView;
    }

    @Override
    public final PlayerView getPlayerView() {
        return this.playerView;
    }

    @CallSuper
    @Override
    public void play() {
        ensureMediaSource();
        ensurePlayerView();
        checkNotNull(player, "Playable#play(): Player is null!");
        player.getPlayer().setPlayWhenReady(true);
    }

    @CallSuper
    @Override
    public void pause() {
        // Player is not required to be non-null here.
        if (player != null) player.getPlayer().setPlayWhenReady(false);
    }

    @CallSuper
    @Override
    public void reset() {
        this.playbackInfo.reset();
        if (player != null) {
            // reset volume to default
            ToroExo.setVolumeInfo(this.player, new VolumeInfo(false, 1.f));
            player.getPlayer().stop();
            player.getPlayer().clearMediaItems();
        }
        this.mediaSource = null; // so it will be re-prepared when play() is called.
        this.sourcePrepared = false;
    }

    @CallSuper
    @Override
    public void release() {
        this.setPlayerView(null);
        if (this.player != null) {
            // reset volume to default
            ToroExo.setVolumeInfo(this.player, new VolumeInfo(false, 1.f));
            player.getPlayer().stop();
            player.getPlayer().clearMediaItems();
            if (listenerApplied) {
                player.getPlayer().removeListener(listeners);
                if (this.player != null) {
                    this.player.removeOnVolumeChangeListener(this.volumeChangeListeners);
                }
                listenerApplied = false;
            }
            with(checkNotNull(creator.getContext(), "ExoCreator has no Context")) //
                    .releasePlayer(this.creator, this.player.getPlayer());
        }
        this.player = null;
        this.mediaSource = null;
        this.sourcePrepared = false;
    }

    @CallSuper
    @NonNull
    @Override
    public PlaybackInfo getPlaybackInfo() {
        updatePlaybackInfo();
        return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition(),
                playbackInfo.getVolumeInfo());
    }

    @CallSuper
    @Override
    public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
        this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
        this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());
        this.setVolumeInfo(playbackInfo.getVolumeInfo());

        if (player != null) {
            ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
            boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
            if (haveResumePosition) {
                player.getPlayer().seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
            }
        }
    }

    @Override
    public final void addEventListener(@NonNull EventListener listener) {
        //noinspection ConstantConditions
        if (listener != null) this.listeners.add(listener);
    }

    @Override
    public final void removeEventListener(EventListener listener) {
        this.listeners.remove(listener);
    }

    @CallSuper
    @Override
    public void setVolume(float volume) {
        checkNotNull(player, "Playable#setVolume(): Player is null!");
        playbackInfo.getVolumeInfo().setTo(volume == 0, volume);
        ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
    }

    @CallSuper
    @Override
    public float getVolume() {
        return checkNotNull(player.getPlayer(), "Playable#getVolume(): Player is null!").getVolume();
    }

    @Override
    public boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
        boolean changed = !this.playbackInfo.getVolumeInfo().equals(checkNotNull(volumeInfo));
        if (changed) {
            this.playbackInfo.getVolumeInfo().setTo(volumeInfo.isMute(), volumeInfo.getVolume());
            if (player != null) ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
        }
        return changed;
    }

    @NonNull
    @Override
    public VolumeInfo getVolumeInfo() {
        return this.playbackInfo.getVolumeInfo();
    }

    @Override
    public void setParameters(@Nullable PlaybackParameters parameters) {
        checkNotNull(player.getPlayer(), "Playable#setParameters(PlaybackParameters): Player is null") //
                .setPlaybackParameters(parameters);
    }

    @Override
    public PlaybackParameters getParameters() {
        return checkNotNull(player.getPlayer(), "Playable#getParameters(): Player is null").getPlaybackParameters();
    }

    @Override
    public void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
        volumeChangeListeners.add(checkNotNull(listener));
    }

    @Override
    public void removeOnVolumeChangeListener(@Nullable ToroPlayer.OnVolumeChangeListener listener) {
        volumeChangeListeners.remove(listener);
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.getPlayer().getPlayWhenReady();
    }

    @Override
    public void addErrorListener(@NonNull ToroPlayer.OnErrorListener listener) {
        this.errorListeners.add(checkNotNull(listener));
    }

    @Override
    public void removeErrorListener(@Nullable ToroPlayer.OnErrorListener listener) {
        this.errorListeners.remove(listener);
    }

    final void updatePlaybackInfo() {
        if (player == null || player.getPlayer().getPlaybackState() == Player.STATE_IDLE) return;
        playbackInfo.setResumeWindow(player.getPlayer().getCurrentWindowIndex());
        playbackInfo.setResumePosition(player.getPlayer().isCurrentWindowSeekable() ? //
                Math.max(0, player.getPlayer().getCurrentPosition()) : TIME_UNSET);
        playbackInfo.setVolumeInfo(ToroExo.getVolumeInfo(player));
    }

    private void ensurePlayerView() {
        if (playerView != null && playerView.getPlayer() != player.getPlayer()) playerView.setPlayer(player.getPlayer());
    }

    // TODO [20180822] Double check this.
    private void ensureMediaSource() {
        if (mediaSource == null) {  // Only actually prepare the source when play() is called.
            sourcePrepared = false;
            mediaSource = creator.createMediaSource(mediaUri, fileExt);
        }

        if (!sourcePrepared) {
            ensurePlayer(); // sourcePrepared is set to false only when player is null.
            beforePrepareMediaSource();
            player.getPlayer().prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
            sourcePrepared = true;
        }
    }

    private void ensurePlayer() {
        if (player == null) {
            sourcePrepared = false;
            player = with(checkNotNull(creator.getContext(), "ExoCreator has no Context")) //
                    .requestPlayer(creator);
            listenerApplied = false;
        }

        if (!listenerApplied) {
            player.addOnVolumeChangeListener(volumeChangeListeners);
            player.getPlayer().addListener(listeners);
            listenerApplied = true;
        }

        ToroExo.setVolumeInfo(player, this.playbackInfo.getVolumeInfo());
        boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.getPlayer().seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
        }
    }

    // Trick to inject to the Player creation event.
    // Required for AdsLoader to set Player.
    protected void beforePrepareMediaSource() {
    }
}
