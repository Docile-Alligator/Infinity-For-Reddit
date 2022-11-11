package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class NsfwAndSpoilerFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.enable_nsfw_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout enableNsfwLinearLayout;
    @BindView(R.id.enable_nsfw_text_view_nsfw_and_spoiler_fragment)
    TextView enableNsfwTextView;
    @BindView(R.id.enable_nsfw_switch_nsfw_and_spoiler_fragment)
    MaterialSwitch enableNsfwSwitchMaterial;
    @BindView(R.id.blur_nsfw_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout blurNsfwLinearLayout;
    @BindView(R.id.blur_nsfw_text_view_nsfw_and_spoiler_fragment)
    TextView blurNsfwTextView;
    @BindView(R.id.blur_nsfw_switch_nsfw_and_spoiler_fragment)
    MaterialSwitch blurNsfwSwitchMaterial;
    @BindView(R.id.do_not_blur_nsfw_in_nsfw_subreddits_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout doNotBlurNsfwInNsfwSubredditsLinearLayout;
    @BindView(R.id.do_not_blur_nsfw_text_view_nsfw_and_spoiler_fragment)
    TextView doNotBlurNsfwInNsfwSubredditsTextView;
    @BindView(R.id.do_not_blur_nsfw_in_nsfw_subreddits_switch_nsfw_and_spoiler_fragment)
    MaterialSwitch doNotBlurNsfwInNsfwSubredditsSwitch;
    @BindView(R.id.blur_spoiler_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout blurSpoilerLinearLayout;
    @BindView(R.id.blur_spoiler_text_view_nsfw_and_spoiler_fragment)
    TextView blurSpoilerTextView;
    @BindView(R.id.blur_spoiler_switch_nsfw_and_spoiler_fragment)
    MaterialSwitch blurSpoilerSwitchMaterial;
    @BindView(R.id.dangerous_text_view_nsfw_and_spoiler_fragment)
    TextView dangerousTextView;
    @BindView(R.id.disable_nsfw_forever_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout disableNsfwForeverLinearLayout;
    @BindView(R.id.disable_nsfw_forever_text_view_nsfw_and_spoiler_fragment)
    TextView disableNsfwForeverTextView;
    @BindView(R.id.disable_nsfw_forever_switch_nsfw_and_spoiler_fragment)
    MaterialSwitch disableNsfwForeverSwitchMaterial;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences nsfwAndBlurringSharedPreferences;

    private SettingsActivity activity;
    private boolean blurNsfw;
    private boolean doNotBlurNsfwInNsfwSubreddits;
    private boolean disableNsfwForever;
    private boolean manuallyCheckDisableNsfwForever = true;

    public NsfwAndSpoilerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nsfw_and_spoiler, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        applyCustomTheme();

        rootView.setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        boolean enableNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        blurNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        doNotBlurNsfwInNsfwSubreddits = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        boolean blurSpoiler = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
        disableNsfwForever = sharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false);

        if (enableNsfw) {
            blurNsfwLinearLayout.setVisibility(View.VISIBLE);
            doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.VISIBLE);
        }

        enableNsfwSwitchMaterial.setChecked(enableNsfw);
        blurNsfwSwitchMaterial.setChecked(blurNsfw);
        doNotBlurNsfwInNsfwSubredditsSwitch.setChecked(doNotBlurNsfwInNsfwSubreddits);
        blurSpoilerSwitchMaterial.setChecked(blurSpoiler);
        disableNsfwForeverSwitchMaterial.setChecked(disableNsfwForever);
        disableNsfwForeverSwitchMaterial.setEnabled(!disableNsfwForever);
        if (disableNsfwForever) {
            disableNsfwForeverTextView.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
            disableNsfwForeverLinearLayout.setEnabled(false);
        }

        enableNsfwLinearLayout.setOnClickListener(view -> enableNsfwSwitchMaterial.performClick());
        enableNsfwSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, b).apply();
            if (b) {
                blurNsfwLinearLayout.setVisibility(View.VISIBLE);
                doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.VISIBLE);
            } else {
                blurNsfwLinearLayout.setVisibility(View.GONE);
                doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.GONE);
            }
            EventBus.getDefault().post(new ChangeNSFWEvent(b));
        });

        blurNsfwLinearLayout.setOnClickListener(view -> blurNsfwSwitchMaterial.performClick());
        blurNsfwSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(b, doNotBlurNsfwInNsfwSubreddits));
        });

        doNotBlurNsfwInNsfwSubredditsLinearLayout.setOnClickListener(view -> {
            doNotBlurNsfwInNsfwSubredditsSwitch.performClick();
        });
        doNotBlurNsfwInNsfwSubredditsSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(blurNsfw, b));
        });

        blurSpoilerLinearLayout.setOnClickListener(view -> blurSpoilerSwitchMaterial.performClick());
        blurSpoilerSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, b).apply();
            EventBus.getDefault().post(new ChangeSpoilerBlurEvent(b));
        });

        disableNsfwForeverLinearLayout.setOnClickListener(view -> {
            disableNsfwForeverSwitchMaterial.performClick();
        });
        disableNsfwForeverSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            if (manuallyCheckDisableNsfwForever) {
                manuallyCheckDisableNsfwForever = false;
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.disable_nsfw_forever_message)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> {
                            sharedPreferences.edit().putBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, true).apply();
                            disableNsfwForever = true;
                            disableNsfwForeverSwitchMaterial.setEnabled(false);
                            disableNsfwForeverLinearLayout.setEnabled(false);
                            disableNsfwForeverSwitchMaterial.setChecked(true);
                            disableNsfwForeverTextView.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
                            EventBus.getDefault().post(new ChangeNSFWEvent(false));
                        })
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                            disableNsfwForeverSwitchMaterial.setChecked(false);
                            manuallyCheckDisableNsfwForever = true;
                        })
                        .setOnDismissListener(dialogInterface -> {
                            if (!disableNsfwForever) {
                                disableNsfwForeverSwitchMaterial.setChecked(false);
                            }
                            manuallyCheckDisableNsfwForever = true;
                        })
                        .show();
            }
        });
        return rootView;
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        enableNsfwTextView.setCompoundDrawablesWithIntrinsicBounds(Utils.getTintedDrawable(activity, R.drawable.ic_nsfw_on_24dp, activity.customThemeWrapper.getPrimaryIconColor()), null, null, null);
        enableNsfwTextView.setTextColor(primaryTextColor);
        blurNsfwTextView.setTextColor(primaryTextColor);
        doNotBlurNsfwInNsfwSubredditsTextView.setTextColor(primaryTextColor);
        blurSpoilerTextView.setTextColor(primaryTextColor);
        dangerousTextView.setTextColor(primaryTextColor);
        disableNsfwForeverTextView.setTextColor(primaryTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (SettingsActivity) context;
    }
}