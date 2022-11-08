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

/**
 * This is an addition layer used in PlayerManager. Setting this where
 * {@link #getDelayToPlay(ToroPlayer)} returns a positive value will result in a delay in playback
 * play(). While returning {@link #DELAY_NONE} will dispatch the action immediately, and returning
 * {@link #DELAY_INFINITE} will not dispatch the action.
 *
 * @author eneim (2018/02/24).
 * @since 3.4.0
 */

public interface PlayerDispatcher {

  int DELAY_INFINITE = -1;

  int DELAY_NONE = 0;

  /**
   * Return the number of milliseconds that a call to {@link ToroPlayer#play()} should be delayed.
   * Returning {@link #DELAY_INFINITE} will not start the playback, while returning {@link
   * #DELAY_NONE} will start it immediately.
   *
   * @param player the player that is about to play.
   * @return number of milliseconds to delay the play, or one of {@link #DELAY_INFINITE} or
   * {@link #DELAY_NONE}. No other negative number should be used.
   */
  int getDelayToPlay(ToroPlayer player);

  PlayerDispatcher DEFAULT = new PlayerDispatcher() {
    @Override public int getDelayToPlay(ToroPlayer player) {
      return DELAY_NONE;
    }
  };
}
