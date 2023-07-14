package ml.docilealligator.infinityforreddit.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

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

        SwitchPreference showCommentDividerSwitchPreference = findPreference(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER);
        ListPreference commentDividerTypeListPreference = findPreference(SharedPreferencesUtils.COMMENT_DIVIDER_TYPE);
        SeekBarPreference showFewerToolbarOptionsThresholdSeekBarPreference = findPreference(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD);

        if (showCommentDividerSwitchPreference != null && commentDividerTypeListPreference != null) {
            commentDividerTypeListPreference.setVisible(sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false));
            showCommentDividerSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                commentDividerTypeListPreference.setVisible((Boolean) newValue);
                return true;
            });
        }

        if (showFewerToolbarOptionsThresholdSeekBarPreference != null) {
            showFewerToolbarOptionsThresholdSeekBarPreference.setSummary(getString(R.string.settings_show_fewer_toolbar_options_threshold_summary, sharedPreferences.getInt(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD, 5)));

            showFewerToolbarOptionsThresholdSeekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                showFewerToolbarOptionsThresholdSeekBarPreference.setSummary(
                        getString(R.string.settings_show_fewer_toolbar_options_threshold_summary, (Integer) newValue));
                return true;
            });
        }
    }
}