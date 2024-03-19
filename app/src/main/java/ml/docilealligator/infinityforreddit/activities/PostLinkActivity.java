package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
import ml.docilealligator.infinityforreddit.apis.TitleSuggestion;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityPostLinkBinding;
import ml.docilealligator.infinityforreddit.events.SubmitTextOrLinkPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PostLinkActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback,
        AccountChooserBottomSheetFragment.AccountChooserListener {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_LINK = "EL";

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

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
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
    private Menu mMemu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;
    private ActivityPostLinkBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        binding = ActivityPostLinkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutPostLinkActivity);
        }

        setSupportActionBar(binding.toolbarPostLinkActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(binding.coordinatorLayoutPostLinkActivity, R.string.posting, Snackbar.LENGTH_INDEFINITE);

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
                        .into(binding.accountIconGifImageViewPostLinkActivity);

                binding.accountNameTextViewPostLinkActivity.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (subredditName != null) {
                binding.subredditNameTextViewPostLinkActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostLinkActivity.setText(subredditName);
                binding.flairCustomTextViewPostLinkActivity.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                binding.flairCustomTextViewPostLinkActivity.setText(flair.getText());
                binding.flairCustomTextViewPostLinkActivity.setBackgroundColor(flairBackgroundColor);
                binding.flairCustomTextViewPostLinkActivity.setBorderColor(flairBackgroundColor);
                binding.flairCustomTextViewPostLinkActivity.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                binding.spoilerCustomTextViewPostLinkActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostLinkActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostLinkActivity.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                binding.nsfwCustomTextViewPostLinkActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostLinkActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostLinkActivity.setTextColor(nsfwTextColor);
            }
        } else {
            isPosting = false;

            loadCurrentAccount();

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                binding.subredditNameTextViewPostLinkActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostLinkActivity.setText(subredditName);
                binding.flairCustomTextViewPostLinkActivity.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(binding.subredditIconGifImageViewPostLinkActivity);
            }

            String link = getIntent().getStringExtra(EXTRA_LINK);
            if (link != null) {
                binding.postLinkEditTextPostLinkActivity.setText(link);
            }
        }

        binding.accountLinearLayoutPostLinkActivity.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.subredditRelativeLayoutPostLinkActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        binding.rulesButtonPostLinkActivity.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(binding.coordinatorLayoutPostLinkActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
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

        binding.flairCustomTextViewPostLinkActivity.setOnClickListener(view -> {
            if (flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                binding.flairCustomTextViewPostLinkActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostLinkActivity.setText(getString(R.string.flair));
                flair = null;
            }
        });

        binding.spoilerCustomTextViewPostLinkActivity.setOnClickListener(view -> {
            if (!isSpoiler) {
                binding.spoilerCustomTextViewPostLinkActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostLinkActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostLinkActivity.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                binding.spoilerCustomTextViewPostLinkActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.spoilerCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        binding.nsfwCustomTextViewPostLinkActivity.setOnClickListener(view -> {
            if (!isNSFW) {
                binding.nsfwCustomTextViewPostLinkActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostLinkActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostLinkActivity.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                binding.nsfwCustomTextViewPostLinkActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.nsfwCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        binding.receivePostReplyNotificationsLinearLayoutPostLinkActivity.setOnClickListener(view -> {
            binding.receivePostReplyNotificationsSwitchMaterialPostLinkActivity.performClick();
        });

        binding.suggestTitleButtonPostLinkActivity.setOnClickListener(view -> {
            Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
            String url = binding.postLinkEditTextPostLinkActivity.getText().toString().trim();
            if (!URLUtil.isHttpsUrl(url) && !URLUtil.isHttpUrl(url)) {
                url = "https://" + url;
            }
            mRetrofit.newBuilder()
                    .baseUrl("http://localhost/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build().create(TitleSuggestion.class).getHtml(url).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        String body = response.body();
                        if (body != null) {
                            int start = body.indexOf("<title>");
                            if (start >= 0) {
                                int end = body.indexOf("</title>");
                                if (end > start) {
                                    binding.postTitleEditTextPostLinkActivity.setText(body.substring(start + 7, end));
                                    return;
                                }
                            }
                        }

                        Toast.makeText(PostLinkActivity.this, R.string.suggest_title_failed, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PostLinkActivity.this, R.string.suggest_title_failed, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(PostLinkActivity.this, R.string.suggest_title_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        PostLinkActivity.this, binding.postContentEditTextPostLinkActivity, item);
            }

            @Override
            public void onUploadImage() {

            }
        });

        binding.markdownBottomBarRecyclerViewPostLinkActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewPostLinkActivity.setAdapter(adapter);
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
                            .into(binding.accountIconGifImageViewPostLinkActivity);

                    binding.accountNameTextViewPostLinkActivity.setText(account.getAccountName());
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
        binding.coordinatorLayoutPostLinkActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostLinkActivity, null, binding.toolbarPostLinkActivity);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.accountNameTextViewPostLinkActivity.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.subredditNameTextViewPostLinkActivity.setTextColor(secondaryTextColor);
        binding.rulesButtonPostLinkActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.rulesButtonPostLinkActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.receivePostReplyNotificationsTextViewPostLinkActivity.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1PostLinkActivity.setDividerColor(dividerColor);
        binding.divider2PostLinkActivity.setDividerColor(dividerColor);
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        binding.flairCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
        binding.spoilerCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
        binding.nsfwCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostLinkActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostLinkActivity.setHintTextColor(secondaryTextColor);
        binding.suggestTitleButtonPostLinkActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.suggestTitleButtonPostLinkActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.postLinkEditTextPostLinkActivity.setTextColor(primaryTextColor);
        binding.postLinkEditTextPostLinkActivity.setHintTextColor(secondaryTextColor);
        binding.postContentEditTextPostLinkActivity.setTextColor(primaryTextColor);
        binding.postContentEditTextPostLinkActivity.setHintTextColor(secondaryTextColor);
        if (typeface != null) {
            binding.subredditNameTextViewPostLinkActivity.setTypeface(typeface);
            binding.rulesButtonPostLinkActivity.setTypeface(typeface);
            binding.receivePostReplyNotificationsTextViewPostLinkActivity.setTypeface(typeface);
            binding.flairCustomTextViewPostLinkActivity.setTypeface(typeface);
            binding.spoilerCustomTextViewPostLinkActivity.setTypeface(typeface);
            binding.nsfwCustomTextViewPostLinkActivity.setTypeface(typeface);
            binding.postTitleEditTextPostLinkActivity.setTypeface(typeface);
            binding.suggestTitleButtonPostLinkActivity.setTypeface(typeface);
        }
        if (contentTypeface != null) {
            binding.postContentEditTextPostLinkActivity.setTypeface(contentTypeface);
            binding.postLinkEditTextPostLinkActivity.setTypeface(contentTypeface);
        }
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.isEmpty()) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.subredditIconGifImageViewPostLinkActivity);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageViewPostLinkActivity);
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
                .setPositiveButton(R.string.discard_dialog_button, (dialogInterface, i) -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_link_activity, menu);
        applyMenuItemTheme(menu);
        mMemu = menu;
        if (isPosting) {
            mMemu.findItem(R.id.action_send_post_link_activity).setEnabled(false);
            mMemu.findItem(R.id.action_send_post_link_activity).getIcon().setAlpha(130);
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
                if (!binding.postTitleEditTextPostLinkActivity.getText().toString().isEmpty()
                        || !binding.postContentEditTextPostLinkActivity.getText().toString().isEmpty()
                        || !binding.postLinkEditTextPostLinkActivity.getText().toString().isEmpty()) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_send_post_link_activity) {
            if (!subredditSelected) {
                Snackbar.make(binding.coordinatorLayoutPostLinkActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (binding.postTitleEditTextPostLinkActivity.getText() == null || binding.postTitleEditTextPostLinkActivity.getText().toString().isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostLinkActivity, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (binding.postLinkEditTextPostLinkActivity.getText() == null || binding.postLinkEditTextPostLinkActivity.getText().toString().isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostLinkActivity, R.string.link_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            isPosting = true;

            item.setEnabled(false);
            item.getIcon().setAlpha(130);

            mPostingSnackbar.show();

            String subredditName;
            if (subredditIsUser) {
                subredditName = "u_" + binding.subredditNameTextViewPostLinkActivity.getText().toString();
            } else {
                subredditName = binding.subredditNameTextViewPostLinkActivity.getText().toString();
            }

            Intent intent = new Intent(this, SubmitPostService.class);
            intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
            intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(SubmitPostService.EXTRA_TITLE, binding.postTitleEditTextPostLinkActivity.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_CONTENT, binding.postContentEditTextPostLinkActivity.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_URL, binding.postLinkEditTextPostLinkActivity.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_KIND, APIUtils.KIND_LINK);
            intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
            intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
            intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
            intent.putExtra(SubmitPostService.EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, binding.receivePostReplyNotificationsSwitchMaterialPostLinkActivity.isChecked());
            intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TEXT_OR_LINK);
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
            if (!binding.postTitleEditTextPostLinkActivity.getText().toString().isEmpty()
                    || !binding.postContentEditTextPostLinkActivity.getText().toString().isEmpty()
                    || !binding.postLinkEditTextPostLinkActivity.getText().toString().isEmpty()) {
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

                binding.subredditNameTextViewPostLinkActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostLinkActivity.setText(subredditName);
                displaySubredditIcon();

                binding.flairCustomTextViewPostLinkActivity.setVisibility(View.VISIBLE);
                binding.flairCustomTextViewPostLinkActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostLinkActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostLinkActivity.setText(getString(R.string.flair));
                flair = null;
            }/* else if (requestCode == MARKDOWN_PREVIEW_REQUEST_CODE) {
                submitPost(mMenu.findItem(R.id.action_send_post_text_activity));
            }*/
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
        binding.flairCustomTextViewPostLinkActivity.setText(flair.getText());
        binding.flairCustomTextViewPostLinkActivity.setBackgroundColor(flairBackgroundColor);
        binding.flairCustomTextViewPostLinkActivity.setBorderColor(flairBackgroundColor);
        binding.flairCustomTextViewPostLinkActivity.setTextColor(flairTextColor);
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.accountIconGifImageViewPostLinkActivity);

            binding.accountNameTextViewPostLinkActivity.setText(selectedAccount.getAccountName());
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitLinkPostEvent(SubmitTextOrLinkPostEvent submitTextOrLinkPostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        if (submitTextOrLinkPostEvent.postSuccess) {
            Intent intent = new Intent(PostLinkActivity.this, ViewPostDetailActivity.class);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, submitTextOrLinkPostEvent.post);
            startActivity(intent);
            finish();
        } else {
            mMemu.findItem(R.id.action_send_post_link_activity).setEnabled(true);
            mMemu.findItem(R.id.action_send_post_link_activity).getIcon().setAlpha(255);
            if (submitTextOrLinkPostEvent.errorMessage == null || submitTextOrLinkPostEvent.errorMessage.isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostLinkActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayoutPostLinkActivity, submitTextOrLinkPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitTextOrLinkPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
