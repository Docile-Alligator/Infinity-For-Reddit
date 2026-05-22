package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentImportantInfoBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ImportantInfoBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    @Inject
    @Named("internal")
    SharedPreferences mInternalSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    private BaseActivity mBaseActivity;

    public ImportantInfoBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((Infinity) mBaseActivity.getApplication()).getAppComponent().inject(this);
        
        FragmentImportantInfoBottomSheetBinding binding = FragmentImportantInfoBottomSheetBinding.inflate(inflater, container, false);

        if (mBaseActivity != null && mBaseActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), mBaseActivity.typeface);
        }

        binding.getRoot().setNestedScrollingEnabled(true);

        binding.titleTextViewImportantInfoBottomSheetFragment.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.descriptionTextViewImportantInfoBottomSheetFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        binding.joinSubredditTextViewImportantInfoBottomSheetFragment.setTextColor(mCustomThemeWrapper.getLinkColor());
        binding.continueButtonImportantInfoBottomSheetFragment.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.continueButtonImportantInfoBottomSheetFragment.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorPrimaryLightTheme()));

        binding.joinSubredditTextViewImportantInfoBottomSheetFragment.setOnClickListener(view -> {
            Intent intent = new Intent(mBaseActivity, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, "Infinity_For_Reddit");
            mBaseActivity.startActivity(intent);

            mInternalSharedPreferences.edit().putInt(SharedPreferencesUtils.CURRENT_VERSION, BuildConfig.VERSION_CODE).apply();
            dismiss();
        });

        binding.continueButtonImportantInfoBottomSheetFragment.setOnClickListener(view -> {
            mInternalSharedPreferences.edit().putInt(SharedPreferencesUtils.CURRENT_VERSION, BuildConfig.VERSION_CODE).apply();
            dismiss();
        });

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mBaseActivity = (BaseActivity) context;
    }
}