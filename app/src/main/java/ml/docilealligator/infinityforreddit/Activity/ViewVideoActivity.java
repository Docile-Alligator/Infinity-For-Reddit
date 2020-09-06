package ml.docilealligator.infinityforreddit.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.thefuntasty.hauler.DragDirection;
import com.thefuntasty.hauler.HaulerView;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.FetchGfycatOrRedgifsVideoLinks;
import ml.docilealligator.infinityforreddit.Font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.Font.FontFamily;
import ml.docilealligator.infinityforreddit.Font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Service.DownloadMediaService;
import ml.docilealligator.infinityforreddit.Service.DownloadRedditVideoService;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class ViewVideoActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_DOWNLOAD_URL = "EVDU";
    public static final String EXTRA_SUBREDDIT = "ES";
    public static final String EXTRA_ID = "EI";
    public static final String EXTRA_POST_TITLE = "EPT";
    public static final String EXTRA_PROGRESS_SECONDS = "EPS";
    public static final String EXTRA_VIDEO_TYPE = "EVT";
    public static final String EXTRA_GFYCAT_ID = "EGI";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final int VIDEO_TYPE_DIRECT = 3;
    public static final int VIDEO_TYPE_REDGIFS = 2;
    public static final int VIDEO_TYPE_GFYCAT = 1;
    private static final int VIDEO_TYPE_NORMAL = 0;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final String IS_MUTE_STATE = "IMS";
    private static final String VIDEO_DOWNLOAD_URL_STATE = "VDUS";
    private static final String VIDEO_URI_STATE = "VUS";
    @BindView(R.id.hauler_view_view_video_activity)
    HaulerView haulerView;
    @BindView(R.id.coordinator_layout_view_video_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.progress_bar_view_video_activity)
    ProgressBar progressBar;
    @BindView(R.id.player_view_view_video_activity)
    PlayerView videoPlayerView;
    @BindView(R.id.mute_exo_playback_control_view)
    ImageButton muteButton;

    private Uri mVideoUri;
    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;

    private String videoDownloadUrl;
    private String videoFileName;
    private String subredditName;
    private String id;
    private boolean wasPlaying;
    private boolean isDownloading = false;
    private boolean isMute = false;
    private String postTitle;
    private boolean isNSFW;
    private long resumePosition = -1;
    private int videoType;

    @Inject
    @Named("gfycat")
    Retrofit gfycatRetrofit;

    @Inject
    @Named("redgifs")
    Retrofit redgifsRetrofit;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        getTheme().applyStyle(R.style.Theme_Normal, true);

        getTheme().applyStyle(FontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        setContentView(R.layout.activity_view_video);

        ButterKnife.bind(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparentActionBarAndExoPlayerControllerColor)));

        if (!mSharedPreferences.getBoolean(SharedPreferencesUtils.VIDEO_PLAYER_IGNORE_NAV_BAR, false)) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || getResources().getBoolean(R.bool.isTablet)) {
                //Set player controller bottom margin in order to display it above the navbar
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.bottomMargin = getResources().getDimensionPixelSize(resourceId);
            } else {
                //Set player controller right margin in order to display it above the navbar
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) controllerLinearLayout.getLayoutParams();
                params.rightMargin = getResources().getDimensionPixelSize(resourceId);
            }
        }

        haulerView.setOnDragDismissedListener(dragDirection -> {
            int slide = dragDirection == DragDirection.UP ? R.anim.slide_out_up : R.anim.slide_out_down;
            finish();
            overridePendingTransition(0, slide);
        });

        Intent intent = getIntent();
        mVideoUri = intent.getData();
        postTitle = intent.getStringExtra(EXTRA_POST_TITLE);
        isNSFW = intent.getBooleanExtra(EXTRA_IS_NSFW, false);
        if (savedInstanceState == null) {
            resumePosition = intent.getLongExtra(EXTRA_PROGRESS_SECONDS, -1);
        }


        if (postTitle != null) {
            setTitle(Html.fromHtml(String.format("<small>%s</small>", postTitle)));
        } else {
            setTitle("");
        }

        videoPlayerView.setControllerVisibilityListener(visibility -> {
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
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        videoPlayerView.setPlayer(player);
        videoType = getIntent().getIntExtra(EXTRA_VIDEO_TYPE, VIDEO_TYPE_NORMAL);
        if (videoType == VIDEO_TYPE_GFYCAT || videoType == VIDEO_TYPE_REDGIFS) {
            if (savedInstanceState != null) {
                String videoUrl = savedInstanceState.getString(VIDEO_URI_STATE);
                if (videoUrl != null) {
                    mVideoUri = Uri.parse(videoUrl);
                    videoDownloadUrl = savedInstanceState.getString(VIDEO_DOWNLOAD_URL_STATE);
                }
            }

            if (mVideoUri == null) {
                String gfycatId = intent.getStringExtra(EXTRA_GFYCAT_ID);
                if (gfycatId != null && gfycatId.contains("-")) {
                    gfycatId = gfycatId.substring(0, gfycatId.indexOf('-'));
                }
                if (videoType == VIDEO_TYPE_GFYCAT) {
                    videoFileName = "Gfycat-" + gfycatId + ".mp4";
                    loadGfycatOrRedgifsVideo(gfycatRetrofit, gfycatId, savedInstanceState, true);
                } else {
                    videoFileName = "Redgifs-" + gfycatId + ".mp4";
                    loadGfycatOrRedgifsVideo(redgifsRetrofit, gfycatId, savedInstanceState, false);
                }
            } else {
                dataSourceFactory = new DefaultDataSourceFactory(ViewVideoActivity.this,
                        Util.getUserAgent(ViewVideoActivity.this, "Infinity"));
                player.prepare(new DashMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                preparePlayer(savedInstanceState);
            }
        } else if (videoType == VIDEO_TYPE_DIRECT) {
            videoDownloadUrl = mVideoUri.toString();
            videoFileName = videoDownloadUrl.substring(videoDownloadUrl.lastIndexOf('/') + 1);
            // Produces DataSource instances through which media data is loaded.
            dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Infinity"));
            // Prepare the player with the source.
            player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
        } else {
            videoDownloadUrl = intent.getStringExtra(EXTRA_VIDEO_DOWNLOAD_URL);
            subredditName = intent.getStringExtra(EXTRA_SUBREDDIT);
            id = intent.getStringExtra(EXTRA_ID);
            videoFileName = subredditName + "-" + id + ".mp4";
            // Produces DataSource instances through which media data is loaded.
            dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Infinity"));
            // Prepare the player with the source.
            player.prepare(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
        }

        preparePlayer(savedInstanceState);
    }

    private void preparePlayer(Bundle savedInstanceState) {
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
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
        FetchGfycatOrRedgifsVideoLinks.fetchGfycatOrRedgifsVideoLinks(retrofit, gfycatId,
                new FetchGfycatOrRedgifsVideoLinks.FetchGfycatOrRedgifsVideoLinksListener() {
                    @Override
                    public void success(String webm, String mp4) {
                        progressBar.setVisibility(View.GONE);
                        mVideoUri = Uri.parse(webm);
                        videoDownloadUrl = mp4;
                        dataSourceFactory = new DefaultDataSourceFactory(ViewVideoActivity.this,
                                Util.getUserAgent(ViewVideoActivity.this, "Infinity"));
                        player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mVideoUri));
                        preparePlayer(savedInstanceState);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_video, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download_view_video_activity:
                if (isDownloading) {
                    return false;
                }

                if (videoDownloadUrl == null) {
                    Toast.makeText(this, R.string.fetching_video_info_please_wait, Toast.LENGTH_SHORT).show();
                    return true;
                }

                isDownloading = true;
                if (Build.VERSION.SDK_INT >= 23) {
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
                return true;
        }

        return false;
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
            Intent intent = new Intent(this, DownloadMediaService.class);
            intent.putExtra(DownloadMediaService.EXTRA_URL, videoDownloadUrl);
            intent.putExtra(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
            intent.putExtra(DownloadMediaService.EXTRA_FILE_NAME, videoFileName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            Intent intent = new Intent(this, DownloadRedditVideoService.class);
            intent.putExtra(DownloadRedditVideoService.EXTRA_VIDEO_URL, videoDownloadUrl);
            intent.putExtra(DownloadRedditVideoService.EXTRA_POST_ID, id);
            intent.putExtra(DownloadRedditVideoService.EXTRA_SUBREDDIT, subredditName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MUTE_STATE, isMute);
        if (mVideoUri != null) {
            outState.putString(VIDEO_URI_STATE, mVideoUri.toString());
            outState.putString(VIDEO_DOWNLOAD_URL_STATE, videoDownloadUrl);
        }
    }
}
