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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityPostImageBinding;
import ml.docilealligator.infinityforreddit.events.SubmitImagePostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitVideoOrGifPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
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
    private ActivityPostImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        binding = ActivityPostImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutPostImageActivity);
        }

        setSupportActionBar(binding.toolbarPostImageActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.posting, Snackbar.LENGTH_INDEFINITE);

        resources = getResources();

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
                        .into(binding.accountIconGifImageViewPostImageActivity);

                binding.accountNameTextViewPostImageActivity.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (savedInstanceState.getString(IMAGE_URI_STATE) != null) {
                imageUri = Uri.parse(savedInstanceState.getString(IMAGE_URI_STATE));
                loadImage();
            }

            if (subredditName != null) {
                binding.subredditNameTextViewPostImageActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostImageActivity.setText(subredditName);
                binding.flairCustomTextViewPostImageActivity.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                binding.flairCustomTextViewPostImageActivity.setText(flair.getText());
                binding.flairCustomTextViewPostImageActivity.setBackgroundColor(flairBackgroundColor);
                binding.flairCustomTextViewPostImageActivity.setBorderColor(flairBackgroundColor);
                binding.flairCustomTextViewPostImageActivity.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                binding.spoilerCustomTextViewPostImageActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostImageActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostImageActivity.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                binding.nsfwCustomTextViewPostImageActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostImageActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostImageActivity.setTextColor(nsfwTextColor);
            }
        } else {
            isPosting = false;

            loadCurrentAccount();

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                binding.subredditNameTextViewPostImageActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostImageActivity.setText(subredditName);
                binding.flairCustomTextViewPostImageActivity.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(binding.subredditIconGifImageViewPostImageActivity);
            }

            imageUri = getIntent().getData();
            if (imageUri != null) {
                loadImage();
            }
        }

        binding.accountLinearLayoutPostImageActivity.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.subredditRelativeLayoutPostImageActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        binding.rulesButtonPostImageActivity.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
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

        binding.flairCustomTextViewPostImageActivity.setOnClickListener(view -> {
            if (flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                binding.flairCustomTextViewPostImageActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostImageActivity.setText(getString(R.string.flair));
                flair = null;
            }
        });

        binding.spoilerCustomTextViewPostImageActivity.setOnClickListener(view -> {
            if (!isSpoiler) {
                binding.spoilerCustomTextViewPostImageActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostImageActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostImageActivity.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                binding.spoilerCustomTextViewPostImageActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.spoilerCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        binding.nsfwCustomTextViewPostImageActivity.setOnClickListener(view -> {
            if (!isNSFW) {
                binding.nsfwCustomTextViewPostImageActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostImageActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostImageActivity.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                binding.nsfwCustomTextViewPostImageActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.nsfwCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        binding.receivePostReplyNotificationsLinearLayoutPostImageActivity.setOnClickListener(view -> {
            binding.receivePostReplyNotificationsSwitchMaterialPostImageActivity.performClick();
        });

        binding.captureFabPostImageActivity.setOnClickListener(view -> {
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider",
                        File.createTempFile("temp_img", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(pictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
            } catch (IOException ex) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.error_creating_temp_file, Snackbar.LENGTH_SHORT).show();
            } catch (ActivityNotFoundException e) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.no_camera_available, Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.selectFromLibraryFabPostImageActivity.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_from_gallery)), PICK_IMAGE_REQUEST_CODE);
        });

        binding.selectAgainTextViewPostImageActivity.setOnClickListener(view -> {
            imageUri = null;
            binding.selectAgainTextViewPostImageActivity.setVisibility(View.GONE);
            mGlide.clear(binding.imageViewPostImageActivity);
            binding.imageViewPostImageActivity.setVisibility(View.GONE);
            binding.selectImageConstraintLayoutPostImageActivity.setVisibility(View.VISIBLE);
        });

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        PostImageActivity.this, binding.postContentEditTextPostImageActivity, item);
            }

            @Override
            public void onUploadImage() {

            }
        });

        binding.markdownBottomBarRecyclerViewPostImageActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewPostImageActivity.setAdapter(adapter);
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
                            .into(binding.accountIconGifImageViewPostImageActivity);

                    binding.accountNameTextViewPostImageActivity.setText(account.getAccountName());
                }
            });
        });
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.coordinatorLayoutPostImageActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostImageActivity, null, binding.toolbarPostImageActivity);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.accountNameTextViewPostImageActivity.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.subredditNameTextViewPostImageActivity.setTextColor(secondaryTextColor);
        binding.rulesButtonPostImageActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.rulesButtonPostImageActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.receivePostReplyNotificationsTextViewPostImageActivity.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1PostImageActivity.setDividerColor(dividerColor);
        binding.divider2PostImageActivity.setDividerColor(dividerColor);
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        binding.flairCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
        binding.spoilerCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
        binding.nsfwCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostImageActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostImageActivity.setHintTextColor(secondaryTextColor);
        binding.postContentEditTextPostImageActivity.setTextColor(primaryTextColor);
        binding.postContentEditTextPostImageActivity.setHintTextColor(secondaryTextColor);
        applyFABTheme(binding.captureFabPostImageActivity);
        applyFABTheme(binding.selectFromLibraryFabPostImageActivity);
        binding.selectAgainTextViewPostImageActivity.setTextColor(mCustomThemeWrapper.getColorAccent());
        if (typeface != null) {
            binding.subredditNameTextViewPostImageActivity.setTypeface(typeface);
            binding.rulesButtonPostImageActivity.setTypeface(typeface);
            binding.receivePostReplyNotificationsTextViewPostImageActivity.setTypeface(typeface);
            binding.flairCustomTextViewPostImageActivity.setTypeface(typeface);
            binding.spoilerCustomTextViewPostImageActivity.setTypeface(typeface);
            binding.nsfwCustomTextViewPostImageActivity.setTypeface(typeface);
            binding.postTitleEditTextPostImageActivity.setTypeface(typeface);
            binding.selectAgainTextViewPostImageActivity.setTypeface(typeface);
        }
        if (contentTypeface != null) {
            binding.postContentEditTextPostImageActivity.setTypeface(contentTypeface);
        }
    }

    private void loadImage() {
        binding.selectImageConstraintLayoutPostImageActivity.setVisibility(View.GONE);
        binding.imageViewPostImageActivity.setVisibility(View.VISIBLE);
        binding.selectAgainTextViewPostImageActivity.setVisibility(View.VISIBLE);
        mGlide.load(imageUri).into(binding.imageViewPostImageActivity);
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.isEmpty()) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.subredditIconGifImageViewPostImageActivity);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageViewPostImageActivity);
        }
    }

    private void loadSubredditIcon() {
        LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditName,
                accessToken, accountName, mOauthRetrofit, mRetrofit, iconImageUrl -> {
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
                if (!binding.postTitleEditTextPostImageActivity.getText().toString().isEmpty()
                        || !binding.postContentEditTextPostImageActivity.getText().toString().isEmpty()
                        || imageUri != null) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_send_post_image_activity) {
            if (!subredditSelected) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (binding.postTitleEditTextPostImageActivity.getText() == null || binding.postTitleEditTextPostImageActivity.getText().toString().isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (imageUri == null) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.select_an_image, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            isPosting = true;

            item.setEnabled(false);
            item.getIcon().setAlpha(130);

            mPostingSnackbar.show();

            String subredditName;
            if (subredditIsUser) {
                subredditName = "u_" + binding.subredditNameTextViewPostImageActivity.getText().toString();
            } else {
                subredditName = binding.subredditNameTextViewPostImageActivity.getText().toString();
            }

            Intent intent = new Intent(this, SubmitPostService.class);
            intent.setData(imageUri);
            intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
            intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(SubmitPostService.EXTRA_TITLE, binding.postTitleEditTextPostImageActivity.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_CONTENT, binding.postContentEditTextPostImageActivity.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
            intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
            intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
            intent.putExtra(SubmitPostService.EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, binding.receivePostReplyNotificationsSwitchMaterialPostImageActivity.isChecked());
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
            if (!binding.postTitleEditTextPostImageActivity.getText().toString().isEmpty()
                    || !binding.postContentEditTextPostImageActivity.getText().toString().isEmpty()
                    || imageUri != null) {
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

                binding.subredditNameTextViewPostImageActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostImageActivity.setText(subredditName);
                displaySubredditIcon();

                binding.flairCustomTextViewPostImageActivity.setVisibility(View.VISIBLE);
                binding.flairCustomTextViewPostImageActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostImageActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostImageActivity.setText(getString(R.string.flair));
                flair = null;
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.error_getting_image, Snackbar.LENGTH_SHORT).show();
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
        binding.flairCustomTextViewPostImageActivity.setText(flair.getText());
        binding.flairCustomTextViewPostImageActivity.setBackgroundColor(flairBackgroundColor);
        binding.flairCustomTextViewPostImageActivity.setBorderColor(flairBackgroundColor);
        binding.flairCustomTextViewPostImageActivity.setTextColor(flairTextColor);
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.accountIconGifImageViewPostImageActivity);

            binding.accountNameTextViewPostImageActivity.setText(selectedAccount.getAccountName());
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
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, accountName);
            startActivity(intent);
            finish();
        } else {
            mMemu.findItem(R.id.action_send_post_image_activity).setEnabled(true);
            mMemu.findItem(R.id.action_send_post_image_activity).getIcon().setAlpha(255);
            if (submitImagePostEvent.errorMessage == null || submitImagePostEvent.errorMessage.isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, submitImagePostEvent.errorMessage.substring(0, 1).toUpperCase()
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
                    accountName);
            startActivity(intent);
            finish();
        } else if (submitVideoOrGifPostEvent.errorProcessingVideoOrGif) {
            Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.error_processing_image, Snackbar.LENGTH_SHORT).show();
        } else {
            if (submitVideoOrGifPostEvent.errorMessage == null || submitVideoOrGifPostEvent.errorMessage.isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayoutPostImageActivity, submitVideoOrGifPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitVideoOrGifPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
