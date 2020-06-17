package ml.docilealligator.infinityforreddit.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.libRG.CustomTextView;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadSubredditIconAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.SubmitVideoPostEvent;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Service.SubmitPostService;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostVideoActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String VIDEO_URI_STATE = "IUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int PICK_VIDEO_REQUEST_CODE = 1;
    private static final int CAPTURE_VIDEO_REQUEST_CODE = 2;

    @BindView(R.id.coordinator_layout_post_video_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_post_video_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_post_video_activity)
    Toolbar toolbar;
    @BindView(R.id.subreddit_icon_gif_image_view_post_video_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_post_video_activity)
    TextView subredditNameTextView;
    @BindView(R.id.rules_button_post_video_activity)
    Button rulesButton;
    @BindView(R.id.divider_1_post_video_activity)
    View divider1;
    @BindView(R.id.flair_custom_text_view_post_video_activity)
    CustomTextView flairTextView;
    @BindView(R.id.spoiler_custom_text_view_post_video_activity)
    CustomTextView spoilerTextView;
    @BindView(R.id.nsfw_custom_text_view_post_video_activity)
    CustomTextView nsfwTextView;
    @BindView(R.id.divider_2_post_video_activity)
    View divider2;
    @BindView(R.id.post_title_edit_text_post_video_activity)
    EditText titleEditText;
    @BindView(R.id.divider_3_post_video_activity)
    View divider3;
    @BindView(R.id.select_video_constraint_layout_post_video_activity)
    ConstraintLayout constraintLayout;
    @BindView(R.id.capture_fab_post_video_activity)
    FloatingActionButton captureFab;
    @BindView(R.id.select_from_library_fab_post_video_activity)
    FloatingActionButton selectFromLibraryFab;
    @BindView(R.id.select_again_text_view_post_video_activity)
    TextView selectAgainTextView;
    @BindView(R.id.player_view_post_video_activity)
    PlayerView videoPlayerView;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("upload_media")
    Retrofit mUploadMediaRetrofit;
    @Inject
    @Named("upload_video")
    Retrofit mUploadVideoRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private Uri videoUri;
    private boolean loadSubredditIconSuccessful = true;
    private boolean isPosting;
    private boolean wasPlaying;
    private int primaryTextColor;
    private int flairBackgroundColor;
    private int flairTextColor;
    private int spoilerBackgroundColor;
    private int spoilerTextColor;
    private int nsfwBackgroundColor;
    private int nsfwTextColor;
    private Flair flair;
    private boolean isSpoiler = false;
    private boolean isNSFW = false;
    private Resources resources;
    private Menu mMemu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment mFlairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_video);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK_FROM_POST_DETAIL, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        player = ExoPlayerFactory.newSimpleInstance(this);
        videoPlayerView.setPlayer(player);
        dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Infinity"));
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        mPostingSnackbar = Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);

        resources = getResources();

        if (savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
            subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
            loadSubredditIconSuccessful = savedInstanceState.getBoolean(LOAD_SUBREDDIT_ICON_STATE);
            isPosting = savedInstanceState.getBoolean(IS_POSTING_STATE);
            flair = savedInstanceState.getParcelable(FLAIR_STATE);
            isSpoiler = savedInstanceState.getBoolean(IS_SPOILER_STATE);
            isNSFW = savedInstanceState.getBoolean(IS_NSFW_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccount();
            }

            if (savedInstanceState.getString(VIDEO_URI_STATE) != null) {
                videoUri = Uri.parse(savedInstanceState.getString(VIDEO_URI_STATE));
                loadVideo();
            }

            if (subredditName != null) {
                subredditNameTextView.setTextColor(primaryTextColor);
                subredditNameTextView.setText(subredditName);
                flairTextView.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                flairTextView.setText(flair.getText());
                flairTextView.setBackgroundColor(flairBackgroundColor);
                flairTextView.setBorderColor(flairBackgroundColor);
                flairTextView.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                spoilerTextView.setBackgroundColor(spoilerBackgroundColor);
                spoilerTextView.setBorderColor(spoilerBackgroundColor);
                spoilerTextView.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                nsfwTextView.setBackgroundColor(nsfwBackgroundColor);
                nsfwTextView.setBorderColor(nsfwBackgroundColor);
                nsfwTextView.setTextColor(nsfwTextColor);
            }
        } else {
            getCurrentAccount();

            isPosting = false;

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                subredditNameTextView.setTextColor(primaryTextColor);
                subredditNameTextView.setText(subredditName);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(iconGifImageView);
            }

            videoUri = getIntent().getData();
            if (videoUri != null) {
                loadVideo();
            }
        }

        iconGifImageView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        subredditNameTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        rulesButton.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, RulesActivity.class);
                if (subredditIsUser) {
                    intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                }
                startActivity(intent);
            }
        });

        flairTextView.setOnClickListener(view -> {
            if (flair == null) {
                mFlairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                mFlairSelectionBottomSheetFragment.setArguments(bundle);
                mFlairSelectionBottomSheetFragment.show(getSupportFragmentManager(), mFlairSelectionBottomSheetFragment.getTag());
            } else {
                flairTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                flairTextView.setTextColor(primaryTextColor);
                flairTextView.setText(getString(R.string.flair));
                flair = null;
            }
        });

        spoilerTextView.setOnClickListener(view -> {
            if (!isSpoiler) {
                spoilerTextView.setBackgroundColor(spoilerBackgroundColor);
                spoilerTextView.setBorderColor(spoilerBackgroundColor);
                spoilerTextView.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                spoilerTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                spoilerTextView.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        nsfwTextView.setOnClickListener(view -> {
            if (!isNSFW) {
                nsfwTextView.setBackgroundColor(nsfwBackgroundColor);
                nsfwTextView.setBorderColor(nsfwBackgroundColor);
                nsfwTextView.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                nsfwTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                nsfwTextView.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        captureFab.setOnClickListener(view -> {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, CAPTURE_VIDEO_REQUEST_CODE);
            }
        });

        selectFromLibraryFab.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_from_gallery)), PICK_VIDEO_REQUEST_CODE);
        });

        selectAgainTextView.setOnClickListener(view -> {
            wasPlaying = false;
            player.setPlayWhenReady(false);
            videoUri = null;
            videoPlayerView.setVisibility(View.GONE);
            selectAgainTextView.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        subredditNameTextView.setTextColor(secondaryTextColor);
        rulesButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        rulesButton.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorPrimaryLightTheme()));
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        divider1.setBackgroundColor(dividerColor);
        divider2.setBackgroundColor(dividerColor);
        divider3.setBackgroundColor(dividerColor);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        flairTextView.setTextColor(primaryTextColor);
        spoilerTextView.setTextColor(primaryTextColor);
        nsfwTextView.setTextColor(primaryTextColor);
        titleEditText.setTextColor(primaryTextColor);
        titleEditText.setHintTextColor(secondaryTextColor);
        applyFABTheme(captureFab);
        applyFABTheme(selectFromLibraryFab);
        selectAgainTextView.setTextColor(mCustomThemeWrapper.getColorAccent());
    }

    private void getCurrentAccount() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
        }).execute();
    }

    private void loadVideo() {
        constraintLayout.setVisibility(View.GONE);
        selectAgainTextView.setVisibility(View.VISIBLE);
        videoPlayerView.setVisibility(View.VISIBLE);
        player.prepare(new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri));
        player.setPlayWhenReady(true);
        wasPlaying = true;
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.equals("")) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(iconGifImageView);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(iconGifImageView);
        }
    }

    private void loadSubredditIcon() {
        new LoadSubredditIconAsyncTask(mRedditDataRoomDatabase, subredditName, mRetrofit, iconImageUrl -> {
            iconUrl = iconImageUrl;
            displaySubredditIcon();
            loadSubredditIconSuccessful = true;
        }).execute();
    }

    private void promptAlertDialog(int titleResId, int messageResId) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_video_activity, menu);
        applyMenuItemTheme(menu);
        mMemu = menu;
        if (isPosting) {
            mMemu.findItem(R.id.action_send_post_video_activity).setEnabled(false);
            mMemu.findItem(R.id.action_send_post_video_activity).getIcon().setAlpha(130);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isPosting) {
                    promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
                    return true;
                } else {
                    if (!titleEditText.getText().toString().equals("") || videoUri != null) {
                        promptAlertDialog(R.string.discard, R.string.discard_detail);
                        return true;
                    }
                }
                finish();
                return true;
            case R.id.action_send_post_video_activity:
                if (!subredditSelected) {
                    Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                if (titleEditText.getText() == null || titleEditText.getText().toString().equals("")) {
                    Snackbar.make(coordinatorLayout, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                if (videoUri == null) {
                    Snackbar.make(coordinatorLayout, R.string.select_an_image, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                isPosting = true;

                item.setEnabled(false);
                item.getIcon().setAlpha(130);

                mPostingSnackbar.show();

                String subredditName;
                if (subredditIsUser) {
                    subredditName = "u_" + subredditNameTextView.getText().toString();
                } else {
                    subredditName = subredditNameTextView.getText().toString();
                }

                Intent intent = new Intent(this, SubmitPostService.class);
                intent.setData(videoUri);
                intent.putExtra(SubmitPostService.EXTRA_ACCESS_TOKEN, mAccessToken);
                intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
                intent.putExtra(SubmitPostService.EXTRA_TITLE, titleEditText.getText().toString());
                intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
                intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
                intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
                intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TYPE_VIDEO);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }

                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (isPosting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
        } else {
            if (!titleEditText.getText().toString().equals("") || videoUri != null) {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            } else {
                finish();
            }
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
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putString(SUBREDDIT_ICON_STATE, iconUrl);
        outState.putBoolean(SUBREDDIT_SELECTED_STATE, subredditSelected);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
        if (videoUri != null) {
            outState.putString(VIDEO_URI_STATE, videoUri.toString());
        }
        outState.putBoolean(LOAD_SUBREDDIT_ICON_STATE, loadSubredditIconSuccessful);
        outState.putBoolean(IS_POSTING_STATE, isPosting);
        outState.putParcelable(FLAIR_STATE, flair);
        outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
        outState.putBoolean(IS_NSFW_STATE, isNSFW);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    subredditName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                    iconUrl = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                    subredditSelected = true;
                    subredditIsUser = data.getBooleanExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER, false);

                    subredditNameTextView.setTextColor(primaryTextColor);
                    subredditNameTextView.setText(subredditName);
                    displaySubredditIcon();

                    flairTextView.setVisibility(View.VISIBLE);
                    flairTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                    flairTextView.setTextColor(primaryTextColor);
                    flairTextView.setText(getString(R.string.flair));
                    flair = null;
                }
            }
        } else if (requestCode == PICK_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Snackbar.make(coordinatorLayout, R.string.error_getting_video, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                videoUri = data.getData();
                loadVideo();
            }
        } else if (requestCode == CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    videoUri = data.getData();
                    loadVideo();
                } else {
                    Snackbar.make(coordinatorLayout, R.string.error_getting_video, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        player.release();
    }

    @Override
    public void flairSelected(Flair flair) {
        this.flair = flair;
        flairTextView.setText(flair.getText());
        flairTextView.setBackgroundColor(flairBackgroundColor);
        flairTextView.setBorderColor(flairBackgroundColor);
        flairTextView.setTextColor(flairTextColor);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitVideoPostEvent(SubmitVideoPostEvent submitVideoPostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        mMemu.findItem(R.id.action_send_post_video_activity).setEnabled(true);
        mMemu.findItem(R.id.action_send_post_video_activity).getIcon().setAlpha(255);

        if (submitVideoPostEvent.postSuccess) {
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                    mAccountName);
            startActivity(intent);
            finish();
        } else if (submitVideoPostEvent.errorProcessingVideo) {
            Snackbar.make(coordinatorLayout, R.string.error_processing_video, Snackbar.LENGTH_SHORT).show();
        } else {
            if (submitVideoPostEvent.errorMessage == null || submitVideoPostEvent.errorMessage.equals("")) {
                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, submitVideoPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitVideoPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
