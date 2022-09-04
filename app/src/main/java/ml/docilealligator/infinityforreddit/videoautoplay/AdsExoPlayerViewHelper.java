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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.ui.PlayerView;

import ml.docilealligator.infinityforreddit.videoautoplay.annotations.Beta;

/**
 * A {@link ToroPlayerHelper} to integrate ExoPlayer IMA Extension. Work together with {@link
 * AdsPlayable}.
 *
 * @author eneim (2018/08/22).
 * @since 3.6.0.2802
 */
@SuppressWarnings("unused") @Beta //
public class AdsExoPlayerViewHelper extends ExoPlayerViewHelper {

  static class DefaultAdViewProvider implements AdsLoader.AdViewProvider {

    @NonNull final ViewGroup viewGroup;

    DefaultAdViewProvider(@NonNull ViewGroup viewGroup) {
      this.viewGroup = viewGroup;
    }

    @Override public ViewGroup getAdViewGroup() {
      return this.viewGroup;
    }

    @Override public View[] getAdOverlayViews() {
      return new View[0];
    }
  }

  private static AdsPlayable createPlayable(  ///
                                                                    ToroPlayer player,      //
                                                                    ExoCreator creator,     //
                                                                    Uri contentUri,         //
                                                                    String fileExt,         //
                                                                    AdsLoader adsLoader,    //
                                                                    AdsLoader.AdViewProvider adViewProvider   //
  ) {
    return new AdsPlayable(creator, contentUri, fileExt, player, adsLoader, adViewProvider);
  }

  private static AdsPlayable createPlayable(  //
                                                                    ToroPlayer player,      //
                                                                    Config config,          //
                                                                    Uri contentUri,         //
                                                                    String fileExt,         //
                                                                    AdsLoader adsLoader,    //
                                                                    AdsLoader.AdViewProvider adViewProvider   //
  ) {
    Context context = player.getPlayerView().getContext();
    return createPlayable(player, ToroExo.with(context).getCreator(config), contentUri, fileExt,
        adsLoader, adViewProvider);
  }

  private static AdsPlayable createPlayable(  //
                                                                    ToroPlayer player,      //
                                                                    Uri contentUri,         //
                                                                    String fileExt,         //
                                                                    AdsLoader adsLoader,    //
                                                                    AdsLoader.AdViewProvider adViewProvider   //
  ) {
    Context context = player.getPlayerView().getContext();
    return createPlayable(player, ToroExo.with(context).getDefaultCreator(), contentUri, fileExt,
        adsLoader, adViewProvider);
  }

  // Neither ExoCreator nor Config are provided.

  /**
   * Create new {@link AdsExoPlayerViewHelper} for a {@link ToroPlayer} and {@link AdsLoader}.
   *
   * @param adContainer if {@code null} then overlay of {@link PlayerView} will be used.
   */
  @Deprecated
  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer   //
  ) {
    super(player,
        createPlayable(player, uri, fileExt, adsLoader,
            adContainer != null ? new DefaultAdViewProvider(adContainer) : null));
  }

  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer,   // will be ignored
      @Nullable AdsLoader.AdViewProvider adViewProvider   //
  ) {
    super(player, createPlayable(player, uri, fileExt, adsLoader, adViewProvider));
  }

  // ExoCreator is provided.

  /**
   * Create new {@link AdsExoPlayerViewHelper} for a {@link ToroPlayer} and {@link AdsLoader}.
   *
   * @param adContainer if {@code null} then overlay of {@link PlayerView} will be used.
   */
  @Deprecated
  public AdsExoPlayerViewHelper(        //
                                        @NonNull ToroPlayer player,       //
                                        @NonNull Uri uri,                 //
                                        @Nullable String fileExt,         //
                                        @NonNull AdsLoader adsLoader,     //
                                        @Nullable ViewGroup adContainer,  //
                                        @NonNull ExoCreator creator       //
  ) {
    super(player, createPlayable(player, creator, uri, fileExt, adsLoader,
        adContainer != null ? new DefaultAdViewProvider(adContainer) : null));
  }

  public AdsExoPlayerViewHelper(        //
                                        @NonNull ToroPlayer player,       //
                                        @NonNull Uri uri,                 //
                                        @Nullable String fileExt,         //
                                        @NonNull AdsLoader adsLoader,     //
                                        @Nullable ViewGroup adContainer,  // will be ignored
                                        @Nullable AdsLoader.AdViewProvider adViewProvider,  //
                                        @NonNull ExoCreator creator       //
  ) {
    super(player, createPlayable(player, creator, uri, fileExt, adsLoader, adViewProvider));
  }
  // Config is provided.

  /**
   * Create new {@link AdsExoPlayerViewHelper} for a {@link ToroPlayer} and {@link AdsLoader}.
   *
   * @param adContainer if {@code null} then overlay of {@link PlayerView} will be used.
   */
  @Deprecated
  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer,  //
      @NonNull Config config            //
  ) {
    super(player, createPlayable(player, config, uri, fileExt, adsLoader,
        adContainer != null ? new DefaultAdViewProvider(adContainer) : null));
  }

  public AdsExoPlayerViewHelper(        //
      @NonNull ToroPlayer player,       //
      @NonNull Uri uri,                 //
      @Nullable String fileExt,         //
      @NonNull AdsLoader adsLoader,     //
      @Nullable ViewGroup adContainer,  // will be ignored
      @Nullable AdsLoader.AdViewProvider adViewProvider,  //
      @NonNull Config config            //
  ) {
    super(player, createPlayable(player, config, uri, fileExt, adsLoader, adViewProvider));
  }
}
