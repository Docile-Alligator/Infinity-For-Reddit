package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.r0adkll.slidr.Slidr;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.SavePostFilter;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizePostFilterActivity extends BaseActivity {

    public static final String EXTRA_POST_FILTER = "EPF";
    public static final String EXTRA_FROM_SETTINGS = "EFS";
    public static final String EXTRA_EXCLUDE_SUBREDDIT = "EES";
    public static final String EXTRA_EXCLUDE_USER = "EEU";
    public static final String EXTRA_EXCLUDE_FLAIR = "EEF";
    public static final String EXTRA_CONTAIN_FLAIR = "ECF";
    public static final String EXTRA_EXCLUDE_DOMAIN = "EED";
    public static final String EXTRA_CONTAIN_DOMAIN = "ECD";
    public static final String RETURN_EXTRA_POST_FILTER = "REPF";
    private static final String POST_FILTER_STATE = "PFS";
    private static final String ORIGINAL_NAME_STATE = "ONS";
    private static final int ADD_SUBREDDITS_REQUEST_CODE = 1;
    private static final int ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE = 2;
    private static final int ADD_USERS_REQUEST_CODE = 3;

    @BindView(R.id.coordinator_layout_customize_post_filter_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_customize_post_filter_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_customize_post_filter_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_customize_post_filter_activity)
    Toolbar toolbar;
    @BindView(R.id.name_text_input_layout_customize_post_filter_activity)
    TextInputLayout nameTextInputLayout;
    @BindView(R.id.name_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText nameTextInputEditText;
    @BindView(R.id.post_type_text_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeTextLinearLayout;
    @BindView(R.id.post_type_text_text_view_customize_post_filter_activity)
    TextView postTypeTextTextView;
    @BindView(R.id.post_type_text_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeTextCheckBox;
    @BindView(R.id.post_type_link_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeLinkLinearLayout;
    @BindView(R.id.post_type_link_text_view_customize_post_filter_activity)
    TextView postTypeLinkTextView;
    @BindView(R.id.post_type_link_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeLinkCheckBox;
    @BindView(R.id.post_type_image_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeImageLinearLayout;
    @BindView(R.id.post_type_image_text_view_customize_post_filter_activity)
    TextView postTypeImageTextView;
    @BindView(R.id.post_type_image_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeImageCheckBox;
    @BindView(R.id.post_type_gif_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeGifLinearLayout;
    @BindView(R.id.post_type_gif_text_view_customize_post_filter_activity)
    TextView postTypeGifTextView;
    @BindView(R.id.post_type_gif_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeGifCheckBox;
    @BindView(R.id.post_type_video_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeVideoLinearLayout;
    @BindView(R.id.post_type_video_text_view_customize_post_filter_activity)
    TextView postTypeVideoTextView;
    @BindView(R.id.post_type_video_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeVideoCheckBox;
    @BindView(R.id.post_type_gallery_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeGalleryLinearLayout;
    @BindView(R.id.post_type_gallery_text_view_customize_post_filter_activity)
    TextView postTypeGalleryTextView;
    @BindView(R.id.post_type_gallery_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeGalleryCheckBox;
    @BindView(R.id.only_nsfw_linear_layout_customize_post_filter_activity)
    LinearLayout onlyNSFWLinearLayout;
    @BindView(R.id.only_nsfw_text_view_customize_post_filter_activity)
    TextView onlyNSFWTextView;
    @BindView(R.id.only_nsfw_switch_customize_post_filter_activity)
    SwitchMaterial onlyNSFWSwitch;
    @BindView(R.id.only_spoiler_linear_layout_customize_post_filter_activity)
    LinearLayout onlySpoilerLinearLayout;
    @BindView(R.id.only_spoiler_text_view_customize_post_filter_activity)
    TextView onlySpoilerTextView;
    @BindView(R.id.only_spoiler_switch_customize_post_filter_activity)
    SwitchMaterial onlySpoilerSwitch;
    @BindView(R.id.title_excludes_strings_text_input_layout_customize_post_filter_activity)
    TextInputLayout titleExcludesStringsTextInputLayout;
    @BindView(R.id.title_excludes_strings_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText titleExcludesStringsTextInputEditText;
    @BindView(R.id.title_contains_strings_text_input_layout_customize_post_filter_activity)
    TextInputLayout titleContainsStringsTextInputLayout;
    @BindView(R.id.title_contains_strings_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText titleContainsStringsTextInputEditText;
    @BindView(R.id.title_excludes_regex_text_input_layout_customize_post_filter_activity)
    TextInputLayout titleExcludesRegexTextInputLayout;
    @BindView(R.id.title_excludes_regex_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText titleExcludesRegexTextInputEditText;
    @BindView(R.id.title_contains_regex_text_input_layout_customize_post_filter_activity)
    TextInputLayout titleContainsRegexTextInputLayout;
    @BindView(R.id.title_contains_regex_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText titleContainsRegexTextInputEditText;
    @BindView(R.id.excludes_subreddits_text_input_layout_customize_post_filter_activity)
    TextInputLayout excludesSubredditsTextInputLayout;
    @BindView(R.id.excludes_subreddits_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludesSubredditsTextInputEditText;
    @BindView(R.id.add_subreddits_image_view_customize_post_filter_activity)
    ImageView addSubredditsImageView;
    @BindView(R.id.excludes_users_text_input_layout_customize_post_filter_activity)
    TextInputLayout excludesUsersTextInputLayout;
    @BindView(R.id.excludes_users_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludesUsersTextInputEditText;
    @BindView(R.id.add_users_image_view_customize_post_filter_activity)
    ImageView addUsersImageView;
    @BindView(R.id.excludes_flairs_text_input_layout_customize_post_filter_activity)
    TextInputLayout excludesFlairsTextInputLayout;
    @BindView(R.id.excludes_flairs_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludesFlairsTextInputEditText;
    @BindView(R.id.contains_flairs_text_input_layout_customize_post_filter_activity)
    TextInputLayout containsFlairsTextInputLayout;
    @BindView(R.id.contains_flairs_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText containsFlairsTextInputEditText;
    @BindView(R.id.exclude_domains_text_input_layout_customize_post_filter_activity)
    TextInputLayout excludeDomainsTextInputLayout;
    @BindView(R.id.exclude_domains_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludeDomainsTextInputEditText;
    @BindView(R.id.contain_domains_text_input_layout_customize_post_filter_activity)
    TextInputLayout containDomainsTextInputLayout;
    @BindView(R.id.contain_domains_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText containDomainsTextInputEditText;
    @BindView(R.id.min_vote_text_input_layout_customize_post_filter_activity)
    TextInputLayout minVoteTextInputLayout;
    @BindView(R.id.min_vote_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText minVoteTextInputEditText;
    @BindView(R.id.max_vote_text_input_layout_customize_post_filter_activity)
    TextInputLayout maxVoteTextInputLayout;
    @BindView(R.id.max_vote_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText maxVoteTextInputEditText;
    @BindView(R.id.min_comments_text_input_layout_customize_post_filter_activity)
    TextInputLayout minCommentsTextInputLayout;
    @BindView(R.id.min_comments_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText minCommentsTextInputEditText;
    @BindView(R.id.max_comments_text_input_layout_customize_post_filter_activity)
    TextInputLayout maxCommentsTextInputLayout;
    @BindView(R.id.max_comments_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText maxCommentsTextInputEditText;
    @BindView(R.id.min_awards_text_input_layout_customize_post_filter_activity)
    TextInputLayout minAwardsTextInputLayout;
    @BindView(R.id.min_awards_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText minAwardsTextInputEditText;
    @BindView(R.id.max_awards_text_input_layout_customize_post_filter_activity)
    TextInputLayout maxAwardsTextInputLayout;
    @BindView(R.id.max_awards_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText maxAwardsTextInputEditText;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences currentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private PostFilter postFilter;
    private boolean fromSettings;
    private String originalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_post_filter);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);

        postTypeTextLinearLayout.setOnClickListener(view -> {
            postTypeTextCheckBox.performClick();
        });

        postTypeLinkLinearLayout.setOnClickListener(view -> {
            postTypeLinkCheckBox.performClick();
        });

        postTypeImageLinearLayout.setOnClickListener(view -> {
            postTypeImageCheckBox.performClick();
        });

        postTypeGifLinearLayout.setOnClickListener(view -> {
            postTypeGifCheckBox.performClick();
        });

        postTypeVideoLinearLayout.setOnClickListener(view -> {
            postTypeVideoCheckBox.performClick();
        });

        postTypeGalleryLinearLayout.setOnClickListener(view -> {
            postTypeGalleryCheckBox.performClick();
        });

        onlyNSFWLinearLayout.setOnClickListener(view -> {
            onlyNSFWSwitch.performClick();
        });

        onlySpoilerLinearLayout.setOnClickListener(view -> {
            onlySpoilerSwitch.performClick();
        });

        String accessToken = currentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        addSubredditsImageView.setOnClickListener(view -> {
            if (accessToken == null) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, true);
                intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
                startActivityForResult(intent, ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE);
            } else {
                Intent intent = new Intent(this, SubredditMultiselectionActivity.class);
                startActivityForResult(intent, ADD_SUBREDDITS_REQUEST_CODE);
            }
        });

        addUsersImageView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
            intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
            startActivityForResult(intent, ADD_USERS_REQUEST_CODE);
        });

        if (savedInstanceState != null) {
            postFilter = savedInstanceState.getParcelable(POST_FILTER_STATE);
            originalName = savedInstanceState.getString(ORIGINAL_NAME_STATE);
        } else {
            postFilter = getIntent().getParcelableExtra(EXTRA_POST_FILTER);
            if (postFilter == null) {
                postFilter = new PostFilter();
                originalName = "";
            } else {
                if (!fromSettings) {
                    originalName = "";
                } else {
                    originalName = postFilter.name;
                }
            }
            bindView();
        }
    }

    private void bindView() {
        nameTextInputEditText.setText(postFilter.name);
        postTypeTextCheckBox.setChecked(postFilter.containTextType);
        postTypeLinkCheckBox.setChecked(postFilter.containLinkType);
        postTypeImageCheckBox.setChecked(postFilter.containImageType);
        postTypeGifCheckBox.setChecked(postFilter.containGifType);
        postTypeVideoCheckBox.setChecked(postFilter.containVideoType);
        postTypeGalleryCheckBox.setChecked(postFilter.containGalleryType);
        onlyNSFWSwitch.setChecked(postFilter.onlyNSFW);
        onlySpoilerSwitch.setChecked(postFilter.onlySpoiler);
        titleExcludesStringsTextInputEditText.setText(postFilter.postTitleExcludesStrings);
        titleContainsStringsTextInputEditText.setText(postFilter.postTitleContainsStrings);
        titleExcludesRegexTextInputEditText.setText(postFilter.postTitleExcludesRegex);
        titleContainsRegexTextInputEditText.setText(postFilter.postTitleContainsRegex);
        excludesSubredditsTextInputEditText.setText(postFilter.excludeSubreddits);
        excludesUsersTextInputEditText.setText(postFilter.excludeUsers);
        excludesFlairsTextInputEditText.setText(postFilter.excludeFlairs);
        containsFlairsTextInputEditText.setText(postFilter.containFlairs);
        excludeDomainsTextInputEditText.setText(postFilter.excludeDomains);
        containDomainsTextInputEditText.setText(postFilter.containDomains);
        minVoteTextInputEditText.setText(Integer.toString(postFilter.minVote));
        maxVoteTextInputEditText.setText(Integer.toString(postFilter.maxVote));
        minCommentsTextInputEditText.setText(Integer.toString(postFilter.minComments));
        maxCommentsTextInputEditText.setText(Integer.toString(postFilter.maxComments));
        minAwardsTextInputEditText.setText(Integer.toString(postFilter.minAwards));
        maxAwardsTextInputEditText.setText(Integer.toString(postFilter.maxAwards));

        Intent intent = getIntent();
        String excludeSubreddit = intent.getStringExtra(EXTRA_EXCLUDE_SUBREDDIT);
        String excludeUser = intent.getStringExtra(EXTRA_EXCLUDE_USER);
        String excludeFlair = intent.getStringExtra(EXTRA_EXCLUDE_FLAIR);
        String containFlair = intent.getStringExtra(EXTRA_CONTAIN_FLAIR);
        String excludeDomain = intent.getStringExtra(EXTRA_EXCLUDE_DOMAIN);
        String containDomain = intent.getStringExtra(EXTRA_CONTAIN_DOMAIN);

        if (excludeSubreddit != null && !excludeSubreddit.equals("")) {
            if (!excludesSubredditsTextInputEditText.getText().toString().equals("")) {
                excludesSubredditsTextInputEditText.append(",");
            }
            excludesSubredditsTextInputEditText.append(excludeSubreddit);
        }
        if (excludeUser != null && !excludeUser.equals("")) {
            if (!excludesUsersTextInputEditText.getText().toString().equals("")) {
                excludesUsersTextInputEditText.append(",");
            }
            excludesUsersTextInputEditText.append(excludeUser);
        }
        if (excludeFlair != null && !excludeFlair.equals("")) {
            if (!excludesFlairsTextInputEditText.getText().toString().equals("")) {
                excludesFlairsTextInputEditText.append(",");
            }
            excludesFlairsTextInputEditText.append(excludeFlair);
        }
        if (containFlair != null && !containFlair.equals("")) {
            if (!containsFlairsTextInputEditText.getText().toString().equals("")) {
                containsFlairsTextInputEditText.append(",");
            }
            containsFlairsTextInputEditText.append(containFlair);
        }
        if (excludeDomain != null && !excludeDomain.equals("")) {
            if (!excludeDomainsTextInputEditText.getText().toString().equals("")) {
                excludeDomainsTextInputEditText.append(",");
            }
            excludeDomainsTextInputEditText.append(Uri.parse(excludeDomain).getHost());
        }
        if (containDomain != null && !containDomain.equals("")) {
            if (!containDomainsTextInputEditText.getText().toString().equals("")) {
                containDomainsTextInputEditText.append(",");
            }
            containDomainsTextInputEditText.append(Uri.parse(containDomain).getHost());
        }
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int primaryIconColor = mCustomThemeWrapper.getPrimaryIconColor();
        Drawable cursorDrawable = Utils.getTintedDrawable(this, R.drawable.edit_text_cursor, primaryTextColor);
        nameTextInputLayout.setBoxStrokeColor(primaryTextColor);
        nameTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        nameTextInputEditText.setTextColor(primaryTextColor);
        postTypeTextTextView.setTextColor(primaryTextColor);
        postTypeLinkTextView.setTextColor(primaryTextColor);
        postTypeImageTextView.setTextColor(primaryTextColor);
        postTypeGifTextView.setTextColor(primaryTextColor);
        postTypeVideoTextView.setTextColor(primaryTextColor);
        postTypeGalleryTextView.setTextColor(primaryTextColor);
        onlyNSFWTextView.setTextColor(primaryTextColor);
        onlySpoilerTextView.setTextColor(primaryTextColor);
        titleExcludesStringsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        titleExcludesStringsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        titleExcludesStringsTextInputEditText.setTextColor(primaryTextColor);
        titleContainsStringsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        titleContainsStringsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        titleContainsStringsTextInputEditText.setTextColor(primaryTextColor);
        titleExcludesRegexTextInputLayout.setBoxStrokeColor(primaryTextColor);
        titleExcludesRegexTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        titleExcludesRegexTextInputEditText.setTextColor(primaryTextColor);
        titleContainsRegexTextInputLayout.setBoxStrokeColor(primaryTextColor);
        titleContainsRegexTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        titleContainsRegexTextInputEditText.setTextColor(primaryTextColor);
        excludesSubredditsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        excludesSubredditsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        excludesSubredditsTextInputEditText.setTextColor(primaryTextColor);
        addSubredditsImageView.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));
        excludesUsersTextInputLayout.setBoxStrokeColor(primaryTextColor);
        excludesUsersTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        excludesUsersTextInputEditText.setTextColor(primaryTextColor);
        addUsersImageView.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));
        excludesFlairsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        excludesFlairsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        excludesFlairsTextInputEditText.setTextColor(primaryTextColor);
        containsFlairsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        containsFlairsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        containsFlairsTextInputEditText.setTextColor(primaryTextColor);
        excludeDomainsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        excludeDomainsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        excludeDomainsTextInputEditText.setTextColor(primaryTextColor);
        containDomainsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        containDomainsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        containDomainsTextInputEditText.setTextColor(primaryTextColor);
        minVoteTextInputLayout.setBoxStrokeColor(primaryTextColor);
        minVoteTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        minVoteTextInputEditText.setTextColor(primaryTextColor);
        maxVoteTextInputLayout.setBoxStrokeColor(primaryTextColor);
        maxVoteTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        maxVoteTextInputEditText.setTextColor(primaryTextColor);
        minCommentsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        minCommentsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        minCommentsTextInputEditText.setTextColor(primaryTextColor);
        maxCommentsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        maxCommentsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        maxCommentsTextInputEditText.setTextColor(primaryTextColor);
        minAwardsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        minAwardsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        minAwardsTextInputEditText.setTextColor(primaryTextColor);
        maxAwardsTextInputLayout.setBoxStrokeColor(primaryTextColor);
        maxAwardsTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        maxAwardsTextInputEditText.setTextColor(primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            nameTextInputEditText.setTextCursorDrawable(cursorDrawable);
            titleExcludesStringsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            titleContainsStringsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            titleExcludesRegexTextInputEditText.setTextCursorDrawable(cursorDrawable);
            titleContainsRegexTextInputEditText.setTextCursorDrawable(cursorDrawable);
            excludesSubredditsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            excludesUsersTextInputEditText.setTextCursorDrawable(cursorDrawable);
            excludesFlairsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            containsFlairsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            excludeDomainsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            containDomainsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            minVoteTextInputEditText.setTextCursorDrawable(cursorDrawable);
            maxVoteTextInputEditText.setTextCursorDrawable(cursorDrawable);
            minCommentsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            maxCommentsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            minAwardsTextInputEditText.setTextCursorDrawable(cursorDrawable);
            maxAwardsTextInputEditText.setTextCursorDrawable(cursorDrawable);
        } else {
            setCursorDrawableColor(nameTextInputEditText, primaryTextColor);
            setCursorDrawableColor(titleExcludesStringsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(titleContainsStringsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(titleExcludesRegexTextInputEditText, primaryTextColor);
            setCursorDrawableColor(titleContainsRegexTextInputEditText, primaryTextColor);
            setCursorDrawableColor(excludesSubredditsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(excludesUsersTextInputEditText, primaryTextColor);
            setCursorDrawableColor(excludesFlairsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(containsFlairsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(excludeDomainsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(containDomainsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(minVoteTextInputEditText, primaryTextColor);
            setCursorDrawableColor(maxVoteTextInputEditText, primaryTextColor);
            setCursorDrawableColor(minCommentsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(maxCommentsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(minAwardsTextInputEditText, primaryTextColor);
            setCursorDrawableColor(maxAwardsTextInputEditText, primaryTextColor);
        }

        if (typeface != null) {
            Utils.setFontToAllTextViews(coordinatorLayout, typeface);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customize_post_filter_activity, menu);
        if (fromSettings) {
            menu.findItem(R.id.action_save_customize_post_filter_activity).setVisible(false);
        }
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save_customize_post_filter_activity) {
            constructPostFilter();
            Intent returnIntent = new Intent();
            returnIntent.putExtra(RETURN_EXTRA_POST_FILTER, postFilter);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();

            return true;
        } else if (item.getItemId() == R.id.action_save_to_database_customize_post_filter_activity) {
            constructPostFilter();

            if (!postFilter.name.equals("")) {
                savePostFilter(originalName);
            } else {
                Toast.makeText(CustomizePostFilterActivity.this, R.string.post_filter_requires_a_name, Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    private void savePostFilter(String originalName) {
        SavePostFilter.savePostFilter(mExecutor, new Handler(), mRedditDataRoomDatabase, postFilter, originalName,
                new SavePostFilter.SavePostFilterListener() {
                    @Override
                    public void success() {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(RETURN_EXTRA_POST_FILTER, postFilter);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }

                    @Override
                    public void duplicate() {
                        new MaterialAlertDialogBuilder(CustomizePostFilterActivity.this, R.style.MaterialAlertDialogTheme)
                                .setTitle(getString(R.string.duplicate_post_filter_dialog_title, postFilter.name))
                                .setMessage(R.string.duplicate_post_filter_dialog_message)
                                .setPositiveButton(R.string.override, (dialogInterface, i) -> savePostFilter(postFilter.name))
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_SUBREDDITS_REQUEST_CODE) {
                ArrayList<String> subredditNames = data.getStringArrayListExtra(SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS);
                updateExcludeSubredditNames(subredditNames);
            } else if (requestCode == ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE) {
                ArrayList<String> subredditNames = data.getStringArrayListExtra(SearchActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES);
                updateExcludeSubredditNames(subredditNames);
            } else if (requestCode == ADD_USERS_REQUEST_CODE) {
                ArrayList<String> usernames = data.getStringArrayListExtra(SearchActivity.RETURN_EXTRA_SELECTED_USERNAMES);
                String currentUsers = excludesUsersTextInputEditText.getText().toString().trim();
                if (usernames != null && !usernames.isEmpty()) {
                    if (!currentUsers.isEmpty() && currentUsers.charAt(currentUsers.length() - 1) != ',') {
                        String newString = currentUsers + ",";
                        excludesUsersTextInputEditText.setText(newString);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String s : usernames) {
                        stringBuilder.append(s).append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    excludesUsersTextInputEditText.append(stringBuilder.toString());
                }
            }
        }
    }

    private void updateExcludeSubredditNames(ArrayList<String> subredditNames) {
        String currentSubreddits = excludesSubredditsTextInputEditText.getText().toString().trim();
        if (subredditNames != null && !subredditNames.isEmpty()) {
            if (!currentSubreddits.isEmpty() && currentSubreddits.charAt(currentSubreddits.length() - 1) != ',') {
                String newString = currentSubreddits + ",";
                excludesSubredditsTextInputEditText.setText(newString);
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : subredditNames) {
                stringBuilder.append(s).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            excludesSubredditsTextInputEditText.append(stringBuilder.toString());
        }
    }

    private void constructPostFilter() {
        postFilter.name = nameTextInputEditText.getText().toString();
        postFilter.maxVote = maxVoteTextInputEditText.getText() == null || maxVoteTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(maxVoteTextInputEditText.getText().toString());
        postFilter.minVote = minVoteTextInputEditText.getText() == null || minVoteTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(minVoteTextInputEditText.getText().toString());
        postFilter.maxComments = maxCommentsTextInputEditText.getText() == null || maxCommentsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(maxCommentsTextInputEditText.getText().toString());
        postFilter.minComments = minCommentsTextInputEditText.getText() == null || minCommentsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(minCommentsTextInputEditText.getText().toString());
        postFilter.maxAwards = maxAwardsTextInputEditText.getText() == null || maxAwardsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(maxAwardsTextInputEditText.getText().toString());
        postFilter.minAwards = minAwardsTextInputEditText.getText() == null || minAwardsTextInputEditText.getText().toString().equals("") ? -1 : Integer.parseInt(minAwardsTextInputEditText.getText().toString());
        postFilter.postTitleExcludesRegex = titleExcludesRegexTextInputEditText.getText().toString();
        postFilter.postTitleContainsRegex = titleContainsRegexTextInputEditText.getText().toString();
        postFilter.postTitleExcludesStrings = titleExcludesStringsTextInputEditText.getText().toString();
        postFilter.postTitleContainsStrings = titleContainsStringsTextInputEditText.getText().toString();
        postFilter.excludeSubreddits = excludesSubredditsTextInputEditText.getText().toString();
        postFilter.excludeUsers = excludesUsersTextInputEditText.getText().toString();
        postFilter.excludeFlairs = excludesFlairsTextInputEditText.getText().toString();
        postFilter.containFlairs = containsFlairsTextInputEditText.getText().toString();
        postFilter.excludeDomains = excludeDomainsTextInputEditText.getText().toString();
        postFilter.containDomains = containDomainsTextInputEditText.getText().toString();
        postFilter.containTextType = postTypeTextCheckBox.isChecked();
        postFilter.containLinkType = postTypeLinkCheckBox.isChecked();
        postFilter.containImageType = postTypeImageCheckBox.isChecked();
        postFilter.containGifType = postTypeGifCheckBox.isChecked();
        postFilter.containVideoType = postTypeVideoCheckBox.isChecked();
        postFilter.containGalleryType = postTypeGalleryCheckBox.isChecked();
        postFilter.onlyNSFW = onlyNSFWSwitch.isChecked();
        postFilter.onlySpoiler = onlySpoilerSwitch.isChecked();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(POST_FILTER_STATE, postFilter);
        outState.putString(ORIGINAL_NAME_STATE, originalName);
    }
}