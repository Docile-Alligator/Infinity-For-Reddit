package ml.docilealligator.infinityforreddit.activities;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.loadingindicator.LoadingIndicator;

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

    private final MaterialButton playPauseButton;
    private final MaterialButton forwardButton;
    private final MaterialButton rewindButton;
    private final MaterialButton muteButton;
    private final MaterialButton videoQualityButton;
    private final BottomAppBar bottomAppBar;
    private final TextView titleTextView;
    private final MaterialButton backButton;
    private final MaterialButton downloadButton;
    private final MaterialButton playbackSpeedButton;

    public ViewVideoActivityBindingAdapter(ActivityViewVideoBinding binding) {
        this.binding = binding;
        playPauseButton = binding.getRoot().findViewById(R.id.exo_play_pause_button_exo_playback_control_view);
        forwardButton = binding.getRoot().findViewById(R.id.exo_ffwd);
        rewindButton = binding.getRoot().findViewById(R.id.exo_rew);
        muteButton = binding.getRoot().findViewById(R.id.mute_exo_playback_control_view);
        videoQualityButton = binding.getRoot().findViewById(R.id.video_quality_exo_playback_control_view);
        bottomAppBar = binding.getRoot().findViewById(R.id.bottom_navigation_exo_playback_control_view);
        titleTextView = binding.getRoot().findViewById(R.id.title_text_view_exo_playback_control_view);
        backButton = binding.getRoot().findViewById(R.id.back_button_exo_playback_control_view);
        downloadButton = binding.getRoot().findViewById(R.id.download_image_view_exo_playback_control_view);
        playbackSpeedButton = binding.getRoot().findViewById(R.id.playback_speed_image_view_exo_playback_control_view);
    }

    public ViewVideoActivityBindingAdapter(ActivityViewVideoZoomableBinding binding) {
        zoomableBinding = binding;
        playPauseButton = binding.getRoot().findViewById(R.id.exo_play_pause_button_exo_playback_control_view);
        forwardButton = binding.getRoot().findViewById(R.id.exo_ffwd);
        rewindButton = binding.getRoot().findViewById(R.id.exo_rew);
        muteButton = binding.getRoot().findViewById(R.id.mute_exo_playback_control_view);
        videoQualityButton = binding.getRoot().findViewById(R.id.video_quality_exo_playback_control_view);
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

    public LoadingIndicator getLoadingIndicator() {
        return binding == null ? zoomableBinding.progressBarViewVideoActivity : binding.progressBarViewVideoActivity;
    }

    public MaterialButton getPlayPauseButton() {
        return playPauseButton;
    }

    public MaterialButton getForwardButton() {
        return forwardButton;
    }

    public MaterialButton getRewindButton() {
        return rewindButton;
    }

    public MaterialButton getMuteButton() {
        return muteButton;
    }

    public MaterialButton getVideoQualityButton() {
        return videoQualityButton;
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
