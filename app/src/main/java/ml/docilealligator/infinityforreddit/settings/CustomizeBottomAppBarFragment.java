package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentCustomizeBottomAppBarBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeBottomAppBarFragment extends Fragment {

    private FragmentCustomizeBottomAppBarBinding binding;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences sharedPreferences;
    private SettingsActivity activity;
    private int mainActivityOptionCount;
    private int mainActivityOption1;
    private int mainActivityOption2;
    private int mainActivityOption3;
    private int mainActivityOption4;
    private int mainActivityFAB;
    private int otherActivitiesOptionCount;
    private int otherActivitiesOption1;
    private int otherActivitiesOption2;
    private int otherActivitiesOption3;
    private int otherActivitiesOption4;
    private int otherActivitiesFAB;

    public CustomizeBottomAppBarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCustomizeBottomAppBarBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        binding.getRoot().setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());

        applyCustomTheme();

        if (activity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    binding.getRoot().setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        Resources resources = activity.getResources();
        String[] mainActivityOptions = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options);
        String[] mainActivityOptionAnonymous = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options_anonymous);
        String[] mainActivityOptionAnonymousValues = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options_anonymous_values);
        String[] fabOptions;
        mainActivityOptionCount = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
        mainActivityOption1 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, 0);
        mainActivityOption2 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, 1);
        mainActivityOption3 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, 2);
        mainActivityOption4 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, 3);
        mainActivityFAB = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? 7: 0);

        mainActivityOption1 = Utils.fixIndexOutOfBounds(mainActivityOptions, mainActivityOption1);
        mainActivityOption2 = Utils.fixIndexOutOfBounds(mainActivityOptions, mainActivityOption2);
        mainActivityOption3 = Utils.fixIndexOutOfBounds(mainActivityOptions, mainActivityOption3);
        mainActivityOption4 = Utils.fixIndexOutOfBounds(mainActivityOptions, mainActivityOption4);

        binding.mainActivityOptionCountTextViewCustomizeBottomAppBarFragment.setText(Integer.toString(mainActivityOptionCount));
        binding.mainActivityOption1TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[mainActivityOption1]);
        binding.mainActivityOption2TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[mainActivityOption2]);
        binding.mainActivityOption3TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[mainActivityOption3]);
        binding.mainActivityOption4TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[mainActivityOption4]);

        if (activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            fabOptions = resources.getStringArray(R.array.settings_bottom_app_bar_fab_options_anonymous);
            ArrayList<String> mainActivityOptionAnonymousValuesList = new ArrayList<>(Arrays.asList(mainActivityOptionAnonymousValues));
            mainActivityOption1 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption1));
            mainActivityOption2 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption2));
            mainActivityOption3 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption3));
            mainActivityOption4 = mainActivityOptionAnonymousValuesList.indexOf(Integer.toString(mainActivityOption4));

            mainActivityFAB = mainActivityFAB >= 9 ? mainActivityFAB - 2 : mainActivityFAB - 1;
        } else {
            fabOptions = resources.getStringArray(R.array.settings_bottom_app_bar_fab_options);
        }

        binding.mainActivityFabTextViewCustomizeBottomAppBarFragment.setText(fabOptions[mainActivityFAB]);

        binding.mainActivityOptionCountLinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_bottom_app_bar_option_count_options, mainActivityOptionCount / 2 - 1, (dialogInterface, i) -> {
                        mainActivityOptionCount = (i + 1) * 2;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, mainActivityOptionCount).apply();
                        binding.mainActivityOptionCountTextViewCustomizeBottomAppBarFragment.setText(Integer.toString(mainActivityOptionCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption1LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_1)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption1, (dialogInterface, i) -> {
                        mainActivityOption1 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption1;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, optionToSaveToPreference).apply();
                        binding.mainActivityOption1TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption2LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_2)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption2, (dialogInterface, i) -> {
                        mainActivityOption2 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption2;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, optionToSaveToPreference).apply();
                        binding.mainActivityOption2TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption3LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_3)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption3, (dialogInterface, i) -> {
                        mainActivityOption3 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption3;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, optionToSaveToPreference).apply();
                        binding.mainActivityOption3TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityOption4LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_4)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mainActivityOptionAnonymous : mainActivityOptions, mainActivityOption4, (dialogInterface, i) -> {
                        mainActivityOption4 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(mainActivityOptionAnonymousValues[i]) : mainActivityOption4;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, optionToSaveToPreference).apply();
                        binding.mainActivityOption4TextViewCustomizeBottomAppBarFragment.setText(mainActivityOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.mainActivityFabLinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_fab)
                    .setSingleChoiceItems(fabOptions, mainActivityFAB, (dialogInterface, i) -> {
                        mainActivityFAB = i;
                        int optionToSaveToPreference;
                        if (activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                            if (i >= 7) {
                                optionToSaveToPreference = i + 2;
                            } else {
                                optionToSaveToPreference = i + 1;
                            }
                        } else {
                            optionToSaveToPreference = i;
                        }
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, optionToSaveToPreference).apply();
                        binding.mainActivityFabTextViewCustomizeBottomAppBarFragment.setText(fabOptions[mainActivityFAB]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        String[] otherActivitiesOptions = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options);
        String[] otherActivitiesOptionAnonymous = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options_anonymous);
        String[] otherActivitiesOptionAnonymousValues = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options_anonymous_values);
        otherActivitiesOptionCount = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, 4);
        otherActivitiesOption1 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, 0);
        otherActivitiesOption2 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, 1);
        otherActivitiesOption3 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, 2);
        otherActivitiesOption4 = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, 3);
        otherActivitiesFAB = sharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? 7: 0);

        otherActivitiesOption1 = Utils.fixIndexOutOfBounds(otherActivitiesOptions, otherActivitiesOption1);
        otherActivitiesOption2 = Utils.fixIndexOutOfBounds(otherActivitiesOptions, otherActivitiesOption2);
        otherActivitiesOption3 = Utils.fixIndexOutOfBounds(otherActivitiesOptions, otherActivitiesOption3);
        otherActivitiesOption4 = Utils.fixIndexOutOfBounds(otherActivitiesOptions, otherActivitiesOption4);

        binding.otherActivitiesOptionCountTextViewCustomizeBottomAppBarFragment.setText(Integer.toString(otherActivitiesOptionCount));
        binding.otherActivitiesOption1TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[otherActivitiesOption1]);
        binding.otherActivitiesOption2TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[otherActivitiesOption2]);
        binding.otherActivitiesOption3TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[otherActivitiesOption3]);
        binding.otherActivitiesOption4TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[otherActivitiesOption4]);

        if (activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            ArrayList<String> otherActivitiesOptionAnonymousValuesList = new ArrayList<>(Arrays.asList(otherActivitiesOptionAnonymousValues));
            otherActivitiesOption1 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption1));
            otherActivitiesOption2 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption2));
            otherActivitiesOption3 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption3));
            otherActivitiesOption4 = otherActivitiesOptionAnonymousValuesList.indexOf(Integer.toString(otherActivitiesOption4));
            otherActivitiesFAB = otherActivitiesFAB >= 9 ? otherActivitiesFAB - 2 : otherActivitiesFAB - 1;
        }

        binding.otherActivitiesFabTextViewCustomizeBottomAppBarFragment.setText(fabOptions[otherActivitiesFAB]);

        binding.otherActivitiesOptionCountLinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_bottom_app_bar_option_count_options, otherActivitiesOptionCount / 2 - 1, (dialogInterface, i) -> {
                        otherActivitiesOptionCount = (i + 1) * 2;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, otherActivitiesOptionCount).apply();
                        binding.otherActivitiesOptionCountTextViewCustomizeBottomAppBarFragment.setText(Integer.toString(otherActivitiesOptionCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption1LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_1)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption1, (dialogInterface, i) -> {
                        otherActivitiesOption1 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption1;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption1TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption2LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_2)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption2, (dialogInterface, i) -> {
                        otherActivitiesOption2 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption2;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption2TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption3LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_3)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption3, (dialogInterface, i) -> {
                        otherActivitiesOption3 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption3;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption3TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesOption4LinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_4)
                    .setSingleChoiceItems(activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? otherActivitiesOptionAnonymous : otherActivitiesOptions, otherActivitiesOption4, (dialogInterface, i) -> {
                        otherActivitiesOption4 = i;
                        int optionToSaveToPreference = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Integer.parseInt(otherActivitiesOptionAnonymousValues[i]) : otherActivitiesOption4;
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, optionToSaveToPreference).apply();
                        binding.otherActivitiesOption4TextViewCustomizeBottomAppBarFragment.setText(otherActivitiesOptions[optionToSaveToPreference]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        binding.otherActivitiesFabLinearLayoutCustomizeBottomAppBarFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_fab)
                    .setSingleChoiceItems(fabOptions, otherActivitiesFAB, (dialogInterface, i) -> {
                        otherActivitiesFAB = i;
                        int optionToSaveToPreference;
                        if (activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                            if (i >= 7) {
                                optionToSaveToPreference = i + 2;
                            } else {
                                optionToSaveToPreference = i + 1;
                            }
                        } else {
                            optionToSaveToPreference = i;
                        }
                        sharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, optionToSaveToPreference).apply();
                        binding.otherActivitiesFabTextViewCustomizeBottomAppBarFragment.setText(fabOptions[otherActivitiesFAB]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        return binding.getRoot();
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = activity.customThemeWrapper.getSecondaryTextColor();
        int accentColor = activity.customThemeWrapper.getColorAccent();
        binding.infoTextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        Drawable infoDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_day_night_24dp, activity.customThemeWrapper.getPrimaryIconColor());
        binding.infoTextViewCustomizeBottomAppBarFragment.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        binding.mainActivityGroupSummaryCustomizeBottomAppBarFragment.setTextColor(accentColor);
        binding.mainActivityOptionCountTitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.mainActivityOptionCountTextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.mainActivityOption1TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.mainActivityOption1TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.mainActivityOption2TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.mainActivityOption2TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.mainActivityOption3TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.mainActivityOption3TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.mainActivityOption4TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.mainActivityOption4TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.mainActivityFabTitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.mainActivityFabTextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);

        binding.otherActivitiesGroupSummaryCustomizeBottomAppBarFragment.setTextColor(accentColor);
        binding.otherActivitiesOptionCountTitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.otherActivitiesOptionCountTextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption1TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.otherActivitiesOption1TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption2TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.otherActivitiesOption2TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption3TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.otherActivitiesOption3TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.otherActivitiesOption4TitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.otherActivitiesOption4TextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
        binding.otherActivitiesFabTitleTextViewCustomizeBottomAppBarFragment.setTextColor(primaryTextColor);
        binding.otherActivitiesFabTextViewCustomizeBottomAppBarFragment.setTextColor(secondaryTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}