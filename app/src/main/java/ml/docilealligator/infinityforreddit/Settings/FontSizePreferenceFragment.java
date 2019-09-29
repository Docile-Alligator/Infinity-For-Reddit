package ml.docilealligator.infinityforreddit.Settings;

import android.app.Activity;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.Event.ChangeFontSizeEvent;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;

public class FontSizePreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.font_size_preferences, rootKey);

        Activity activity = getActivity();
        if(activity != null) {
            ListPreference fontSizePreference = findPreference(SharedPreferencesUtils.FONT_SIZE_KEY);
            ListPreference titleFontSizePreference = findPreference(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY);
            ListPreference contentFontSizePreference = findPreference(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY);

            if(fontSizePreference != null) {
                fontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeFontSizeEvent());
                    activity.recreate();
                    return true;
                });
            }

            if(titleFontSizePreference != null) {
                titleFontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeFontSizeEvent());
                    return true;
                });
            }

            if(contentFontSizePreference != null) {
                contentFontSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeFontSizeEvent());
                    return true;
                });
            }
        }
    }
}
