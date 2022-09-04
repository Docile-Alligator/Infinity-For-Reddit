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

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import ml.docilealligator.infinityforreddit.videoautoplay.annotations.Beta;
import ml.docilealligator.infinityforreddit.videoautoplay.widget.PressablePlayerSelector;

/**
 * @author eneim (2018/08/18).
 * @since 3.6.0.2802
 *
 * Work with {@link PressablePlayerSelector} and {@link PlayerView} to handle user's custom playback
 * interaction. A common use-case is when user clicks the Play button to manually start a playback.
 * We should respect this by putting the {@link ToroPlayer}'s priority to highest, and request a
 * refresh for all {@link ToroPlayer}.
 *
 * The same behaviour should be handled for the case user clicks the Pause button.
 *
 * All behaviour should be cleared once user scroll the selection out of playable region. This is
 * already handled by {@link PressablePlayerSelector}.
 */
@Beta //
public class ExoPlayerDispatcher extends DefaultControlDispatcher {

  private final PressablePlayerSelector playerSelector;
  private final ToroPlayer toroPlayer;

  public ExoPlayerDispatcher(PressablePlayerSelector playerSelector, ToroPlayer toroPlayer) {
    this.playerSelector = playerSelector;
    this.toroPlayer = toroPlayer;
  }

  @Override public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
    if (playWhenReady) {
      // Container will handle the call to play.
      return playerSelector.toPlay(toroPlayer.getPlayerOrder());
    } else {
      player.setPlayWhenReady(false);
      playerSelector.toPause(toroPlayer.getPlayerOrder());
      return true;
    }
  }
}
