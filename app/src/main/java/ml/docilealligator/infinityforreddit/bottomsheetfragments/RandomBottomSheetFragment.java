package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentRandomBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class RandomBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_IS_NSFW = "EIN";
    public static final int RANDOM_SUBREDDIT = 0;
    public static final int RANDOM_POST = 1;
    public static final int RANDOM_NSFW_SUBREDDIT = 2;
    public static final int RANDOM_NSFW_POST = 3;

    private RandomOptionSelectionCallback activity;

    public RandomBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentRandomBottomSheetBinding binding = FragmentRandomBottomSheetBinding.inflate(inflater, container, false);

        boolean isNSFW = getArguments().getBoolean(EXTRA_IS_NSFW, false);

        if (!isNSFW) {
            binding.randomNsfwSubredditTextViewRandomBottomSheetFragment.setVisibility(View.GONE);
            binding.randomNsfwPostTextViewRandomBottomSheetFragment.setVisibility(View.GONE);
        } else {
            binding.randomNsfwSubredditTextViewRandomBottomSheetFragment.setOnClickListener(view -> {
                activity.randomOptionSelected(RANDOM_NSFW_SUBREDDIT);
                dismiss();
            });

            binding.randomNsfwPostTextViewRandomBottomSheetFragment.setOnClickListener(view -> {
                activity.randomOptionSelected(RANDOM_NSFW_POST);
                dismiss();
            });
        }

        binding.randomSubredditTextViewRandomBottomSheetFragment.setOnClickListener(view -> {
            activity.randomOptionSelected(RANDOM_SUBREDDIT);
            dismiss();
        });

        binding.randomPostTextViewRandomBottomSheetFragment.setOnClickListener(view -> {
            activity.randomOptionSelected(RANDOM_POST);
            dismiss();
        });

        Activity baseActivity = getActivity();
        if (baseActivity instanceof BaseActivity) {
            if (((BaseActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((BaseActivity) activity).typeface);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (RandomOptionSelectionCallback) context;
    }

    public interface RandomOptionSelectionCallback {
        void randomOptionSelected(int option);
    }
}