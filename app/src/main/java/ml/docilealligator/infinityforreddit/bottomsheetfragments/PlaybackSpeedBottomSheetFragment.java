package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.ViewImgurMediaActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.fragments.ViewImgurVideoFragment;
import ml.docilealligator.infinityforreddit.fragments.ViewRedditGalleryVideoFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PlaybackSpeedBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_PLAYBACK_SPEED = "EPS";

    @BindView(R.id.playback_speed_025_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed025TextView;
    @BindView(R.id.playback_speed_050_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed050TextView;
    @BindView(R.id.playback_speed_075_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed075TextView;
    @BindView(R.id.playback_speed_normal_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeedNormalTextView;
    @BindView(R.id.playback_speed_125_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed125TextView;
    @BindView(R.id.playback_speed_150_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed150TextView;
    @BindView(R.id.playback_speed_175_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed175TextView;
    @BindView(R.id.playback_speed_200_text_view_playback_speed_bottom_sheet_fragment)
    TextView playbackSpeed200TextView;
    private Activity activity;

    public PlaybackSpeedBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_playback_speed, container, false);

        ButterKnife.bind(this, rootView);
        int playbackSpeed = getArguments().getInt(EXTRA_PLAYBACK_SPEED, ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
        switch (playbackSpeed) {
            case ViewVideoActivity.PLAYBACK_SPEED_25:
                playbackSpeed025TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_50:
                playbackSpeed050TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_75:
                playbackSpeed075TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_NORMAL:
                playbackSpeedNormalTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_125:
                playbackSpeed125TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_150:
                playbackSpeed150TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_175:
                playbackSpeed175TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_200:
                playbackSpeed200TextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_24dp, 0);
                break;
        }

        playbackSpeed025TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_25);
            dismiss();
        });

        playbackSpeed050TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_50);
            dismiss();
        });

        playbackSpeed075TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_75);
            dismiss();
        });

        playbackSpeedNormalTextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
            dismiss();
        });

        playbackSpeed125TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_125);
            dismiss();
        });

        playbackSpeed150TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_150);
            dismiss();
        });

        playbackSpeed175TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_175);
            dismiss();
        });

        playbackSpeed200TextView.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_200);
            dismiss();
        });

        if (activity instanceof ViewVideoActivity) {
            if (((ViewVideoActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((ViewVideoActivity) activity).typeface);
            }
        } else if (activity instanceof ViewImgurMediaActivity) {
            if (((ViewImgurMediaActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((ViewImgurMediaActivity) activity).typeface);
            }
        } else if (activity instanceof ViewRedditGalleryActivity) {
            if (((ViewRedditGalleryActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((ViewRedditGalleryActivity) activity).typeface);
            }
        }
        return rootView;
    }

    private void setPlaybackSpeed(int playbackSpeed) {
        if (activity instanceof ViewVideoActivity) {
            ((ViewVideoActivity) activity).setPlaybackSpeed(playbackSpeed);
        } else {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof ViewImgurVideoFragment) {
                ((ViewImgurVideoFragment) parentFragment).setPlaybackSpeed(playbackSpeed);
            } else if (parentFragment instanceof ViewRedditGalleryVideoFragment) {
                ((ViewRedditGalleryVideoFragment) parentFragment).setPlaybackSpeed(playbackSpeed);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}