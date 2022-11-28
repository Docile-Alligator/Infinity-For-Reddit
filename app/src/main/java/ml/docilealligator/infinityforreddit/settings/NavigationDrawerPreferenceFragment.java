package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeHideKarmaEvent;
import ml.docilealligator.infinityforreddit.events.ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class NavigationDrawerPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.NAVIGATION_DRAWER_SHARED_PREFERENCES_FILE);
        setPreferencesFromResource(R.xml.navigation_drawer_preferences, rootKey);

        SwitchPreference showAvatarOnTheRightSwitch = findPreference(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT);
        SwitchPreference hideKarmaSwitch = findPreference(SharedPreferencesUtils.HIDE_ACCOUNT_KARMA_NAV_BAR);

        if (showAvatarOnTheRightSwitch != null) {
            showAvatarOnTheRightSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideKarmaSwitch != null) {
            hideKarmaSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideKarmaEvent((Boolean) newValue));
                return true;
            });
        }
    }
}