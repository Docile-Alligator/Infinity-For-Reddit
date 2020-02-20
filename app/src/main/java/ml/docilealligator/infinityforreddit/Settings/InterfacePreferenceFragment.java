package ml.docilealligator.infinityforreddit.Settings;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.Event.ChangeDefaultPostLayoutEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeShowElapsedTimeEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeVoteButtonsPositionEvent;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Event.ShowDividerInCompactLayoutPreferenceEvent;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

/**
 * A simple {@link Fragment} subclass.
 */
public class InterfacePreferenceFragment extends PreferenceFragmentCompat {

    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.interface_preference, rootKey);

        ListPreference themePreference = findPreference(SharedPreferencesUtils.THEME_KEY);
        SwitchPreference amoledDarkSwitch = findPreference(SharedPreferencesUtils.AMOLED_DARK_KEY);
        SwitchPreference immersiveInterfaceSwitch = findPreference(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY);
        SwitchPreference bottomAppBarSwitch = findPreference(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY);
        SwitchPreference voteButtonsOnTheRightSwitch = findPreference(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY);
        SwitchPreference showElapsedTimeSwitch = findPreference(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY);
        ListPreference defaultPostLayoutSwitch = findPreference(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY);
        SwitchPreference showDividerInCompactLayout = findPreference(SharedPreferencesUtils.SHOW_DIVIDER_IN_COMPACT_LAYOUT);

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        if (themePreference != null) {
            if (systemDefault) {
                themePreference.setEntries(R.array.settings_theme_q);
            } else {
                themePreference.setEntries(R.array.settings_theme);
            }

            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int option = Integer.parseInt((String) newValue);
                switch (option) {
                    case 0:
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                        break;
                    case 1:
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                        break;
                    case 2:
                        if (systemDefault) {
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                        }
                }
                return true;
            });
        }

        if (amoledDarkSwitch != null) {
            amoledDarkSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_NO) {
                    EventBus.getDefault().post(new RecreateActivityEvent());
                    activity.recreate();
                }
                return true;
            });
        }

        if (immersiveInterfaceSwitch != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                immersiveInterfaceSwitch.setVisible(true);
                immersiveInterfaceSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new RecreateActivityEvent());
                    return true;
                });
            } else {
                immersiveInterfaceSwitch.setVisible(false);
            }
        }

        if (bottomAppBarSwitch != null) {
            bottomAppBarSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (voteButtonsOnTheRightSwitch != null) {
            voteButtonsOnTheRightSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeVoteButtonsPositionEvent((Boolean) newValue));
                return true;
            });
        }

        if (showElapsedTimeSwitch != null) {
            showElapsedTimeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeShowElapsedTimeEvent((Boolean) newValue));
                return true;
            });
        }

        if (defaultPostLayoutSwitch != null) {
            defaultPostLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDefaultPostLayoutEvent(Integer.parseInt((String) newValue)));
                return true;
            });
        }

        if (showDividerInCompactLayout != null) {
            showDividerInCompactLayout.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ShowDividerInCompactLayoutPreferenceEvent((Boolean) newValue));
                return true;
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
