package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.events.ChangeCompactLayoutToolbarHiddenByDefaultEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDefaultPostLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHidePostFlairEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHidePostTypeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideSubredditAndUserPrefixEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideTheNumberOfAwardsEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideTheNumberOfCommentsEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideTheNumberOfVotesEvent;
import ml.docilealligator.infinityforreddit.events.ChangeLongPressToHideToolbarInCompactLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangeShowAbsoluteNumberOfVotesEvent;
import ml.docilealligator.infinityforreddit.events.ShowDividerInCompactLayoutPreferenceEvent;
import ml.docilealligator.infinityforreddit.events.ShowThumbnailOnTheRightInCompactLayoutEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class PostPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.post_preferences, rootKey);

        ListPreference defaultPostLayoutList = findPreference(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY);
        SwitchPreference showDividerInCompactLayoutSwitch = findPreference(SharedPreferencesUtils.SHOW_DIVIDER_IN_COMPACT_LAYOUT);
        SwitchPreference showThumbnailOnTheRightInCompactLayoutSwitch = findPreference(SharedPreferencesUtils.SHOW_THUMBNAIL_ON_THE_LEFT_IN_COMPACT_LAYOUT);
        SwitchPreference showAbsoluteNumberOfVotesSwitch = findPreference(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES);
        SwitchPreference longPressToHideToolbarInCompactLayoutSwitch = findPreference(SharedPreferencesUtils.LONG_PRESS_TO_HIDE_TOOLBAR_IN_COMPACT_LAYOUT);
        SwitchPreference postCompactLayoutToolbarHiddenByDefaultSwitch = findPreference(SharedPreferencesUtils.POST_COMPACT_LAYOUT_TOOLBAR_HIDDEN_BY_DEFAULT);
        SwitchPreference hidePostTypeSwitch = findPreference(SharedPreferencesUtils.HIDE_POST_TYPE);
        SwitchPreference hidePostFlairSwitch = findPreference(SharedPreferencesUtils.HIDE_POST_FLAIR);
        SwitchPreference hideTheNumberOfAwardsSwitch = findPreference(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_AWARDS);
        SwitchPreference hideSubredditAndUserPrefixSwitch = findPreference(SharedPreferencesUtils.HIDE_SUBREDDIT_AND_USER_PREFIX);
        SwitchPreference hideTheNumberOfVotesSwitch = findPreference(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES);
        SwitchPreference hideTheNumberOfCommentsSwitch = findPreference(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_COMMENTS);

        if (defaultPostLayoutList != null) {
            defaultPostLayoutList.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDefaultPostLayoutEvent(Integer.parseInt((String) newValue)));
                return true;
            });
        }

        if (showDividerInCompactLayoutSwitch != null) {
            showDividerInCompactLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ShowDividerInCompactLayoutPreferenceEvent((Boolean) newValue));
                return true;
            });
        }

        if (showThumbnailOnTheRightInCompactLayoutSwitch != null) {
            showThumbnailOnTheRightInCompactLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ShowThumbnailOnTheRightInCompactLayoutEvent((Boolean) newValue));
                return true;
            });
        }

        if (showAbsoluteNumberOfVotesSwitch != null) {
            showAbsoluteNumberOfVotesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeShowAbsoluteNumberOfVotesEvent((Boolean) newValue));
                return true;
            });
        }

        if (longPressToHideToolbarInCompactLayoutSwitch != null) {
            longPressToHideToolbarInCompactLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeLongPressToHideToolbarInCompactLayoutEvent((Boolean) newValue));
                return true;
            });
        }

        if (postCompactLayoutToolbarHiddenByDefaultSwitch != null) {
            postCompactLayoutToolbarHiddenByDefaultSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeCompactLayoutToolbarHiddenByDefaultEvent((Boolean) newValue));
                return true;
            });
        }

        if (hidePostTypeSwitch != null) {
            hidePostTypeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHidePostTypeEvent((Boolean) newValue));
                return true;
            });
        }

        if (hidePostFlairSwitch != null) {
            hidePostFlairSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHidePostFlairEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideTheNumberOfAwardsSwitch != null) {
            hideTheNumberOfAwardsSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideTheNumberOfAwardsEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideSubredditAndUserPrefixSwitch != null) {
            hideSubredditAndUserPrefixSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideSubredditAndUserPrefixEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideTheNumberOfVotesSwitch != null) {
            hideTheNumberOfVotesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideTheNumberOfVotesEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideTheNumberOfCommentsSwitch != null) {
            hideTheNumberOfCommentsSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    EventBus.getDefault().post(new ChangeHideTheNumberOfCommentsEvent((Boolean) newValue));
                    return true;
                }
            });
        }
    }
}