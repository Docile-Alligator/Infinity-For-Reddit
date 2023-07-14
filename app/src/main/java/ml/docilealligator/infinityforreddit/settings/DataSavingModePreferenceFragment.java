package ml.ino6962.postinfinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.ino6962.postinfinityforreddit.R;
import ml.ino6962.postinfinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.ino6962.postinfinityforreddit.events.ChangeDataSavingModeEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeDisableImagePreviewEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeOnlyDisablePreviewInVideoAndGifPostsEvent;
import ml.ino6962.postinfinityforreddit.utils.SharedPreferencesUtils;

public class DataSavingModePreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.data_saving_mode_preferences, rootKey);

        ListPreference dataSavingModeListPreference = findPreference(SharedPreferencesUtils.DATA_SAVING_MODE);
        SwitchPreference disableImagePreviewPreference = findPreference(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW);
        SwitchPreference onlyDisablePreviewInVideoAndGifPostsPreference = findPreference(SharedPreferencesUtils.ONLY_DISABLE_PREVIEW_IN_VIDEO_AND_GIF_POSTS);
        ListPreference redditVideoDefaultResolutionListPreference = findPreference(SharedPreferencesUtils.REDDIT_VIDEO_DEFAULT_RESOLUTION);

        if (dataSavingModeListPreference != null) {
            if (dataSavingModeListPreference.getValue().equals("0")) {
                if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
                    onlyDisablePreviewInVideoAndGifPostsPreference.setVisible(false);
                }
                if (disableImagePreviewPreference != null) {
                    disableImagePreviewPreference.setVisible(false);
                }
                if (redditVideoDefaultResolutionListPreference != null) {
                    redditVideoDefaultResolutionListPreference.setVisible(false);
                }
            }
            dataSavingModeListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDataSavingModeEvent((String) newValue));
                if (newValue.equals("0")) {
                    if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
                        onlyDisablePreviewInVideoAndGifPostsPreference.setVisible(false);
                    }
                    if (disableImagePreviewPreference != null) {
                        disableImagePreviewPreference.setVisible(false);
                    }
                    if (redditVideoDefaultResolutionListPreference != null) {
                        redditVideoDefaultResolutionListPreference.setVisible(false);
                    }
                } else {
                    if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
                        onlyDisablePreviewInVideoAndGifPostsPreference.setVisible(true);
                    }
                    if (disableImagePreviewPreference != null) {
                        disableImagePreviewPreference.setVisible(true);
                    }
                    if (redditVideoDefaultResolutionListPreference != null) {
                        redditVideoDefaultResolutionListPreference.setVisible(true);
                    }
                }
                return true;
            });
        }

        if (disableImagePreviewPreference != null) {
            disableImagePreviewPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDisableImagePreviewEvent((Boolean) newValue));
                if ((Boolean) newValue) {
                    EventBus.getDefault().post(new ChangeOnlyDisablePreviewInVideoAndGifPostsEvent(false));
                    if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
                        onlyDisablePreviewInVideoAndGifPostsPreference.setChecked(false);
                    }
                }
                return true;
            });
        }

        if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
            onlyDisablePreviewInVideoAndGifPostsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeOnlyDisablePreviewInVideoAndGifPostsEvent((Boolean) newValue));
                if ((Boolean) newValue) {
                    EventBus.getDefault().post(new ChangeDisableImagePreviewEvent(false));
                    if (disableImagePreviewPreference != null) {
                        disableImagePreviewPreference.setChecked(false);
                    }
                }
                return true;
            });
        }
    }
}
