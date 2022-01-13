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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
import ml.docilealligator.infinityforreddit.apis.TitleSuggestion;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SubmitTextOrLinkPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PostLinkActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_LINK = "EL";

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

    @BindView(R.id.coordinator_layout_post_link_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_post_link_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_post_link_activity)
    Toolbar toolbar;
    @BindView(R.id.subreddit_icon_gif_image_view_post_link_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_post_link_activity)
    TextView subredditNameTextView;
    @BindView(R.id.rules_button_post_link_activity)
    MaterialButton rulesButton;
    @BindView(R.id.divider_1_post_link_activity)
    MaterialDivider divider1;
    @BindView(R.id.divider_2_post_link_activity)
    MaterialDivider divider2;
    @BindView(R.id.flair_custom_text_view_post_link_activity)
    CustomTextView flairTextView;
    @BindView(R.id.spoiler_custom_text_view_post_link_activity)
    CustomTextView spoilerTextView;
    @BindView(R.id.nsfw_custom_text_view_post_link_activity)
    CustomTextView nsfwTextView;
    @BindView(R.id.receive_post_reply_notifications_linear_layout_post_link_activity)
    LinearLayout receivePostReplyNotificationsLinearLayout;
    @BindView(R.id.receive_post_reply_notifications_text_view_post_link_activity)
    TextView receivePostReplyNotificationsTextView;
    @BindView(R.id.receive_post_reply_notifications_switch_material_post_link_activity)
    SwitchMaterial receivePostReplyNotificationsSwitchMaterial;
    @BindView(R.id.post_title_edit_text_post_link_activity)
    EditText titleEditText;
    @BindView(R.id.suggest_title_button_post_link_activity)
    MaterialButton suggestTitleButton;
    @BindView(R.id.post_link_edit_text_post_link_activity)
    EditText linkEditText;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
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
    private String mAccessToken;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_link);

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

            String link = getIntent().getStringExtra(EXTRA_LINK);
            if (link != null) {
                linkEditText.setText(link);
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

        suggestTitleButton.setOnClickListener(view -> {
            Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
            String url = linkEditText.getText().toString().trim();
            if (!url.startsWith("https://") || !url.startsWith("http://")) {
                url = "https://" + url;
            }
            new Retrofit.Builder()
                    .baseUrl("http://localhost/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build().create(TitleSuggestion.class).getHtml(url).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        titleEditText.setText(response.body().substring(response.body().indexOf("<title>") + 7, response.body().indexOf("</title>")));
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
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        subredditNameTextView.setTextColor(secondaryTextColor);
        rulesButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        rulesButton.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
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
        suggestTitleButton.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        suggestTitleButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        linkEditText.setTextColor(primaryTextColor);
        linkEditText.setHintTextColor(secondaryTextColor);
        if (typeface != null) {
            subredditNameTextView.setTypeface(typeface);
            rulesButton.setTypeface(typeface);
            receivePostReplyNotificationsTextView.setTypeface(typeface);
            flairTextView.setTypeface(typeface);
            spoilerTextView.setTypeface(typeface);
            nsfwTextView.setTypeface(typeface);
            titleEditText.setTypeface(typeface);
            suggestTitleButton.setTypeface(typeface);
        }
        if (contentTypeface != null) {
            linkEditText.setTypeface(contentTypeface);
        }
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
        LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditName,
                mAccessToken, mOauthRetrofit, mRetrofit, iconImageUrl -> {
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
                if (!titleEditText.getText().toString().equals("") || !linkEditText.getText().toString().equals("")) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_send_post_link_activity) {
            if (!subredditSelected) {
                Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (titleEditText.getText() == null || titleEditText.getText().toString().equals("")) {
                Snackbar.make(coordinatorLayout, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (linkEditText.getText() == null || linkEditText.getText().toString().equals("")) {
                Snackbar.make(coordinatorLayout, R.string.link_required, Snackbar.LENGTH_SHORT).show();
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
            intent.putExtra(SubmitPostService.EXTRA_ACCESS_TOKEN, mAccessToken);
            intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(SubmitPostService.EXTRA_TITLE, titleEditText.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_CONTENT, linkEditText.getText().toString());
            intent.putExtra(SubmitPostService.EXTRA_KIND, APIUtils.KIND_LINK);
            intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
            intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
            intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
            intent.putExtra(SubmitPostService.EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, receivePostReplyNotificationsSwitchMaterial.isChecked());
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
            if (!titleEditText.getText().toString().equals("") || !linkEditText.getText().toString().equals("")) {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
            if (submitTextOrLinkPostEvent.errorMessage == null || submitTextOrLinkPostEvent.errorMessage.equals("")) {
                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, submitTextOrLinkPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitTextOrLinkPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
