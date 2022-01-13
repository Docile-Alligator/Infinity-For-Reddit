package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.MultiredditSelectionActivity;
import ml.docilealligator.infinityforreddit.activities.SearchActivity;
import ml.docilealligator.infinityforreddit.activities.SubredditSelectionActivity;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeMainPageTabsFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.info_text_view_customize_main_page_tabs_fragment)
    TextView infoTextView;
    @BindView(R.id.tab_count_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tabCountLinearLayout;
    @BindView(R.id.tab_count_text_view_customize_main_page_tabs_fragment)
    TextView tabCountTextView;
    @BindView(R.id.show_tab_names_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout showTabNamesLinearLayout;
    @BindView(R.id.show_tab_names_switch_material_customize_main_page_tabs_fragment)
    SwitchMaterial showTabNamesSwitch;
    @BindView(R.id.divider_1_customize_main_page_tabs_fragment)
    View divider1;
    @BindView(R.id.tab_1_group_summary_customize_main_page_tabs_fragment)
    TextView tab1GroupSummaryTextView;
    @BindView(R.id.tab_1_title_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab1TitleLinearLayout;
    @BindView(R.id.tab_1_title_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab1TitleSummaryTextView;
    @BindView(R.id.tab_1_type_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab1TypeLinearLayout;
    @BindView(R.id.tab_1_type_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab1TypeSummaryTextView;
    @BindView(R.id.tab_1_name_constraint_layout_customize_main_page_tabs_fragment)
    ConstraintLayout tab1NameConstraintLayout;
    @BindView(R.id.tab_1_name_title_text_view_customize_main_page_tabs_fragment)
    TextView tab1NameTitleTextView;
    @BindView(R.id.tab_1_name_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab1NameSummaryTextView;
    @BindView(R.id.tab_1_name_add_image_view_customize_main_page_tabs_fragment)
    ImageView tab1AddImageView;
    @BindView(R.id.divider_2_customize_main_page_tabs_fragment)
    View divider2;
    @BindView(R.id.tab_2_group_summary_customize_main_page_tabs_fragment)
    TextView tab2GroupSummaryTextView;
    @BindView(R.id.tab_2_title_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab2TitleLinearLayout;
    @BindView(R.id.tab_2_title_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab2TitleSummaryTextView;
    @BindView(R.id.tab_2_type_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab2TypeLinearLayout;
    @BindView(R.id.tab_2_type_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab2TypeSummaryTextView;
    @BindView(R.id.tab_2_name_constraint_layout_customize_main_page_tabs_fragment)
    ConstraintLayout tab2NameConstraintLayout;
    @BindView(R.id.tab_2_name_title_text_view_customize_main_page_tabs_fragment)
    TextView tab2NameTitleTextView;
    @BindView(R.id.tab_2_name_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab2NameSummaryTextView;
    @BindView(R.id.tab_2_name_add_image_view_customize_main_page_tabs_fragment)
    ImageView tab2AddImageView;
    @BindView(R.id.divider_3_customize_main_page_tabs_fragment)
    View divider3;
    @BindView(R.id.tab_3_group_summary_customize_main_page_tabs_fragment)
    TextView tab3GroupSummaryTextView;
    @BindView(R.id.tab_3_title_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab3TitleLinearLayout;
    @BindView(R.id.tab_3_title_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab3TitleSummaryTextView;
    @BindView(R.id.tab_3_type_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab3TypeLinearLayout;
    @BindView(R.id.tab_3_type_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab3TypeSummaryTextView;
    @BindView(R.id.tab_3_name_constraint_layout_customize_main_page_tabs_fragment)
    ConstraintLayout tab3NameConstraintLayout;
    @BindView(R.id.tab_3_name_title_text_view_customize_main_page_tabs_fragment)
    TextView tab3NameTitleTextView;
    @BindView(R.id.tab_3_name_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab3NameSummaryTextView;
    @BindView(R.id.tab_3_name_add_image_view_customize_main_page_tabs_fragment)
    ImageView tab3AddImageView;
    @BindView(R.id.divider_4_customize_main_page_tabs_fragment)
    View divider4;
    @BindView(R.id.more_tabs_group_summary_customize_main_page_tabs_fragment)
    TextView moreTabsGroupSummaryTextView;
    @BindView(R.id.more_tabs_info_text_view_customize_main_page_tabs_fragment)
    TextView moreTabsInfoTextView;
    @BindView(R.id.show_favorite_multireddits_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout showFavoriteMultiredditsLinearLayout;
    @BindView(R.id.show_favorite_multireddits_switch_material_customize_main_page_tabs_fragment)
    SwitchMaterial showFavoriteMultiredditsSwitchMaterial;
    @BindView(R.id.show_multireddits_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout showMultiredditsLinearLayout;
    @BindView(R.id.show_multireddits_switch_material_customize_main_page_tabs_fragment)
    SwitchMaterial showMultiredditsSwitchMaterial;
    @BindView(R.id.show_subscribed_subreddits_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout showSubscribedSubredditsLinearLayout;
    @BindView(R.id.show_subscribed_subreddits_switch_material_customize_main_page_tabs_fragment)
    SwitchMaterial showSubscribedSubredditsSwitchMaterial;
    @BindView(R.id.show_favorite_subscribed_subreddits_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout showFavoriteSubscribedSubredditsLinearLayout;
    @BindView(R.id.show_favorite_subscribed_subreddits_switch_material_customize_main_page_tabs_fragment)
    SwitchMaterial showFavoriteSubscribedSubredditsSwitchMaterial;
    @Inject
    @Named("main_activity_tabs")
    SharedPreferences mainActivityTabsSharedPreferences;
    private BaseActivity activity;
    private String accountName;
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
        View rootView = inflater.inflate(R.layout.fragment_customize_main_page_tabs, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        String[] typeValues;
        if (accountName == null) {
            typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type_anonymous);
        } else {
            typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type);
        }

        tabCount = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        tabCountTextView.setText(Integer.toString(tabCount));
        tabCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_main_page_tab_count, tabCount - 1, (dialogInterface, i) -> {
                        tabCount = i + 1;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, tabCount).apply();
                        tabCountTextView.setText(Integer.toString(tabCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        boolean showTabNames = mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true);
        showTabNamesSwitch.setChecked(showTabNames);
        showTabNamesSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, b).apply());
        showTabNamesLinearLayout.setOnClickListener(view -> showTabNamesSwitch.performClick());

        tab1CurrentTitle = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home));
        tab1CurrentPostType = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
        tab1CurrentName = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
        tab1TypeSummaryTextView.setText(typeValues[tab1CurrentPostType]);
        tab1TitleSummaryTextView.setText(tab1CurrentTitle);
        tab1NameSummaryTextView.setText(tab1CurrentName);
        applyTab1NameView(tab1NameConstraintLayout, tab1NameTitleTextView, tab1CurrentPostType);

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.edit_text_edit_text_dialog);

        tab1TitleLinearLayout.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab1CurrentTitle);
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab1CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, tab1CurrentTitle).apply();
                        tab1TitleSummaryTextView.setText(tab1CurrentTitle);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });

        tab1TypeLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab1CurrentPostType, (dialogInterface, i) -> {
                        tab1CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, i).apply();
                        tab1TypeSummaryTextView.setText(typeValues[i]);
                        applyTab1NameView(tab1NameConstraintLayout, tab1NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        tab1NameConstraintLayout.setOnClickListener(view -> {
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
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab1CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                        tab1NameSummaryTextView.setText(tab1CurrentName);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });

        tab1AddImageView.setOnClickListener(view -> selectName(0));

        tab2CurrentTitle = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular));
        tab2CurrentPostType = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
        tab2CurrentName = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
        tab2TypeSummaryTextView.setText(typeValues[tab2CurrentPostType]);
        tab2TitleSummaryTextView.setText(tab2CurrentTitle);
        tab2NameSummaryTextView.setText(tab2CurrentName);
        applyTab2NameView(tab2NameConstraintLayout, tab2NameTitleTextView, tab2CurrentPostType);

        tab2TitleLinearLayout.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab2CurrentTitle);
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab2CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, tab2CurrentTitle).apply();
                        tab2TitleSummaryTextView.setText(tab2CurrentTitle);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });

        tab2TypeLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab2CurrentPostType, (dialogInterface, i) -> {
                        tab2CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, i).apply();
                        tab2TypeSummaryTextView.setText(typeValues[i]);
                        applyTab2NameView(tab2NameConstraintLayout, tab2NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        tab2NameConstraintLayout.setOnClickListener(view -> {
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
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab2CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                        tab2NameSummaryTextView.setText(tab2CurrentName);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });

        tab2AddImageView.setOnClickListener(view -> selectName(1));

        tab3CurrentTitle = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all));
        tab3CurrentPostType = mainActivityTabsSharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
        tab3CurrentName = mainActivityTabsSharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
        tab3TypeSummaryTextView.setText(typeValues[tab3CurrentPostType]);
        tab3TitleSummaryTextView.setText(tab3CurrentTitle);
        tab3NameSummaryTextView.setText(tab3CurrentName);
        applyTab3NameView(tab3NameConstraintLayout, tab3NameTitleTextView, tab3CurrentPostType);

        tab3TitleLinearLayout.setOnClickListener(view -> {
            editText.setHint(R.string.settings_tab_title);
            editText.setText(tab3CurrentTitle);
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab3CurrentTitle = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, tab3CurrentTitle).apply();
                        tab3TitleSummaryTextView.setText(tab3CurrentTitle);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });

        tab3TypeLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_title)
                    .setSingleChoiceItems(typeValues, tab3CurrentPostType, (dialogInterface, i) -> {
                        tab3CurrentPostType = i;
                        mainActivityTabsSharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, i).apply();
                        tab3TypeSummaryTextView.setText(typeValues[i]);
                        applyTab3NameView(tab3NameConstraintLayout, tab3NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        tab3NameConstraintLayout.setOnClickListener(view -> {
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
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            if (dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(titleId)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        tab3CurrentName = editText.getText().toString();
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                        tab3NameSummaryTextView.setText(tab3CurrentName);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });

        tab3AddImageView.setOnClickListener(view -> selectName(2));

        showMultiredditsSwitchMaterial.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, false));
        showMultiredditsSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, b).apply());
        showMultiredditsLinearLayout.setOnClickListener(view -> {
            showMultiredditsSwitchMaterial.performClick();
        });

        showFavoriteMultiredditsSwitchMaterial.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, false));
        showFavoriteMultiredditsSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, b).apply());
        showFavoriteMultiredditsLinearLayout.setOnClickListener(view -> {
            showFavoriteMultiredditsSwitchMaterial.performClick();
        });

        showSubscribedSubredditsSwitchMaterial.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false));
        showSubscribedSubredditsSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, b).apply());
        showSubscribedSubredditsLinearLayout.setOnClickListener(view -> {
            showSubscribedSubredditsSwitchMaterial.performClick();
        });

        showFavoriteSubscribedSubredditsSwitchMaterial.setChecked(mainActivityTabsSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false));
        showFavoriteSubscribedSubredditsSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> mainActivityTabsSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, b).apply());
        showFavoriteSubscribedSubredditsLinearLayout.setOnClickListener(view -> {
            showFavoriteSubscribedSubredditsSwitchMaterial.performClick();
        });

        return rootView;
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
                        Intent intent = new Intent(activity, SubredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, MultiredditSelectionActivity.class);
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
                        Intent intent = new Intent(activity, SubredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, MultiredditSelectionActivity.class);
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
                        Intent intent = new Intent(activity, SubredditSelectionActivity.class);
                        startActivityForResult(intent, tab);
                        break;
                    }
                    case 4: {
                        Intent intent = new Intent(activity, MultiredditSelectionActivity.class);
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
            switch (requestCode) {
                case 0:
                    if (data.hasExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME)) {
                        tab1CurrentName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                        tab1NameSummaryTextView.setText(tab1CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                    } else if (data.hasExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT)) {
                        MultiReddit multireddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                        if (multireddit != null) {
                            tab1CurrentName = multireddit.getPath();
                            tab1NameSummaryTextView.setText(tab1CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                        }
                    } else if (data.hasExtra(SearchActivity.EXTRA_RETURN_USER_NAME)) {
                        tab1CurrentName = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
                        tab1NameSummaryTextView.setText(tab1CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
                    }
                    break;
                case 1:
                    if (data.hasExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME)) {
                        tab2CurrentName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                        tab2NameSummaryTextView.setText(tab2CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                    } else if (data.hasExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT)) {
                        MultiReddit multireddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                        if (multireddit != null) {
                            tab2CurrentName = multireddit.getPath();
                            tab2NameSummaryTextView.setText(tab2CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                        }
                    } else if (data.hasExtra(SearchActivity.EXTRA_RETURN_USER_NAME)) {
                        tab2CurrentName = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
                        tab2NameSummaryTextView.setText(tab2CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
                    }
                    break;
                case 2:
                    if (data.hasExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME)) {
                        tab3CurrentName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                        tab3NameSummaryTextView.setText(tab3CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                    } else if (data.hasExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT)) {
                        MultiReddit multireddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                        if (multireddit != null) {
                            tab3CurrentName = multireddit.getPath();
                            tab3NameSummaryTextView.setText(tab3CurrentName);
                            mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                        }
                    } else if (data.hasExtra(SearchActivity.EXTRA_RETURN_USER_NAME)) {
                        tab3CurrentName = data.getStringExtra(SearchActivity.EXTRA_RETURN_USER_NAME);
                        tab3NameSummaryTextView.setText(tab3CurrentName);
                        mainActivityTabsSharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
                    }
                    break;
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}