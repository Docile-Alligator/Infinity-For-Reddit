package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SubmitImagePostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitVideoOrGifPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostImageActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback,
        AccountChooserBottomSheetFragment.AccountChooserListener {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String SELECTED_ACCOUNT_STATE = "SAS";
    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String IMAGE_URI_STATE = "IUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;

    @BindView(R.id.coordinator_layout_post_image_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_post_image_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_post_image_activity)
    Toolbar toolbar;
    @BindView(R.id.account_linear_layout_post_image_activity)
    LinearLayout accountLinearLayout;
    @BindView(R.id.account_icon_gif_image_view_post_image_activity)
    GifImageView accountIconImageView;
    @BindView(R.id.account_name_text_view_post_image_activity)
    TextView accountNameTextView;
    @BindView(R.id.subreddit_icon_gif_image_view_post_image_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_post_image_activity)
    TextView subredditNameTextView;
    @BindView(R.id.rules_button_post_image_activity)
    MaterialButton rulesButton;
    @BindView(R.id.divider_1_post_image_activity)
    MaterialDivider divider1;
    @BindView(R.id.flair_custom_text_view_post_image_activity)
    CustomTextView flairTextView;
    @BindView(R.id.spoiler_custom_text_view_post_image_activity)
    CustomTextView spoilerTextView;
    @BindView(R.id.nsfw_custom_text_view_post_image_activity)
    CustomTextView nsfwTextView;
    @BindView(R.id.receive_post_reply_notifications_linear_layout_post_image_activity)
    LinearLayout receivePostReplyNotificationsLinearLayout;
    @BindView(R.id.receive_post_reply_notifications_text_view_post_image_activity)
    TextView receivePostReplyNotificationsTextView;
    @BindView(R.id.receive_post_reply_notifications_switch_material_post_image_activity)
    MaterialSwitch receivePostReplyNotificationsSwitchMaterial;
    @BindView(R.id.divider_2_post_image_activity)
    MaterialDivider divider2;
    @BindView(R.id.post_title_edit_text_post_image_activity)
    EditText titleEditText;
    @BindView(R.id.select_image_constraint_layout_post_image_activity)
    ConstraintLayout constraintLayout;
    @BindView(R.id.capture_fab_post_image_activity)
    FloatingActionButton captureFab;
    @BindView(R.id.select_from_library_fab_post_image_activity)
    FloatingActionButton selectFromLibraryFab;
    @BindView(R.id.select_again_text_view_post_image_activity)
    TextView selectAgainTextView;
    @BindView(R.id.image_view_post_image_activity)
    ImageView imageView;
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
    RedditDataRoomDatabase mRedditDataRoomDatabase;
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
    private Account selectedAccount;
    private String mAccessToken;
    private String mAccountName;
    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private boolean loadSubredditIconSuccessful = true;
    private boolean isPosting;
    private Uri imageUri;
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
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_image);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);

        resources = getResources();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState != null) {
            selectedAccount = savedInstanceState.getParcelable(SELECTED_ACCOUNT_STATE);
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
            subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
            loadSubredditIconSuccessful = savedInstanceState.getBoolean(LOAD_SUBREDDIT_ICON_STATE);
            isPosting = savedInstanceState.getBoolean(IS_POSTING_STATE);
            flair = savedInstanceState.getParcelable(FLAIR_STATE);
            isSpoiler = savedInstanceState.getBoolean(IS_SPOILER_STATE);
            isNSFW = savedInstanceState.getBoolean(IS_NSFW_STATE);

            if (selectedAccount != null) {
                mGlide.load(selectedAccount.getProfileImageUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(accountIconImageView);

                accountNameTextView.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (savedInstanceState.getString(IMAGE_URI_STATE) != null) {
                imageUri = Uri.parse(savedInstanceState.getString(IMAGE_URI_STATE));
                loadImage();
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
            isPosting = false;

            loadCurrentAccount();

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                subredditNameTextView.setTextColor(primaryTextColor);
                subredditNameTextView.setText(subredditName);
                flairTextView.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(iconGifImageView);
            }

            imageUri = getIntent().getData();
            if (imageUri != null) {
                loadImage();
            }
        }

        accountLinearLayout.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        iconGifImageView.setOnClickListener(view -> subredditNameTextView.performClick());

        subredditNameTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
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
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
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

        receivePostReplyNotificationsLinearLayout.setOnClickListener(view -> {
            receivePostReplyNotificationsSwitchMaterial.performClick();
        });

        captureFab.setOnClickListener(view -> {
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider",
                        File.createTempFile("temp_img", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(pictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
            } catch (IOException ex) {
                Snackbar.make(coordinatorLayout, R.string.error_creating_temp_file, Snackbar.LENGTH_SHORT).show();
            } catch (ActivityNotFoundException e) {
                Snackbar.make(coordinatorLayout, R.string.no_camera_available, Snackbar.LENGTH_SHORT).show();
            }
        });

        selectFromLibraryFab.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_from_gallery)), PICK_IMAGE_REQUEST_CODE);
        });

        selectAgainTextView.setOnClickListener(view -> {
            imageUri = null;
            selectAgainTextView.setVisibility(View.GONE);
            mGlide.clear(imageView);
            imageView.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
        });
    }

    private void loadCurrentAccount() {
        Handler handler = new Handler();
        mExecutor.execute(() -> {
            Account account = mRedditDataRoomDatabase.accountDao().getCurrentAccount();
            selectedAccount = account;
            handler.post(() -> {
                if (!isFinishing() && !isDestroyed() && account != null) {
                    mGlide.load(account.getProfileImageUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(accountIconImageView);

                    accountNameTextView.setText(account.getAccountName());
                }
            });
        });
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        accountNameTextView.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        subredditNameTextView.setTextColor(secondaryTextColor);
        rulesButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        rulesButton.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        receivePostReplyNotificationsTextView.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        divider1.setDividerColor(dividerColor);
        divider2.setDividerColor(dividerColor);
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
        if (typeface != null) {
            subredditNameTextView.setTypeface(typeface);
            rulesButton.setTypeface(typeface);
            receivePostReplyNotificationsTextView.setTypeface(typeface);
            flairTextView.setTypeface(typeface);
            spoilerTextView.setTypeface(typeface);
            nsfwTextView.setTypeface(typeface);
            titleEditText.setTypeface(typeface);
            selectAgainTextView.setTypeface(typeface);
        }
    }

    private void loadImage() {
        constraintLayout.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        selectAgainTextView.setVisibility(View.VISIBLE);
        mGlide.load(imageUri).into(imageView);
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
        LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditName, mAccessToken, mOauthRetrofit, mRetrofit, iconImageUrl -> {
            iconUrl = iconImageUrl;
            displaySubredditIcon();
            loadSubredditIconSuccessful = true;
        });
    }

    private void promptAlertDialog(int titleResId, int messageResId) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.discard_dialog_button, (dialogInterface, i)
                        -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_image_activity, menu);
        applyMenuItemTheme(menu);
        mMemu = menu;
        if (isPosting) {
            mMemu.findItem(R.id.action_send_post_image_activity).setEnabled(false);
            mMemu.findItem(R.id.action_send_post_image_activity).getIcon().setAlpha(130);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (isPosting) {
                promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
                return true;
            } else {
                if (!titleEditText.getText().toString().equals("") || imageUri != null) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_send_post_image_activity) {
            if (!subredditSelected) {
                Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (titleEditText.getText() == null || titleEditText.getText().toString().equals("")) {
                Snackbar.make(coordinatorLayout, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (imageUri == null) {
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
            intent.setData(imageUri);
            intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
            intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(SubmitPostService.EXTRA_TITLE, titleEditText.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
            intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
            intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
            intent.putExtra(SubmitPostService.EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, receivePostReplyNotificationsSwitchMaterial.isChecked());
            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType != null && mimeType.contains("gif")) {
                intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TYPE_VIDEO);
            } else {
                intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TYPE_IMAGE);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            ContextCompat.startForegroundService(this, intent);

            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (isPosting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
        } else {
            if (!titleEditText.getText().toString().equals("") || imageUri != null) {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SELECTED_ACCOUNT_STATE, selectedAccount);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putString(SUBREDDIT_ICON_STATE, iconUrl);
        outState.putBoolean(SUBREDDIT_SELECTED_STATE, subredditSelected);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
        if (imageUri != null) {
            outState.putString(IMAGE_URI_STATE, imageUri.toString());
        }
        outState.putBoolean(LOAD_SUBREDDIT_ICON_STATE, loadSubredditIconSuccessful);
        outState.putBoolean(IS_POSTING_STATE, isPosting);
        outState.putParcelable(FLAIR_STATE, flair);
        outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
        outState.putBoolean(IS_NSFW_STATE, isNSFW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                subredditSelected = true;
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                subredditNameTextView.setTextColor(primaryTextColor);
                subredditNameTextView.setText(subredditName);
                displaySubredditIcon();

                flairTextView.setVisibility(View.VISIBLE);
                flairTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                flairTextView.setTextColor(primaryTextColor);
                flairTextView.setText(getString(R.string.flair));
                flair = null;
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Snackbar.make(coordinatorLayout, R.string.error_getting_image, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                imageUri = data.getData();
                loadImage();
            }
        } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadImage();
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void flairSelected(Flair flair) {
        this.flair = flair;
        flairTextView.setText(flair.getText());
        flairTextView.setBackgroundColor(flairBackgroundColor);
        flairTextView.setBorderColor(flairBackgroundColor);
        flairTextView.setTextColor(flairTextColor);
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(accountIconImageView);

            accountNameTextView.setText(selectedAccount.getAccountName());
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitImagePostEvent(SubmitImagePostEvent submitImagePostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        if (submitImagePostEvent.postSuccess) {
            Intent intent = new Intent(PostImageActivity.this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
            startActivity(intent);
            finish();
        } else {
            mMemu.findItem(R.id.action_send_post_image_activity).setEnabled(true);
            mMemu.findItem(R.id.action_send_post_image_activity).getIcon().setAlpha(255);
            if (submitImagePostEvent.errorMessage == null || submitImagePostEvent.errorMessage.equals("")) {
                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, submitImagePostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitImagePostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Subscribe
    public void onSubmitGifPostEvent(SubmitVideoOrGifPostEvent submitVideoOrGifPostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        mMemu.findItem(R.id.action_send_post_image_activity).setEnabled(true);
        mMemu.findItem(R.id.action_send_post_image_activity).getIcon().setAlpha(255);

        if (submitVideoOrGifPostEvent.postSuccess) {
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                    mAccountName);
            startActivity(intent);
            finish();
        } else if (submitVideoOrGifPostEvent.errorProcessingVideoOrGif) {
            Snackbar.make(coordinatorLayout, R.string.error_processing_image, Snackbar.LENGTH_SHORT).show();
        } else {
            if (submitVideoOrGifPostEvent.errorMessage == null || submitVideoOrGifPostEvent.errorMessage.equals("")) {
                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, submitVideoOrGifPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitVideoOrGifPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
