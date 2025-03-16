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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
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
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.TrackSelectionDialogBuilder;

import com.google.android.material.button.MaterialButton;
import com.google.common.collect.ImmutableList;
import com.otaliastudios.zoom.ZoomEngine;
import com.otaliastudios.zoom.ZoomSurfaceView;

import org.apache.commons.io.FilenameUtils;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import app.futured.hauler.DragDirection;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.FetchVideoLinkListener;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.VideoLinkFetcher;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PlaybackSpeedBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewVideoBinding;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewVideoZoomableBinding;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.services.DownloadRedditVideoService;
import ml.docilealligator.infinityforreddit.thing.StreamableVideo;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

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
    public static final int VIDEO_TYPE_IMGUR = 7;
    public static final int VIDEO_TYPE_STREAMABLE = 5;
    public static final int VIDEO_TYPE_V_REDD_IT = 4;
    public static final int VIDEO_TYPE_DIRECT = 3;
    public static final int VIDEO_TYPE_REDGIFS = 2;
    private static final int VIDEO_TYPE_NORMAL = 0;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private static final String IS_MUTE_STATE = "IMS";
    private static final String VIDEO_DOWNLOAD_URL_STATE = "VDUS";
    private static final String VIDEO_URI_STATE = "VUS";
    private static final String VIDEO_TYPE_STATE = "VTS";
    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String ID_STATE=  "IS";
    private static final String PLAYBACK_SPEED_STATE = "PSS";

    public Typeface typeface;

    private Uri mVideoUri;
    private ExoPlayer player;
    @UnstableApi
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;

    private String videoDownloadUrl;
    private String videoFileName;
    private String videoFallbackDirectUrl;
    private String subredditName;
    private String id;
    private boolean wasPlaying;
    private boolean isDownloading = false;
    private boolean isMute = false;
    private boolean isNSFW;
    private long resumePosition = -1;
    private int videoType;
    private boolean isDataSavingMode;
    private int dataSavingModeDefaultResolution;
    private Integer originalOrientation;
    private int playbackSpeed = 100;
    private boolean useBottomAppBar;
    private ViewVideoActivityBindingAdapter binding;

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
    Provider<StreamableAPI> mStreamableApiProvider;

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

    private Post post;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

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

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setTitle(" ");

        if (typeface != null) {
            binding.getTitleTextView().setTypeface(typeface);
        }

        Resources resources = getResources();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        useBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.USE_BOTTOM_TOOLBAR_IN_MEDIA_VIEWER, false);
        if (useBottomAppBar) {
            getSupportActionBar().hide();
            binding.getBottomAppBar().setVisibility(View.VISIBLE);
            binding.getBackButton().setOnClickListener(view -> {
                finish();
            });

            binding.getDownloadButton().setOnClickListener(view -> {
                if (isDownloading) {
                    return;
                }

                if (videoDownloadUrl == null) {
                    Toast.makeText(this, R.string.fetching_video_info_please_wait, Toast.LENGTH_SHORT).show();
                    return;
                }

                isDownloading = true;
                requestPermissionAndDownload();
            });

            binding.getPlaybackSpeedButton().setOnClickListener(view -> {
                changePlaybackSpeed();
            });
        } else {
            ActionBar actionBar = getSupportActionBar();
            Drawable upArrow = resources.getDrawable(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.transparentActionBarAndExoPlayerControllerColor)));
        }

        String dataSavingModeString = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        int networkType = Utils.getConnectedNetwork(this);
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            isDataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            isDataSavingMode = networkType == Utils.NETWORK_TYPE_CELLULAR;
        }
        dataSavingModeDefaultResolution = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.REDDIT_VIDEO_DEFAULT_RESOLUTION, "360"));

        if (!mSharedPreferences.getBoolean(SharedPreferencesUtils.VIDEO_PLAYER_IGNORE_NAV_BAR, false)) {
            LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
            ViewCompat.setOnApplyWindowInsetsListener(controllerLinearLayout, new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                    params.bottomMargin = navigationBars.bottom;
                    params.rightMargin = navigationBars.right;
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        binding.getRoot().setOnDragDismissedListener(dragDirection -> {
            player.stop();
            int slide = dragDirection == DragDirection.UP ? R.anim.slide_out_up : R.anim.slide_out_down;
            finish();
            overridePendingTransition(0, slide);
        });

        Intent intent = getIntent();
        isNSFW = intent.getBooleanExtra(EXTRA_IS_NSFW, false);
        if (savedInstanceState == null) {
            resumePosition = intent.getLongExtra(EXTRA_PROGRESS_SECONDS, -1);
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

        post = intent.getParcelableExtra(EXTRA_POST);
        if (post != null) {
            binding.getTitleTextView().setText(post.getTitle());
            videoFallbackDirectUrl = post.getVideoFallBackDirectUrl();
        }

        trackSelector = new DefaultTrackSelector(this);
        if (videoType == VIDEO_TYPE_NORMAL && isDataSavingMode && dataSavingModeDefaultResolution > 0) {
            trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                            .setMaxVideoSize(dataSavingModeDefaultResolution, dataSavingModeDefaultResolution));
        }
        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setRenderersFactory(new DefaultRenderersFactory(this).setEnableDecoderFallback(true))
                .build();

        if (zoomable) {
            PlayerControlView playerControlView = findViewById(R.id.player_control_view_view_video_activity);
            playerControlView.addVisibilityListener(visibility -> {
                switch (visibility) {
                    case View.GONE:
                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        break;
                    case View.VISIBLE:
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
                        binding.getRoot().setDragEnabled(true);
                        binding.getNestedScrollView().setScrollEnabled(true);
                    } else {
                        binding.getRoot().setDragEnabled(false);
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
                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        break;
                    case View.VISIBLE:
                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            });
        }

        MaterialButton playPauseButton = findViewById(R.id.exo_play_pause_button_exo_playback_control_view);
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
                    if (videoType == VIDEO_TYPE_NORMAL) {
                        binding.getHdButton().setVisibility(View.VISIBLE);
                        binding.getHdButton().setOnClickListener(view -> {
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
                    }

                    for (Tracks.Group trackGroup : tracks.getGroups()) {
                        if (trackGroup.getType() == C.TRACK_TYPE_AUDIO) {
                            if (videoType == VIDEO_TYPE_NORMAL && trackGroup.length > 1) {
                                // Reddit video HLS usually has two audio tracks. The first is mono.
                                // The second (index 1) is stereo.
                                // Select the stereo audio track if possible.
                                trackSelector.setParameters(
                                        trackSelector.buildUponParameters()
                                                .setOverrideForType(new TrackSelectionOverride(
                                                                trackGroup.getMediaTrackGroup(),
                                                                1
                                                        )
                                                )
                                );
                            }
                            if (binding.getMuteButton().getVisibility() != View.VISIBLE) {
                                binding.getMuteButton().setVisibility(View.VISIBLE);
                                binding.getMuteButton().setOnClickListener(view -> {
                                    if (isMute) {
                                        isMute = false;
                                        player.setVolume(1f);
                                        binding.getMuteButton().setIconResource(R.drawable.ic_unmute_24dp);
                                    } else {
                                        isMute = true;
                                        player.setVolume(0f);
                                        binding.getMuteButton().setIconResource(R.drawable.ic_mute_24dp);
                                    }
                                });
                            }
                        }
                    }
                } else {
                    binding.getMuteButton().setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                loadFallbackVideo(savedInstanceState);
            }
        });

        if (savedInstanceState == null) {
            mVideoUri = intent.getData();
            videoType = getIntent().getIntExtra(EXTRA_VIDEO_TYPE, VIDEO_TYPE_NORMAL);
        } else {
            String videoUrl = savedInstanceState.getString(VIDEO_URI_STATE);
            if (videoUrl != null) {
                mVideoUri = Uri.parse(videoUrl);
            }
            videoType = savedInstanceState.getInt(VIDEO_TYPE_STATE);
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            id = savedInstanceState.getString(ID_STATE);
            playbackSpeed = savedInstanceState.getInt(PLAYBACK_SPEED_STATE);
        }
        setPlaybackSpeed(Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_PLAYBACK_SPEED, "100")));

        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new CacheDataSource.Factory().setCache(mSimpleCache)
                .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).setUserAgent(APIUtils.USER_AGENT));
        String redgifsId = null;
        if (videoType == VIDEO_TYPE_STREAMABLE) {
            if (savedInstanceState != null) {
                videoDownloadUrl = savedInstanceState.getString(VIDEO_DOWNLOAD_URL_STATE);
            } else {
                videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            }

            String shortCode = intent.getStringExtra(EXTRA_STREAMABLE_SHORT_CODE);
            videoFileName = "Streamable-" + shortCode + ".mp4";
        } else if (videoType == VIDEO_TYPE_REDGIFS) {
            if (savedInstanceState != null) {
                videoDownloadUrl = savedInstanceState.getString(VIDEO_DOWNLOAD_URL_STATE);
            } else {
                videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            }

            redgifsId = intent.getStringExtra(EXTRA_REDGIFS_ID);
            if (redgifsId != null && redgifsId.contains("-")) {
                redgifsId = redgifsId.substring(0, redgifsId.indexOf('-'));
            }
            videoFileName = "Redgifs-" + redgifsId + ".mp4";
        } else if (videoType == VIDEO_TYPE_DIRECT || videoType == VIDEO_TYPE_IMGUR) {
            videoDownloadUrl = mVideoUri.toString();
            if (videoType == VIDEO_TYPE_DIRECT) {
                videoFileName = FilenameUtils.getName(videoDownloadUrl);
            } else {
                videoFileName = "Imgur-" + FilenameUtils.getName(videoDownloadUrl);
            }
        } else {
            videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            subredditName = intent.getStringExtra(EXTRA_SUBREDDIT);
            id = intent.getStringExtra(EXTRA_ID);
            videoFileName = subredditName + "-" + id + ".mp4";
        }

        if (mVideoUri == null) {
            binding.getProgressBar().setVisibility(View.VISIBLE);

            VideoLinkFetcher.fetchVideoLink(mExecutor, new Handler(getMainLooper()), mRetrofit, mVReddItRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCurrentAccountSharedPreferences, videoType,
                    redgifsId, getIntent().getStringExtra(EXTRA_V_REDD_IT_URL),
                    intent.getStringExtra(EXTRA_STREAMABLE_SHORT_CODE),
                    new FetchVideoLinkListener() {
                        @Override
                        public void onFetchRedditVideoLinkSuccess(Post post, String fileName) {
                            videoType = VIDEO_TYPE_NORMAL;
                            videoFileName = fileName;

                            binding.getProgressBar().setVisibility(View.GONE);
                            mVideoUri = Uri.parse(post.getVideoUrl());
                            subredditName = post.getSubredditName();
                            id = post.getId();
                            ViewVideoActivity.this.videoDownloadUrl = post.getVideoDownloadUrl();

                            videoFileName = subredditName + "-" + id + ".mp4";
                            // Prepare the player with the source.
                            preparePlayer(savedInstanceState);
                            player.prepare();
                            player.setMediaSource(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                        }

                        @Override
                        public void onFetchImgurVideoLinkSuccess(String videoUrl, String videoDownloadUrl, String fileName) {
                            videoType = VIDEO_TYPE_IMGUR;
                            videoFileName = fileName;

                            binding.getProgressBar().setVisibility(View.GONE);
                            mVideoUri = Uri.parse(videoUrl);
                            ViewVideoActivity.this.videoDownloadUrl = videoDownloadUrl;
                            videoFileName = "Imgur-" + FilenameUtils.getName(videoDownloadUrl);
                            // Prepare the player with the source.
                            player.prepare();
                            player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                            preparePlayer(savedInstanceState);
                        }

                        @Override
                        public void onFetchRedgifsVideoLinkSuccess(String webm, String mp4) {
                            videoType = VIDEO_TYPE_REDGIFS;

                            binding.getProgressBar().setVisibility(View.GONE);
                            mVideoUri = Uri.parse(webm);
                            videoDownloadUrl = mp4;
                            preparePlayer(savedInstanceState);
                            player.prepare();
                            player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                        }

                        @Override
                        public void onFetchStreamableVideoLinkSuccess(StreamableVideo streamableVideo) {
                            videoType = VIDEO_TYPE_STREAMABLE;

                            binding.getProgressBar().setVisibility(View.GONE);
                            if (streamableVideo.mp4 == null && streamableVideo.mp4Mobile == null) {
                                Toast.makeText(ViewVideoActivity.this, R.string.fetch_streamable_video_failed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            binding.getTitleTextView().setText(streamableVideo.title);
                            videoDownloadUrl = streamableVideo.mp4 == null ? streamableVideo.mp4Mobile.url : streamableVideo.mp4.url;
                            mVideoUri = Uri.parse(videoDownloadUrl);
                            preparePlayer(savedInstanceState);
                            player.prepare();
                            player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                        }

                        @Override
                        public void onChangeFileName(String fileName) {
                            videoFileName = fileName;
                        }

                        @Override
                        public void onFetchVideoFallbackDirectUrlSuccess(String videoFallbackDirectUrl) {
                            ViewVideoActivity.this.videoFallbackDirectUrl = videoFallbackDirectUrl;
                        }

                        @Override
                        public void failed(@Nullable Integer messageRes) {
                            binding.getProgressBar().setVisibility(View.GONE);
                            if (videoType == VIDEO_TYPE_V_REDD_IT) {
                                if (messageRes != null) {
                                    Toast.makeText(ViewVideoActivity.this, messageRes, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                loadFallbackVideo(savedInstanceState);
                            }
                        }
                    });
        } else {
            if (videoType == VIDEO_TYPE_NORMAL) {
                // Prepare the player with the source.
                player.prepare();
                player.setMediaSource(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                preparePlayer(savedInstanceState);
            } else {
                // Prepare the player with the source.
                player.prepare();
                player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                preparePlayer(savedInstanceState);
            }
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                player.stop();
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void preparePlayer(Bundle savedInstanceState) {
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.LOOP_VIDEO, true)) {
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
        } else {
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
        }
        if (resumePosition > 0) {
            player.seekTo(resumePosition);
        }
        player.setPlayWhenReady(true);
        wasPlaying = true;

        boolean muteVideo = mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_VIDEO, false) ||
                (mSharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_NSFW_VIDEO, false) && isNSFW);

        if (savedInstanceState != null) {
            isMute = savedInstanceState.getBoolean(IS_MUTE_STATE);
            if (isMute) {
                player.setVolume(0f);
                binding.getMuteButton().setIconResource(R.drawable.ic_mute_24dp);
            } else {
                player.setVolume(1f);
                binding.getMuteButton().setIconResource(R.drawable.ic_unmute_24dp);
            }
        } else if (muteVideo) {
            isMute = true;
            player.setVolume(0f);
            binding.getMuteButton().setIconResource(R.drawable.ic_mute_24dp);
        } else {
            binding.getMuteButton().setIconResource(R.drawable.ic_unmute_24dp);
        }
    }

    private void changePlaybackSpeed() {
        PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, playbackSpeed);
        playbackSpeedBottomSheetFragment.setArguments(bundle);
        playbackSpeedBottomSheetFragment.show(getSupportFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
    }

    @OptIn(markerClass = UnstableApi.class)
    private int inferPrimaryTrackType(Format format) {
        int trackType = MimeTypes.getTrackType(format.sampleMimeType);
        if (trackType != C.TRACK_TYPE_UNKNOWN) {
            return trackType;
        }
        if (MimeTypes.getVideoMediaMimeType(format.codecs) != null) {
            return C.TRACK_TYPE_VIDEO;
        }
        if (MimeTypes.getAudioMediaMimeType(format.codecs) != null) {
            return C.TRACK_TYPE_AUDIO;
        }
        if (format.width != Format.NO_VALUE || format.height != Format.NO_VALUE) {
            return C.TRACK_TYPE_VIDEO;
        }
        if (format.channelCount != Format.NO_VALUE || format.sampleRate != Format.NO_VALUE) {
            return C.TRACK_TYPE_AUDIO;
        }
        return C.TRACK_TYPE_UNKNOWN;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadFallbackVideo(Bundle savedInstanceState) {
        if (videoFallbackDirectUrl != null) {
            MediaItem mediaItem = player.getCurrentMediaItem();
            if (mediaItem == null || (mediaItem.localConfiguration != null && !videoFallbackDirectUrl.equals(mediaItem.localConfiguration.uri.toString()))) {
                videoType = VIDEO_TYPE_DIRECT;
                videoDownloadUrl = videoFallbackDirectUrl;
                mVideoUri = Uri.parse(videoFallbackDirectUrl);
                videoFileName = videoFileName == null ? FilenameUtils.getName(videoDownloadUrl) : videoFileName;
                player.prepare();
                player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mVideoUri)));
                preparePlayer(savedInstanceState);
            }
        }
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
            if (isDownloading) {
                return false;
            }

            if (videoDownloadUrl == null) {
                Toast.makeText(this, R.string.fetching_video_info_please_wait, Toast.LENGTH_SHORT).show();
                return true;
            }

            isDownloading = true;
            requestPermissionAndDownload();
            return true;
        } else if (itemId == R.id.action_playback_speed_view_video_activity) {
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
        if (wasPlaying) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasPlaying = player.getPlayWhenReady();
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
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                download();
            }
            isDownloading = false;
        }
    }

    private void download() {
        isDownloading = false;

        if (videoType != VIDEO_TYPE_NORMAL) {
            PersistableBundle extras = new PersistableBundle();
            if (post.getPostType() == Post.GIF_TYPE) {
                extras.putString(DownloadMediaService.EXTRA_URL, post.getVideoUrl());
                extras.putInt(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_GIF);
                extras.putString(DownloadMediaService.EXTRA_FILE_NAME, post.getSubredditName()
                        + "-" + post.getId() + ".gif");
            } else {
                extras.putString(DownloadMediaService.EXTRA_URL, videoDownloadUrl);
                extras.putInt(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
                extras.putString(DownloadMediaService.EXTRA_FILE_NAME, videoFileName);
            }

            extras.putString(DownloadMediaService.EXTRA_SUBREDDIT_NAME, subredditName);
            extras.putInt(DownloadMediaService.EXTRA_IS_NSFW, isNSFW ? 1 : 0);

            //TODO: contentEstimatedBytes
            JobInfo jobInfo = DownloadMediaService.constructJobInfo(this, 5000000, extras);
            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);
        } else {
            PersistableBundle extras = new PersistableBundle();
            extras.putString(DownloadRedditVideoService.EXTRA_VIDEO_URL, videoDownloadUrl);
            extras.putString(DownloadRedditVideoService.EXTRA_POST_ID, id);
            extras.putString(DownloadRedditVideoService.EXTRA_SUBREDDIT, subredditName);
            extras.putInt(DownloadRedditVideoService.EXTRA_IS_NSFW, isNSFW ? 1 : 0);

            //TODO: contentEstimatedBytes
            JobInfo jobInfo = DownloadRedditVideoService.constructJobInfo(this, 5000000, extras);
            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);
        }

        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MUTE_STATE, isMute);
        outState.putInt(VIDEO_TYPE_STATE, videoType);
        if (mVideoUri != null) {
            outState.putString(VIDEO_URI_STATE, mVideoUri.toString());
            outState.putString(VIDEO_DOWNLOAD_URL_STATE, videoDownloadUrl);
            outState.putString(SUBREDDIT_NAME_STATE, subredditName);
            outState.putString(ID_STATE, id);
        }
        outState.putInt(PLAYBACK_SPEED_STATE, playbackSpeed);
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }
}
