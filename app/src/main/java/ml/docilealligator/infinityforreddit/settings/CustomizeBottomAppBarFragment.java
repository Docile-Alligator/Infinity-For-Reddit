package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class CustomizeBottomAppBarFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.info_text_view_customize_bottom_app_bar_fragment)
    TextView infoTextView;
    @BindView(R.id.main_activity_group_summary_customize_bottom_app_bar_fragment)
    TextView mainActivityGroupSummaryTextView;
    @BindView(R.id.main_activity_option_count_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout mainActivityOptionCountLinearLayout;
    @BindView(R.id.main_activity_option_count_text_view_customize_bottom_app_bar_fragment)
    TextView mainActivityOptionCountTextView;
    @BindView(R.id.main_activity_option_1_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout mainActivityOption1LinearLayout;
    @BindView(R.id.main_activity_option_1_text_view_customize_bottom_app_bar_fragment)
    TextView mainActivityOption1TextView;
    @BindView(R.id.main_activity_option_2_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout mainActivityOption2LinearLayout;
    @BindView(R.id.main_activity_option_2_text_view_customize_bottom_app_bar_fragment)
    TextView mainActivityOption2TextView;
    @BindView(R.id.main_activity_option_3_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout mainActivityOption3LinearLayout;
    @BindView(R.id.main_activity_option_3_text_view_customize_bottom_app_bar_fragment)
    TextView mainActivityOption3TextView;
    @BindView(R.id.main_activity_option_4_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout mainActivityOption4LinearLayout;
    @BindView(R.id.main_activity_option_4_text_view_customize_bottom_app_bar_fragment)
    TextView mainActivityOption4TextView;
    @BindView(R.id.main_activity_fab_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout mainActivityFABLinearLayout;
    @BindView(R.id.main_activity_fab_text_view_customize_bottom_app_bar_fragment)
    TextView mainActivityFABTextView;

    @BindView(R.id.divider_2_customize_bottom_app_bar_fragment)
    View divider2;
    @BindView(R.id.other_activities_group_summary_customize_bottom_app_bar_fragment)
    TextView otherActivitiesGroupSummaryTextView;
    @BindView(R.id.other_activities_option_count_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout otherActivitiesOptionCountLinearLayout;
    @BindView(R.id.other_activities_option_count_text_view_customize_bottom_app_bar_fragment)
    TextView otherActivitiesOptionCountTextView;
    @BindView(R.id.other_activities_option_1_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout otherActivitiesOption1LinearLayout;
    @BindView(R.id.other_activities_option_1_text_view_customize_bottom_app_bar_fragment)
    TextView otherActivitiesOption1TextView;
    @BindView(R.id.other_activities_option_2_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout otherActivitiesOption2LinearLayout;
    @BindView(R.id.other_activities_option_2_text_view_customize_bottom_app_bar_fragment)
    TextView otherActivitiesOption2TextView;
    @BindView(R.id.other_activities_option_3_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout otherActivitiesOption3LinearLayout;
    @BindView(R.id.other_activities_option_3_text_view_customize_bottom_app_bar_fragment)
    TextView otherActivitiesOption3TextView;
    @BindView(R.id.other_activities_option_4_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout otherActivitiesOption4LinearLayout;
    @BindView(R.id.other_activities_option_4_text_view_customize_bottom_app_bar_fragment)
    TextView otherActivitiesOption4TextView;
    @BindView(R.id.other_activities_fab_linear_layout_customize_bottom_app_bar_fragment)
    LinearLayout otherActivitiesFABLinearLayout;
    @BindView(R.id.other_activities_fab_text_view_customize_bottom_app_bar_fragment)
    TextView otherActivitiesFABTextView;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences sharedPreferences;
    private Activity activity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_customize_bottom_app_bar, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        if (accountName == null) {
            infoTextView.setText(R.string.only_for_logged_in_user);
            mainActivityGroupSummaryTextView.setVisibility(View.GONE);
            mainActivityOptionCountLinearLayout.setVisibility(View.GONE);
            mainActivityOption1LinearLayout.setVisibility(View.GONE);
            mainActivityOption2LinearLayout.setVisibility(View.GONE);
            mainActivityOption3LinearLayout.setVisibility(View.GONE);
            mainActivityOption4LinearLayout.setVisibility(View.GONE);
            mainActivityFABLinearLayout.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
            otherActivitiesGroupSummaryTextView.setVisibility(View.GONE);
            otherActivitiesOptionCountLinearLayout.setVisibility(View.GONE);
            otherActivitiesOption1LinearLayout.setVisibility(View.GONE);
            otherActivitiesOption2LinearLayout.setVisibility(View.GONE);
            otherActivitiesOption3LinearLayout.setVisibility(View.GONE);
            otherActivitiesOption4LinearLayout.setVisibility(View.GONE);
            otherActivitiesFABLinearLayout.setVisibility(View.GONE);

            return rootView;
        }

        Resources resources = activity.getResources();
        String[] mainActivityOptions = resources.getStringArray(R.array.settings_main_activity_bottom_app_bar_options);
        String[] fabOptions = resources.getStringArray(R.array.settings_bottom_app_bar_fab_options);
        mainActivityOptionCount = sharedPreferences.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
        mainActivityOption1 = sharedPreferences.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, 0);
        mainActivityOption2 = sharedPreferences.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, 1);
        mainActivityOption3 = sharedPreferences.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, 2);
        mainActivityOption4 = sharedPreferences.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, 3);
        mainActivityFAB = sharedPreferences.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, 0);

        mainActivityOptionCountTextView.setText(Integer.toString(mainActivityOptionCount));
        mainActivityOption1TextView.setText(mainActivityOptions[mainActivityOption1]);
        mainActivityOption2TextView.setText(mainActivityOptions[mainActivityOption2]);
        mainActivityOption3TextView.setText(mainActivityOptions[mainActivityOption3]);
        mainActivityOption4TextView.setText(mainActivityOptions[mainActivityOption4]);
        mainActivityFABTextView.setText(fabOptions[mainActivityFAB]);

        mainActivityOptionCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_bottom_app_bar_option_count_options, mainActivityOptionCount / 2 - 1, (dialogInterface, i) -> {
                        mainActivityOptionCount = (i + 1) * 2;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, mainActivityOptionCount).apply();
                        mainActivityOptionCountTextView.setText(Integer.toString(mainActivityOptionCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        mainActivityOption1LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_1)
                    .setSingleChoiceItems(mainActivityOptions, mainActivityOption1, (dialogInterface, i) -> {
                        mainActivityOption1 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, mainActivityOption1).apply();
                        mainActivityOption1TextView.setText(mainActivityOptions[mainActivityOption1]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        mainActivityOption2LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_2)
                    .setSingleChoiceItems(mainActivityOptions, mainActivityOption2, (dialogInterface, i) -> {
                        mainActivityOption2 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, mainActivityOption2).apply();
                        mainActivityOption2TextView.setText(mainActivityOptions[mainActivityOption2]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        mainActivityOption3LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_3)
                    .setSingleChoiceItems(mainActivityOptions, mainActivityOption3, (dialogInterface, i) -> {
                        mainActivityOption3 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, mainActivityOption3).apply();
                        mainActivityOption3TextView.setText(mainActivityOptions[mainActivityOption3]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        mainActivityOption4LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_4)
                    .setSingleChoiceItems(mainActivityOptions, mainActivityOption4, (dialogInterface, i) -> {
                        mainActivityOption4 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, mainActivityOption4).apply();
                        mainActivityOption4TextView.setText(mainActivityOptions[mainActivityOption4]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        mainActivityFABLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_fab)
                    .setSingleChoiceItems(fabOptions, mainActivityFAB, (dialogInterface, i) -> {
                        mainActivityFAB = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, mainActivityFAB).apply();
                        mainActivityFABTextView.setText(fabOptions[mainActivityFAB]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        String[] otherActivitiesOptions = resources.getStringArray(R.array.settings_other_activities_bottom_app_bar_options);
        otherActivitiesOptionCount = sharedPreferences.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, 4);
        otherActivitiesOption1 = sharedPreferences.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, 0);
        otherActivitiesOption2 = sharedPreferences.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, 1);
        otherActivitiesOption3 = sharedPreferences.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, 2);
        otherActivitiesOption4 = sharedPreferences.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, 3);
        otherActivitiesFAB = sharedPreferences.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, 0);

        otherActivitiesOptionCountTextView.setText(Integer.toString(otherActivitiesOptionCount));
        otherActivitiesOption1TextView.setText(otherActivitiesOptions[otherActivitiesOption1]);
        otherActivitiesOption2TextView.setText(otherActivitiesOptions[otherActivitiesOption2]);
        otherActivitiesOption3TextView.setText(otherActivitiesOptions[otherActivitiesOption3]);
        otherActivitiesOption4TextView.setText(otherActivitiesOptions[otherActivitiesOption4]);
        otherActivitiesFABTextView.setText(fabOptions[otherActivitiesFAB]);

        otherActivitiesOptionCountLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_tab_count)
                    .setSingleChoiceItems(R.array.settings_bottom_app_bar_option_count_options, otherActivitiesOptionCount / 2 - 1, (dialogInterface, i) -> {
                        otherActivitiesOptionCount = (i + 1) * 2;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, otherActivitiesOptionCount).apply();
                        otherActivitiesOptionCountTextView.setText(Integer.toString(otherActivitiesOptionCount));
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        otherActivitiesOption1LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_1)
                    .setSingleChoiceItems(otherActivitiesOptions, otherActivitiesOption1, (dialogInterface, i) -> {
                        otherActivitiesOption1 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, otherActivitiesOption1).apply();
                        otherActivitiesOption1TextView.setText(otherActivitiesOptions[otherActivitiesOption1]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        otherActivitiesOption2LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_2)
                    .setSingleChoiceItems(otherActivitiesOptions, otherActivitiesOption2, (dialogInterface, i) -> {
                        otherActivitiesOption2 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, otherActivitiesOption2).apply();
                        otherActivitiesOption2TextView.setText(otherActivitiesOptions[otherActivitiesOption2]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        otherActivitiesOption3LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_3)
                    .setSingleChoiceItems(otherActivitiesOptions, otherActivitiesOption3, (dialogInterface, i) -> {
                        otherActivitiesOption3 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, otherActivitiesOption3).apply();
                        otherActivitiesOption3TextView.setText(otherActivitiesOptions[otherActivitiesOption3]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        otherActivitiesOption4LinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_option_4)
                    .setSingleChoiceItems(otherActivitiesOptions, otherActivitiesOption4, (dialogInterface, i) -> {
                        otherActivitiesOption4 = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, otherActivitiesOption4).apply();
                        otherActivitiesOption4TextView.setText(otherActivitiesOptions[otherActivitiesOption4]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        otherActivitiesFABLinearLayout.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.settings_bottom_app_bar_fab)
                    .setSingleChoiceItems(fabOptions, otherActivitiesFAB, (dialogInterface, i) -> {
                        otherActivitiesFAB = i;
                        sharedPreferences.edit().putInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, otherActivitiesFAB).apply();
                        otherActivitiesFABTextView.setText(fabOptions[otherActivitiesFAB]);
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}