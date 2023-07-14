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

package ml.docilealligator.infinityforreddit.videoautoplay.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author eneim | 6/5/17.
 *
 * A definition of DRM media type.
 */

public interface DrmMedia {

  // DRM Scheme
  @NonNull String getType();

  @Nullable String getLicenseUrl();

  @Nullable String[] getKeyRequestPropertiesArray();

  boolean multiSession();
}
