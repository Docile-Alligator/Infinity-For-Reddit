package ml.docilealligator.infinityforreddit.bottomsheetfragments;

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
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;

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
    private ViewVideoActivity viewVideoActivity;

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
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_25);
            dismiss();
        });

        playbackSpeed050TextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_50);
            dismiss();
        });

        playbackSpeed075TextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_75);
            dismiss();
        });

        playbackSpeedNormalTextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
            dismiss();
        });

        playbackSpeed125TextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_125);
            dismiss();
        });

        playbackSpeed150TextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_150);
            dismiss();
        });

        playbackSpeed175TextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_175);
            dismiss();
        });

        playbackSpeed200TextView.setOnClickListener(view -> {
            viewVideoActivity.setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_200);
            dismiss();
        });
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewVideoActivity = (ViewVideoActivity) context;
    }
}