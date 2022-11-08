package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.CustomThemeWrapperReceiver;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;

public abstract class CustomFontPreferenceFragmentCompat extends PreferenceFragmentCompat {
    protected SettingsActivity activity;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen == null)
            return;

        int preferenceCount = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (preference instanceof CustomThemeWrapperReceiver) {
                ((CustomThemeWrapperReceiver) preference).setCustomThemeWrapper(activity.customThemeWrapper);
            }
            if (preference instanceof CustomFontReceiver) {
                ((CustomFontReceiver) preference).setCustomFont(activity.typeface, null, null);
            }
        }

        view.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}
