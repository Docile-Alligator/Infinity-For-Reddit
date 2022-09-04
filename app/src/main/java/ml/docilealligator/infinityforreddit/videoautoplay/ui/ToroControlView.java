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

package ml.docilealligator.infinityforreddit.videoautoplay.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroExo;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroExoPlayer;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroPlayer;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;

/**
 * An extension of {@link PlayerControlView} that adds Volume control buttons. It works on-par
 * with {@link PlayerView}. Will be automatically inflated when client uses {@link R.layout.toro_exo_player_view}
 * for {@link PlayerView} layout.
 *
 * @author eneim (2018/08/20).
 * @since 3.6.0.2802
 */
public class ToroControlView extends PlayerControlView {

  @SuppressWarnings("unused") static final String TAG = "ToroExo:Control";

  // Statically obtain from super class.
  protected static Method hideAfterTimeoutMethod; // from parent ...
  protected static boolean hideMethodFetched;
  protected static Field hideActionField;
  protected static boolean hideActionFetched;

  final ComponentListener componentListener;
  final View volumeUpButton;
  final View volumeOffButton;
  final TimeBar volumeBar;
  final VolumeInfo volumeInfo = new VolumeInfo(false, 1);

  public ToroControlView(Context context) {
    this(context, null);
  }

  public ToroControlView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ToroControlView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    volumeOffButton = findViewById(R.id.exo_volume_off);
    volumeUpButton = findViewById(R.id.exo_volume_up);
    volumeBar = findViewById(R.id.volume_bar);
    componentListener = new ComponentListener();
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (volumeUpButton != null) volumeUpButton.setOnClickListener(componentListener);
    if (volumeOffButton != null) volumeOffButton.setOnClickListener(componentListener);
    if (volumeBar != null) volumeBar.addListener(componentListener);

    updateVolumeButtons();
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (volumeUpButton != null) volumeUpButton.setOnClickListener(null);
    if (volumeOffButton != null) volumeOffButton.setOnClickListener(null);
    if (volumeBar != null) volumeBar.removeListener(componentListener);
    this.setPlayer(null);
  }

  @SuppressLint("ClickableViewAccessibility") @Override
  public boolean onTouchEvent(MotionEvent event) {
    // After processing all children' touch event, this View will just stop it here.
    // User can click to PlayerView to show/hide this view, but since this View's height is not
    // significantly large, clicking to show/hide may disturb other actions like clicking to button,
    // seeking the bars, etc. This extension will stop the touch event here so that PlayerView has
    // nothing to do when User touch this View.
    return true;
  }

  @Override public void setPlayer(Player player) {
    Player current = super.getPlayer();
    if (current == player) return;

    if (current instanceof ToroExoPlayer) {
      ((ToroExoPlayer) current).removeOnVolumeChangeListener(componentListener);
    }

    super.setPlayer(player);
    current = super.getPlayer();
    @NonNull final VolumeInfo tempVol;
    if (current instanceof ToroExoPlayer) {
      tempVol = ((ToroExoPlayer) current).getVolumeInfo();
      ((ToroExoPlayer) current).addOnVolumeChangeListener(componentListener);
    } else if (current instanceof SimpleExoPlayer) {
      float volume = ((SimpleExoPlayer) current).getVolume();
      tempVol = new VolumeInfo(volume == 0, volume);
    } else {
      tempVol = new VolumeInfo(false, 1f);
    }

    this.volumeInfo.setTo(tempVol.isMute(), tempVol.getVolume());
    updateVolumeButtons();
  }

  @Override protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    if (changedView == this) updateVolumeButtons();
  }

  @SuppressWarnings("ConstantConditions") //
  void updateVolumeButtons() {
    if (!isVisible() || !ViewCompat.isAttachedToWindow(this)) {
      return;
    }
    boolean requestButtonFocus = false;
    // if muted then show volumeOffButton, or else show volumeUpButton
    boolean muted = volumeInfo.isMute();
    if (volumeOffButton != null) {
      requestButtonFocus |= muted && volumeOffButton.isFocused();
      volumeOffButton.setVisibility(muted ? View.VISIBLE : View.GONE);
    }
    if (volumeUpButton != null) {
      requestButtonFocus |= !muted && volumeUpButton.isFocused();
      volumeUpButton.setVisibility(!muted ? View.VISIBLE : View.GONE);
    }

    if (volumeBar != null) {
      volumeBar.setDuration(100);
      volumeBar.setPosition(muted ? 0 : (long) (volumeInfo.getVolume() * 100));
    }

    if (requestButtonFocus) {
      requestButtonFocus();
    }

    // A hack to access PlayerControlView's hideAfterTimeout. Don't want to re-implement it.
    // Reflection happens once for all instances, so it should not affect the performance.
    if (!hideMethodFetched) {
      try {
        hideAfterTimeoutMethod = PlayerControlView.class.getDeclaredMethod("hideAfterTimeout");
        hideAfterTimeoutMethod.setAccessible(true);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
      hideMethodFetched = true;
    }

    if (hideAfterTimeoutMethod != null) {
      try {
        hideAfterTimeoutMethod.invoke(this);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  private void requestButtonFocus() {
    boolean muted = volumeInfo.isMute();
    if (!muted && volumeUpButton != null) {
      volumeUpButton.requestFocus();
    } else if (muted && volumeOffButton != null) {
      volumeOffButton.requestFocus();
    }
  }

  void dispatchOnScrubStart() {
    // Fetch the 'hideAction' Runnable from super class. We need this to synchronize the show/hide
    // behaviour when user does something.
    if (!hideActionFetched) {
      try {
        hideActionField = PlayerControlView.class.getDeclaredField("hideAction");
        hideActionField.setAccessible(true);
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
      hideActionFetched = true;
    }

    if (hideActionField != null) {
      try {
        removeCallbacks((Runnable) hideActionField.get(this));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  // Scrub Move will always modify actual Volume, there is no 'mute-with-non-zero-volume' state.
  void dispatchOnScrubMove(long position) {
    if (position > 100) position = 100;
    if (position < 0) position = 0;

    float actualVolume = position / (float) 100;
    this.volumeInfo.setTo(actualVolume == 0, actualVolume);
    if (getPlayer() instanceof SimpleExoPlayer) {
      ToroExo.setVolumeInfo((SimpleExoPlayer) getPlayer(), this.volumeInfo);
    }

    updateVolumeButtons();
  }

  void dispatchOnScrubStop(long position) {
    this.dispatchOnScrubMove(position);
  }

  private class ComponentListener
      implements OnClickListener, TimeBar.OnScrubListener, ToroPlayer.OnVolumeChangeListener {

    ComponentListener() {
    }

    @Override public void onClick(View v) {
      Player player = ToroControlView.super.getPlayer();
      if (!(player instanceof SimpleExoPlayer)) return;
      if (v == volumeOffButton) {  // click to vol Off --> unmute
        volumeInfo.setTo(false, volumeInfo.getVolume());
      } else if (v == volumeUpButton) {  // click to vol Up --> mute
        volumeInfo.setTo(true, volumeInfo.getVolume());
      }
      ToroExo.setVolumeInfo((SimpleExoPlayer) player, volumeInfo);
      updateVolumeButtons();
    }

    /// TimeBar.OnScrubListener

    @Override public void onScrubStart(TimeBar timeBar, long position) {
      dispatchOnScrubStart();
    }

    @Override public void onScrubMove(TimeBar timeBar, long position) {
      dispatchOnScrubMove(position);
    }

    @Override public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
      dispatchOnScrubStop(position);
    }

    /// ToroPlayer.OnVolumeChangeListener

    @Override public void onVolumeChanged(@NonNull VolumeInfo volumeInfo) {
      ToroControlView.this.volumeInfo.setTo(volumeInfo.isMute(), volumeInfo.getVolume());
      updateVolumeButtons();
    }
  }
}
