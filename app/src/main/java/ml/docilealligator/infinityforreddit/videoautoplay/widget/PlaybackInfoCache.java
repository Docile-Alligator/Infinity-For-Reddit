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

import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.SCRAP;
import static ml.docilealligator.infinityforreddit.videoautoplay.widget.Common.ORDER_COMPARATOR_INT;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ml.docilealligator.infinityforreddit.videoautoplay.ToroPlayer;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil;
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;

/**
 * @author eneim (2018/04/24).
 *
 * Design Target:
 *
 * [1] Manage the {@link PlaybackInfo} of current {@link ToroPlayer}s. Should match 1-1 with the
 * {@link ToroPlayer}s that {@link PlayerManager} is managing.
 *
 * [2] If a non-null {@link CacheManager} provided to the {@link Container}, this class must
 * properly manage the {@link PlaybackInfo} of detached {@link ToroPlayer} and restore it to
 * previous state after being re-attached.
 */
@SuppressWarnings({ "unused" })
@SuppressLint("UseSparseArrays") //
final class PlaybackInfoCache extends RecyclerView.AdapterDataObserver {

  @NonNull private final Container container;
  // Cold cache represents the map between key obtained from CacheManager and PlaybackInfo. If the
  // CacheManager is null, this cache will hold nothing.
  /* pkg */ HashMap<Object, PlaybackInfo> coldCache = new HashMap<>();

  // Hot cache represents the map between Player's order and its PlaybackInfo. A key-value map only
  // lives within a Player's attached state.
  // Being a TreeMap because we need to traversal through it in order sometime.
  /* pkg */ TreeMap<Integer, PlaybackInfo> hotCache; // only cache attached Views.

  // Holds the map between Player's order and its key obtain from CacheManager.
  /* pkg */ TreeMap<Integer, Object> coldKeyToOrderMap = new TreeMap<>(ORDER_COMPARATOR_INT);

  PlaybackInfoCache(@NonNull Container container) {
    this.container = container;
  }

  final void onAttach() {
    hotCache = new TreeMap<>(ORDER_COMPARATOR_INT);
  }

  final void onDetach() {
    if (hotCache != null) {
      hotCache.clear();
      hotCache = null;
    }
    coldKeyToOrderMap.clear();
  }

  final void onPlayerAttached(ToroPlayer player) {
    int playerOrder = player.getPlayerOrder();
    // [1] Check if there is cold cache for this player
    Object key = getKey(playerOrder);
    if (key != null) coldKeyToOrderMap.put(playerOrder, key);

    PlaybackInfo cache = key == null ? null : coldCache.get(key);
    if (cache == null || cache == SCRAP) {
      // We init this even if there is no CacheManager available, because this is what User expects.
      cache = container.playerInitializer.initPlaybackInfo(playerOrder);
      // Only save to cold cache when there is a valid CacheManager (key is not null).
      if (key != null) coldCache.put(key, cache);
    }

    if (hotCache != null) hotCache.put(playerOrder, cache);
  }

  // Will be called from Container#onChildViewDetachedFromWindow(View)
  // Therefore, it may not be called on all views. For example: when user close the App, by default
  // when RecyclerView is detached from Window, it will not call onChildViewDetachedFromWindow for
  // its children.
  // This method will:
  // [1] Take current hot cache entry of the player, and put back to cold cache.
  // [2] Remove the hot cache entry of the player.
  final void onPlayerDetached(ToroPlayer player) {
    int playerOrder = player.getPlayerOrder();
    if (hotCache != null && hotCache.containsKey(playerOrder)) {
      PlaybackInfo cache = hotCache.remove(playerOrder);
      Object key = getKey(playerOrder);
      if (key != null) coldCache.put(key, cache);
    }
  }

  @SuppressWarnings("unused") final void onPlayerRecycled(ToroPlayer player) {
    // TODO do anything here?
  }

  /// Adapter change events handling

  @Override public void onChanged() {
    if (container.getCacheManager() != null) {
      for (Integer key : coldKeyToOrderMap.keySet()) {
        Object cacheKey = getKey(key);
        coldCache.put(cacheKey, SCRAP);
        coldKeyToOrderMap.put(key, cacheKey);
      }
    }

    if (hotCache != null) {
      for (Integer key : hotCache.keySet()) {
        hotCache.put(key, SCRAP);
      }
    }
  }

  @Override public void onItemRangeChanged(final int positionStart, final int itemCount) {
    if (itemCount == 0) return;
    if (container.getCacheManager() != null) {
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= positionStart && key < positionStart + itemCount) {
          changedColdKeys.add(key);
        }
      }

      for (Integer key : changedColdKeys) {
        Object cacheKey = getKey(key);
        coldCache.put(cacheKey, SCRAP);
        coldKeyToOrderMap.put(key, cacheKey);
      }
    }

    if (hotCache != null) {
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : hotCache.keySet()) {
        if (key >= positionStart && key < positionStart + itemCount) {
          changedHotKeys.add(key);
        }
      }

      for (Integer key : changedHotKeys) {
        hotCache.put(key, SCRAP);
      }
    }
  }

  @Override public void onItemRangeInserted(final int positionStart, final int itemCount) {
    if (itemCount == 0) return;
    PlaybackInfo value;
    // Cold cache update
    if (container.getCacheManager() != null) {
      // [1] Take keys of old one.
      // 1.1 Extract subset of keys only:
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= positionStart) {
          changedColdKeys.add(key);
        }
      }

      // 1.2 Extract entries from cold cache to a temp cache.
      final Map<Object, PlaybackInfo> changeColdEntriesCache = new HashMap<>();
      for (Integer key : changedColdKeys) {
        if ((value = coldCache.remove(coldKeyToOrderMap.get(key))) != null) {
          changeColdEntriesCache.put(key, value);
        }
      }

      // 1.2 Update cold Cache with new keys
      for (Integer key : changedColdKeys) {
        coldCache.put(getKey(key + itemCount), changeColdEntriesCache.get(key));
      }

      // 1.3 Update coldKeyToOrderMap;
      for (Integer key : changedColdKeys) {
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    // [1] Remove cache if there is any appearance
    if (hotCache != null) {
      // [2] Shift cache by specific number
      Map<Integer, PlaybackInfo> changedHotEntriesCache = new HashMap<>();
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : hotCache.keySet()) {
        if (key >= positionStart) {
          changedHotKeys.add(key);
        }
      }

      for (Integer key : changedHotKeys) {
        if ((value = hotCache.remove(key)) != null) {
          changedHotEntriesCache.put(key, value);
        }
      }

      for (Integer key : changedHotKeys) {
        hotCache.put(key + itemCount, changedHotEntriesCache.get(key));
      }
    }
  }

  @Override public void onItemRangeRemoved(final int positionStart, final int itemCount) {
    if (itemCount == 0) return;
    PlaybackInfo value;
    // Cold cache update
    if (container.getCacheManager() != null) {
      // [1] Take keys of old one.
      // 1.1 Extract subset of keys only:
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= positionStart + itemCount) changedColdKeys.add(key);
      }
      // 1.2 Extract entries from cold cache to a temp cache.
      final Map<Object, PlaybackInfo> changeColdEntriesCache = new HashMap<>();
      for (Integer key : changedColdKeys) {
        if ((value = coldCache.remove(coldKeyToOrderMap.get(key))) != null) {
          changeColdEntriesCache.put(key, value);
        }
      }

      // 1.2 Update cold Cache with new keys
      for (Integer key : changedColdKeys) {
        coldCache.put(getKey(key - itemCount), changeColdEntriesCache.get(key));
      }

      // 1.3 Update coldKeyToOrderMap;
      for (Integer key : changedColdKeys) {
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    // [1] Remove cache if there is any appearance
    if (hotCache != null) {
      for (int i = 0; i < itemCount; i++) {
        hotCache.remove(positionStart + i);
      }

      // [2] Shift cache by specific number
      Map<Integer, PlaybackInfo> changedHotEntriesCache = new HashMap<>();
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : hotCache.keySet()) {
        if (key >= positionStart + itemCount) changedHotKeys.add(key);
      }

      for (Integer key : changedHotKeys) {
        if ((value = hotCache.remove(key)) != null) {
          changedHotEntriesCache.put(key, value);
        }
      }

      for (Integer key : changedHotKeys) {
        hotCache.put(key - itemCount, changedHotEntriesCache.get(key));
      }
    }
  }

  // Dude I wanna test this thing >.<
  @Override public void onItemRangeMoved(final int fromPos, final int toPos, int itemCount) {
    if (fromPos == toPos) return;

    final int low = fromPos < toPos ? fromPos : toPos;
    final int high = fromPos + toPos - low;
    final int shift = fromPos < toPos ? -1 : 1;  // how item will be shifted due to the move
    PlaybackInfo value;
    // [1] Migrate cold cache.
    if (container.getCacheManager() != null) {
      // 1.1 Extract subset of keys only:
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= low && key <= high) changedColdKeys.add(key);
      }
      // 1.2 Extract entries from cold cache to a temp cache.
      final Map<Object, PlaybackInfo> changeColdEntries = new HashMap<>();
      for (Integer key : changedColdKeys) {
        if ((value = coldCache.remove(coldKeyToOrderMap.get(key))) != null) {
          changeColdEntries.put(key, value);
        }
      }

      // 1.2 Update cold Cache with new keys
      for (Integer key : changedColdKeys) {
        if (key == low) {
          coldCache.put(getKey(high), changeColdEntries.get(key));
        } else {
          coldCache.put(getKey(key + shift), changeColdEntries.get(key));
        }
      }

      // 1.3 Update coldKeyToOrderMap;
      for (Integer key : changedColdKeys) {
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    // [2] Migrate hot cache.
    if (hotCache != null) {
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR_INT);
      for (Integer key : hotCache.keySet()) {
        if (key >= low && key <= high) changedHotKeys.add(key);
      }

      Map<Integer, PlaybackInfo> changedHotEntriesCache = new HashMap<>();
      for (Integer key : changedHotKeys) {
        if ((value = hotCache.remove(key)) != null) changedHotEntriesCache.put(key, value);
      }

      for (Integer key : changedHotKeys) {
        if (key == low) {
          hotCache.put(high, changedHotEntriesCache.get(key));
        } else {
          hotCache.put(key + shift, changedHotEntriesCache.get(key));
        }
      }
    }
  }

  @Nullable private Object getKey(int position) {
    return position == RecyclerView.NO_POSITION ? null : container.getCacheManager() == null ? null
        : container.getCacheManager().getKeyForOrder(position);
  }

  //@Nullable private Integer getOrder(Object key) {
  //  return container.getCacheManager() == null ? null
  //      : container.getCacheManager().getOrderForKey(key);
  //}

  @NonNull final PlaybackInfo getPlaybackInfo(int position) {
    PlaybackInfo info = hotCache != null ? hotCache.get(position) : null;
    if (info == SCRAP) {  // has hot cache, but was SCRAP.
      info = container.playerInitializer.initPlaybackInfo(position);
    }

    Object key = getKey(position);
    info = info != null ? info : (key != null ? coldCache.get(key) : null);
    if (info == null) info = container.playerInitializer.initPlaybackInfo(position);
    return info;
  }

  // Call by Container#savePlaybackInfo and that method is called right before any pausing.
  final void savePlaybackInfo(int position, @NonNull PlaybackInfo playbackInfo) {
    ToroUtil.checkNotNull(playbackInfo);
    if (hotCache != null) hotCache.put(position, playbackInfo);
    Object key = getKey(position);
    if (key != null) coldCache.put(key, playbackInfo);
  }

  @NonNull SparseArray<PlaybackInfo> saveStates() {
    SparseArray<PlaybackInfo> states = new SparseArray<>();
    if (container.getCacheManager() != null) {
      for (Map.Entry<Integer, Object> entry : coldKeyToOrderMap.entrySet()) {
        states.put(entry.getKey(), coldCache.get(entry.getValue()));
      }
    } else if (hotCache != null) {
      for (Map.Entry<Integer, PlaybackInfo> entry : hotCache.entrySet()) {
        states.put(entry.getKey(), entry.getValue());
      }
    }
    return states;
  }

  void restoreStates(@Nullable SparseArray<?> savedStates) {
    int cacheSize;
    if (savedStates != null && (cacheSize = savedStates.size()) > 0) {
      for (int i = 0; i < cacheSize; i++) {
        int order = savedStates.keyAt(i);
        Object key = getKey(order);
        coldKeyToOrderMap.put(order, key);
        PlaybackInfo playbackInfo = (PlaybackInfo) savedStates.get(order);
        if (playbackInfo != null) this.savePlaybackInfo(order, playbackInfo);
      }
    }
  }

  final void clearCache() {
    coldCache.clear();
    if (hotCache != null) hotCache.clear();
  }
}
