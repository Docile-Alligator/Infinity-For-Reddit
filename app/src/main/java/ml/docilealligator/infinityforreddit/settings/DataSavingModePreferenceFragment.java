package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeDataSavingModeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDisableImagePreviewEvent;
import ml.docilealligator.infinityforreddit.events.ChangeOnlyDisablePreviewInVideoAndGifPostsEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class DataSavingModePreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.data_saving_mode_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        ListPreference dataSavingModeListPreference = findPreference(SharedPreferencesUtils.DATA_SAVING_MODE);
        SwitchPreference disableImagePreviewPreference = findPreference(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW);
        SwitchPreference onlyDisablePreviewInVideoAndGifPostsPreference = findPreference(SharedPreferencesUtils.ONLY_DISABLE_PREVIEW_IN_VIDEO_AND_GIF_POSTS);


        if (dataSavingModeListPreference != null) {
            if (dataSavingModeListPreference.getValue().equals("0")) {
                if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
                    onlyDisablePreviewInVideoAndGifPostsPreference.setVisible(false);
                }
                if (disableImagePreviewPreference != null) {
                    disableImagePreviewPreference.setVisible(false);
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
                } else {
                    if (onlyDisablePreviewInVideoAndGifPostsPreference != null) {
                        onlyDisablePreviewInVideoAndGifPostsPreference.setVisible(true);
                    }
                    if (disableImagePreviewPreference != null) {
                        disableImagePreviewPreference.setVisible(true);
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
