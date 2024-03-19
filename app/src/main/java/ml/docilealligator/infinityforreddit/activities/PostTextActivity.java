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
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.UploadedImage;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityPostTextBinding;
import ml.docilealligator.infinityforreddit.events.SubmitTextOrLinkPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class PostTextActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback,
        UploadImageEnabledActivity, AccountChooserBottomSheetFragment.AccountChooserListener {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_CONTENT = "EC";

    private static final String SELECTED_ACCOUNT_STATE = "SAS";
    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";
    private static final String UPLOADED_IMAGES_STATE = "UIS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int MARKDOWN_PREVIEW_REQUEST_CODE = 300;

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
    private Menu mMenu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();
    private ActivityPostTextBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        binding = ActivityPostTextBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutPostTextActivity);
        }

        setSupportActionBar(binding.toolbarPostTextActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(binding.coordinatorLayoutPostTextActivity, R.string.posting, Snackbar.LENGTH_INDEFINITE);

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
            uploadedImages = savedInstanceState.getParcelableArrayList(UPLOADED_IMAGES_STATE);

            if (selectedAccount != null) {
                mGlide.load(selectedAccount.getProfileImageUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(binding.accountIconGifImageViewPostTextActivity);

                binding.accountNameTextViewPostTextActivity.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (subredditName != null) {
                binding.subredditNameTextViewPostTextActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostTextActivity.setText(subredditName);
                binding.flairCustomTextViewPostTextActivity.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                binding.flairCustomTextViewPostTextActivity.setText(flair.getText());
                binding.flairCustomTextViewPostTextActivity.setBackgroundColor(flairBackgroundColor);
                binding.flairCustomTextViewPostTextActivity.setBorderColor(flairBackgroundColor);
                binding.flairCustomTextViewPostTextActivity.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                binding.spoilerCustomTextViewPostTextActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostTextActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostTextActivity.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                binding.nsfwCustomTextViewPostTextActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostTextActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostTextActivity.setTextColor(nsfwTextColor);
            }
        } else {
            isPosting = false;

            loadCurrentAccount();

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                binding.subredditNameTextViewPostTextActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostTextActivity.setText(subredditName);
                binding.flairCustomTextViewPostTextActivity.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(binding.subredditIconGifImageViewPostTextActivity);
            }

            String text = getIntent().getStringExtra(EXTRA_CONTENT);
            if (text != null) {
                binding.postTextContentEditTextPostTextActivity.setText(text);
            }
        }

        binding.accountLinearLayoutPostTextActivity.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.subredditRelativeLayoutPostTextActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        binding.rulesButtonPostTextActivity.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(binding.coordinatorLayoutPostTextActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
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

        binding.flairCustomTextViewPostTextActivity.setOnClickListener(view -> {
            if (flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                if (subredditIsUser) {
                    bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                }
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                binding.flairCustomTextViewPostTextActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostTextActivity.setText(getString(R.string.flair));
                flair = null;
            }
        });

        binding.spoilerCustomTextViewPostTextActivity.setOnClickListener(view -> {
            if (!isSpoiler) {
                binding.spoilerCustomTextViewPostTextActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostTextActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostTextActivity.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                binding.spoilerCustomTextViewPostTextActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.spoilerCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        binding.nsfwCustomTextViewPostTextActivity.setOnClickListener(view -> {
            if (!isNSFW) {
                binding.nsfwCustomTextViewPostTextActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostTextActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostTextActivity.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                binding.nsfwCustomTextViewPostTextActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.nsfwCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        binding.receivePostReplyNotificationsLinearLayoutPostTextActivity.setOnClickListener(view -> {
            binding.receivePostReplyNotificationsSwitchMaterialPostTextActivity.performClick();
        });

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, true,
                new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onClick(int item) {
                        MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                                PostTextActivity.this, binding.postTextContentEditTextPostTextActivity, item);
                    }

                    @Override
                    public void onUploadImage() {
                        Utils.hideKeyboard(PostTextActivity.this);
                        UploadedImagesBottomSheetFragment fragment = new UploadedImagesBottomSheetFragment();
                        Bundle arguments = new Bundle();
                        arguments.putParcelableArrayList(UploadedImagesBottomSheetFragment.EXTRA_UPLOADED_IMAGES,
                                uploadedImages);
                        fragment.setArguments(arguments);
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
                    }
                });

        binding.markdownBottomBarRecyclerViewPostTextActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewPostTextActivity.setAdapter(adapter);
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
                            .into(binding.accountIconGifImageViewPostTextActivity);

                    binding.accountNameTextViewPostTextActivity.setText(account.getAccountName());
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
        binding.coordinatorLayoutPostTextActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostTextActivity, null, binding.toolbarPostTextActivity);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.accountNameTextViewPostTextActivity.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.subredditNameTextViewPostTextActivity.setTextColor(secondaryTextColor);
        binding.rulesButtonPostTextActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.rulesButtonPostTextActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.receivePostReplyNotificationsTextViewPostTextActivity.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1PostTextActivity.setDividerColor(dividerColor);
        binding.divider2PostTextActivity.setDividerColor(dividerColor);
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        binding.flairCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
        binding.spoilerCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
        binding.nsfwCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostTextActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostTextActivity.setHintTextColor(secondaryTextColor);
        binding.postTextContentEditTextPostTextActivity.setTextColor(primaryTextColor);
        binding.postTextContentEditTextPostTextActivity.setHintTextColor(secondaryTextColor);
        if (typeface != null) {
            binding.subredditNameTextViewPostTextActivity.setTypeface(typeface);
            binding.rulesButtonPostTextActivity.setTypeface(typeface);
            binding.receivePostReplyNotificationsTextViewPostTextActivity.setTypeface(typeface);
            binding.flairCustomTextViewPostTextActivity.setTypeface(typeface);
            binding.spoilerCustomTextViewPostTextActivity.setTypeface(typeface);
            binding.nsfwCustomTextViewPostTextActivity.setTypeface(typeface);
            binding.postTitleEditTextPostTextActivity.setTypeface(typeface);
        }
        if (contentTypeface != null) {
            binding.postTextContentEditTextPostTextActivity.setTypeface(contentTypeface);
        }
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.equals("")) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.subredditIconGifImageViewPostTextActivity);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageViewPostTextActivity);
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
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_text_activity, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        if (isPosting) {
            mMenu.findItem(R.id.action_send_post_text_activity).setEnabled(false);
            mMenu.findItem(R.id.action_send_post_text_activity).getIcon().setAlpha(130);
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
                if (!binding.postTitleEditTextPostTextActivity.getText().toString().equals("") || !binding.postTextContentEditTextPostTextActivity.getText().toString().equals("")) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_preview_post_text_activity) {
            Intent intent = new Intent(this, FullMarkdownActivity.class);
            intent.putExtra(FullMarkdownActivity.EXTRA_MARKDOWN, binding.postTextContentEditTextPostTextActivity.getText().toString());
            intent.putExtra(FullMarkdownActivity.EXTRA_SUBMIT_POST, true);
            startActivityForResult(intent, MARKDOWN_PREVIEW_REQUEST_CODE);
        } else if (itemId == R.id.action_send_post_text_activity) {
            submitPost(item);
            return true;
        }

        return false;
    }

    private void submitPost(MenuItem item) {
        if (!subredditSelected) {
            Snackbar.make(binding.coordinatorLayoutPostTextActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (binding.postTitleEditTextPostTextActivity.getText() == null || binding.postTitleEditTextPostTextActivity.getText().toString().equals("")) {
            Snackbar.make(binding.coordinatorLayoutPostTextActivity, R.string.title_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        isPosting = true;

        if (item != null) {
            item.setEnabled(false);
            item.getIcon().setAlpha(130);
        }

        mPostingSnackbar.show();

        String subredditName;
        if (subredditIsUser) {
            subredditName = "u_" + binding.subredditNameTextViewPostTextActivity.getText().toString();
        } else {
            subredditName = binding.subredditNameTextViewPostTextActivity.getText().toString();
        }

        Intent intent = new Intent(this, SubmitPostService.class);
        intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
        intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
        intent.putExtra(SubmitPostService.EXTRA_TITLE, binding.postTitleEditTextPostTextActivity.getText().toString());
        intent.putExtra(SubmitPostService.EXTRA_CONTENT, binding.postTextContentEditTextPostTextActivity.getText().toString());
        if (!uploadedImages.isEmpty()) {
            intent.putExtra(SubmitPostService.EXTRA_IS_RICHTEXT_JSON, true);
            intent.putExtra(SubmitPostService.EXTRA_UPLOADED_IMAGES, uploadedImages);
        }

        intent.putExtra(SubmitPostService.EXTRA_KIND, APIUtils.KIND_SELF);
        intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
        intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
        intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
        intent.putExtra(SubmitPostService.EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, binding.receivePostReplyNotificationsSwitchMaterialPostTextActivity.isChecked());
        intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TEXT_OR_LINK);
        ContextCompat.startForegroundService(this, intent);
    }

    @Override
    public void onBackPressed() {
        if (isPosting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
        } else {
            if (!binding.postTitleEditTextPostTextActivity.getText().toString().equals("") || !binding.postTextContentEditTextPostTextActivity.getText().toString().equals("")) {
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
        outState.putBoolean(LOAD_SUBREDDIT_ICON_STATE, loadSubredditIconSuccessful);
        outState.putBoolean(IS_POSTING_STATE, isPosting);
        outState.putParcelable(FLAIR_STATE, flair);
        outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
        outState.putBoolean(IS_NSFW_STATE, isNSFW);
        outState.putParcelableArrayList(UPLOADED_IMAGES_STATE, uploadedImages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                subredditSelected = true;
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                binding.subredditNameTextViewPostTextActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostTextActivity.setText(subredditName);
                displaySubredditIcon();

                binding.flairCustomTextViewPostTextActivity.setVisibility(View.VISIBLE);
                binding.flairCustomTextViewPostTextActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostTextActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostTextActivity.setText(getString(R.string.flair));
                flair = null;

            } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                if (data == null) {
                    Toast.makeText(PostTextActivity.this, R.string.error_getting_image, Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        accessToken, binding.postTextContentEditTextPostTextActivity, binding.coordinatorLayoutPostTextActivity, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        accessToken, binding.postTextContentEditTextPostTextActivity, binding.coordinatorLayoutPostTextActivity, capturedImageUri, uploadedImages);
            } else if (requestCode == MARKDOWN_PREVIEW_REQUEST_CODE) {
                submitPost(mMenu.findItem(R.id.action_send_post_text_activity));
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
        binding.flairCustomTextViewPostTextActivity.setText(flair.getText());
        binding.flairCustomTextViewPostTextActivity.setBackgroundColor(flairBackgroundColor);
        binding.flairCustomTextViewPostTextActivity.setBorderColor(flairBackgroundColor);
        binding.flairCustomTextViewPostTextActivity.setTextColor(flairTextColor);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitTextPostEvent(SubmitTextOrLinkPostEvent submitTextOrLinkPostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        if (submitTextOrLinkPostEvent.postSuccess) {
            Intent intent = new Intent(PostTextActivity.this, ViewPostDetailActivity.class);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, submitTextOrLinkPostEvent.post);
            startActivity(intent);
            finish();
        } else {
            mMenu.findItem(R.id.action_send_post_text_activity).setEnabled(true);
            mMenu.findItem(R.id.action_send_post_text_activity).getIcon().setAlpha(255);
            if (submitTextOrLinkPostEvent.errorMessage == null || submitTextOrLinkPostEvent.errorMessage.equals("")) {
                Snackbar.make(binding.coordinatorLayoutPostTextActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayoutPostTextActivity, submitTextOrLinkPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitTextOrLinkPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getResources().getString(R.string.select_from_gallery)), PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void captureImage() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            capturedImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider",
                    File.createTempFile("captured_image", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            startActivityForResult(pictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
        } catch (IOException ex) {
            Toast.makeText(this, R.string.error_creating_temp_file, Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_camera_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void insertImageUrl(UploadedImage uploadedImage) {
        int start = Math.max(binding.postTextContentEditTextPostTextActivity.getSelectionStart(), 0);
        int end = Math.max(binding.postTextContentEditTextPostTextActivity.getSelectionEnd(), 0);
        int realStart = Math.min(start, end);
        if (realStart > 0 && binding.postTextContentEditTextPostTextActivity.getText().toString().charAt(realStart - 1) != '\n') {
            binding.postTextContentEditTextPostTextActivity.getText().replace(realStart, Math.max(start, end),
                    "\n![](" + uploadedImage.imageUrlOrKey + ")\n",
                    0, "\n![]()\n".length() + uploadedImage.imageUrlOrKey.length());
        } else {
            binding.postTextContentEditTextPostTextActivity.getText().replace(realStart, Math.max(start, end),
                    "![](" + uploadedImage.imageUrlOrKey + ")\n",
                    0, "![]()\n".length() + uploadedImage.imageUrlOrKey.length());
        }
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.accountIconGifImageViewPostTextActivity);

            binding.accountNameTextViewPostTextActivity.setText(selectedAccount.getAccountName());
        }
    }
}
