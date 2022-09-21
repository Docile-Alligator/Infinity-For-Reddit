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

package ml.docilealligator.infinityforreddit.videoautoplay;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import ml.docilealligator.infinityforreddit.videoautoplay.widget.Container;

/**
 * @author eneim | 5/31/17.
 */

public final class ToroUtil {

    @SuppressWarnings("unused")
    private static final String TAG = "ToroLib:Util";

    private ToroUtil() {
        throw new RuntimeException("Meh!");
    }

    /**
     * Get the ratio in range of 0.0 ~ 1.0 the visible area of a {@link ToroPlayer}'s playerView.
     *
     * @param player    the {@link ToroPlayer} need to investigate.
     * @param container the {@link ViewParent} that holds the {@link ToroPlayer}. If {@code null}
     *                  then this method must returns 0.0f;
     * @return the value in range of 0.0 ~ 1.0 of the visible area.
     */
    @FloatRange(from = 0.0, to = 1.0) //
    public static float visibleAreaOffset(@NonNull ToroPlayer player, ViewParent container) {
        if (container == null) return 0.0f;

        View playerView = player.getPlayerView();
        Rect drawRect = new Rect();
        playerView.getDrawingRect(drawRect);
        int drawArea = drawRect.width() * drawRect.height();

        Rect playerRect = new Rect();
        boolean visible = playerView.getGlobalVisibleRect(playerRect, new Point());

        float offset = 0.f;
        if (visible && drawArea > 0) {
            int visibleArea = playerRect.height() * playerRect.width();
            offset = visibleArea / (float) drawArea;
        }
        return offset;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling
     * method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static @NonNull
    <T> T checkNotNull(final T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling
     * method is not null.
     *
     * @param reference    an object reference
     * @param errorMessage the exception message to use if the check fails; will
     *                     be converted to a string using {@link String#valueOf(Object)}
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static @NonNull
    <T> T checkNotNull(final T reference, final Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    @SuppressWarnings("unchecked")  //
    public static void wrapParamBehavior(@NonNull final Container container,
                                         final Container.BehaviorCallback callback) {
        container.setBehaviorCallback(callback);
        ViewGroup.LayoutParams params = container.getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior temp = ((CoordinatorLayout.LayoutParams) params).getBehavior();
            if (temp != null) {
                ((CoordinatorLayout.LayoutParams) params).setBehavior(new Container.Behavior(temp));
            }
        }
    }
}
