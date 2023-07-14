package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class RandomBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_IS_NSFW = "EIN";
    public static final int RANDOM_SUBREDDIT = 0;
    public static final int RANDOM_POST = 1;
    public static final int RANDOM_NSFW_SUBREDDIT = 2;
    public static final int RANDOM_NSFW_POST = 3;

    @BindView(R.id.random_subreddit_text_view_random_bottom_sheet_fragment)
    TextView randomSubredditTextView;
    @BindView(R.id.random_post_text_view_random_bottom_sheet_fragment)
    TextView randomPostTextView;
    @BindView(R.id.random_nsfw_subreddit_text_view_random_bottom_sheet_fragment)
    TextView randomNSFWSubredditTextView;
    @BindView(R.id.random_nsfw_post_text_view_random_bottom_sheet_fragment)
    TextView randomNSFWPostTextView;

    private RandomOptionSelectionCallback activity;

    public RandomBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_random_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        boolean isNSFW = getArguments().getBoolean(EXTRA_IS_NSFW, false);

        if (!isNSFW) {
            randomNSFWSubredditTextView.setVisibility(View.GONE);
            randomNSFWPostTextView.setVisibility(View.GONE);
        } else {
            randomNSFWSubredditTextView.setOnClickListener(view -> {
                activity.randomOptionSelected(RANDOM_NSFW_SUBREDDIT);
                dismiss();
            });

            randomNSFWPostTextView.setOnClickListener(view -> {
                activity.randomOptionSelected(RANDOM_NSFW_POST);
                dismiss();
            });
        }

        randomSubredditTextView.setOnClickListener(view -> {
            activity.randomOptionSelected(RANDOM_SUBREDDIT);
            dismiss();
        });

        randomPostTextView.setOnClickListener(view -> {
            activity.randomOptionSelected(RANDOM_POST);
            dismiss();
        });

        Activity baseActivity = getActivity();
        if (baseActivity instanceof BaseActivity) {
            if (((BaseActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((BaseActivity) activity).typeface);
            }
        }

        return rootView;
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