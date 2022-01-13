package ml.docilealligator.infinityforreddit.settings;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeAutoplayNsfwVideosEvent;
import ml.docilealligator.infinityforreddit.events.ChangeMuteAutoplayingVideosEvent;
import ml.docilealligator.infinityforreddit.events.ChangeMuteNSFWVideoEvent;
import ml.docilealligator.infinityforreddit.events.ChangeRememberMutingOptionInPostFeedEvent;
import ml.docilealligator.infinityforreddit.events.ChangeStartAutoplayVisibleAreaOffsetEvent;
import ml.docilealligator.infinityforreddit.events.ChangeVideoAutoplayEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class VideoPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.video_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        ListPreference videoAutoplayListPreference = findPreference(SharedPreferencesUtils.VIDEO_AUTOPLAY);
        SwitchPreference muteAutoplayingVideosSwitchPreference = findPreference(SharedPreferencesUtils.MUTE_AUTOPLAYING_VIDEOS);
        SwitchPreference rememberMutingOptionInPostFeedSwitchPreference = findPreference(SharedPreferencesUtils.REMEMBER_MUTING_OPTION_IN_POST_FEED);
        SwitchPreference muteNSFWVideosSwitchPreference = findPreference(SharedPreferencesUtils.MUTE_NSFW_VIDEO);
        SwitchPreference autoplayNsfwVideosSwitchPreference = findPreference(SharedPreferencesUtils.AUTOPLAY_NSFW_VIDEOS);
        SeekBarPreference startAutoplayVisibleAreaOffsetPortrait = findPreference(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_PORTRAIT);
        SeekBarPreference startAutoplayVisibleAreaOffsetLandscape = findPreference(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_LANDSCAPE);

        if (videoAutoplayListPreference != null && autoplayNsfwVideosSwitchPreference != null) {
            videoAutoplayListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeVideoAutoplayEvent((String) newValue));
                return true;
            });

            autoplayNsfwVideosSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeAutoplayNsfwVideosEvent((Boolean) newValue));
                return true;
            });
        }

        if (muteNSFWVideosSwitchPreference != null) {
            muteNSFWVideosSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeMuteNSFWVideoEvent((Boolean) newValue));
                return true;
            });
        }

        if (muteAutoplayingVideosSwitchPreference != null) {
            muteAutoplayingVideosSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeMuteAutoplayingVideosEvent((Boolean) newValue));
                return true;
            });
        }

        if (rememberMutingOptionInPostFeedSwitchPreference != null) {
            rememberMutingOptionInPostFeedSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeRememberMutingOptionInPostFeedEvent((Boolean) newValue));
                return true;
            });
        }

        int orientation = getResources().getConfiguration().orientation;

        if (startAutoplayVisibleAreaOffsetPortrait != null) {
            startAutoplayVisibleAreaOffsetPortrait.setSummary(
                    getString(R.string.settings_start_autoplay_visible_area_offset_portrait_summary,
                            sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_PORTRAIT, 75)));
            startAutoplayVisibleAreaOffsetPortrait.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    EventBus.getDefault().post(new ChangeStartAutoplayVisibleAreaOffsetEvent((Integer) newValue));
                }
                startAutoplayVisibleAreaOffsetPortrait.setSummary(
                        getString(R.string.settings_start_autoplay_visible_area_offset_portrait_summary, (Integer) newValue));
                return true;
            });
        }

        if (startAutoplayVisibleAreaOffsetLandscape != null) {
            startAutoplayVisibleAreaOffsetLandscape.setSummary(
                    getString(R.string.settings_start_autoplay_visible_area_offset_portrait_summary,
                            sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_LANDSCAPE, 50)));
            startAutoplayVisibleAreaOffsetLandscape.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    EventBus.getDefault().post(new ChangeStartAutoplayVisibleAreaOffsetEvent((Integer) newValue));
                }
                startAutoplayVisibleAreaOffsetLandscape.setSummary(
                        getString(R.string.settings_start_autoplay_visible_area_offset_landscape_summary, (Integer) newValue));
                return true;
            });
        }
    }
}