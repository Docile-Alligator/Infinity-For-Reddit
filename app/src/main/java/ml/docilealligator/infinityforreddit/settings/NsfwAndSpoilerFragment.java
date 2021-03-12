package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class NsfwAndSpoilerFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.enable_nsfw_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout enableNsfwLinearLayout;
    @BindView(R.id.enable_nsfw_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial enableNsfwSwitchMaterial;
    @BindView(R.id.blur_nsfw_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout blurNsfwLinearLayout;
    @BindView(R.id.blur_nsfw_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial blurNsfwSwitchMaterial;
    @BindView(R.id.do_not_blur_nsfw_in_nsfw_subreddits_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout doNotBlurNsfwInNsfwSubredditsLinearLayout;
    @BindView(R.id.do_not_blur_nsfw_in_nsfw_subreddits_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial doNotBlurNsfwInNsfwSubredditsSwitch;
    @BindView(R.id.blur_spoiler_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout blurSpoilerLinearLayout;
    @BindView(R.id.blur_spoiler_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial blurSpoilerSwitchMaterial;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences nsfwAndBlurringSharedPreferences;

    private Activity activity;
    private boolean blurNsfw;
    private boolean doNotBlurNsfwInNsfwSubreddits;

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

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        boolean enableNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        blurNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        doNotBlurNsfwInNsfwSubreddits = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        boolean blurSpoiler = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);

        if (enableNsfw) {
            blurNsfwLinearLayout.setVisibility(View.VISIBLE);
            doNotBlurNsfwInNsfwSubredditsLinearLayout.setVisibility(View.VISIBLE);
        }

        enableNsfwSwitchMaterial.setChecked(enableNsfw);
        blurNsfwSwitchMaterial.setChecked(blurNsfw);
        doNotBlurNsfwInNsfwSubredditsSwitch.setChecked(doNotBlurNsfwInNsfwSubreddits);
        blurSpoilerSwitchMaterial.setChecked(blurSpoiler);

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
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (AppCompatActivity) context;
    }
}