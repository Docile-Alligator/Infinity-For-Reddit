package ml.docilealligator.infinityforreddit.Settings;

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
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

public class NsfwAndBlurringFragment extends Fragment {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.enable_nsfw_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout enableNsfwLinearLayout;
    @BindView(R.id.enable_nsfw_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial enableNsfwSwitchMaterial;
    @BindView(R.id.blur_nsfw_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout blurNsfwLinearLayout;
    @BindView(R.id.blur_nsfw_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial blurNsfwSwitchMaterial;
    @BindView(R.id.blur_spoiler_linear_layout_nsfw_and_spoiler_fragment)
    LinearLayout blurSpoilerLinearLayout;
    @BindView(R.id.blur_spoiler_switch_nsfw_and_spoiler_fragment)
    SwitchMaterial blurSpoilerSwitchMaterial;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences nsfwAndBlurringSharedPreferences;

    private Activity activity;

    public NsfwAndBlurringFragment() {
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
        boolean blurNsfw = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        boolean blurSpoiler = nsfwAndBlurringSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);

        if (enableNsfw) {
            blurNsfwLinearLayout.setVisibility(View.VISIBLE);
        }

        enableNsfwSwitchMaterial.setChecked(enableNsfw);
        blurNsfwSwitchMaterial.setChecked(blurNsfw);
        blurSpoilerSwitchMaterial.setChecked(blurSpoiler);

        enableNsfwLinearLayout.setOnClickListener(view -> enableNsfwSwitchMaterial.performClick());
        enableNsfwSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, b).apply();
            if (b) {
                blurNsfwLinearLayout.setVisibility(View.VISIBLE);
            } else {
                blurNsfwLinearLayout.setVisibility(View.GONE);
            }
            EventBus.getDefault().post(new ChangeNSFWEvent(b));
        });

        blurNsfwLinearLayout.setOnClickListener(view -> blurNsfwSwitchMaterial.performClick());
        blurNsfwSwitchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(b));
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