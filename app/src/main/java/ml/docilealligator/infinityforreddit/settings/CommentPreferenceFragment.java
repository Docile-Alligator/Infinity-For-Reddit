package ml.docilealligator.infinityforreddit.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class CommentPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.comment_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        SeekBarPreference showFewerToolbarOptionsThresholdSeekBarPreference = findPreference(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD);

        if (showFewerToolbarOptionsThresholdSeekBarPreference != null) {
            showFewerToolbarOptionsThresholdSeekBarPreference.setSummary(getString(R.string.settings_show_fewer_toolbar_options_threshold_summary, sharedPreferences.getInt(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD, 5)));

            showFewerToolbarOptionsThresholdSeekBarPreference.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                showFewerToolbarOptionsThresholdSeekBarPreference.setSummary(
                        getString(R.string.settings_show_fewer_toolbar_options_threshold_summary, (Integer) newValue));
                return true;
            });
        }
    }
}