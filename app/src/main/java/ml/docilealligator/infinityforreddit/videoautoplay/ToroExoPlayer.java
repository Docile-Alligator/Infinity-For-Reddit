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

import static ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil.checkNotNull;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;

import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;

/**
 * A custom {@link SimpleExoPlayer} that also notify the change of Volume.
 *
 * @author eneim (2018/03/27).
 */
@SuppressWarnings("WeakerAccess") //
public class ToroExoPlayer {

    private ExoPlayer player;

    public ToroExoPlayer(Context context, RenderersFactory renderersFactory,
                            TrackSelector trackSelector, LoadControl loadControl, BandwidthMeter bandwidthMeter,
                            Looper looper) {
        player = new ExoPlayer.Builder(context).setRenderersFactory(renderersFactory).setTrackSelector(trackSelector).setLoadControl(loadControl).setBandwidthMeter(bandwidthMeter).setLooper(looper).build();
    }

    public ToroExoPlayer(ExoPlayer exoPlayer) {
        this.player = exoPlayer;
    }

    private ToroPlayer.VolumeChangeListeners listeners;

    public final void addOnVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
        if (this.listeners == null) this.listeners = new ToroPlayer.VolumeChangeListeners();
        this.listeners.add(checkNotNull(listener));
    }

    public final void removeOnVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
        if (this.listeners != null) this.listeners.remove(listener);
    }

    public final void clearOnVolumeChangeListener() {
        if (this.listeners != null) this.listeners.clear();
    }

    @CallSuper
    public void setVolume(float audioVolume) {
        this.setVolumeInfo(new VolumeInfo(audioVolume == 0, audioVolume));
    }

    private final VolumeInfo volumeInfo = new VolumeInfo(false, 1f);

    @SuppressWarnings("UnusedReturnValue")
    public final boolean setVolumeInfo(@NonNull VolumeInfo volumeInfo) {
        boolean changed = !this.volumeInfo.equals(volumeInfo);
        if (changed) {
            this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
            player.setVolume(volumeInfo.isMute() ? 0 : volumeInfo.getVolume());
            if (listeners != null) {
                for (ToroPlayer.OnVolumeChangeListener listener : this.listeners) {
                    listener.onVolumeChanged(volumeInfo);
                }
            }
        }

        return changed;
    }

    @SuppressWarnings("unused")
    @NonNull
    public final VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public ExoPlayer getPlayer() {
        return player;
    }
}
