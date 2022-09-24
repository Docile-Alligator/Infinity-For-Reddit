/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package ml.docilealligator.infinityforreddit.videoautoplay.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ml.docilealligator.infinityforreddit.videoautoplay.PlayerDispatcher;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroPlayer;

/**
 * Manage the collection of {@link ToroPlayer}s for a specific {@link Container}.
 *
 * Task: collect all Players in which "{@link Common#allowsToPlay(ToroPlayer)}" returns true, then
 * initialize them.
 *
 * @author eneim | 5/31/17.
 */
@SuppressWarnings({ "unused", "UnusedReturnValue", "StatementWithEmptyBody" }) //
final class PlayerManager implements Handler.Callback {

  private static final String TAG = "ToroLib:Manager";
  private Handler handler;

  // Make sure each ToroPlayer will present only once in this Manager.
  private final Set<ToroPlayer> players = new ArraySet<>();

  boolean attachPlayer(@NonNull ToroPlayer player) {
    return players.add(player);
  }

  boolean detachPlayer(@NonNull ToroPlayer player) {
    if (handler != null) handler.removeCallbacksAndMessages(player);
    return players.remove(player);
  }

  boolean manages(@NonNull ToroPlayer player) {
    return players.contains(player);
  }

  /**
   * Return a "Copy" of the collection of players this manager is managing.
   *
   * @return a non null collection of Players those a managed.
   */
  @NonNull List<ToroPlayer> getPlayers() {
    return new ArrayList<>(this.players);
  }

  void initialize(@NonNull ToroPlayer player, Container container) {
    player.initialize(container, container.getPlaybackInfo(player.getPlayerOrder()));
  }

  // 2018.07.02 Directly pass PlayerDispatcher so that we can easily expand the ability in the future.
  void play(@NonNull ToroPlayer player, PlayerDispatcher dispatcher) {
    this.play(player, dispatcher.getDelayToPlay(player));
  }

  private void play(@NonNull ToroPlayer player, int delay) {
    if (delay < PlayerDispatcher.DELAY_INFINITE) throw new IllegalArgumentException("Too negative");
    if (handler == null) return;  // equals to that this is not attached yet.
    handler.removeMessages(MSG_PLAY, player); // remove undone msg for this player
    if (delay == PlayerDispatcher.DELAY_INFINITE) {
      // do nothing
    } else if (delay == PlayerDispatcher.DELAY_NONE) {
      player.play();
    } else {
      handler.sendMessageDelayed(handler.obtainMessage(MSG_PLAY, player), delay);
    }
  }

  void pause(@NonNull ToroPlayer player) {
    // remove all msg sent for the player
    if (handler != null) handler.removeCallbacksAndMessages(player);
    player.pause();
  }

  // return false if this manager could not release the player.
  // normally when this manager doesn't manage the player.
  boolean release(@NonNull ToroPlayer player) {
    if (handler != null) handler.removeCallbacksAndMessages(null);
    if (manages(player)) {
      player.release();
      return true;
    } else {
      return false;
    }
  }

  void recycle(ToroPlayer player) {
    if (handler != null) handler.removeCallbacksAndMessages(player);
  }

  void clear() {
    if (handler != null) handler.removeCallbacksAndMessages(null);
    this.players.clear();
  }

  void deferPlaybacks() {
    if (handler != null) handler.removeMessages(MSG_PLAY);
  }

  void onAttach() {
    // do nothing
    if (handler == null) handler = new Handler(Looper.getMainLooper(), this);
  }

  void onDetach() {
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
      handler = null;
    }
  }

  @SuppressWarnings("WeakerAccess") static final int MSG_PLAY = 100;

  @Override public boolean handleMessage(Message msg) {
    if (msg.what == MSG_PLAY && msg.obj instanceof ToroPlayer) {
      ToroPlayer player = (ToroPlayer) msg.obj;
      player.play();
    }
    return true;
  }
}
