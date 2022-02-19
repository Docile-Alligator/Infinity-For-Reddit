package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.Manifest;
import android.app.AlertDialog;
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
import android.provider.Settings;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;
import com.otaliastudios.zoom.ZoomEngine;
import com.otaliastudios.zoom.ZoomSurfaceView;

import org.apache.commons.io.FilenameUtils;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import app.futured.hauler.DragDirection;
import app.futured.hauler.HaulerView;
import app.futured.hauler.LockableNestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.FetchGfycatOrRedgifsVideoLinks;
import ml.docilealligator.infinityforreddit.FetchStreamableVideo;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.StreamableVideo;
import ml.docilealligator.infinityforreddit.apis.VReddIt;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PlaybackSpeedBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.post.FetchPost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.services.DownloadRedditVideoService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
    public static final String EXTRA_POST_TITLE = "EPT";
    public static final String EXTRA_PROGRESS_SECONDS = "EPS";
    public static final String EXTRA_VIDEO_TYPE = "EVT";
    public static final String EXTRA_GFYCAT_ID = "EGI";
    public static final String EXTRA_V_REDD_IT_URL = "EVRIU";
    public static final String EXTRA_STREAMABLE_SHORT_CODE = "ESSC";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final int VIDEO_TYPE_IMGUR = 7;
    public static final int VIDEO_TYPE_STREAMABLE = 5;
    public static final int VIDEO_TYPE_V_REDD_IT = 4;
    public static final int VIDEO_TYPE_DIRECT = 3;
    public static final int VIDEO_TYPE_REDGIFS = 2;
    public static final int VIDEO_TYPE_GFYCAT = 1;
    private static final int VIDEO_TYPE_NORMAL = 0;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private static final String IS_MUTE_STATE = "IMS";
    private static final String VIDEO_DOWNLOAD_URL_STATE = "VDUS";
    private static final String VIDEO_URI_STATE = "VUS";
    private static final String VIDEO_TYPE_STATE = "VTS";
    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String ID_STATE=  "IS";
    private static final String PLAYBACK_SPEED_STATE = "PSS";

    @BindView(R.id.hauler_view_view_video_activity)
    HaulerView haulerView;
    @BindView(R.id.coordinator_layout_view_video_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.progress_bar_view_video_activity)
    ProgressBar progressBar;
    @BindView(R.id.zoom_surface_view_view_video_activity)
    ZoomSurfaceView zoomSurfaceView;
    @BindView(R.id.player_control_view_view_video_activity)
    PlayerControlView playerControlView;
    @BindView(R.id.mute_exo_playback_control_view)
    ImageButton muteButton;
    @BindView(R.id.hd_exo_playback_control_view)
    ImageButton hdButton;
    @BindView(R.id.bottom_navigation_exo_playback_control_view)
    BottomAppBar bottomAppBar;
    @BindView(R.id.title_text_view_exo_playback_control_view)
    TextView titleTextView;
    @BindView(R.id.download_image_view_exo_playback_control_view)
    ImageView downloadImageView;
    @BindView(R.id.playback_speed_image_view_exo_playback_control_view)
    ImageView playbackSpeedImageView;
    @BindView(R.id.lockable_nested_scroll_view_view_video_activity)
    LockableNestedScrollView nestedScrollView;

    public Typeface typeface;

    private Uri mVideoUri;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;

    private String videoDownloadUrl;
    private String videoFileName;
    private String subredditName;
    private String id;
    private boolean wasPlaying;
    private boolean isDownloading = false;
    private boolean isMute = false;
    private boolean isNSFW;
    private long resumePosition = -1;
    private int videoType;
    private boolean isDataSavingMode;
    private boolean isHd;
    private Integer originalOrientation;
    private int playbackSpeed = 100;
    private boolean useBottomAppBar;

    @Inject
    @Named("no_oauth")
    Retrofit retrofit;

    @Inject
    @Named("gfycat")
    Retrofit gfycatRetrofit;

    @Inject
    @Named("redgifs")
    Retrofit redgifsRetrofit;

    @Inject
    @Named("vReddIt")
    Retrofit vReddItRetrofit;

    @Inject
    @Named("streamable")
    Retrofit streamableRetrofit;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Inject
    Executor mExecutor;

    @Inject
    SimpleCache mSimpleCache;

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

        setContentView(R.layout.activity_view_video);

        ButterKnife.bind(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Resources resources = getResources();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        useBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.USE_BOTTOM_TOOLBAR_IN_MEDIA_VIEWER, false);
        if (useBottomAppBar) {
            getSupportActionBar().hide();
            bottomAppBar.setVisibility(View.VISIBLE);
            downloadImageView.setOnClickListener(view -> {
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

            playbackSpeedImageView.setOnClickListener(view -> {
                PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, playbackSpeed);
                playbackSpeedBottomSheetFragment.setArguments(bundle);
                playbackSpeedBottomSheetFragment.show(getSupportFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
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
        isHd = !isDataSavingMode;

        if (!mSharedPreferences.getBoolean(SharedPreferencesUtils.VIDEO_PLAYER_IGNORE_NAV_BAR, false)) {
            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                //Set player controller bottom margin in order to display it above the navbar
                int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.bottomMargin = resources.getDimensionPixelSize(resourceId);
            } else {
                //Set player controller right margin in order to display it above the navbar
                int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.rightMargin = resources.getDimensionPixelSize(resourceId);
            }
        }

        haulerView.setOnDragDismissedListener(dragDirection -> {
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

        String postTitle = intent.getStringExtra(EXTRA_POST_TITLE);
        setSmallTitle(postTitle);

        playerControlView.setVisibilityListener(visibility -> {
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

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        playerControlView.setPlayer(player);

        player.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                zoomSurfaceView.setContentSize(width, height);
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
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.PINCH_TO_ZOOM_VIDEO, false)) {
            zoomSurfaceView.getEngine().addListener(new ZoomEngine.Listener() {
                @Override
                public void onUpdate(@NonNull ZoomEngine zoomEngine, @NonNull Matrix matrix) {
                    if (zoomEngine.getZoom() < 1.00001) {
                        haulerView.setDragEnabled(true);
                        nestedScrollView.setScrollEnabled(true);
                    } else {
                        haulerView.setDragEnabled(false);
                        nestedScrollView.setScrollEnabled(false);
                    }
                }

                @Override
                public void onIdle(@NonNull ZoomEngine zoomEngine) {

                }
            });
        } else {
            zoomSurfaceView.setZoomEnabled(false);
        }
        zoomSurfaceView.setOnClickListener(view -> {
            if (playerControlView.isVisible()) {
                playerControlView.hide();
            } else {
                playerControlView.show();
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

        if (videoType == VIDEO_TYPE_STREAMABLE) {
            if (savedInstanceState != null) {
                videoDownloadUrl = savedInstanceState.getString(VIDEO_DOWNLOAD_URL_STATE);
            } else {
                videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            }

            String shortCode = intent.getStringExtra(EXTRA_STREAMABLE_SHORT_CODE);
            videoFileName = "Streamable-" + shortCode + ".mp4";
            if (mVideoUri == null) {
                loadStreamableVideo(shortCode, savedInstanceState);
            } else {
                dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                        new DefaultDataSourceFactory(ViewVideoActivity.this,
                                Util.getUserAgent(ViewVideoActivity.this, "Infinity")));
                player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                preparePlayer(savedInstanceState);
            }
        } else if (videoType == VIDEO_TYPE_V_REDD_IT) {
            loadVReddItVideo(savedInstanceState);
        } else if (videoType == VIDEO_TYPE_GFYCAT || videoType == VIDEO_TYPE_REDGIFS) {
            if (savedInstanceState != null) {
                videoDownloadUrl = savedInstanceState.getString(VIDEO_DOWNLOAD_URL_STATE);
            } else {
                videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            }

            String gfycatId = intent.getStringExtra(EXTRA_GFYCAT_ID);
            if (gfycatId != null && gfycatId.contains("-")) {
                gfycatId = gfycatId.substring(0, gfycatId.indexOf('-'));
            }
            if (videoType == VIDEO_TYPE_GFYCAT) {
                videoFileName = "Gfycat-" + gfycatId + ".mp4";
            } else {
                videoFileName = "Redgifs-" + gfycatId + ".mp4";
            }

            if (mVideoUri == null) {
                if (videoType == VIDEO_TYPE_GFYCAT) {
                    loadGfycatOrRedgifsVideo(gfycatRetrofit, gfycatId, savedInstanceState, true);
                } else {
                    loadGfycatOrRedgifsVideo(redgifsRetrofit, gfycatId, savedInstanceState, false);
                }
            } else {
                dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                        new DefaultDataSourceFactory(ViewVideoActivity.this,
                        Util.getUserAgent(ViewVideoActivity.this, "Infinity")));
                player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                preparePlayer(savedInstanceState);
            }
        } else if (videoType == VIDEO_TYPE_DIRECT || videoType == VIDEO_TYPE_IMGUR) {
            videoDownloadUrl = mVideoUri.toString();
            if (videoType == VIDEO_TYPE_DIRECT) {
                videoFileName = FilenameUtils.getName(videoDownloadUrl);
            } else {
                videoFileName = "imgur-" + FilenameUtils.getName(videoDownloadUrl);
            }
            // Produces DataSource instances through which media data is loaded.
            dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                    new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Infinity")));
            // Prepare the player with the source.
            player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
            preparePlayer(savedInstanceState);
        } else {
            videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            subredditName = intent.getStringExtra(EXTRA_SUBREDDIT);
            id = intent.getStringExtra(EXTRA_ID);
            videoFileName = subredditName + "-" + id + ".mp4";
            // Produces DataSource instances through which media data is loaded.
            dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                    new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Infinity")));
            // Prepare the player with the source.
            player.prepare(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
            preparePlayer(savedInstanceState);
        }
    }

    private void setSmallTitle(String title) {
        if (title != null) {
            if (useBottomAppBar) {
                titleTextView.setText(Html.fromHtml(String.format("<font color=\"#FFFFFF\"><small>%s</small></font>", title)));
            } else {
                setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml(String.format("<font color=\"#FFFFFF\"><small>%s</small></font>", title))));
            }
        } else {
            if (!useBottomAppBar) {
                setTitle("");
            }
        }
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
                    if (videoType == VIDEO_TYPE_NORMAL) {
                        if (isDataSavingMode) {
                            trackSelector.setParameters(
                                    trackSelector.buildUponParameters()
                                            .setMaxVideoSize(720, 720));
                        }

                        hdButton.setVisibility(View.VISIBLE);
                        hdButton.setOnClickListener(view -> {
                            TrackSelectionDialogBuilder builder = new TrackSelectionDialogBuilder(ViewVideoActivity.this, getString(R.string.select_video_quality), trackSelector, 0);
                            builder.setShowDisableOption(true);
                            builder.setAllowAdaptiveSelections(false);
                            AlertDialog alertDialog = builder.build();
                            alertDialog.show();
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
                        });
                    }

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

    private void loadGfycatOrRedgifsVideo(Retrofit retrofit, String gfycatId, Bundle savedInstanceState, boolean needErrorHandling) {
        progressBar.setVisibility(View.VISIBLE);
        FetchGfycatOrRedgifsVideoLinks.fetchGfycatOrRedgifsVideoLinks(mExecutor, new Handler(), retrofit, gfycatId,
                new FetchGfycatOrRedgifsVideoLinks.FetchGfycatOrRedgifsVideoLinksListener() {
                    @Override
                    public void success(String webm, String mp4) {
                        progressBar.setVisibility(View.GONE);
                        mVideoUri = Uri.parse(webm);
                        videoDownloadUrl = mp4;
                        dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                                new DefaultDataSourceFactory(ViewVideoActivity.this,
                                Util.getUserAgent(ViewVideoActivity.this, "Infinity")));
                        preparePlayer(savedInstanceState);
                        player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                    }

                    @Override
                    public void failed(int errorCode) {
                        progressBar.setVisibility(View.GONE);
                        if (videoType == VIDEO_TYPE_GFYCAT) {
                            if (errorCode == 404 && needErrorHandling) {
                                if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AUTOMATICALLY_TRY_REDGIFS, true)) {
                                    loadGfycatOrRedgifsVideo(redgifsRetrofit, gfycatId, savedInstanceState, false);
                                } else {
                                    Snackbar.make(coordinatorLayout, R.string.load_video_in_redgifs, Snackbar.LENGTH_INDEFINITE).setAction(R.string.yes,
                                            view -> loadGfycatOrRedgifsVideo(redgifsRetrofit, gfycatId, savedInstanceState, false)).show();
                                }
                            } else {
                                Toast.makeText(ViewVideoActivity.this, R.string.fetch_gfycat_video_failed, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ViewVideoActivity.this, R.string.fetch_redgifs_video_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadVReddItVideo(Bundle savedInstanceState) {
        progressBar.setVisibility(View.VISIBLE);
        vReddItRetrofit.create(VReddIt.class).getRedirectUrl(getIntent().getStringExtra(EXTRA_V_REDD_IT_URL)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Uri redirectUri = Uri.parse(response.raw().request().url().toString());
                String redirectPath = redirectUri.getPath();
                if (redirectPath != null && (redirectPath.matches("/r/\\w+/comments/\\w+/?\\w+/?") || redirectPath.matches("/user/\\w+/comments/\\w+/?\\w+/?"))) {
                    List<String> segments = redirectUri.getPathSegments();
                    int commentsIndex = segments.lastIndexOf("comments");
                    String postId = segments.get(commentsIndex + 1);
                    FetchPost.fetchPost(mExecutor, new Handler(), retrofit, postId, null,
                            new FetchPost.FetchPostListener() {
                                @Override
                                public void fetchPostSuccess(Post post) {
                                    if (post.isGfycat()) {
                                        videoType = VIDEO_TYPE_GFYCAT;
                                        String gfycatId = post.getGfycatId();
                                        if (gfycatId != null && gfycatId.contains("-")) {
                                            gfycatId = gfycatId.substring(0, gfycatId.indexOf('-'));
                                        }
                                        if (videoType == VIDEO_TYPE_GFYCAT) {
                                            videoFileName = "Gfycat-" + gfycatId + ".mp4";
                                        } else {
                                            videoFileName = "Redgifs-" + gfycatId + ".mp4";
                                        }
                                        loadGfycatOrRedgifsVideo(gfycatRetrofit, gfycatId, savedInstanceState, true);
                                    } else if (post.isRedgifs()) {
                                        videoType = VIDEO_TYPE_REDGIFS;
                                        String gfycatId = post.getGfycatId();
                                        if (gfycatId != null && gfycatId.contains("-")) {
                                            gfycatId = gfycatId.substring(0, gfycatId.indexOf('-'));
                                        }
                                        if (videoType == VIDEO_TYPE_GFYCAT) {
                                            videoFileName = "Gfycat-" + gfycatId + ".mp4";
                                        } else {
                                            videoFileName = "Redgifs-" + gfycatId + ".mp4";
                                        }
                                        loadGfycatOrRedgifsVideo(redgifsRetrofit, gfycatId, savedInstanceState, false);
                                    } else if (post.isStreamable()) {
                                        videoType = VIDEO_TYPE_STREAMABLE;
                                        String shortCode = post.getStreamableShortCode();
                                        videoFileName = "Streamable-" + shortCode + ".mp4";
                                        loadStreamableVideo(shortCode, savedInstanceState);
                                    } else if (post.isImgur()) {
                                        mVideoUri = Uri.parse(post.getVideoUrl());
                                        videoDownloadUrl = post.getVideoDownloadUrl();
                                        videoType = VIDEO_TYPE_IMGUR;
                                        videoFileName = "imgur-" + FilenameUtils.getName(videoDownloadUrl);
                                        // Produces DataSource instances through which media data is loaded.
                                        dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                                                new DefaultHttpDataSourceFactory(Util.getUserAgent(ViewVideoActivity.this, "Infinity")));
                                        // Prepare the player with the source.
                                        player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                                        preparePlayer(savedInstanceState);
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        if (post.getVideoUrl() != null) {
                                            mVideoUri = Uri.parse(post.getVideoUrl());
                                            subredditName = post.getSubredditName();
                                            id = post.getId();
                                            videoDownloadUrl = post.getVideoDownloadUrl();

                                            videoFileName = subredditName + "-" + id + ".mp4";
                                            // Produces DataSource instances through which media data is loaded.
                                            dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                                                    new DefaultHttpDataSourceFactory(
                                                            Util.getUserAgent(ViewVideoActivity.this,
                                                                    "Infinity")));
                                            // Prepare the player with the source.
                                            preparePlayer(savedInstanceState);
                                            player.prepare(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                                        } else {
                                            Toast.makeText(ViewVideoActivity.this, R.string.error_fetching_v_redd_it_video_cannot_get_video_url, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void fetchPostFailed() {
                                    Toast.makeText(ViewVideoActivity.this, R.string.error_fetching_v_redd_it_video_cannot_get_post, Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(ViewVideoActivity.this, R.string.error_fetching_v_redd_it_video_cannot_get_post_id, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ViewVideoActivity.this, R.string.error_fetching_v_redd_it_video_cannot_get_redirect_url, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadStreamableVideo(String shortCode, Bundle savedInstanceState) {
        progressBar.setVisibility(View.VISIBLE);
        FetchStreamableVideo.fetchStreamableVideo(mExecutor, new Handler(), streamableRetrofit, shortCode,
                new FetchStreamableVideo.FetchStreamableVideoListener() {
                    @Override
                    public void success(StreamableVideo streamableVideo) {
                        if (streamableVideo.mp4 == null && streamableVideo.mp4Mobile == null) {
                            Toast.makeText(ViewVideoActivity.this, R.string.fetch_streamable_video_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        setSmallTitle(streamableVideo.title);
                        progressBar.setVisibility(View.GONE);
                        videoDownloadUrl = streamableVideo.mp4 == null ? streamableVideo.mp4Mobile.url : streamableVideo.mp4.url;
                        mVideoUri = Uri.parse(videoDownloadUrl);
                        dataSourceFactory = new CacheDataSourceFactory(mSimpleCache,
                                new DefaultDataSourceFactory(ViewVideoActivity.this,
                                        Util.getUserAgent(ViewVideoActivity.this, "Infinity")));
                        preparePlayer(savedInstanceState);
                        player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                    }

                    @Override
                    public void failed() {
                        Toast.makeText(ViewVideoActivity.this, R.string.fetch_streamable_video_failed, Toast.LENGTH_SHORT).show();
                    }
                });
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
        player.stop(true);
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
            PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, playbackSpeed);
            playbackSpeedBottomSheetFragment.setArguments(bundle);
            playbackSpeedBottomSheetFragment.show(getSupportFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
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

        Intent intent;
        if (videoType != VIDEO_TYPE_NORMAL) {
            intent = new Intent(this, DownloadMediaService.class);
            intent.putExtra(DownloadMediaService.EXTRA_URL, videoDownloadUrl);
            intent.putExtra(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
            intent.putExtra(DownloadMediaService.EXTRA_FILE_NAME, videoFileName);
            intent.putExtra(DownloadMediaService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(DownloadMediaService.EXTRA_IS_NSFW, isNSFW);
        } else {
            intent = new Intent(this, DownloadRedditVideoService.class);
            intent.putExtra(DownloadRedditVideoService.EXTRA_VIDEO_URL, videoDownloadUrl);
            intent.putExtra(DownloadRedditVideoService.EXTRA_POST_ID, id);
            intent.putExtra(DownloadRedditVideoService.EXTRA_SUBREDDIT, subredditName);
            intent.putExtra(DownloadRedditVideoService.EXTRA_IS_NSFW, isNSFW);
        }
        ContextCompat.startForegroundService(this, intent);
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
