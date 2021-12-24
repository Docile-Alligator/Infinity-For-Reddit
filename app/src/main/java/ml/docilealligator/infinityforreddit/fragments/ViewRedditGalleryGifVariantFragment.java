package ml.docilealligator.infinityforreddit.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
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
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PlaybackSpeedBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ViewRedditGalleryGifVariantFragment extends Fragment {

    public static final String EXTRA_REDDIT_GALLERY_VIDEO = "EIV";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_INDEX = "EI";
    public static final String EXTRA_MEDIA_COUNT = "EMC";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final String POSITION_STATE = "PS";
    private static final String PLAYBACK_SPEED_STATE = "PSS";
    private final boolean isMute = false;
    @BindView(R.id.player_view_view_reddit_gallery_gif_variant_fragment)
    PlayerView videoPlayerView;
    @BindView(R.id.bottom_navigation_exo_playback_gif_variant_view)
    BottomAppBar bottomAppBar;
    @BindView(R.id.caption_layout_view_reddit_gallery_gif_variant_fragment)
    LinearLayout captionLayout;
    @BindView(R.id.caption_text_view_view_reddit_gallery_gif_variant_fragment)
    TextView captionTextView;
    @BindView(R.id.caption_url_text_view_view_reddit_gallery_gif_variant_fragment)
    TextView captionUrlTextView;
    @BindView(R.id.bottom_app_bar_menu_view_reddit_gallery_gif_variant_fragment)
    LinearLayout bottomAppBarMenu;
    @BindView(R.id.title_text_view_exo_playback_gif_variant_view)
    TextView titleTextView;
    @BindView(R.id.bottom_action_menu_view_reddit_gallery_gif_variant_fragment)
    ActionMenuView bottomActionMenu;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    private ViewRedditGalleryActivity activity;
    private Post.Gallery galleryVideo;
    private String subredditName;
    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private boolean wasPlaying = false;
    private boolean isDownloading = false;
    private int playbackSpeed = 100;
    private boolean downloadGif = false;
    private String gifFileName;
    private boolean isActionBarHidden = false;
    private boolean isUseBottomCaption = false;

    public ViewRedditGalleryGifVariantFragment() {
        // Required empty public constructor
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_reddit_gallery_gif_variant, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        galleryVideo = getArguments().getParcelable(EXTRA_REDDIT_GALLERY_VIDEO);
        subredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);

        videoPlayerView.getVideoSurfaceView().setOnClickListener(view -> {
            if (isActionBarHidden) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                isActionBarHidden = false;
                if (activity.isUseBottomAppBar() || isUseBottomCaption) {
                    bottomAppBar.setVisibility(View.VISIBLE);
                }
            } else {
                hideAppBar();
            }
        });

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector);
        videoPlayerView.setPlayer(player);
        dataSourceFactory = new DefaultDataSourceFactory(activity,
                Util.getUserAgent(activity, "Infinity"));
        player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(galleryVideo.url)));

        if (savedInstanceState != null) {
            playbackSpeed = savedInstanceState.getInt(PLAYBACK_SPEED_STATE);
        }
        Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_PLAYBACK_SPEED, "100"));
        preparePlayer(savedInstanceState);

        captionLayout.setOnClickListener(view -> hideAppBar());

        if (activity.isUseBottomAppBar()) {
            onCreateOptionsMenu(bottomActionMenu.getMenu(), activity.getMenuInflater());
            bottomActionMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
            bottomAppBar.setVisibility(View.VISIBLE);
            titleTextView.setText(getString(R.string.view_reddit_gallery_activity_video_label,
                    getArguments().getInt(EXTRA_INDEX) + 1, getArguments().getInt(EXTRA_MEDIA_COUNT)));
        }

        String caption = galleryVideo.caption;
        String captionUrl = galleryVideo.captionUrl;
        boolean captionIsEmpty = TextUtils.isEmpty(caption);
        boolean captionUrlIsEmpty = TextUtils.isEmpty(captionUrl);
        if (!captionIsEmpty || !captionUrlIsEmpty) {
            isUseBottomCaption = true;

            if (!activity.isUseBottomAppBar()) {
                bottomAppBar.setVisibility(View.VISIBLE);
                bottomAppBarMenu.setVisibility(View.GONE);
            }

            captionLayout.setVisibility(View.VISIBLE);

            if (!captionIsEmpty) {
                captionTextView.setVisibility(View.VISIBLE);
                captionTextView.setText(caption);
                captionTextView.setOnClickListener(view -> hideAppBar());
                captionTextView.setOnLongClickListener(view -> {
                    if (activity != null
                            && !activity.isDestroyed()
                            && !activity.isFinishing()
                            && captionTextView.getSelectionStart() == -1
                            && captionTextView.getSelectionEnd() == -1) {
                        Bundle bundle = new Bundle();
                        bundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, caption);
                        CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
                        copyTextBottomSheetFragment.setArguments(bundle);
                        copyTextBottomSheetFragment.show(activity.getSupportFragmentManager(), copyTextBottomSheetFragment.getTag());
                    }
                    return true;
                });
            }
            if (!captionUrlIsEmpty) {
                String scheme = Uri.parse(captionUrl).getScheme();
                String urlWithoutScheme = "";
                if (!TextUtils.isEmpty(scheme)) {
                    urlWithoutScheme = captionUrl.substring(scheme.length() + 3);
                }

                captionUrlTextView.setText(TextUtils.isEmpty(urlWithoutScheme) ? captionUrl : urlWithoutScheme);

                BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, captionUrlTextView).setOnLinkLongClickListener((textView, url) -> {
                    if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                        UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, captionUrl);
                        urlMenuBottomSheetFragment.setArguments(bundle);
                        urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
                    }
                    return true;
                });
                captionUrlTextView.setVisibility(View.VISIBLE);
                captionUrlTextView.setHighlightColor(Color.TRANSPARENT);
            }
        }

        return rootView;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        inflater.inflate(R.menu.view_reddit_gallery_gif_variant_fragment, menu);
        if (galleryVideo.isGifVariant()) {
            menu.findItem(R.id.action_download_gif_variant_original_view_reddit_gallery_gif_variant_fragment).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_download_video_view_reddit_gallery_gif_variant_fragment) {
            isDownloading = true;
            requestPermissionAndDownload();
            return true;
        } else if (item.getItemId() == R.id.action_playback_speed_view_reddit_gallery_gif_variant_fragment) {
            PlaybackSpeedBottomSheetFragment playbackSpeedBottomSheetFragment = new PlaybackSpeedBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(PlaybackSpeedBottomSheetFragment.EXTRA_PLAYBACK_SPEED, playbackSpeed);
            playbackSpeedBottomSheetFragment.setArguments(bundle);
            playbackSpeedBottomSheetFragment.show(getChildFragmentManager(), playbackSpeedBottomSheetFragment.getTag());
            return true;
        } else if (item.getItemId() == R.id.action_download_gif_variant_original_view_reddit_gallery_gif_variant_fragment) {
            if (isDownloading) {
                return false;
            }
            isDownloading = true;
            downloadGif = true;
            requestPermissionAndDownload();
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
            isDownloading = false;
            downloadGif = false;
        }
    }

    private void download() {
        isDownloading = false;

        Intent intent = new Intent(activity, DownloadMediaService.class);
        if (downloadGif && galleryVideo.isGifVariant()) {
            intent = new Intent(activity, DownloadMediaService.class);
            intent.putExtra(DownloadMediaService.EXTRA_URL, galleryVideo.getGifVariantOriginalUrl());
            intent.putExtra(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_GIF);
            intent.putExtra(DownloadMediaService.EXTRA_FILE_NAME, galleryVideo.fileName.substring(0, galleryVideo.fileName.length() - 4) + ".gif");
            intent.putExtra(DownloadMediaService.EXTRA_SUBREDDIT_NAME, subredditName);
            downloadGif = false;
        } else {
            intent.putExtra(DownloadMediaService.EXTRA_URL, galleryVideo.url);
            intent.putExtra(DownloadMediaService.EXTRA_MEDIA_TYPE, DownloadMediaService.EXTRA_MEDIA_TYPE_VIDEO);
            intent.putExtra(DownloadMediaService.EXTRA_FILE_NAME, galleryVideo.fileName);
            intent.putExtra(DownloadMediaService.EXTRA_SUBREDDIT_NAME, subredditName);
        }
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

        if (savedInstanceState != null) {
            long position = savedInstanceState.getLong(POSITION_STATE);
            if (position > 0) {
                player.seekTo(position);
            }
        }
    }


    private void hideAppBar() {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        isActionBarHidden = true;
        if (activity.isUseBottomAppBar() || isUseBottomCaption) {
            bottomAppBar.setVisibility(View.GONE);
        }
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
        activity = (ViewRedditGalleryActivity) context;
    }
}
