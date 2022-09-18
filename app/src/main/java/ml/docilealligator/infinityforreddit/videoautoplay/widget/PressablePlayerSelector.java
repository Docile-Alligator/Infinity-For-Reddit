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

package ml.docilealligator.infinityforreddit.videoautoplay.widget;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static java.util.Collections.singletonList;

import static ml.docilealligator.infinityforreddit.videoautoplay.widget.Common.allowsToPlay;
import static ml.docilealligator.infinityforreddit.videoautoplay.widget.Common.findFirst;

import android.view.View;
import android.view.View.OnLongClickListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ml.docilealligator.infinityforreddit.videoautoplay.PlayerSelector;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroPlayer;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil;
import ml.docilealligator.infinityforreddit.videoautoplay.annotations.Beta;

/**
 * @author eneim (2018/08/17).
 *
 * A 'Press to Play' {@link PlayerSelector}.
 *
 * This is a {@link OnLongClickListener} that co-operates with {@link Container} to selectively
 * trigger the playback. The common usecase is to allow user to long click on a {@link ToroPlayer}
 * to trigger its playback. In that case, we should set that {@link ToroPlayer} to highest priority
 * among the candidates, and also to clear its priority when user scroll it out of the playable region.
 *
 * This class also have a {@link #toPause} field to handle the case where User want to specifically
 * pause a {@link ToroPlayer}. This selection will also be cleared with the same rules of toPlay.
 * @since 3.6.0.2802
 */
@SuppressWarnings("WeakerAccess") @Beta //
public class PressablePlayerSelector implements PlayerSelector, OnLongClickListener {

  protected final WeakReference<Container> weakContainer;
  protected final PlayerSelector delegate;
  protected final AtomicInteger toPlay = new AtomicInteger(NO_POSITION);
  protected final AtomicInteger toPause = new AtomicInteger(NO_POSITION);

  final Common.Filter<ToroPlayer> filterToPlay = new Common.Filter<ToroPlayer>() {
    @Override public boolean accept(ToroPlayer target) {
      return target.getPlayerOrder() == toPlay.get();
    }
  };

  final Common.Filter<ToroPlayer> filterToPause = new Common.Filter<ToroPlayer>() {
    @Override public boolean accept(ToroPlayer target) {
      return target.getPlayerOrder() == toPause.get();
    }
  };

  public PressablePlayerSelector(Container container) {
    this(container, DEFAULT);
  }

  public PressablePlayerSelector(Container container, PlayerSelector delegate) {
    this(new WeakReference<>(ToroUtil.checkNotNull(container)), ToroUtil.checkNotNull(delegate));
  }

  PressablePlayerSelector(WeakReference<Container> container, PlayerSelector delegate) {
    this.weakContainer = container;
    this.delegate = ToroUtil.checkNotNull(delegate);
  }

  @Override public boolean onLongClick(View v) {
    Container container = weakContainer.get();
    if (container == null) return false;  // fail fast

    toPause.set(NO_POSITION); // long click will always mean to 'press to play'.

    RecyclerView.ViewHolder viewHolder = container.findContainingViewHolder(v);
    boolean handled = viewHolder instanceof ToroPlayer;
    if (handled) handled = allowsToPlay((ToroPlayer) viewHolder);

    int position = handled ? viewHolder.getAdapterPosition() : NO_POSITION;
    if (handled) handled = position != toPlay.getAndSet(position);

    if (handled) container.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);
    return handled;
  }

  @Override @NonNull public Collection<ToroPlayer> select(@NonNull Container container,
      @NonNull List<ToroPlayer> items) {
    // Make sure client doesn't use this instance to wrong Container.
    if (container != this.weakContainer.get()) return new ArrayList<>();

    // If there is a request to pause, we need to prioritize that first.
    ToroPlayer toPauseCandidate = null;
    if (toPause.get() >= 0) {
      toPauseCandidate = findFirst(items, filterToPause);
      if (toPauseCandidate == null) {
        // the order to pause doesn't present in candidate, we clear the selection.
        toPause.set(NO_POSITION); // remove the paused one.
      }
    }

    if (toPlay.get() >= 0) {
      ToroPlayer toPlayCandidate = findFirst(items, filterToPlay);
      if (toPlayCandidate != null) {
        if (allowsToPlay(toPlayCandidate)) {
          return singletonList(toPlayCandidate);
        }
      }
    }
    // In the list of candidates, selected item no longer presents or is not allowed to play,
    // we should reset the selection.
    toPlay.set(NO_POSITION);
    // Wrap by an ArrayList to make it modifiable.
    Collection<ToroPlayer> selected = new ArrayList<>(delegate.select(container, items));
    if (toPauseCandidate != null) selected.remove(toPauseCandidate);
    return selected;
  }

  @Override @NonNull public PlayerSelector reverse() {
    return new PressablePlayerSelector(this.weakContainer, delegate.reverse());
  }

  public boolean toPlay(int position) {
    if (toPause.get() == position) toPause.set(NO_POSITION);
    Container container = weakContainer.get();
    if (container == null) return false;
    if (position != toPlay.getAndSet(position)) {
      container.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);
      return true;
    }

    return false;
  }

  public void toPause(int position) {
    toPlay.set(NO_POSITION);
    toPause.set(position);
  }
}
