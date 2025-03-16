package ml.docilealligator.infinityforreddit.fragments;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.ui.PlayerView;

import com.google.android.material.button.MaterialButton;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PlaybackSpeedBottomSheetFragment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ViewRedditGalleryVideoFragment extends Fragment {

    public static final String EXTRA_REDDIT_GALLERY_VIDEO = "EIV";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_INDEX = "EI";
    public static final String EXTRA_MEDIA_COUNT = "EMC";
    public static final String EXTRA_IS_NSFW = "EIN";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final String IS_MUTE_STATE = "IMS";
    private static final String POSITION_STATE = "PS";
    private static final String PLAYBACK_SPEED_STATE = "PSS";

    private ViewRedditGalleryActivity activity;
    private Post.Gallery galleryVideo;
    private String subredditName;
    private boolean isNsfw;
    private ExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private boolean wasPlaying = false;
    private boolean isMute = false;
    private boolean isDownloading = false;
    private int playbackSpeed = 100;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @UnstableApi
    @Inject
    SimpleCache mSimpleCache;
    private ViewRedditGalleryVideoFragmentBindingAdapter binding;

    public ViewRedditGalleryVideoFragment() {
        // Required empty public constructor
    }


    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = new ViewRedditGalleryVideoFragmentBindingAdapter(ml.docilealligator.infinityforreddit.databinding.FragmentViewRedditGalleryVideoBinding.inflate(inflater, container, false));

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        setHasOptionsMenu(true);

        if (activity.typeface != null) {
            binding.getTitleTextView().setTypeface(activity.typeface);
        }

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        galleryVideo = getArguments().getParcelable(EXTRA_REDDIT_GALLERY_VIDEO);
        subredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);
        isNsfw = getArguments().getBoolean(EXTRA_IS_NSFW, false);

        if (!mSharedPreferences.getBoolean(SharedPreferencesUtils.VIDEO_PLAYER_IGNORE_NAV_BAR, false)) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || getResources().getBoolean(R.bool.isTablet)) {
                //Set player controller bottom margin in order to display it above the navbar
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = binding.getRoot().findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.bottomMargin = getResources().getDimensionPixelSize(resourceId);
            } else {
                //Set player controller right margin in order to display it above the navbar
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = binding.getRoot().findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.rightMargin = getResources().getDimensionPixelSize(resourceId);
            }
        }

        binding.getPlayerView().setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
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

        TrackSelector trackSelector = new DefaultTrackSelector(activity);
        player = new ExoPlayer.Builder(activity)
                .setTrackSelector(trackSelector)
                .setRenderersFactory(new DefaultRenderersFactory(activity).setEnableDecoderFallback(true))
                .build();
        binding.getPlayerView().setPlayer(player);
        dataSourceFactory = new CacheDataSource.Factory().setCache(mSimpleCache)
                .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).setUserAgent(APIUtils.USER_AGENT));
        player.prepare();
        player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(galleryVideo.url)));

        if (savedInstanceState != null) {
            playbackSpeed = savedInstanceState.getInt(PLAYBACK_SPEED_STATE);
        }
        Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_PLAYBACK_SPEED, "100"));
        preparePlayer(savedInstanceState);

        binding.getTitleTextView().setText(getString(R.string.view_reddit_gallery_activity_video_label,
                getArguments().getInt(EXTRA_INDEX) + 1, getArguments().getInt(EXTRA_MEDIA_COUNT)));

        if (activity.isUseBottomAppBar()) {
            binding.getBottomAppBar().setVisibility(View.VISIBLE);
            binding.getBackButton().setOnClickListener(view -> {
                activity.finish();
            });
            binding.getDownloadButton().setOnClickListener(view -> {
                if (isDownloading) {
                    return;
                }
                isDownloading = true;
                requestPermissionAndDownload();
            });
            binding.getPlaybackSpeedButton().setOnClickListener(view -> {
                changePlaybackSpeed();
            });
        }

        return binding.getRoot();
    }

    private void changePlaybackSpeed() {
        PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, playbackSpeed);
        playbackSpeedBottomSheetFragment.setArguments(bundle);
        playbackSpeedBottomSheetFragment.show(getChildFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.view_reddit_gallery_video_fragment, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, null);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_download_view_reddit_gallery_video_fragment) {
            isDownloading = true;
            requestPermissionAndDownload();
            return true;
        } else if (item.getItemId() == R.id.action_playback_speed_view_reddit_gallery_video_fragment) {
            changePlaybackSpeed();
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

        PersistableBundle extras = new PersistableBundle();
        extras.putString(DownloadMediaService.EXTRA_URL, galleryVideo.url);
        extras.putInt(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
        extras.putString(DownloadMediaService.EXTRA_FILE_NAME, galleryVideo.fileName);
        extras.putString(DownloadMediaService.EXTRA_SUBREDDIT_NAME, subredditName);
        extras.putInt(DownloadMediaService.EXTRA_IS_NSFW, isNsfw ? 1 : 0);

        //TODO: contentEstimatedBytes
        JobInfo jobInfo = DownloadMediaService.constructJobInfo(activity, 5000000, extras);
        ((JobScheduler) activity.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);

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
                binding.getMuteButton().setImageResource(R.drawable.ic_mute_24dp);
            } else {
                player.setVolume(1f);
                binding.getMuteButton().setImageResource(R.drawable.ic_unmute_24dp);
            }
        } else if (muteVideo) {
            isMute = true;
            player.setVolume(0f);
            binding.getMuteButton().setImageResource(R.drawable.ic_mute_24dp);
        } else {
            binding.getMuteButton().setImageResource(R.drawable.ic_unmute_24dp);
        }

        MaterialButton playPauseButton = binding.getRoot().findViewById(R.id.exo_play_pause_button_exo_playback_control_view);
        Drawable playDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_24dp, null);
        Drawable pauseDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_24dp, null);
        playPauseButton.setOnClickListener(view -> {
            Util.handlePlayPauseButtonAction(player);
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
                if (events.containsAny(
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED)) {
                    playPauseButton.setIcon(Util.shouldShowPlayButton(player) ? playDrawable : pauseDrawable);
                }
            }

            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
                if (!trackGroups.isEmpty()) {
                    for (int i = 0; i < trackGroups.size(); i++) {
                        String mimeType = trackGroups.get(i).getTrackFormat(0).sampleMimeType;
                        if (mimeType != null && mimeType.contains("audio")) {
                            binding.getMuteButton().setVisibility(View.VISIBLE);
                            binding.getMuteButton().setOnClickListener(view -> {
                                if (isMute) {
                                    isMute = false;
                                    player.setVolume(1f);
                                    binding.getMuteButton().setImageResource(R.drawable.ic_unmute_24dp);
                                } else {
                                    isMute = true;
                                    player.setVolume(0f);
                                    binding.getMuteButton().setImageResource(R.drawable.ic_mute_24dp);
                                }
                            });
                            break;
                        }
                    }
                } else {
                    binding.getMuteButton().setVisibility(View.GONE);
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
        player.stop();
        player.release();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ViewRedditGalleryActivity) context;
    }
}