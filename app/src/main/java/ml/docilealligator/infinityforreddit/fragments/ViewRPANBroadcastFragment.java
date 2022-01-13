package ml.docilealligator.infinityforreddit.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RPANBroadcast;
import ml.docilealligator.infinityforreddit.RPANComment;
import ml.docilealligator.infinityforreddit.activities.RPANActivity;
import ml.docilealligator.infinityforreddit.adapters.RPANCommentStreamRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Retrofit;

public class ViewRPANBroadcastFragment extends Fragment {

    public static final String EXTRA_RPAN_BROADCAST = "ERB";
    private static final String IS_MUTE_STATE = "IMS";

    @BindView(R.id.constraint_layout_exo_rpan_broadcast_playback_control_view)
    ConstraintLayout constraintLayout;
    @BindView(R.id.player_view_view_rpan_broadcast_fragment)
    PlayerView playerView;
    @BindView(R.id.subreddit_icon_image_view_exo_rpan_broadcast_playback_control_view)
    ImageView subredditIconImageView;
    @BindView(R.id.subreddit_name_text_view_exo_rpan_broadcast_playback_control_view)
    TextView subredditNameTextView;
    @BindView(R.id.username_text_view_exo_rpan_broadcast_playback_control_view)
    TextView usernameTextView;
    @BindView(R.id.title_text_view_exo_rpan_broadcast_playback_control_view)
    TextView titleTextView;
    @BindView(R.id.recycler_view_exo_rpan_broadcast_playback_control_view)
    RecyclerView recyclerView;
    @BindView(R.id.mute_exo_rpan_broadcast_playback_control_view)
    ImageButton muteButton;
    @BindView(R.id.hd_exo_rpan_broadcast_playback_control_view)
    ImageButton hdButton;
    @Inject
    @Named("strapi")
    Retrofit mStrapiRetrofit;
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
    @Inject
    @Named("rpan")
    OkHttpClient okHttpClient;
    private RPANActivity mActivity;
    private RPANBroadcast rpanBroadcast;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;
    private Handler handler;
    private RPANCommentStreamRecyclerViewAdapter adapter;
    private WebSocket webSocket;

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

        if (mActivity.typeface != null) {
            subredditNameTextView.setTypeface(mActivity.typeface);
            usernameTextView.setTypeface(mActivity.typeface);
            titleTextView.setTypeface(mActivity.typeface);
        }

        rpanBroadcast = getArguments().getParcelable(EXTRA_RPAN_BROADCAST);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || getResources().getBoolean(R.bool.isTablet)) {
            //Set player controller bottom margin in order to display it above the navbar
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            //LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) constraintLayout.getLayoutParams();
            params.bottomMargin = getResources().getDimensionPixelSize(resourceId);
        } else {
            //Set player controller right margin in order to display it above the navbar
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            //LinearLayout controllerLinearLayout = findViewById(R.id.linear_layout_exo_playback_control_view);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) constraintLayout.getLayoutParams();
            params.rightMargin = getResources().getDimensionPixelSize(resourceId);
        }

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
                    playerView.setControllerShowTimeoutMs(0);
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
                        TrackSelectionDialogBuilder builder = new TrackSelectionDialogBuilder(mActivity,
                                getString(R.string.select_video_quality), trackSelector, 0);
                        builder.setShowDisableOption(true);
                        builder.setAllowAdaptiveSelections(false);
                        AlertDialog alertDialog = builder.build();
                        alertDialog.show();
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
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

        Glide.with(mActivity).load(rpanBroadcast.rpanPost.subredditIconUrl)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                .error(Glide.with(mActivity).load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                .into(subredditIconImageView);
        subredditNameTextView.setText(rpanBroadcast.rpanPost.subredditName);
        usernameTextView.setText(rpanBroadcast.rpanPost.username);
        titleTextView.setText(rpanBroadcast.rpanPost.title);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            float x1;
            float x2;
            float y1;
            float y2;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = motionEvent.getX();
                        y1 = motionEvent.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        x2 = motionEvent.getX();
                        y2 = motionEvent.getY();

                        if (x1 == x2 && y1 == y2) {
                            playerView.hideController();
                        }

                        return true;
                }

                return false;
            }
        });
        adapter = new RPANCommentStreamRecyclerViewAdapter(mActivity);
        recyclerView.setAdapter(adapter);

        handler = new Handler();

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

    private void parseComment(String commentJson) {
        mExecutor.execute(() -> {
            try {
                JSONObject commentObject = new JSONObject(commentJson);
                if (commentObject.getString(JSONUtils.TYPE_KEY).equals("new_comment")) {
                    JSONObject payload = commentObject.getJSONObject(JSONUtils.PAYLOAD_KEY);
                    RPANComment rpanComment = new RPANComment(
                            payload.getString(JSONUtils.AUTHOR_KEY),
                            payload.getString(JSONUtils.AUTHOR_ICON_IMAGE),
                            payload.getString(JSONUtils.BODY_KEY),
                            payload.getLong(JSONUtils.CREATED_UTC_KEY));

                    handler.post(() -> {
                        LinearLayoutManagerBugFixed manager = ((LinearLayoutManagerBugFixed) recyclerView.getLayoutManager());
                        boolean shouldScrollToBottom = false;
                        if (manager != null) {
                            int lastPosition = manager.findLastCompletelyVisibleItemPosition();
                            int currentItemCount = adapter.getItemCount();
                            if (currentItemCount > 0 && lastPosition == currentItemCount - 1) {
                                shouldScrollToBottom = true;
                            }
                        }
                        adapter.addRPANComment(rpanComment);
                        if (shouldScrollToBottom) {
                            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MUTE_STATE, isMute);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataSourceFactory == null) {
            dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(mActivity, "Infinity"));
            // Prepare the player with the source.
            player.prepare(new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(rpanBroadcast.rpanStream.hlsUrl)));
            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.LOOP_VIDEO, true)) {
                player.setRepeatMode(Player.REPEAT_MODE_ALL);
            } else {
                player.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
            if (resumePosition > 0) {
                player.seekTo(resumePosition);
            }
        }

        if (wasPlaying) {
            player.setPlayWhenReady(true);
        }

        if (webSocket == null) {
            Request request = new Request.Builder().url(rpanBroadcast.rpanPost.liveCommentsWebsocketUrl).build();
            CommentStreamWebSocketListener listener = new CommentStreamWebSocketListener(this::parseComment);
            webSocket = okHttpClient.newWebSocket(request, listener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPlaying = player.getPlayWhenReady();
        player.setPlayWhenReady(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.seekToDefaultPosition();
        player.stop(true);
        player.release();

        if (webSocket != null) {
            webSocket.cancel();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (RPANActivity) context;
    }

    private static class CommentStreamWebSocketListener extends WebSocketListener {
        MessageReceivedListener messageReceivedListener;

        CommentStreamWebSocketListener(MessageReceivedListener messageReceivedListener) {
            this.messageReceivedListener = messageReceivedListener;
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            messageReceivedListener.onMessage(text);
        }

        interface MessageReceivedListener {
            void onMessage(String text);
        }
    }
}