package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.r0adkll.slidr.Slidr;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class CustomizePostFilterActivity extends BaseActivity {

    @BindView(R.id.coordinator_layout_customize_post_filter_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_customize_post_filter_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_customize_post_filter_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_customize_post_filter_activity)
    Toolbar toolbar;
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
    @BindView(R.id.post_type_video_linear_layout_customize_post_filter_activity)
    LinearLayout postTypeVideoLinearLayout;
    @BindView(R.id.post_type_video_text_view_customize_post_filter_activity)
    TextView postTypeVideoTextView;
    @BindView(R.id.post_type_video_check_box_customize_post_filter_activity)
    MaterialCheckBox postTypeVideoCheckBox;
    @BindView(R.id.title_excludes_strings_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText titleExcludesStringsTextInputEditText;
    @BindView(R.id.title_excludes_regex_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText titleExcludesRegexTextInputEditText;
    @BindView(R.id.excludes_subreddits_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludesSubredditsTextInputEditText;
    @BindView(R.id.excludes_users_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludesUsersTextInputEditText;
    @BindView(R.id.excludes_flairs_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText excludesFlairsTextInputEditText;
    @BindView(R.id.contains_flairs_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText containsFlairsTextInputEditText;
    @BindView(R.id.min_vote_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText minVoteTextInputEditText;
    @BindView(R.id.max_vote_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText maxVoteTextInputEditText;
    @BindView(R.id.min_comments_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText minCommentsTextInputEditText;
    @BindView(R.id.max_comments_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText maxCommentsTextInputEditText;
    @BindView(R.id.min_awards_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText minAwardsTextInputEditText;
    @BindView(R.id.max_awards_text_input_edit_text_customize_post_filter_activity)
    TextInputEditText maxAwardsTextInputEditText;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

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
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        titleExcludesStringsTextInputEditText.setTextColor(primaryTextColor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customize_post_filter_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save_customize_post_filter_activity) {

        }
        return false;
    }
}