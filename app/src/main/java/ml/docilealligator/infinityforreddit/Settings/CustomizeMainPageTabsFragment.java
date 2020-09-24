package ml.docilealligator.infinityforreddit.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

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
    @BindView(R.id.tab_1_name_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab1NameLinearLayout;
    @BindView(R.id.tab_1_name_title_text_view_customize_main_page_tabs_fragment)
    TextView tab1NameTitleTextView;
    @BindView(R.id.tab_1_name_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab1NameSummaryTextView;
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
    @BindView(R.id.tab_2_name_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab2NameLinearLayout;
    @BindView(R.id.tab_2_name_title_text_view_customize_main_page_tabs_fragment)
    TextView tab2NameTitleTextView;
    @BindView(R.id.tab_2_name_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab2NameSummaryTextView;
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
    @BindView(R.id.tab_3_name_linear_layout_customize_main_page_tabs_fragment)
    LinearLayout tab3NameLinearLayout;
    @BindView(R.id.tab_3_name_title_text_view_customize_main_page_tabs_fragment)
    TextView tab3NameTitleTextView;
    @BindView(R.id.tab_3_name_summary_text_view_customize_main_page_tabs_fragment)
    TextView tab3NameSummaryTextView;
    @BindView(R.id.divider_4_customize_main_page_tabs_fragment)
    View divider4;
    @BindView(R.id.more_tabs_group_summary_customize_main_page_tabs_fragment)
    TextView moreTabsGroupSummaryTextView;
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
    SharedPreferences sharedPreferences;
    private Activity activity;
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

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        if (accountName == null) {
            infoTextView.setText(R.string.settings_customize_tabs_in_main_page_summary);
            divider1.setVisibility(View.GONE);
            tabCountLinearLayout.setVisibility(View.GONE);
            showTabNamesLinearLayout.setVisibility(View.GONE);
            tab1GroupSummaryTextView.setVisibility(View.GONE);
            tab1TitleLinearLayout.setVisibility(View.GONE);
            tab1TypeLinearLayout.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
            tab2GroupSummaryTextView.setVisibility(View.GONE);
            tab2TitleLinearLayout.setVisibility(View.GONE);
            tab2TypeLinearLayout.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);
            tab3GroupSummaryTextView.setVisibility(View.GONE);
            tab3TitleLinearLayout.setVisibility(View.GONE);
            tab3TypeLinearLayout.setVisibility(View.GONE);
            divider4.setVisibility(View.GONE);
            moreTabsGroupSummaryTextView.setVisibility(View.GONE);
            showSubscribedSubredditsLinearLayout.setVisibility(View.GONE);
            showFavoriteSubscribedSubredditsLinearLayout.setVisibility(View.GONE);

            return rootView;
        }

        tabCount = sharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        tabCountTextView.setText(Integer.toString(tabCount));
        tabCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_main_page_tab_count, tabCount - 1, (dialogInterface, i) -> {
                        tabCount = i + 1;
                        sharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, tabCount).apply();
                        tabCountTextView.setText(Integer.toString(tabCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        boolean showTabNames = sharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true);
        showTabNamesSwitch.setChecked(showTabNames);
        showTabNamesSwitch.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, b).apply());
        showTabNamesLinearLayout.setOnClickListener(view -> showTabNamesSwitch.performClick());

        String[] typeValues = activity.getResources().getStringArray(R.array.settings_tab_post_type);

        tab1CurrentTitle = sharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home));
        tab1CurrentPostType = sharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
        tab1CurrentName = sharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
        tab1TypeSummaryTextView.setText(typeValues[tab1CurrentPostType]);
        tab1TitleSummaryTextView.setText(tab1CurrentTitle);
        tab1NameSummaryTextView.setText(tab1CurrentName);
        applyTab1NameView(tab1NameLinearLayout, tab1NameTitleTextView, tab1CurrentPostType);

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
                        sharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, tab1CurrentTitle).apply();
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
                        sharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, i).apply();
                        tab1TypeSummaryTextView.setText(typeValues[i]);
                        applyTab1NameView(tab1NameLinearLayout, tab1NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        tab1NameLinearLayout.setOnClickListener(view -> {
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
                        sharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, tab1CurrentName).apply();
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

        tab2CurrentTitle = sharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular));
        tab2CurrentPostType = sharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
        tab2CurrentName = sharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
        tab2TypeSummaryTextView.setText(typeValues[tab2CurrentPostType]);
        tab2TitleSummaryTextView.setText(tab2CurrentTitle);
        tab2NameSummaryTextView.setText(tab2CurrentName);
        applyTab2NameView(tab2NameLinearLayout, tab2NameTitleTextView, tab2CurrentPostType);

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
                        sharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, tab2CurrentTitle).apply();
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
                        sharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, i).apply();
                        tab2TypeSummaryTextView.setText(typeValues[i]);
                        applyTab2NameView(tab2NameLinearLayout, tab2NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        tab2NameLinearLayout.setOnClickListener(view -> {
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
                        sharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, tab2CurrentName).apply();
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

        tab3CurrentTitle = sharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all));
        tab3CurrentPostType = sharedPreferences.getInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
        tab3CurrentName = sharedPreferences.getString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
        tab3TypeSummaryTextView.setText(typeValues[tab3CurrentPostType]);
        tab3TitleSummaryTextView.setText(tab3CurrentTitle);
        tab3NameSummaryTextView.setText(tab3CurrentName);
        applyTab3NameView(tab3NameLinearLayout, tab3NameTitleTextView, tab3CurrentPostType);

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
                        sharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, tab3CurrentTitle).apply();
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
                        sharedPreferences.edit().putInt((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, i).apply();
                        tab3TypeSummaryTextView.setText(typeValues[i]);
                        applyTab3NameView(tab3NameLinearLayout, tab3NameTitleTextView, i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        tab3NameLinearLayout.setOnClickListener(view -> {
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
                        sharedPreferences.edit().putString((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, tab3CurrentName).apply();
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

        showSubscribedSubredditsSwitchMaterial.setChecked(sharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false));
        showSubscribedSubredditsSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, b).apply());
        showSubscribedSubredditsLinearLayout.setOnClickListener(view -> {
            showSubscribedSubredditsSwitchMaterial.performClick();
        });

        showFavoriteSubscribedSubredditsSwitchMaterial.setChecked(sharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false));
        showFavoriteSubscribedSubredditsSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> sharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, b).apply());
        showFavoriteSubscribedSubredditsLinearLayout.setOnClickListener(view -> {
            showFavoriteSubscribedSubredditsSwitchMaterial.performClick();
        });

        return rootView;
    }

    private void applyTab1NameView(LinearLayout linearLayout, TextView titleTextView, int postType) {
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

    private void applyTab2NameView(LinearLayout linearLayout, TextView titleTextView, int postType) {
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

    private void applyTab3NameView(LinearLayout linearLayout, TextView titleTextView, int postType) {
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}