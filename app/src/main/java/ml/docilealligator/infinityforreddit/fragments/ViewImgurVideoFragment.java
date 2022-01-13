package ml.docilealligator.infinityforreddit.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomappbar.BottomAppBar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ImgurMedia;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.ViewImgurMediaActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PlaybackSpeedBottomSheetFragment;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ViewImgurVideoFragment extends Fragment {

    public static final String EXTRA_IMGUR_VIDEO = "EIV";
    public static final String EXTRA_INDEX = "EI";
    public static final String EXTRA_MEDIA_COUNT = "EMC";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final String IS_MUTE_STATE = "IMS";
    private static final String POSITION_STATE = "PS";
    private static final String PLAYBACK_SPEED_STATE = "PSS";
    @BindView(R.id.player_view_view_imgur_video_fragment)
    PlayerView videoPlayerView;
    @BindView(R.id.mute_exo_playback_control_view)
    ImageButton muteButton;
    @BindView(R.id.bottom_navigation_exo_playback_control_view)
    BottomAppBar bottomAppBar;
    @BindView(R.id.title_text_view_exo_playback_control_view)
    TextView titleTextView;
    @BindView(R.id.download_image_view_exo_playback_control_view)
    ImageView downloadImageView;
    private ViewImgurMediaActivity activity;
    private ImgurMedia imgurMedia;
    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private boolean wasPlaying = false;
    private boolean isMute = false;
    private boolean isDownloading = false;
    private int playbackSpeed = 100;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    public ViewImgurVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_imgur_video, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        imgurMedia = getArguments().getParcelable(EXTRA_IMGUR_VIDEO);

        if (!mSharedPreferences.getBoolean(SharedPreferencesUtils.VIDEO_PLAYER_IGNORE_NAV_BAR, false)) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || getResources().getBoolean(R.bool.isTablet)) {
                //Set player controller bottom margin in order to display it above the navbar
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = rootView.findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.bottomMargin = getResources().getDimensionPixelSize(resourceId);
            } else {
                //Set player controller right margin in order to display it above the navbar
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = rootView.findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.rightMargin = getResources().getDimensionPixelSize(resourceId);
            }
        }

        videoPlayerView.setControllerVisibilityListener(visibility -> {
            switch (visibility) {
                case View.GONE:
                    activity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
                    break;
                case View.VISIBLE:
                    activity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        });

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector);
        videoPlayerView.setPlayer(player);
        dataSourceFactory = new DefaultDataSourceFactory(activity,
                Util.getUserAgent(activity, "Infinity"));
        player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(imgurMedia.getLink())));

        if (savedInstanceState != null) {
            playbackSpeed = savedInstanceState.getInt(PLAYBACK_SPEED_STATE);
        }
        setPlaybackSpeed(Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_PLAYBACK_SPEED, "100")));
        preparePlayer(savedInstanceState);

        if (activity.isUseBottomAppBar()) {
            bottomAppBar.setVisibility(View.VISIBLE);
            titleTextView.setText(getString(R.string.view_imgur_media_activity_video_label,
                    getArguments().getInt(EXTRA_INDEX) + 1, getArguments().getInt(EXTRA_MEDIA_COUNT)));
            downloadImageView.setOnClickListener(view -> {
                if (isDownloading) {
                    return;
                }
                isDownloading = true;
                requestPermissionAndDownload();
            });
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.view_imgur_video_fragment, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, null);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_download_view_imgur_video_fragment) {
            isDownloading = true;
            requestPermissionAndDownload();
            return true;
        } else if (item.getItemId() == R.id.action_playback_speed_view_imgur_video_fragment) {
            PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, playbackSpeed);
            playbackSpeedBottomSheetFragment.setArguments(bundle);
            playbackSpeedBottomSheetFragment.show(getChildFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
            return true;
        }
        return false;
    }

    public void setPlaybackSpeed(int speed100X) {
        this.playbackSpeed = speed100X;
        player.setPlaybackParameters(new PlaybackParameters((float) (speed100X / 100.0)));
    }

    private void requestPermissionAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                // Permission has already been granted
                download();
            }
        } else {
            download();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(activity, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
                isDownloading = false;
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                download();
            }
        }
    }

    private void download() {
        isDownloading = false;

        Intent intent = new Intent(activity, DownloadMediaService.class);
        intent.putExtra(DownloadMediaService.EXTRA_URL, imgurMedia.getLink());
        intent.putExtra(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
        intent.putExtra(DownloadMediaService.EXTRA_FILE_NAME, imgurMedia.getFileName());
        ContextCompat.startForegroundService(activity, intent);
        Toast.makeText(activity, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    private void preparePlayer(Bundle savedInstanceState) {
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.LOOP_VIDEO, true)) {
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
        } else {
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
        }
        wasPlaying = true;

        boolean muteVideo = mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_VIDEO, false);

        if (savedInstanceState != null) {
            long position = savedInstanceState.getLong(POSITION_STATE);
            if (position > 0) {
                player.seekTo(position);
            }
            isMute = savedInstanceState.getBoolean(IS_MUTE_STATE);
            if (isMute) {
                player.setVolume(0f);
                muteButton.setImageResource(R.drawable.ic_mute_24dp);
            } else {
                player.setVolume(1f);
                muteButton.setImageResource(R.drawable.ic_unmute_24dp);
            }
        } else if (muteVideo) {
            isMute = true;
            player.setVolume(0f);
            muteButton.setImageResource(R.drawable.ic_mute_24dp);
        } else {
            muteButton.setImageResource(R.drawable.ic_unmute_24dp);
        }

        player.addListener(new Player.EventListener() {
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                if (!trackGroups.isEmpty()) {
                    for (int i = 0; i < trackGroups.length; i++) {
                        String mimeType = trackGroups.get(i).getFormat(0).sampleMimeType;
                        if (mimeType != null && mimeType.contains("audio")) {
                            muteButton.setVisibility(View.VISIBLE);
                            muteButton.setOnClickListener(view -> {
                                if (isMute) {
                                    isMute = false;
                                    player.setVolume(1f);
                                    muteButton.setImageResource(R.drawable.ic_unmute_24dp);
                                } else {
                                    isMute = true;
                                    player.setVolume(0f);
                                    muteButton.setImageResource(R.drawable.ic_mute_24dp);
                                }
                            });
                            break;
                        }
                    }
                } else {
                    muteButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wasPlaying) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPlaying = player.getPlayWhenReady();
        player.setPlayWhenReady(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MUTE_STATE, isMute);
        outState.putLong(POSITION_STATE, player.getCurrentPosition());
        outState.putInt(PLAYBACK_SPEED_STATE, playbackSpeed);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.seekToDefaultPosition();
        player.stop(true);
        player.release();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ViewImgurMediaActivity) context;
    }
}
