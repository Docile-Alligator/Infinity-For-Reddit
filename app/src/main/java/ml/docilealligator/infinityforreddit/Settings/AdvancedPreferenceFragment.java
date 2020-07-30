package ml.docilealligator.infinityforreddit.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.AsyncTask.DeleteAllPostLayoutsAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.DeleteAllSortTypesAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.DeleteAllSubredditsAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.DeleteAllThemesAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.DeleteAllUsersAsyncTask;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedPreferenceFragment extends PreferenceFragmentCompat {

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("light_theme")
    SharedPreferences lightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences darkThemeSharedPreferences;
    @Inject
    @Named("post_feed_scrolled_position_cache")
    SharedPreferences postFeedScrolledPositionSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences amoledThemeSharedPreferences;
    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.advanced_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        Preference deleteSubredditsPreference = findPreference(SharedPreferencesUtils.DELETE_ALL_SUBREDDITS_DATA_IN_DATABASE);
        Preference deleteUsersPreference = findPreference(SharedPreferencesUtils.DELETE_ALL_USERS_DATA_IN_DATABASE);
        Preference deleteSortTypePreference = findPreference(SharedPreferencesUtils.DELETE_ALL_SORT_TYPE_DATA_IN_DATABASE);
        Preference deletePostLaoutPreference = findPreference(SharedPreferencesUtils.DELETE_ALL_POST_LAYOUT_DATA_IN_DATABASE);
        Preference deleteAllThemesPreference = findPreference(SharedPreferencesUtils.DELETE_ALL_THEMES_IN_DATABASE);
        Preference deletePostFeedScrolledPositionsPreference = findPreference(SharedPreferencesUtils.DELETE_POST_FEED_SCROLLED_POSITIONS_IN_DATABASE);
        Preference resetAllSettingsPreference = findPreference(SharedPreferencesUtils.RESET_ALL_SETTINGS);

        if (deleteSubredditsPreference != null) {
            deleteSubredditsPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> new DeleteAllSubredditsAsyncTask(mRedditDataRoomDatabase,
                                        () -> Toast.makeText(activity, R.string.delete_all_subreddits_success, Toast.LENGTH_SHORT).show()).execute())
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }

        if (deleteUsersPreference != null) {
            deleteUsersPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> new DeleteAllUsersAsyncTask(mRedditDataRoomDatabase,
                                        () -> Toast.makeText(activity, R.string.delete_all_users_success, Toast.LENGTH_SHORT).show()).execute())
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }

        if (deleteSortTypePreference != null) {
            deleteSortTypePreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> new DeleteAllSortTypesAsyncTask(mSharedPreferences, mSortTypeSharedPreferences, () -> {
                                    Toast.makeText(activity, R.string.delete_all_sort_types_success, Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().post(new RecreateActivityEvent());
                                }).execute())
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }

        if (deletePostLaoutPreference != null) {
            deletePostLaoutPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> new DeleteAllPostLayoutsAsyncTask(mSharedPreferences, mPostLayoutSharedPreferences, () -> {
                                    Toast.makeText(activity, R.string.delete_all_post_layouts_success, Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().post(new RecreateActivityEvent());
                                }).execute())
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }

        if (deleteAllThemesPreference != null) {
            deleteAllThemesPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> new DeleteAllThemesAsyncTask(mRedditDataRoomDatabase, lightThemeSharedPreferences,
                                        darkThemeSharedPreferences, amoledThemeSharedPreferences, () -> {
                                    Toast.makeText(activity, R.string.delete_all_themes_success, Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().post(new RecreateActivityEvent());
                                }).execute())
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }

        if (deletePostFeedScrolledPositionsPreference != null) {
            deletePostFeedScrolledPositionsPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> {
                            postFeedScrolledPositionSharedPreferences.edit().clear().apply();
                            Toast.makeText(activity, R.string.delete_all_post_feed_scrolled_positions_success, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }

        if (resetAllSettingsPreference != null) {
            resetAllSettingsPreference.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.are_you_sure)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> {
                            mSharedPreferences.edit().clear().apply();
                            Toast.makeText(activity, R.string.reset_all_settings_success, Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new RecreateActivityEvent());
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
