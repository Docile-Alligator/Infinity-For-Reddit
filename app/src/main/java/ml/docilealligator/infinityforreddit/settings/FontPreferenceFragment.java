package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class FontPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.font_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        ListPreference fontFamilyPreference = findPreference(SharedPreferencesUtils.FONT_FAMILY_KEY);
        ListPreference titleFontFamilyPreference = findPreference(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY);
        ListPreference contentFontFamilyPreference = findPreference(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY);
        ListPreference fontSizePreference = findPreference(SharedPreferencesUtils.FONT_SIZE_KEY);
        ListPreference titleFontSizePreference = findPreference(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY);
        ListPreference contentFontSizePreference = findPreference(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY);

        if (fontFamilyPreference != null) {
            fontFamilyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                ActivityCompat.recreate(activity);
                return true;
            });
        }

        if (titleFontFamilyPreference != null) {
            titleFontFamilyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (contentFontFamilyPreference != null) {
            contentFontFamilyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (fontSizePreference != null) {
            fontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                ActivityCompat.recreate(activity);
                return true;
            });
        }

        if (titleFontSizePreference != null) {
            titleFontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (contentFontSizePreference != null) {
            contentFontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }
    }
}
