package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
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
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.PollPayload;
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
import ml.docilealligator.infinityforreddit.databinding.ActivityPostPollBinding;
import ml.docilealligator.infinityforreddit.events.SubmitPollPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.RichTextJSONConverter;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class PostPollActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback,
        UploadImageEnabledActivity, AccountChooserBottomSheetFragment.AccountChooserListener {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

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
    private ActivityPostPollBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityPostPollBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutPostPollActivity);
        }

        setSupportActionBar(binding.toolbarPostPollActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.posting, Snackbar.LENGTH_INDEFINITE);

        resources = getResources();

        Resources resources = getResources();

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
                        .into(binding.accountIconGifImageViewPostPollActivity);

                binding.accountNameTextViewPostPollActivity.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (subredditName != null) {
                binding.subredditNameTextViewPostPollActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostPollActivity.setText(subredditName);
                binding.flairCustomTextViewPostPollActivity.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                binding.flairCustomTextViewPostPollActivity.setText(flair.getText());
                binding.flairCustomTextViewPostPollActivity.setBackgroundColor(flairBackgroundColor);
                binding.flairCustomTextViewPostPollActivity.setBorderColor(flairBackgroundColor);
                binding.flairCustomTextViewPostPollActivity.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                binding.spoilerCustomTextViewPostPollActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostPollActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostPollActivity.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                binding.nsfwCustomTextViewPostPollActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostPollActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostPollActivity.setTextColor(nsfwTextColor);
            }
        } else {
            isPosting = false;

            loadCurrentAccount();

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                binding.subredditNameTextViewPostPollActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostPollActivity.setText(subredditName);
                binding.flairCustomTextViewPostPollActivity.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(binding.subredditIconGifImageViewPostPollActivity);
            }
        }

        binding.accountLinearLayoutPostPollActivity.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.subredditRelativeLayoutPostPollActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        binding.rulesButtonPostPollActivity.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
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

        binding.flairCustomTextViewPostPollActivity.setOnClickListener(view -> {
            if (flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                binding.flairCustomTextViewPostPollActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostPollActivity.setText(getString(R.string.flair));
                flair = null;
            }
        });

        binding.spoilerCustomTextViewPostPollActivity.setOnClickListener(view -> {
            if (!isSpoiler) {
                binding.spoilerCustomTextViewPostPollActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostPollActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostPollActivity.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                binding.spoilerCustomTextViewPostPollActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.spoilerCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        binding.nsfwCustomTextViewPostPollActivity.setOnClickListener(view -> {
            if (!isNSFW) {
                binding.nsfwCustomTextViewPostPollActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostPollActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostPollActivity.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                binding.nsfwCustomTextViewPostPollActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.nsfwCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        binding.receivePostReplyNotificationsLinearLayoutPostPollActivity.setOnClickListener(view -> {
            binding.receivePostReplyNotificationsSwitchMaterialPostPollActivity.performClick();
        });

        binding.votingLengthTextViewPostPollActivity.setText(getString(R.string.voting_length, (int) binding.votingLengthSliderPostPollActivity.getValue()));
        binding.votingLengthSliderPostPollActivity.addOnChangeListener((slider, value, fromUser) -> binding.votingLengthTextViewPostPollActivity.setText(getString(R.string.voting_length, (int) value)));

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, true,
                new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onClick(int item) {
                        MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                                PostPollActivity.this, binding.postContentEditTextPostPollActivity, item);
                    }

                    @Override
                    public void onUploadImage() {
                        Utils.hideKeyboard(PostPollActivity.this);
                        UploadedImagesBottomSheetFragment fragment = new UploadedImagesBottomSheetFragment();
                        Bundle arguments = new Bundle();
                        arguments.putParcelableArrayList(UploadedImagesBottomSheetFragment.EXTRA_UPLOADED_IMAGES,
                                uploadedImages);
                        fragment.setArguments(arguments);
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
                    }
                });

        binding.markdownBottomBarRecyclerViewPostPollActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewPostPollActivity.setAdapter(adapter);
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
                            .into(binding.accountIconGifImageViewPostPollActivity);

                    binding.accountNameTextViewPostPollActivity.setText(account.getAccountName());
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
        binding.coordinatorLayoutPostPollActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostPollActivity, null, binding.toolbarPostPollActivity);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.accountNameTextViewPostPollActivity.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.subredditNameTextViewPostPollActivity.setTextColor(secondaryTextColor);
        binding.rulesButtonPostPollActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.rulesButtonPostPollActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.receivePostReplyNotificationsTextViewPostPollActivity.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1PostPollActivity.setDividerColor(dividerColor);
        binding.divider2PostPollActivity.setDividerColor(dividerColor);
        binding.divider3PostPollActivity.setDividerColor(dividerColor);
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        binding.flairCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
        binding.spoilerCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
        binding.nsfwCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostPollActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostPollActivity.setHintTextColor(secondaryTextColor);
        binding.postContentEditTextPostPollActivity.setTextColor(primaryTextColor);
        binding.postContentEditTextPostPollActivity.setHintTextColor(secondaryTextColor);
        binding.option1TextInputLayoutPostPollActivity.setBoxStrokeColor(primaryTextColor);
        binding.option1TextInputLayoutPostPollActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.option1TextInputLayoutEditTextPostPollActivity.setTextColor(primaryTextColor);

        binding.option2TextInputLayoutPostPollActivity.setBoxStrokeColor(primaryTextColor);
        binding.option2TextInputLayoutPostPollActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.option2TextInputLayoutEditTextPostPollActivity.setTextColor(primaryTextColor);

        binding.option3TextInputLayoutPostPollActivity.setBoxStrokeColor(primaryTextColor);
        binding.option3TextInputLayoutPostPollActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.option3TextInputLayoutEditTextPostPollActivity.setTextColor(primaryTextColor);

        binding.option4TextInputLayoutPostPollActivity.setBoxStrokeColor(primaryTextColor);
        binding.option4TextInputLayoutPostPollActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.option4TextInputLayoutEditTextPostPollActivity.setTextColor(primaryTextColor);

        binding.option5TextInputLayoutPostPollActivity.setBoxStrokeColor(primaryTextColor);
        binding.option5TextInputLayoutPostPollActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.option5TextInputLayoutEditTextPostPollActivity.setTextColor(primaryTextColor);

        binding.option6TextInputLayoutPostPollActivity.setBoxStrokeColor(primaryTextColor);
        binding.option6TextInputLayoutPostPollActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.option6TextInputLayoutEditTextPostPollActivity.setTextColor(primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Drawable cursorDrawable = Utils.getTintedDrawable(this, R.drawable.edit_text_cursor, primaryTextColor);
            binding.option1TextInputLayoutEditTextPostPollActivity.setTextCursorDrawable(cursorDrawable);
            binding.option2TextInputLayoutEditTextPostPollActivity.setTextCursorDrawable(cursorDrawable);
            binding.option3TextInputLayoutEditTextPostPollActivity.setTextCursorDrawable(cursorDrawable);
            binding.option4TextInputLayoutEditTextPostPollActivity.setTextCursorDrawable(cursorDrawable);
            binding.option5TextInputLayoutEditTextPostPollActivity.setTextCursorDrawable(cursorDrawable);
            binding.option6TextInputLayoutEditTextPostPollActivity.setTextCursorDrawable(cursorDrawable);
        } else {
            setCursorDrawableColor(binding.option1TextInputLayoutEditTextPostPollActivity, primaryTextColor);
            setCursorDrawableColor(binding.option2TextInputLayoutEditTextPostPollActivity, primaryTextColor);
            setCursorDrawableColor(binding.option3TextInputLayoutEditTextPostPollActivity, primaryTextColor);
            setCursorDrawableColor(binding.option4TextInputLayoutEditTextPostPollActivity, primaryTextColor);
            setCursorDrawableColor(binding.option5TextInputLayoutEditTextPostPollActivity, primaryTextColor);
            setCursorDrawableColor(binding.option6TextInputLayoutEditTextPostPollActivity, primaryTextColor);
        }

        if (typeface != null) {
            binding.subredditNameTextViewPostPollActivity.setTypeface(typeface);
            binding.rulesButtonPostPollActivity.setTypeface(typeface);
            binding.receivePostReplyNotificationsTextViewPostPollActivity.setTypeface(typeface);
            binding.flairCustomTextViewPostPollActivity.setTypeface(typeface);
            binding.spoilerCustomTextViewPostPollActivity.setTypeface(typeface);
            binding.nsfwCustomTextViewPostPollActivity.setTypeface(typeface);
            binding.postTitleEditTextPostPollActivity.setTypeface(typeface);
            binding.option1TextInputLayoutEditTextPostPollActivity.setTypeface(typeface);
            binding.option2TextInputLayoutEditTextPostPollActivity.setTypeface(typeface);
            binding.option3TextInputLayoutEditTextPostPollActivity.setTypeface(typeface);
            binding.option4TextInputLayoutEditTextPostPollActivity.setTypeface(typeface);
            binding.option5TextInputLayoutEditTextPostPollActivity.setTypeface(typeface);
            binding.option6TextInputLayoutEditTextPostPollActivity.setTypeface(typeface);
        }
        if (contentTypeface != null) {
            binding.postContentEditTextPostPollActivity.setTypeface(contentTypeface);
        }
    }

    public void setCursorDrawableColor(EditText editText, int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[1] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (Throwable ignored) { }
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.isEmpty()) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.subredditIconGifImageViewPostPollActivity);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageViewPostPollActivity);
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
        getMenuInflater().inflate(R.menu.post_poll_activity, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        if (isPosting) {
            mMenu.findItem(R.id.action_send_post_poll_activity).setEnabled(false);
            mMenu.findItem(R.id.action_send_post_poll_activity).getIcon().setAlpha(130);
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
                if (!binding.postTitleEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.postContentEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.option1TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.option2TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.option3TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.option4TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.option5TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                        || !binding.option6TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_preview_post_poll_activity) {
            Intent intent = new Intent(this, FullMarkdownActivity.class);
            intent.putExtra(FullMarkdownActivity.EXTRA_MARKDOWN, binding.postContentEditTextPostPollActivity.getText().toString());
            intent.putExtra(FullMarkdownActivity.EXTRA_SUBMIT_POST, true);
            startActivityForResult(intent, MARKDOWN_PREVIEW_REQUEST_CODE);
        } else if (itemId == R.id.action_send_post_poll_activity) {
            submitPost(item);
            return true;
        }

        return false;
    }

    private void submitPost(MenuItem item) {
        if (!subredditSelected) {
            Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (binding.postTitleEditTextPostPollActivity.getText() == null) {
            Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.title_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        String subredditName;
        if (subredditIsUser) {
            subredditName = "u_" + binding.subredditNameTextViewPostPollActivity.getText().toString();
        } else {
            subredditName = binding.subredditNameTextViewPostPollActivity.getText().toString();
        }

        ArrayList<String> optionList = new ArrayList<>();
        if (!binding.option1TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
            optionList.add(binding.option1TextInputLayoutEditTextPostPollActivity.getText().toString());
        }
        if (!binding.option2TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
            optionList.add(binding.option2TextInputLayoutEditTextPostPollActivity.getText().toString());
        }
        if (!binding.option3TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
            optionList.add(binding.option3TextInputLayoutEditTextPostPollActivity.getText().toString());
        }
        if (!binding.option4TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
            optionList.add(binding.option4TextInputLayoutEditTextPostPollActivity.getText().toString());
        }
        if (!binding.option5TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
            optionList.add(binding.option5TextInputLayoutEditTextPostPollActivity.getText().toString());
        }
        if (!binding.option6TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
            optionList.add(binding.option6TextInputLayoutEditTextPostPollActivity.getText().toString());
        }

        if (optionList.size() < 2) {
            Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.two_options_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        isPosting = true;

        item.setEnabled(false);
        item.getIcon().setAlpha(130);

        mPostingSnackbar.show();

        Intent intent = new Intent(this, SubmitPostService.class);
        intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
        intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
        intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TYPE_POLL);

        PollPayload payload;
        if (!binding.postContentEditTextPostPollActivity.getText().toString().isEmpty()) {
            if (uploadedImages.isEmpty()) {
                payload = new PollPayload(subredditName, binding.postTitleEditTextPostPollActivity.getText().toString(),
                        optionList.toArray(new String[0]), (int) binding.votingLengthSliderPostPollActivity.getValue(), isNSFW, isSpoiler, flair,
                        null, binding.postContentEditTextPostPollActivity.getText().toString(),
                        binding.receivePostReplyNotificationsSwitchMaterialPostPollActivity.isChecked(),
                        subredditIsUser ? "profile" : "subreddit");
            } else {
                try {
                    payload = new PollPayload(subredditName, binding.postTitleEditTextPostPollActivity.getText().toString(),
                            optionList.toArray(new String[0]), (int) binding.votingLengthSliderPostPollActivity.getValue(), isNSFW, isSpoiler, flair,
                            new RichTextJSONConverter().constructRichTextJSON(this, binding.postContentEditTextPostPollActivity.getText().toString(), uploadedImages),
                            null, binding.receivePostReplyNotificationsSwitchMaterialPostPollActivity.isChecked(),
                            subredditIsUser ? "profile" : "subreddit");
                } catch (JSONException e) {
                    Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.convert_to_richtext_json_failed, Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            payload = new PollPayload(subredditName, binding.postTitleEditTextPostPollActivity.getText().toString(),
                    optionList.toArray(new String[0]), (int) binding.votingLengthSliderPostPollActivity.getValue(), isNSFW, isSpoiler, flair,
                    binding.receivePostReplyNotificationsSwitchMaterialPostPollActivity.isChecked(),
                    subredditIsUser ? "profile" : "subreddit");
        }
        intent.putExtra(SubmitPostService.EXTRA_POLL_PAYLOAD, new Gson().toJson(payload));

        ContextCompat.startForegroundService(this, intent);
    }

    @Override
    public void onBackPressed() {
        if (isPosting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
        } else {
            if (!binding.postTitleEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.postContentEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.option1TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.option2TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.option3TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.option4TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.option5TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()
                    || !binding.option6TextInputLayoutEditTextPostPollActivity.getText().toString().isEmpty()) {
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

                binding.subredditNameTextViewPostPollActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostPollActivity.setText(subredditName);
                displaySubredditIcon();

                binding.flairCustomTextViewPostPollActivity.setVisibility(View.VISIBLE);
                binding.flairCustomTextViewPostPollActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostPollActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostPollActivity.setText(getString(R.string.flair));
                flair = null;
            } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                if (data == null) {
                    Toast.makeText(PostPollActivity.this, R.string.error_getting_image, Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        accessToken, binding.postContentEditTextPostPollActivity, binding.coordinatorLayoutPostPollActivity, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        accessToken, binding.postContentEditTextPostPollActivity, binding.coordinatorLayoutPostPollActivity, capturedImageUri, uploadedImages);
            } else if (requestCode == MARKDOWN_PREVIEW_REQUEST_CODE) {
                submitPost(mMenu.findItem(R.id.action_send_post_poll_activity));
            }
        }
    }

    @Override
    public void flairSelected(Flair flair) {
        this.flair = flair;
        binding.flairCustomTextViewPostPollActivity.setText(flair.getText());
        binding.flairCustomTextViewPostPollActivity.setBackgroundColor(flairBackgroundColor);
        binding.flairCustomTextViewPostPollActivity.setBorderColor(flairBackgroundColor);
        binding.flairCustomTextViewPostPollActivity.setTextColor(flairTextColor);
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
        int start = Math.max(binding.postContentEditTextPostPollActivity.getSelectionStart(), 0);
        int end = Math.max(binding.postContentEditTextPostPollActivity.getSelectionEnd(), 0);
        int realStart = Math.min(start, end);
        if (realStart > 0 && binding.postContentEditTextPostPollActivity.getText().toString().charAt(realStart - 1) != '\n') {
            binding.postContentEditTextPostPollActivity.getText().replace(realStart, Math.max(start, end),
                    "\n![](" + uploadedImage.imageUrlOrKey + ")\n",
                    0, "\n![]()\n".length() + uploadedImage.imageUrlOrKey.length());
        } else {
            binding.postContentEditTextPostPollActivity.getText().replace(realStart, Math.max(start, end),
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
                    .into(binding.accountIconGifImageViewPostPollActivity);

            binding.accountNameTextViewPostPollActivity.setText(selectedAccount.getAccountName());
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitPollPostEvent(SubmitPollPostEvent submitPollPostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        if (submitPollPostEvent.postSuccess) {
            Intent intent = new Intent(this, LinkResolverActivity.class);
            intent.setData(Uri.parse(submitPollPostEvent.postUrl));
            startActivity(intent);
            finish();
        } else {
            mMenu.findItem(R.id.action_send_post_poll_activity).setEnabled(true);
            mMenu.findItem(R.id.action_send_post_poll_activity).getIcon().setAlpha(255);
            if (submitPollPostEvent.errorMessage == null || submitPollPostEvent.errorMessage.isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostPollActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayoutPostPollActivity, submitPollPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitPollPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}