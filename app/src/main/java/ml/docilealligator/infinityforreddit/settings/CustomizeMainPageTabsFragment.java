package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.SearchActivity;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentCustomizeMainPageTabsBinding;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeMainPageTabsFragment extends Fragment {

    private FragmentCustomizeMainPageTabsBinding binding;
    @Inject
    @Named("main_activity_tabs")
    SharedPreferences mainActivityTabsSharedPreferences;
    private SettingsActivity activity;
    private int tabCount;
    private String tab1CurrentTitle;
    private int tab1CurrentPostType;
    private String tab1CurrentName;
    private String tab2CurrentTitle;
    private int tab2CurrentPostType;
    private String tab2CurrentName;
    private String tab3CurrentTitle;
    private int tab3CurrentPostType;
    private String tab3CurrentName;

    public CustomizeMainPageTabsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCustomizeMainPageTabsBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        binding.getRoot().setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());
        applyCustomTheme();

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        String[] typeValues;
        if (activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type_anonymous);
        } else {
            typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type);
        }

        tabCount = mainActivityTabsSharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        binding.tabCountTextViewCustomizeMainPageTabsFragment.setText(Integer.toString(tabCount));
        binding.tabCountLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_main_page_tab_count, tabCount - 1, (dialogInterface, i) -> {
                        tabCount = i + 1;
                        mainActivityTabsSharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, tabCount).apply();
                        binding.tabCountTextViewCustomizeMainPageTabsFragment.setText(Integer.toString(tabCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        boolean showTabNames = mainActivityTabsSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true);
        binding.showTabNamesSwitchMaterialCustomizeMainPageTabsFragment.setChecked(showTabNames);
        binding.showTabNamesSwitchMaterialCustomizeMainPageTabsFragment.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, b).apply());
        binding.showTabNamesLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> binding.showTabNamesSwitchMaterialCustomizeMainPageTabsFragment.performClick());

        tab1CurrentTitle = mainActivityTabsSharedPreferences.getString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home));
        tab1CurrentPostType = mainActivityTabsSharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
        if (!activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            tab1CurrentPostType = Utils.fixIndexOutOfBoundsUsingPredetermined(typeValues, tab1CurrentPostType, 1);
        }
        tab1CurrentName = mainActivityTabsSharedPreferences.getString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
        binding.tab1TypeSummaryTextViewCustomizeMainPageTabsFragment.setText(typeValues[tab1CurrentPostType]);
        binding.tab1TitleSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentTitle);
        binding.tab1NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentName);
        applyTab1NameView(binding.tab1NameConstraintLayoutCustomizeMainPageTabsFragment, binding.tab1NameTitleTextViewCustomizeMainPageTabsFragment, tab1CurrentPostType);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.edit_text_edit_text_dialog);

        binding.tab1TitleLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab1CurrentTitle);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab1CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, tab1CurrentTitle).apply();
                        binding.tab1TitleSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentTitle);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab1TypeLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab1CurrentPostType, (dialogInterface, i) -> {
                        tab1CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, i).apply();
                        binding.tab1TypeSummaryTextViewCustomizeMainPageTabsFragment.setText(typeValues[i]);
                        applyTab1NameView(binding.tab1NameConstraintLayoutCustomizeMainPageTabsFragment, binding.tab1NameTitleTextViewCustomizeMainPageTabsFragment, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.tab1NameConstraintLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            int titleId;
            switch (tab1CurrentPostType) {
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                    titleId = R.string.settings_tab_subreddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                    titleId = R.string.settings_tab_multi_reddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                    titleId = R.string.settings_tab_username;
                    break;
                default:
                    return;
            }
            editText.setText(tab1CurrentName);
            editText.setHint(titleId);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab1CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                        binding.tab1NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentName);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab1NameAddImageViewCustomizeMainPageTabsFragment.setOnClickListener(view -> selectName(0));

        tab2CurrentTitle = mainActivityTabsSharedPreferences.getString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular));
        tab2CurrentPostType = mainActivityTabsSharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
        if (!activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            tab2CurrentPostType = Utils.fixIndexOutOfBoundsUsingPredetermined(typeValues, tab2CurrentPostType, 1);
        }
        tab2CurrentName = mainActivityTabsSharedPreferences.getString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
        binding.tab2TypeSummaryTextViewCustomizeMainPageTabsFragment.setText(typeValues[tab2CurrentPostType]);
        binding.tab2TitleSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentTitle);
        binding.tab2NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentName);
        applyTab2NameView(binding.tab2NameConstraintLayoutCustomizeMainPageTabsFragment, binding.tab2NameTitleTextViewCustomizeMainPageTabsFragment, tab2CurrentPostType);

        binding.tab2TitleLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab2CurrentTitle);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab2CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, tab2CurrentTitle).apply();
                        binding.tab2TitleSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentTitle);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab2TypeLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab2CurrentPostType, (dialogInterface, i) -> {
                        tab2CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, i).apply();
                        binding.tab2TypeSummaryTextViewCustomizeMainPageTabsFragment.setText(typeValues[i]);
                        applyTab2NameView(binding.tab2NameConstraintLayoutCustomizeMainPageTabsFragment, binding.tab2NameTitleTextViewCustomizeMainPageTabsFragment, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.tab2NameConstraintLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            int titleId;
            switch (tab2CurrentPostType) {
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                    titleId = R.string.settings_tab_subreddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                    titleId = R.string.settings_tab_multi_reddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                    titleId = R.string.settings_tab_username;
                    break;
                default:
                    return;
            }
            editText.setText(tab2CurrentName);
            editText.setHint(titleId);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab2CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                        binding.tab2NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentName);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab2NameAddImageViewCustomizeMainPageTabsFragment.setOnClickListener(view -> selectName(1));

        tab3CurrentTitle = mainActivityTabsSharedPreferences.getString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all));
        tab3CurrentPostType = mainActivityTabsSharedPreferences.getInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
        if (!activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            tab3CurrentPostType = Utils.fixIndexOutOfBoundsUsingPredetermined(typeValues, tab3CurrentPostType, 1);
        }
        tab3CurrentName = mainActivityTabsSharedPreferences.getString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
        binding.tab3TypeSummaryTextViewCustomizeMainPageTabsFragment.setText(typeValues[tab3CurrentPostType]);
        binding.tab3TitleSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentTitle);
        binding.tab3NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentName);
        applyTab3NameView(binding.tab3NameConstraintLayoutCustomizeMainPageTabsFragment, binding.tab3NameTitleTextViewCustomizeMainPageTabsFragment, tab3CurrentPostType);

        binding.tab3TitleLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab3CurrentTitle);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab3CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, tab3CurrentTitle).apply();
                        binding.tab3TitleSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentTitle);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab3TypeLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab3CurrentPostType, (dialogInterface, i) -> {
                        tab3CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, i).apply();
                        binding.tab3TypeSummaryTextViewCustomizeMainPageTabsFragment.setText(typeValues[i]);
                        applyTab3NameView(binding.tab3NameConstraintLayoutCustomizeMainPageTabsFragment, binding.tab3NameTitleTextViewCustomizeMainPageTabsFragment, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.tab3NameConstraintLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            int titleId;
            switch (tab3CurrentPostType) {
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                    titleId = R.string.settings_tab_subreddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                    titleId = R.string.settings_tab_multi_reddit_name;
                    break;
                case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                    titleId = R.string.settings_tab_username;
                    break;
                default:
                    return;
            }
            editText.setText(tab3CurrentName);
            editText.setHint(titleId);
            editText.requestFocus();
            Utils.showKeyboard(activity, new Handler(), editText);
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab3CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                        binding.tab3NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentName);
                        Utils.hideKeyboard(activity);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        Utils.hideKeyboard(activity);
                    })
                    .show();
        });

        binding.tab3NameAddImageViewCustomizeMainPageTabsFragment.setOnClickListener(view -> selectName(2));

        binding.showMultiredditsSwitchMaterialCustomizeMainPageTabsFragment.setChecked(mainActivityTabsSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, false));
        binding.showMultiredditsSwitchMaterialCustomizeMainPageTabsFragment.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, b).apply());
        binding.showMultiredditsLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            binding.showMultiredditsSwitchMaterialCustomizeMainPageTabsFragment.performClick();
        });

        binding.showFavoriteMultiredditsSwitchMaterialCustomizeMainPageTabsFragment.setChecked(mainActivityTabsSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, false));
        binding.showFavoriteMultiredditsSwitchMaterialCustomizeMainPageTabsFragment.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, b).apply());
        binding.showFavoriteMultiredditsLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            binding.showFavoriteMultiredditsSwitchMaterialCustomizeMainPageTabsFragment.performClick();
        });

        binding.showSubscribedSubredditsSwitchMaterialCustomizeMainPageTabsFragment.setChecked(mainActivityTabsSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false));
        binding.showSubscribedSubredditsSwitchMaterialCustomizeMainPageTabsFragment.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, b).apply());
        binding.showSubscribedSubredditsLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            binding.showSubscribedSubredditsSwitchMaterialCustomizeMainPageTabsFragment.performClick();
        });

        binding.showFavoriteSubscribedSubredditsSwitchMaterialCustomizeMainPageTabsFragment.setChecked(mainActivityTabsSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false));
        binding.showFavoriteSubscribedSubredditsSwitchMaterialCustomizeMainPageTabsFragment.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, b).apply());
        binding.showFavoriteSubscribedSubredditsLinearLayoutCustomizeMainPageTabsFragment.setOnClickListener(view -> {
            binding.showFavoriteSubscribedSubredditsSwitchMaterialCustomizeMainPageTabsFragment.performClick();
        });

        return binding.getRoot();
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = activity.customThemeWrapper.getSecondaryTextColor();
        int colorAccent = activity.customThemeWrapper.getColorAccent();
        int primaryIconColor = activity.customThemeWrapper.getPrimaryIconColor();
        binding.infoTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        Drawable infoDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_day_night_24dp, secondaryTextColor);
        binding.infoTextViewCustomizeMainPageTabsFragment.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        binding.tabCountTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tabCountTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.showTabNamesTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab1GroupSummaryCustomizeMainPageTabsFragment.setTextColor(colorAccent);
        binding.tab1TitleTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab1TitleSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab1TypeTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab1TypeSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab1NameTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab1NameSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab1NameAddImageViewCustomizeMainPageTabsFragment.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tab2GroupSummaryCustomizeMainPageTabsFragment.setTextColor(colorAccent);
        binding.tab2TitleTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab2TitleSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab2TypeTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab2TypeSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab2NameTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab2NameSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab2NameAddImageViewCustomizeMainPageTabsFragment.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tab3GroupSummaryCustomizeMainPageTabsFragment.setTextColor(colorAccent);
        binding.tab3TitleTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab3TitleSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab3TypeTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab3TypeSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab3NameTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.tab3NameSummaryTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.tab3NameAddImageViewCustomizeMainPageTabsFragment.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.moreTabsGroupSummaryCustomizeMainPageTabsFragment.setTextColor(colorAccent);
        binding.moreTabsInfoTextViewCustomizeMainPageTabsFragment.setTextColor(secondaryTextColor);
        binding.moreTabsInfoTextViewCustomizeMainPageTabsFragment.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        binding.showFavoriteMultiredditsTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.showMultiredditsTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.showSubscribedSubredditsTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
        binding.showFavoriteSubscribedSubredditsTitleTextViewCustomizeMainPageTabsFragment.setTextColor(primaryTextColor);
    }

    private void applyTab1NameView(ConstraintLayout constraintLayout, TextView titleTextView, int postType) {
        switch (postType) {
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_subreddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_multi_reddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_username);
                break;
            default:
                constraintLayout.setVisibility(View.GONE);
        }
    }

    private void applyTab2NameView(ConstraintLayout linearLayout, TextView titleTextView, int postType) {
        switch (postType) {
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                linearLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_subreddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                linearLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_multi_reddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                linearLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_username);
                break;
            default:
                linearLayout.setVisibility(View.GONE);
        }
    }

    private void applyTab3NameView(ConstraintLayout constraintLayout, TextView titleTextView, int postType) {
        switch (postType) {
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_subreddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_multi_reddit_name);
                break;
            case SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER:
                constraintLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(R.string.settings_tab_username);
                break;
            default:
                constraintLayout.setVisibility(View.GONE);
        }
    }

    private void selectName(int tab) {
        switch (tab) {
            case 0:
                switch (tab1CurrentPostType) {
                    case 3: {
                        Intent intent = new Intent(activity, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE,
                                SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE_SUBREDDIT);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE,
                                SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE_MULTIREDDIT);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, tab);
                        break;
                    }
                }
                break;
            case 1:
                switch (tab2CurrentPostType) {
                    case 3: {
                        Intent intent = new Intent(activity, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE,
                                SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE_SUBREDDIT);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE,
                                SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE_MULTIREDDIT);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, tab);
                        break;
                    }
                }
                break;
            case 2:
                switch (tab3CurrentPostType) {
                    case 3: {
                        Intent intent = new Intent(activity, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE,
                                SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE_SUBREDDIT);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE,
                                SubscribedThingListingActivity.EXTRA_THING_SELECTION_TYPE_MULTIREDDIT);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
                        startActivityForResult(intent, tab);
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            int thingType = data.getIntExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.SUBREDDIT);
            switch (requestCode) {
                case 0:
                    if (thingType == SelectThingReturnKey.THING_TYPE.SUBREDDIT) {
                        tab1CurrentName = data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
                        binding.tab1NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                    } else if (thingType == SelectThingReturnKey.THING_TYPE.MULTIREDDIT) {
                        MultiReddit multireddit = data.getParcelableExtra(SelectThingReturnKey.RETRUN_EXTRA_MULTIREDDIT);
                        if (multireddit != null) {
                            tab1CurrentName = multireddit.getPath();
                            binding.tab1NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                        }
                    } else if (thingType == SelectThingReturnKey.THING_TYPE.USER) {
                        tab1CurrentName = data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
                        binding.tab1NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab1CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                    }
                    break;
                case 1:
                    if (thingType == SelectThingReturnKey.THING_TYPE.SUBREDDIT) {
                        tab2CurrentName = data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
                        binding.tab2NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                    } else if (thingType == SelectThingReturnKey.THING_TYPE.MULTIREDDIT) {
                        MultiReddit multireddit = data.getParcelableExtra(SelectThingReturnKey.RETRUN_EXTRA_MULTIREDDIT);
                        if (multireddit != null) {
                            tab2CurrentName = multireddit.getPath();
                            binding.tab2NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                        }
                    } else if (thingType == SelectThingReturnKey.THING_TYPE.USER) {
                        tab2CurrentName = data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
                        binding.tab2NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab2CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                    }
                    break;
                case 2:
                    if (thingType == SelectThingReturnKey.THING_TYPE.SUBREDDIT) {
                        tab3CurrentName = data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
                        binding.tab3NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                    } else if (thingType == SelectThingReturnKey.THING_TYPE.MULTIREDDIT) {
                        MultiReddit multireddit = data.getParcelableExtra(SelectThingReturnKey.RETRUN_EXTRA_MULTIREDDIT);
                        if (multireddit != null) {
                            tab3CurrentName = multireddit.getPath();
                            binding.tab3NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                        }
                    } else if (thingType == SelectThingReturnKey.THING_TYPE.USER) {
                        tab3CurrentName = data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
                        binding.tab3NameSummaryTextViewCustomizeMainPageTabsFragment.setText(tab3CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                    }
                    break;
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
    }
}