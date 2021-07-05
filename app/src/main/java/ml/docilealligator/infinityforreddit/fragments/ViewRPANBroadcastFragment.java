package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RPANBroadcast;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ViewRPANBroadcastFragment extends Fragment {

    public static final String EXTRA_RPAN_BROADCAST = "ERB";
    private static final String IS_MUTE_STATE = "IMS";

    @BindView(R.id.player_view_view_rpan_broadcast_fragment)
    PlayerView playerView;
    @BindView(R.id.progress_bar_view_rpan_broadcast_fragment)
    ProgressBar progressBar;
    @BindView(R.id.mute_exo_playback_control_view)
    ImageButton muteButton;
    @BindView(R.id.hd_exo_playback_control_view)
    ImageButton hdButton;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    SimpleCache mSimpleCache;
    private AppCompatActivity mActivity;
    private RPANBroadcast rpanBroadcast;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;

    private boolean wasPlaying;
    private boolean isMute = false;
    private long resumePosition = -1;
    private boolean isDataSavingMode;

    public ViewRPANBroadcastFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_rpan_broadcast, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        rpanBroadcast = getArguments().getParcelable(EXTRA_RPAN_BROADCAST);

        playerView.setControllerVisibilityListener(visibility -> {
            switch (visibility) {
                case View.GONE:
                    mActivity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
                    break;
                case View.VISIBLE:
                    mActivity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        });

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(mActivity, trackSelector);
        playerView.setPlayer(player);

        dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(mActivity, "Infinity"));
        // Prepare the player with the source.
        player.prepare(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(rpanBroadcast.rpanStream.hlsUrl)));
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        if (resumePosition > 0) {
            player.seekTo(resumePosition);
        }
        player.setPlayWhenReady(true);
        wasPlaying = true;

        boolean muteVideo = mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_VIDEO, false) ||
                (mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_NSFW_VIDEO, false) && rpanBroadcast.rpanPost.isNsfw);

        if (savedInstanceState != null) {
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
                    if (isDataSavingMode) {
                        trackSelector.setParameters(
                                trackSelector.buildUponParameters()
                                        .setMaxVideoSize(720, 720));
                    }

                    hdButton.setVisibility(View.VISIBLE);
                    hdButton.setOnClickListener(view -> {
                        TrackSelectionDialogBuilder build = new TrackSelectionDialogBuilder(mActivity,
                                getString(R.string.select_video_quality), trackSelector, 0);
                        build.setShowDisableOption(true);
                        build.setAllowAdaptiveSelections(false);
                        build.build().show();
                    });

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

        return rootView;
    }

    private boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MUTE_STATE, isMute);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (wasPlaying) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        wasPlaying = player.getPlayWhenReady();
        player.setPlayWhenReady(false);
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
        mActivity = (AppCompatActivity) context;
    }
}