package ml.docilealligator.infinityforreddit.customviews.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapperReceiver;
import ml.docilealligator.infinityforreddit.utils.Utils;

public abstract class CustomFontPreferenceFragmentCompat extends PreferenceFragmentCompat implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    private static final String DIALOG_FRAGMENT_TAG =
            "androidx.preference.PreferenceFragment.DIALOG";

    protected SettingsActivity activity;
    protected View view;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        applyStyle();
    }

    protected void applyStyle() {
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

        if (activity.isImmersiveInterface()) {
            View recyclerView = getListView();
            if (recyclerView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets allInsets = Utils.getInsets(insets, false);
                        recyclerView.setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        }
    }

    @Override
    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        if (pref instanceof ListPreference) {
            DialogFragment f = CustomStyleListPreferenceDialogFragmentCompat.newInstance(pref.getKey());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
            return true;
        } else if (pref instanceof EditTextPreference) {
            DialogFragment f = CustomStyleEditTextPreferenceDialogFragmentCompat.newInstance(pref.getKey());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
            return true;
        }

        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}
