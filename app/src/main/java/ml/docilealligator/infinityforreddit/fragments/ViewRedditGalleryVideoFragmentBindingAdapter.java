package ml.docilealligator.infinityforreddit.fragments;

import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.media3.ui.PlayerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.databinding.FragmentViewRedditGalleryVideoBinding;

class ViewRedditGalleryVideoFragmentBindingAdapter {
    private FragmentViewRedditGalleryVideoBinding binding;

    private ImageButton muteButton;
    private BottomAppBar bottomAppBar;
    private TextView titleTextView;
    private MaterialButton backButton;
    private MaterialButton downloadButton;
    private MaterialButton playbackSpeedButton;

    ViewRedditGalleryVideoFragmentBindingAdapter(FragmentViewRedditGalleryVideoBinding binding) {
        this.binding = binding;
        muteButton = binding.getRoot().findViewById(R.id.mute_exo_playback_control_view);
        bottomAppBar = binding.getRoot().findViewById(R.id.bottom_navigation_exo_playback_control_view);
        titleTextView = binding.getRoot().findViewById(R.id.title_text_view_exo_playback_control_view);
        backButton = binding.getRoot().findViewById(R.id.back_button_exo_playback_control_view);
        downloadButton = binding.getRoot().findViewById(R.id.download_image_view_exo_playback_control_view);
        playbackSpeedButton = binding.getRoot().findViewById(R.id.playback_speed_image_view_exo_playback_control_view);
    }

    RelativeLayout getRoot() {
        return binding.getRoot();
    }

    PlayerView getPlayerView() {
        return binding.playerViewViewRedditGalleryVideoFragment;
    }

    ImageButton getMuteButton() {
        return muteButton;
    }

    BottomAppBar getBottomAppBar() {
        return bottomAppBar;
    }

    TextView getTitleTextView() {
        return titleTextView;
    }

    MaterialButton getBackButton() {
        return backButton;
    }

    MaterialButton getDownloadButton() {
        return downloadButton;
    }

    MaterialButton getPlaybackSpeedButton() {
        return playbackSpeedButton;
    }
}
