package ml.docilealligator.infinityforreddit.activities;

import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;

import app.futured.hauler.HaulerView;
import app.futured.hauler.LockableNestedScrollView;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewVideoBinding;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewVideoZoomableBinding;

public class ViewVideoActivityBindingAdapter {
    @Nullable
    private ActivityViewVideoBinding binding;
    @Nullable
    private ActivityViewVideoZoomableBinding zoomableBinding;

    private MaterialButton muteButton;
    private MaterialButton hdButton;
    private BottomAppBar bottomAppBar;
    private TextView titleTextView;
    private MaterialButton backButton;
    private MaterialButton downloadButton;
    private MaterialButton playbackSpeedButton;

    public ViewVideoActivityBindingAdapter(ActivityViewVideoBinding binding) {
        this.binding = binding;
        muteButton = binding.getRoot().findViewById(R.id.mute_exo_playback_control_view);
        hdButton = binding.getRoot().findViewById(R.id.hd_exo_playback_control_view);
        bottomAppBar = binding.getRoot().findViewById(R.id.bottom_navigation_exo_playback_control_view);
        titleTextView = binding.getRoot().findViewById(R.id.title_text_view_exo_playback_control_view);
        backButton = binding.getRoot().findViewById(R.id.back_button_exo_playback_control_view);
        downloadButton = binding.getRoot().findViewById(R.id.download_image_view_exo_playback_control_view);
        playbackSpeedButton = binding.getRoot().findViewById(R.id.playback_speed_image_view_exo_playback_control_view);
    }

    public ViewVideoActivityBindingAdapter(ActivityViewVideoZoomableBinding binding) {
        zoomableBinding = binding;
        muteButton = binding.getRoot().findViewById(R.id.mute_exo_playback_control_view);
        hdButton = binding.getRoot().findViewById(R.id.hd_exo_playback_control_view);
        bottomAppBar = binding.getRoot().findViewById(R.id.bottom_navigation_exo_playback_control_view);
        titleTextView = binding.getRoot().findViewById(R.id.title_text_view_exo_playback_control_view);
        backButton = binding.getRoot().findViewById(R.id.back_button_exo_playback_control_view);
        downloadButton = binding.getRoot().findViewById(R.id.download_image_view_exo_playback_control_view);
        playbackSpeedButton = binding.getRoot().findViewById(R.id.playback_speed_image_view_exo_playback_control_view);
    }

    public HaulerView getRoot() {
        return binding == null ? zoomableBinding.getRoot() : binding.getRoot();
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return binding == null ? zoomableBinding.coordinatorLayoutViewVideoActivity : binding.coordinatorLayoutViewVideoActivity;
    }

    public ProgressBar getProgressBar() {
        return binding == null ? zoomableBinding.progressBarViewVideoActivity : binding.progressBarViewVideoActivity;
    }

    public MaterialButton getMuteButton() {
        return getRoot().findViewById(R.id.mute_exo_playback_control_view);
    }

    public MaterialButton getHdButton() {
        return hdButton;
    }

    public BottomAppBar getBottomAppBar() {
        return bottomAppBar;
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public MaterialButton getBackButton() {
        return backButton;
    }

    public MaterialButton getDownloadButton() {
        return downloadButton;
    }

    public MaterialButton getPlaybackSpeedButton() {
        return playbackSpeedButton;
    }

    public LockableNestedScrollView getNestedScrollView() {
        return binding == null ? zoomableBinding.lockableNestedScrollViewViewVideoActivity : binding.lockableNestedScrollViewViewVideoActivity;
    }
}
