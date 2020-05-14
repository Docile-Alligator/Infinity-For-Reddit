package ml.docilealligator.infinityforreddit.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Activity.SettingsActivity;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomizeMainPageTabsFragment extends PreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    private SettingsActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.customize_main_page_tabs_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        EditTextPreference tab1TitlePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE);
        EditTextPreference tab2TitlePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE);
        EditTextPreference tab3TitlePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE);
        ListPreference tab1PostTypePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE);
        ListPreference tab2PostTypePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE);
        ListPreference tab3PostTypePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE);
        EditTextPreference tab1NamePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME);
        EditTextPreference tab2NamePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME);
        EditTextPreference tab3NamePreference = findPreference(SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME);

        if (tab1TitlePreference != null && tab1PostTypePreference != null && tab1NamePreference != null) {
            String postType = mSharedPreferences.getString(SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE,
                    SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
            if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT.equals(postType)) {
                tab1NamePreference.setVisible(true);
                tab1NamePreference.setTitle(R.string.settings_tab_subreddit_name);
            } else if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT.equals(postType)) {
                tab1NamePreference.setVisible(true);
                tab1NamePreference.setTitle(R.string.settings_tab_multi_reddit_name);
            } else if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER.equals(postType)) {
                tab1NamePreference.setVisible(true);
                tab1NamePreference.setTitle(R.string.settings_tab_username);
            }

            tab1PostTypePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                switch ((String) newValue) {
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME:
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR:
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL:
                        tab1NamePreference.setVisible(false);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                        tab1NamePreference.setVisible(true);
                        tab1NamePreference.setTitle(R.string.settings_tab_subreddit_name);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                        tab1NamePreference.setVisible(true);
                        tab1NamePreference.setTitle(R.string.settings_tab_multi_reddit_name);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                        tab1NamePreference.setVisible(true);
                        tab1NamePreference.setTitle(R.string.settings_tab_username);
                }
                return true;
            });
        }

        if (tab2TitlePreference != null && tab2PostTypePreference != null && tab2NamePreference != null) {
            String postType = mSharedPreferences.getString(SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE,
                    SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
            if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT.equals(postType)) {
                tab2NamePreference.setVisible(true);
                tab2NamePreference.setTitle(R.string.settings_tab_subreddit_name);
            } else if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT.equals(postType)) {
                tab2NamePreference.setVisible(true);
                tab2NamePreference.setTitle(R.string.settings_tab_multi_reddit_name);
            } else if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER.equals(postType)) {
                tab2NamePreference.setVisible(true);
                tab2NamePreference.setTitle(R.string.settings_tab_username);
            }

            tab2PostTypePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                switch ((String) newValue) {
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME:
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR:
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL:
                        tab2NamePreference.setVisible(false);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                        tab2NamePreference.setVisible(true);
                        tab2NamePreference.setTitle(R.string.settings_tab_subreddit_name);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                        tab2NamePreference.setVisible(true);
                        tab2NamePreference.setTitle(R.string.settings_tab_multi_reddit_name);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                        tab2NamePreference.setVisible(true);
                        tab2NamePreference.setTitle(R.string.settings_tab_username);
                }
                return true;
            });
        }

        if (tab3TitlePreference != null && tab3PostTypePreference != null && tab3NamePreference != null) {
            String postType = mSharedPreferences.getString(SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE,
                    SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
            if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT.equals(postType)) {
                tab3NamePreference.setVisible(true);
                tab3NamePreference.setTitle(R.string.settings_tab_subreddit_name);
            } else if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT.equals(postType)) {
                tab3NamePreference.setVisible(true);
                tab3NamePreference.setTitle(R.string.settings_tab_multi_reddit_name);
            } else if (SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER.equals(postType)) {
                tab3NamePreference.setVisible(true);
                tab3NamePreference.setTitle(R.string.settings_tab_username);
            }

            tab3PostTypePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                switch ((String) newValue) {
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME:
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR:
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL:
                        tab3NamePreference.setVisible(false);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                        tab3NamePreference.setVisible(true);
                        tab3NamePreference.setTitle(R.string.settings_tab_subreddit_name);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                        tab3NamePreference.setVisible(true);
                        tab3NamePreference.setTitle(R.string.settings_tab_multi_reddit_name);
                        break;
                    case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                        tab3NamePreference.setVisible(true);
                        tab3NamePreference.setTitle(R.string.settings_tab_username);
                }
                return true;
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}
