package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.Manifest;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.TrackSelectionDialogBuilder;

import com.google.common.collect.ImmutableList;
import com.otaliastudios.zoom.ZoomEngine;
import com.otaliastudios.zoom.ZoomSurfaceView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import app.futured.hauler.DragDirection;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.LiveDataState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.apis.StreamableAPIKt;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PlaybackSpeedBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewVideoBinding;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewVideoZoomableBinding;
import ml.docilealligator.infinityforreddit.events.FinishViewMediaActivityEvent;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.services.DownloadRedditVideoService;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.viewmodels.ViewVideoViewModel;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@UnstableApi
public class ViewVideoActivity extends AppCompatActivity implements CustomFontReceiver {

    public static final int PLAYBACK_SPEED_25 = 25;
    public static final int PLAYBACK_SPEED_50 = 50;
    public static final int PLAYBACK_SPEED_75 = 75;
    public static final int PLAYBACK_SPEED_NORMAL = 100;
    public static final int PLAYBACK_SPEED_125 = 125;
    public static final int PLAYBACK_SPEED_150 = 150;
    public static final int PLAYBACK_SPEED_175 = 175;
    public static final int PLAYBACK_SPEED_200 = 200;
    public static final String EXTRA_VIDEO_DOWNLOAD_URL = "EVDU";
    public static final String EXTRA_SUBREDDIT = "ES";
    public static final String EXTRA_ID = "EI";
    public static final String EXTRA_POST = "EP";
    public static final String EXTRA_PROGRESS_SECONDS = "EPS";
    public static final String EXTRA_REDGIFS_ID = "EGI";
    public static final String EXTRA_V_REDD_IT_URL = "EVRIU";
    public static final String EXTRA_STREAMABLE_SHORT_CODE = "ESSC";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_VIDEO_TYPE = "EVT";
    public static final int VIDEO_TYPE_MARKDOWN_PARSED = 8;
    public static final int VIDEO_TYPE_IMGUR = 7;
    public static final int VIDEO_TYPE_STREAMABLE = 5;
    public static final int VIDEO_TYPE_V_REDD_IT = 4;
    public static final int VIDEO_TYPE_DIRECT = 3;
    public static final int VIDEO_TYPE_REDGIFS = 2;
    private static final int VIDEO_TYPE_NORMAL = 0;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    public Typeface typeface;

    private ExoPlayer player;
    @UnstableApi
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;

    private Integer originalOrientation;
    private boolean useBottomToolbar;
    private ViewVideoActivityBindingAdapter binding;

    public ViewVideoViewModel viewVideoViewModel;

    @Inject
    @Named("media3")
    OkHttpClient mOkHttpClient;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("redgifs")
    Retrofit mRedgifsRetrofit;

    @Inject
    @Named("vReddIt")
    Retrofit mVReddItRetrofit;

    @Inject
    Provider<StreamableAPIKt> mStreamableApiProvider;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Inject
    Executor mExecutor;

    @UnstableApi
    @Inject
    SimpleCache mSimpleCache;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int systemThemeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (systemThemeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                getTheme().applyStyle(R.style.Theme_Normal, true);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                if(mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                    getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                } else {
                    getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                }
                break;
            case 2:
                if (systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }
                if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    getTheme().applyStyle(R.style.Theme_Normal, true);
                } else {
                    if(mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                        getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                    } else {
                        getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                    }
                }
        }

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(FontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        boolean zoomable = mSharedPreferences.getBoolean(SharedPreferencesUtils.PINCH_TO_ZOOM_VIDEO, false);
        if (zoomable) {
            binding = new ViewVideoActivityBindingAdapter(ActivityViewVideoZoomableBinding.inflate(getLayoutInflater()));
            setContentView(binding.getRoot());
        } else {
            binding = new ViewVideoActivityBindingAdapter(ActivityViewVideoBinding.inflate(getLayoutInflater()));
            setContentView(binding.getRoot());
        }

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setTitle(" ");

        if (typeface != null) {
            binding.getTitleTextView().setTypeface(typeface);
        }

        Resources resources = getResources();

        useBottomToolbar = mSharedPreferences.getBoolean(SharedPreferencesUtils.USE_BOTTOM_TOOLBAR_IN_MEDIA_VIEWER, false);
        if (useBottomToolbar) {
            binding.getToolbar().setVisibility(View.GONE);
            binding.getBottomAppBar().setVisibility(View.VISIBLE);
            binding.getBackButton().setOnClickListener(view -> {
                finish();
            });

            binding.getDownloadButton().setOnClickListener(view -> {
                if (viewVideoViewModel.isDownloading()) {
                    return;
                }

                if (viewVideoViewModel.getVideoDownloadUrl() == null) {
                    Toast.makeText(this, R.string.fetching_video_info_please_wait, Toast.LENGTH_SHORT).show();
                    return;
                }

                viewVideoViewModel.setDownloading(true);
                requestPermissionAndDownload();
            });

            binding.getPlaybackSpeedButton().setOnClickListener(view -> {
                changePlaybackSpeed();
            });
        } else {
            setSupportActionBar(binding.getToolbar());
        }


        LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets allInsets = Utils.getInsets(insets, false, false);

                ViewGroup.MarginLayoutParams toolbarParams = (ViewGroup.MarginLayoutParams) binding.getToolbar().getLayoutParams();
                toolbarParams.topMargin = allInsets.top;
                binding.getToolbar().setLayoutParams(toolbarParams);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.bottomMargin = allInsets.bottom;
                params.setMarginStart(allInsets.left);
                params.setMarginEnd(allInsets.right);
                controllerLinearLayout.setLayoutParams(params);
                return WindowInsetsCompat.CONSUMED;
            }
        });

        Intent intent = getIntent();
        Post post = intent.getParcelableExtra(EXTRA_POST);
        if (post != null) {
            binding.getTitleTextView().setText(post.getTitle());
        }

        String dataSavingModeString = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        int networkType = Utils.getConnectedNetwork(this);
        boolean isDataSavingMode = false;
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            isDataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            isDataSavingMode = networkType == Utils.NETWORK_TYPE_CELLULAR;
        }

        viewVideoViewModel = new ViewModelProvider(
                this,
                ViewVideoViewModel.Companion.provideFactory(post,
                        intent.getData(), intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL),
                        post != null ? post.getVideoFallBackDirectUrl() : null,
                        intent.getStringExtra(EXTRA_SUBREDDIT), intent.getStringExtra(EXTRA_ID),
                        intent.getBooleanExtra(EXTRA_IS_NSFW, false),
                        intent.getLongExtra(EXTRA_PROGRESS_SECONDS, -1),
                        intent.getIntExtra(EXTRA_VIDEO_TYPE, VIDEO_TYPE_NORMAL),
                        intent.getStringExtra(EXTRA_REDGIFS_ID),
                        intent.getStringExtra(EXTRA_V_REDD_IT_URL),
                        intent.getStringExtra(EXTRA_STREAMABLE_SHORT_CODE),
                        isDataSavingMode, Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.REDDIT_VIDEO_DEFAULT_RESOLUTION, "360")),
                        Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.REDDIT_VIDEO_DEFAULT_RESOLUTION_NO_DATA_SAVING, "0")),
                        Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_PLAYBACK_SPEED, "100"))
                )
        ).get(ViewVideoViewModel.class);

        binding.getHaulerView().setOnDragDismissedListener(dragDirection -> {
            player.stop();
            int slide = dragDirection == DragDirection.UP ? R.anim.slide_out_up : R.anim.slide_out_down;
            finish();
            overridePendingTransition(0, slide);
        });


        if (savedInstanceState == null) {
            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.VIDEO_PLAYER_AUTOMATIC_LANDSCAPE_ORIENTATION, false)) {
                originalOrientation = resources.getConfiguration().orientation;
                try {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

                    if (android.provider.Settings.System.getInt(getContentResolver(),
                            Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
                        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
                            @Override
                            public void onOrientationChanged(int orientation) {
                                int epsilon = 10;
                                int leftLandscape = 90;
                                int rightLandscape = 270;
                                if(epsilonCheck(orientation, leftLandscape, epsilon) ||
                                        epsilonCheck(orientation, rightLandscape, epsilon)) {
                                    try {
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                        disable();
                                    } catch (Exception ignore) {}
                                }
                            }

                            private boolean epsilonCheck(int a, int b, int epsilon) {
                                return a > b - epsilon && a < b + epsilon;
                            }
                        };
                        orientationEventListener.enable();
                    }
                } catch (Exception ignore) {}
            }
        }

        trackSelector = new DefaultTrackSelector(this);
        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setRenderersFactory(new DefaultRenderersFactory(this).setEnableDecoderFallback(true))
                .build();

        if (zoomable) {
            PlayerControlView playerControlView = findViewById(R.id.player_control_view_view_video_activity);
            playerControlView.addVisibilityListener(visibility -> {
                switch (visibility) {
                    case View.GONE:
                        if (!useBottomToolbar) {
                            binding.getToolbar().setVisibility(View.GONE);
                        }

                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        break;
                    case View.VISIBLE:
                        if (!useBottomToolbar) {
                            binding.getToolbar().setVisibility(View.VISIBLE);
                        }

                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            });
            playerControlView.setPlayer(player);

            ZoomSurfaceView zoomSurfaceView = findViewById(R.id.zoom_surface_view_view_video_activity);
            player.addListener(new Player.Listener() {
                @Override
                public void onVideoSizeChanged(VideoSize videoSize) {
                    zoomSurfaceView.setContentSize(videoSize.width, videoSize.height);
                }
            });
            zoomSurfaceView.addCallback(new ZoomSurfaceView.Callback() {
                @Override
                public void onZoomSurfaceCreated(@NonNull ZoomSurfaceView zoomSurfaceView) {
                    player.setVideoSurface(zoomSurfaceView.getSurface());
                }

                @Override
                public void onZoomSurfaceDestroyed(@NonNull ZoomSurfaceView zoomSurfaceView) {

                }
            });
            zoomSurfaceView.getEngine().addListener(new ZoomEngine.Listener() {
                @Override
                public void onUpdate(@NonNull ZoomEngine zoomEngine, @NonNull Matrix matrix) {
                    if (zoomEngine.getZoom() < 1.00001) {
                        binding.getHaulerView().setDragEnabled(true);
                        binding.getNestedScrollView().setScrollEnabled(true);
                    } else {
                        binding.getHaulerView().setDragEnabled(false);
                        binding.getNestedScrollView().setScrollEnabled(false);
                    }
                }

                @Override
                public void onIdle(@NonNull ZoomEngine zoomEngine) {

                }
            });
            zoomSurfaceView.setOnClickListener(view -> {
                if (playerControlView.isVisible()) {
                    playerControlView.hide();
                } else {
                    playerControlView.show();
                }
            });
        } else {
            PlayerView videoPlayerView = findViewById(R.id.player_view_view_video_activity);
            videoPlayerView.setPlayer(player);
            videoPlayerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
                switch (visibility) {
                    case View.GONE:
                        if (!useBottomToolbar) {
                            binding.getToolbar().setVisibility(View.GONE);
                        }

                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        break;
                    case View.VISIBLE:
                        if (!useBottomToolbar) {
                            binding.getToolbar().setVisibility(View.VISIBLE);
                        }

                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            });
        }

        setPlaybackSpeed(viewVideoViewModel.getPlaybackSpeed());

        Drawable playDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_24dp, null);
        Drawable pauseDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_24dp, null);
        binding.getPlayPauseButton().setOnClickListener(view -> {
            Util.handlePlayPauseButtonAction(player);
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
                if (events.containsAny(
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED)) {
                    binding.getPlayPauseButton().setIcon(Util.shouldShowPlayButton(player) ? playDrawable : pauseDrawable);
                }
            }

            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
                if (!trackGroups.isEmpty()) {
                    if (viewVideoViewModel.getVideoType() == VIDEO_TYPE_NORMAL
                            || viewVideoViewModel.getVideoType() == VIDEO_TYPE_MARKDOWN_PARSED) {
                        binding.getVideoQualityButton().setVisibility(View.VISIBLE);
                        binding.getVideoQualityButton().setOnClickListener(view -> {
                            TrackSelectionDialogBuilder builder = new TrackSelectionDialogBuilder(ViewVideoActivity.this, getString(R.string.select_video_quality), player, C.TRACK_TYPE_VIDEO);
                            builder.setShowDisableOption(true);
                            builder.setAllowAdaptiveSelections(false);
                            Dialog dialog = builder.setTheme(R.style.MaterialAlertDialogTheme).build();
                            dialog.show();
                            if (dialog instanceof AlertDialog) {
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
                            }
                        });

                        if (!viewVideoViewModel.getSetDefaultResolutionAlready()) {
                            int desiredResolution = 0;
                            if (viewVideoViewModel.isDataSavingMode()) {
                                if (viewVideoViewModel.getDataSavingModeDefaultResolution() > 0) {
                                    desiredResolution = viewVideoViewModel.getDataSavingModeDefaultResolution();
                                }
                            } else if (viewVideoViewModel.getNonDataSavingModeDefaultResolution() > 0) {
                                desiredResolution = viewVideoViewModel.getNonDataSavingModeDefaultResolution();
                            }

                            if (desiredResolution > 0) {
                                TrackSelectionOverride trackSelectionOverride = null;
                                int bestTrackIndex = -1;
                                int bestResolution = -1;
                                int worstResolution = Integer.MAX_VALUE;
                                int worstTrackIndex = -1;
                                Tracks.Group bestTrackGroup = null;
                                Tracks.Group worstTrackGroup = null;
                                for (Tracks.Group trackGroup : tracks.getGroups()) {
                                    if (trackGroup.getType() == C.TRACK_TYPE_VIDEO) {
                                        for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                                            int trackResolution = Math.min(trackGroup.getTrackFormat(trackIndex).height, trackGroup.getTrackFormat(trackIndex).width);
                                            if (trackResolution <= desiredResolution && trackResolution > bestResolution) {
                                                bestTrackIndex = trackIndex;
                                                bestResolution = trackResolution;
                                                bestTrackGroup = trackGroup;
                                            }
                                            if (trackResolution < worstResolution) {
                                                worstTrackIndex = trackIndex;
                                                worstResolution = trackResolution;
                                                worstTrackGroup = trackGroup;
                                            }
                                        }
                                    }
                                }

                                if (bestTrackIndex != -1 && bestTrackGroup != null) {
                                    trackSelectionOverride = new TrackSelectionOverride(
                                            bestTrackGroup.getMediaTrackGroup(),
                                            ImmutableList.of(bestTrackIndex)
                                    );
                                } else if (worstTrackIndex != -1 && worstTrackGroup != null) {
                                    trackSelectionOverride = new TrackSelectionOverride(
                                            worstTrackGroup.getMediaTrackGroup(),
                                            ImmutableList.of(worstTrackIndex)
                                    );
                                }

                                if (trackSelectionOverride != null) {
                                    player.setTrackSelectionParameters(
                                            player.getTrackSelectionParameters()
                                                    .buildUpon()
                                                    .addOverride(trackSelectionOverride)
                                                    .build()
                                    );
                                }
                            }
                            viewVideoViewModel.setSetDefaultResolutionAlready(true);
                        }
                    }

                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_AUDIO) {
                            if (viewVideoViewModel.getVideoType() == VIDEO_TYPE_NORMAL
                                    || viewVideoViewModel.getVideoType() == VIDEO_TYPE_MARKDOWN_PARSED && trackGroup.length > 1) {
                                // Reddit video HLS usually has two audio tracks. The first is mono.
                                // The second (index 1) is stereo.
                                // Select the stereo audio track if possible.
                                trackSelector.setParameters(
                                        trackSelector.buildUponParameters()
                                                .setOverrideForType(new TrackSelectionOverride(
                                                                trackGroup.getMediaTrackGroup(),
                                                                trackGroup.getMediaTrackGroup().length > 1 ? 1 : 0
                                                        )
                                                )
                                );
                            }
                            if (binding.getMuteButton().getVisibility() != View.VISIBLE) {
                                binding.getMuteButton().setVisibility(View.VISIBLE);
                                binding.getMuteButton().setOnClickListener(view -> {
                                    if (viewVideoViewModel.isMute()) {
                                        viewVideoViewModel.setMute(false);
                                        player.setVolume(1f);
                                        binding.getMuteButton().setIconResource(R.drawable.ic_unmute_24dp);
                                    } else {
                                        viewVideoViewModel.setMute(true);
                                        player.setVolume(0f);
                                        binding.getMuteButton().setIconResource(R.drawable.ic_mute_24dp);
                                    }
                                });
                            }
                            break;
                        }
                    }
                } else {
                    binding.getMuteButton().setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                viewVideoViewModel.loadFallbackVideo(player.getCurrentMediaItem());
            }
        });

        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new CacheDataSource.Factory().setCache(mSimpleCache)
                .setUpstreamDataSourceFactory(new OkHttpDataSource.Factory(mOkHttpClient).setUserAgent(APIUtils.USER_AGENT));

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                player.stop();
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        viewVideoViewModel.getVideoUriLiveData().observe(this, uri -> {
            if (uri == null) {
                binding.getLoadingIndicator().setVisibility(View.VISIBLE);

                viewVideoViewModel.loadVideoLink(mRetrofit, mVReddItRetrofit, mRedgifsRetrofit,
                        mStreamableApiProvider, mCurrentAccountSharedPreferences);
            } else {
                binding.getLoadingIndicator().setVisibility(View.GONE);
                if (viewVideoViewModel.getVideoType() == VIDEO_TYPE_NORMAL || viewVideoViewModel.getVideoType() == VIDEO_TYPE_MARKDOWN_PARSED) {
                    // Prepare the player with the source.
                    player.prepare();
                    player.setMediaSource(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri)));
                    preparePlayer(savedInstanceState);
                } else {
                    // Prepare the player with the source.
                    player.prepare();
                    player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri)));
                    preparePlayer(savedInstanceState);
                }
            }
        });

        viewVideoViewModel.getErrorResId().observe(this, resIdLiveDataState -> {
            if (resIdLiveDataState instanceof LiveDataState.Value) {
                binding.getLoadingIndicator().setVisibility(View.GONE);
                if (viewVideoViewModel.getVideoType() == VIDEO_TYPE_V_REDD_IT) {
                    Integer resId = ((LiveDataState.Value<Integer>) resIdLiveDataState).getData();
                    if (resId != null) {
                        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (!viewVideoViewModel.loadFallbackVideo(player.getCurrentMediaItem())) {
                        Integer resId = ((LiveDataState.Value<Integer>) resIdLiveDataState).getData();
                        if (resId != null) {
                            Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    private void applyCustomTheme() {
        if (binding.getToolbar().getNavigationIcon() != null) {
            binding.getToolbar().getNavigationIcon().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (binding.getToolbar().getOverflowIcon() != null) {
            binding.getToolbar().getOverflowIcon().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        binding.getPlayPauseButton().setBackgroundColor(mCustomThemeWrapper.getColorAccent());
        binding.getPlayPauseButton().setIconTint(ColorStateList.valueOf(mCustomThemeWrapper.getFABIconColor()));
    }

    private void preparePlayer(Bundle savedInstanceState) {
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.LOOP_VIDEO, true)) {
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
        } else {
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
        }
        if (viewVideoViewModel.getResumePosition() > 0) {
            player.seekTo(viewVideoViewModel.getResumePosition());
            viewVideoViewModel.setResumePosition(-1);
        }
        player.setPlayWhenReady(true);
        viewVideoViewModel.setWasPlaying(true);

        boolean muteVideo = mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_VIDEO, false) ||
                (mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_NSFW_VIDEO, false) && viewVideoViewModel.isNSFW());

        if (savedInstanceState != null) {
            if (viewVideoViewModel.isMute()) {
                player.setVolume(0f);
                binding.getMuteButton().setIconResource(R.drawable.ic_mute_24dp);
            } else {
                player.setVolume(1f);
                binding.getMuteButton().setIconResource(R.drawable.ic_unmute_24dp);
            }
        } else if (muteVideo) {
            viewVideoViewModel.setMute(true);
            player.setVolume(0f);
            binding.getMuteButton().setIconResource(R.drawable.ic_mute_24dp);
        } else {
            binding.getMuteButton().setIconResource(R.drawable.ic_unmute_24dp);
        }
    }

    private void changePlaybackSpeed() {
        PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, viewVideoViewModel.getPlaybackSpeed());
        playbackSpeedBottomSheetFragment.setArguments(bundle);
        playbackSpeedBottomSheetFragment.show(getSupportFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_video_activity, menu);
        for (int i = 0; i < menu.size(); i++) {
            Utils.setTitleWithCustomFontToMenuItem(typeface, menu.getItem(i), null);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        player.seekToDefaultPosition();
        player.stop();
        player.release();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_download_view_video_activity) {
            if (viewVideoViewModel.isDownloading()) {
                return false;
            }

            if (viewVideoViewModel.getVideoDownloadUrl() == null) {
                Toast.makeText(this, R.string.fetching_video_info_please_wait, Toast.LENGTH_SHORT).show();
                return true;
            }

            viewVideoViewModel.setDownloading(true);
            requestPermissionAndDownload();
            return true;
        } else if (itemId == R.id.action_playback_speed_view_video_activity) {
            changePlaybackSpeed();
            return true;
        }

        return false;
    }

    public void setPlaybackSpeed(int speed100X) {
        viewVideoViewModel.setPlaybackSpeed(speed100X <= 0 ? 100 : speed100X);
        player.setPlaybackParameters(new PlaybackParameters((speed100X / 100.0f)));
    }

    private void requestPermissionAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
    protected void onStart() {
        super.onStart();
        if (viewVideoViewModel.getWasPlaying()) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewVideoViewModel.setWasPlaying(player.getPlayWhenReady());
        player.setPlayWhenReady(false);
        if (originalOrientation != null) {
            try {
                setRequestedOrientation(originalOrientation);
            } catch (Exception ignore) {}
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && viewVideoViewModel.isDownloading()) {
                download();
            }
            viewVideoViewModel.setDownloading(false);
        }
    }

    private void download() {
        viewVideoViewModel.setDownloading(false);

        if (viewVideoViewModel.getVideoType() != VIDEO_TYPE_NORMAL && viewVideoViewModel.getVideoType() != VIDEO_TYPE_MARKDOWN_PARSED) {
            PersistableBundle extras = new PersistableBundle();
            if (viewVideoViewModel.getPost() != null && viewVideoViewModel.getPost().getPostType() == Post.GIF_TYPE) {
                extras.putString(DownloadMediaService.EXTRA_URL, viewVideoViewModel.getPost().getVideoUrl());
                extras.putInt(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_GIF);
                extras.putString(DownloadMediaService.EXTRA_FILE_NAME, viewVideoViewModel.getFileName());
            } else {
                extras.putString(DownloadMediaService.EXTRA_URL, viewVideoViewModel.getVideoDownloadUrl());
                extras.putInt(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
                extras.putString(DownloadMediaService.EXTRA_FILE_NAME, viewVideoViewModel.getFileName());
            }

            extras.putString(DownloadMediaService.EXTRA_SUBREDDIT_NAME, viewVideoViewModel.getSubredditName());
            extras.putInt(DownloadMediaService.EXTRA_IS_NSFW, viewVideoViewModel.isNSFW() ? 1 : 0);

            //TODO: contentEstimatedBytes
            JobInfo jobInfo = DownloadMediaService.constructJobInfo(this, 5000000, extras);
            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);
        } else {
            PersistableBundle extras = new PersistableBundle();
            extras.putString(DownloadRedditVideoService.EXTRA_VIDEO_URL, viewVideoViewModel.getVideoDownloadUrl());
            extras.putString(DownloadRedditVideoService.EXTRA_POST_ID, viewVideoViewModel.getId());
            extras.putString(DownloadRedditVideoService.EXTRA_SUBREDDIT, viewVideoViewModel.getSubredditName());
            extras.putInt(DownloadRedditVideoService.EXTRA_IS_NSFW, viewVideoViewModel.isNSFW() ? 1 : 0);

            //TODO: contentEstimatedBytes
            JobInfo jobInfo = DownloadRedditVideoService.constructJobInfo(this, 5000000, extras);
            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);
        }

        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }

    @Subscribe
    public void onFinishViewMediaActivityEvent(FinishViewMediaActivityEvent e) {
        finish();
    }
}
