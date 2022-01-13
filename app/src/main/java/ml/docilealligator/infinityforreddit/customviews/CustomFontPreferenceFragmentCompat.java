package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;

public abstract class CustomFontPreferenceFragmentCompat extends PreferenceFragmentCompat {
    protected SettingsActivity activity;

    protected void setFont(Typeface typeface) {
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof CustomFontReceiver) {
                ((CustomFontReceiver) preference).setCustomFont(typeface, null, null);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}
