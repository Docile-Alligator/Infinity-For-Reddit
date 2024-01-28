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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityCustomizePostFilterBinding;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.SavePostFilter;
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
    public static final String EXTRA_START_FILTERED_POSTS_WHEN_FINISH = "ESFPWF";
    public static final String RETURN_EXTRA_POST_FILTER = "REPF";
    private static final String POST_FILTER_STATE = "PFS";
    private static final String ORIGINAL_NAME_STATE = "ONS";
    private static final int ADD_SUBREDDITS_REQUEST_CODE = 1;
    private static final int ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE = 2;
    private static final int ADD_USERS_REQUEST_CODE = 3;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences currentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private ActivityCustomizePostFilterBinding binding;
    private PostFilter postFilter;
    private boolean fromSettings;
    private String originalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityCustomizePostFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutCustomizePostFilterActivity);
        }

        setSupportActionBar(binding.toolbarCustomizePostFilterActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarCustomizePostFilterActivity);

        fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);

        binding.postTypeTextLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.postTypeTextCheckBoxCustomizePostFilterActivity.performClick();
        });

        binding.postTypeLinkLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.postTypeLinkCheckBoxCustomizePostFilterActivity.performClick();
        });

        binding.postTypeImageLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.postTypeImageCheckBoxCustomizePostFilterActivity.performClick();
        });

        binding.postTypeGifLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.postTypeGifCheckBoxCustomizePostFilterActivity.performClick();
        });

        binding.postTypeVideoLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.postTypeVideoCheckBoxCustomizePostFilterActivity.performClick();
        });

        binding.postTypeGalleryLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.postTypeGalleryCheckBoxCustomizePostFilterActivity.performClick();
        });

        binding.onlyNsfwLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.onlyNsfwSwitchCustomizePostFilterActivity.performClick();
        });

        binding.onlySpoilerLinearLayoutCustomizePostFilterActivity.setOnClickListener(view -> {
            binding.onlySpoilerSwitchCustomizePostFilterActivity.performClick();
        });

        binding.addSubredditsImageViewCustomizePostFilterActivity.setOnClickListener(view -> {
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, true);
                intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
                startActivityForResult(intent, ADD_SUBREDDITS_ANONYMOUS_REQUEST_CODE);
            } else {
                Intent intent = new Intent(this, SubredditMultiselectionActivity.class);
                startActivityForResult(intent, ADD_SUBREDDITS_REQUEST_CODE);
            }
        });

        binding.addUsersImageViewCustomizePostFilterActivity.setOnClickListener(view -> {
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
        binding.nameTextInputEditTextCustomizePostFilterActivity.setText(postFilter.name);
        binding.postTypeTextCheckBoxCustomizePostFilterActivity.setChecked(postFilter.containTextType);
        binding.postTypeLinkCheckBoxCustomizePostFilterActivity.setChecked(postFilter.containLinkType);
        binding.postTypeImageCheckBoxCustomizePostFilterActivity.setChecked(postFilter.containImageType);
        binding.postTypeGifCheckBoxCustomizePostFilterActivity.setChecked(postFilter.containGifType);
        binding.postTypeVideoCheckBoxCustomizePostFilterActivity.setChecked(postFilter.containVideoType);
        binding.postTypeGalleryCheckBoxCustomizePostFilterActivity.setChecked(postFilter.containGalleryType);
        binding.onlyNsfwSwitchCustomizePostFilterActivity.setChecked(postFilter.onlyNSFW);
        binding.onlySpoilerSwitchCustomizePostFilterActivity.setChecked(postFilter.onlySpoiler);
        binding.titleExcludesStringsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.postTitleExcludesStrings);
        binding.titleContainsStringsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.postTitleContainsStrings);
        binding.titleExcludesRegexTextInputEditTextCustomizePostFilterActivity.setText(postFilter.postTitleExcludesRegex);
        binding.titleContainsRegexTextInputEditTextCustomizePostFilterActivity.setText(postFilter.postTitleContainsRegex);
        binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.excludeSubreddits);
        binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.setText(postFilter.excludeUsers);
        binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.excludeFlairs);
        binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.containFlairs);
        binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.excludeDomains);
        binding.containDomainsTextInputEditTextCustomizePostFilterActivity.setText(postFilter.containDomains);
        binding.minVoteTextInputEditTextCustomizePostFilterActivity.setText(Integer.toString(postFilter.minVote));
        binding.maxVoteTextInputEditTextCustomizePostFilterActivity.setText(Integer.toString(postFilter.maxVote));
        binding.minCommentsTextInputEditTextCustomizePostFilterActivity.setText(Integer.toString(postFilter.minComments));
        binding.maxCommentsTextInputEditTextCustomizePostFilterActivity.setText(Integer.toString(postFilter.maxComments));

        Intent intent = getIntent();
        String excludeSubreddit = intent.getStringExtra(EXTRA_EXCLUDE_SUBREDDIT);
        String excludeUser = intent.getStringExtra(EXTRA_EXCLUDE_USER);
        String excludeFlair = intent.getStringExtra(EXTRA_EXCLUDE_FLAIR);
        String containFlair = intent.getStringExtra(EXTRA_CONTAIN_FLAIR);
        String excludeDomain = intent.getStringExtra(EXTRA_EXCLUDE_DOMAIN);
        String containDomain = intent.getStringExtra(EXTRA_CONTAIN_DOMAIN);

        if (excludeSubreddit != null && !excludeSubreddit.equals("")) {
            if (!binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("")) {
                binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.append(",");
            }
            binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.append(excludeSubreddit);
        }
        if (excludeUser != null && !excludeUser.equals("")) {
            if (!binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("")) {
                binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.append(",");
            }
            binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.append(excludeUser);
        }
        if (excludeFlair != null && !excludeFlair.equals("")) {
            if (!binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("")) {
                binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.append(",");
            }
            binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.append(excludeFlair);
        }
        if (containFlair != null && !containFlair.equals("")) {
            if (!binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("")) {
                binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.append(",");
            }
            binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.append(containFlair);
        }
        if (excludeDomain != null && !excludeDomain.equals("")) {
            if (!binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("")) {
                binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.append(",");
            }
            binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.append(Uri.parse(excludeDomain).getHost());
        }
        if (containDomain != null && !containDomain.equals("")) {
            if (!binding.containDomainsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("")) {
                binding.containDomainsTextInputEditTextCustomizePostFilterActivity.append(",");
            }
            binding.containDomainsTextInputEditTextCustomizePostFilterActivity.append(Uri.parse(containDomain).getHost());
        }
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
        binding.coordinatorLayoutCustomizePostFilterActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCustomizePostFilterActivity,
                binding.collapsingToolbarLayoutCustomizePostFilterActivity, binding.toolbarCustomizePostFilterActivity);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int primaryIconColor = mCustomThemeWrapper.getPrimaryIconColor();
        Drawable cursorDrawable = Utils.getTintedDrawable(this, R.drawable.edit_text_cursor, primaryTextColor);
        binding.nameTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.nameTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.nameTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.postTypeTextTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.postTypeLinkTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.postTypeImageTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.postTypeGifTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.postTypeVideoTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.postTypeGalleryTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.onlyNsfwTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.onlySpoilerTextViewCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.titleExcludesStringsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.titleExcludesStringsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleExcludesStringsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.titleContainsStringsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.titleContainsStringsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleContainsStringsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.titleExcludesRegexTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.titleExcludesRegexTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleExcludesRegexTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.titleContainsRegexTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.titleContainsRegexTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.titleContainsRegexTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.excludesSubredditsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.excludesSubredditsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.addSubredditsImageViewCustomizePostFilterActivity.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));
        binding.excludesUsersTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.excludesUsersTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.addUsersImageViewCustomizePostFilterActivity.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));
        binding.excludesFlairsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.excludesFlairsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.containsFlairsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.containsFlairsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.excludeDomainsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.excludeDomainsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.containDomainsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.containDomainsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.containDomainsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.minVoteTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.minVoteTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.minVoteTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.maxVoteTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.maxVoteTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.maxVoteTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.minCommentsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.minCommentsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.minCommentsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);
        binding.maxCommentsTextInputLayoutCustomizePostFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.maxCommentsTextInputLayoutCustomizePostFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.maxCommentsTextInputEditTextCustomizePostFilterActivity.setTextColor(primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.nameTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.titleExcludesStringsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.titleContainsStringsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.titleExcludesRegexTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.titleContainsRegexTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.containDomainsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.minVoteTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.maxVoteTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.minCommentsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
            binding.maxCommentsTextInputEditTextCustomizePostFilterActivity.setTextCursorDrawable(cursorDrawable);
        } else {
            setCursorDrawableColor(binding.nameTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.titleExcludesStringsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.titleContainsStringsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.titleExcludesRegexTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.titleContainsRegexTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.excludesUsersTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.containsFlairsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.containDomainsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.minVoteTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.maxVoteTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.minCommentsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.maxCommentsTextInputEditTextCustomizePostFilterActivity, primaryTextColor);
        }

        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.coordinatorLayoutCustomizePostFilterActivity, typeface);
        }
    }                    void setCursorDrawableColor(EditText editText, int color) {
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
            try {
                constructPostFilter();
                if (getIntent().getBooleanExtra(EXTRA_START_FILTERED_POSTS_WHEN_FINISH, false)) {
                    Intent intent = new Intent(this, FilteredPostsActivity.class);
                    intent.putExtras(getIntent());
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONSTRUCTED_POST_FILTER, postFilter);
                    startActivity(intent);
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RETURN_EXTRA_POST_FILTER, postFilter);
                    setResult(Activity.RESULT_OK, returnIntent);
                }
                finish();
            } catch (PatternSyntaxException e) {
                Toast.makeText(this, R.string.invalid_regex, Toast.LENGTH_SHORT).show();
            }

            return true;
        } else if (item.getItemId() == R.id.action_save_to_database_customize_post_filter_activity) {
            try {
                constructPostFilter();

                if (!postFilter.name.equals("")) {
                    savePostFilter(originalName);
                } else {
                    Toast.makeText(CustomizePostFilterActivity.this, R.string.post_filter_requires_a_name, Toast.LENGTH_LONG).show();
                }
            } catch (PatternSyntaxException e) {
                Toast.makeText(this, R.string.invalid_regex, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private void savePostFilter(String originalName) {
        SavePostFilter.savePostFilter(mExecutor, new Handler(), mRedditDataRoomDatabase, postFilter, originalName,
                new SavePostFilter.SavePostFilterListener() {
                    @Override
                    public void success() {
                        if (getIntent().getBooleanExtra(EXTRA_START_FILTERED_POSTS_WHEN_FINISH, false)) {
                            Intent intent = new Intent(CustomizePostFilterActivity.this, FilteredPostsActivity.class);
                            intent.putExtras(getIntent());
                            intent.putExtra(FilteredPostsActivity.EXTRA_CONSTRUCTED_POST_FILTER, postFilter);
                            startActivity(intent);
                        } else {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RETURN_EXTRA_POST_FILTER, postFilter);
                            setResult(Activity.RESULT_OK, returnIntent);
                        }
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
                String currentUsers = binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.getText().toString().trim();
                if (usernames != null && !usernames.isEmpty()) {
                    if (!currentUsers.isEmpty() && currentUsers.charAt(currentUsers.length() - 1) != ',') {
                        String newString = currentUsers + ",";
                        binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.setText(newString);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String s : usernames) {
                        stringBuilder.append(s).append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.append(stringBuilder.toString());
                }
            }
        }
    }

    private void updateExcludeSubredditNames(ArrayList<String> subredditNames) {
        String currentSubreddits = binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.getText().toString().trim();
        if (subredditNames != null && !subredditNames.isEmpty()) {
            if (!currentSubreddits.isEmpty() && currentSubreddits.charAt(currentSubreddits.length() - 1) != ',') {
                String newString = currentSubreddits + ",";
                binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.setText(newString);
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : subredditNames) {
                stringBuilder.append(s).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.append(stringBuilder.toString());
        }
    }

    private void constructPostFilter() throws PatternSyntaxException {
        postFilter.name = binding.nameTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.maxVote = binding.maxVoteTextInputEditTextCustomizePostFilterActivity.getText() == null || binding.maxVoteTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("") ? -1 : Integer.parseInt(binding.maxVoteTextInputEditTextCustomizePostFilterActivity.getText().toString());
        postFilter.minVote = binding.minVoteTextInputEditTextCustomizePostFilterActivity.getText() == null || binding.minVoteTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("") ? -1 : Integer.parseInt(binding.minVoteTextInputEditTextCustomizePostFilterActivity.getText().toString());
        postFilter.maxComments = binding.maxCommentsTextInputEditTextCustomizePostFilterActivity.getText() == null || binding.maxCommentsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("") ? -1 : Integer.parseInt(binding.maxCommentsTextInputEditTextCustomizePostFilterActivity.getText().toString());
        postFilter.minComments = binding.minCommentsTextInputEditTextCustomizePostFilterActivity.getText() == null || binding.minCommentsTextInputEditTextCustomizePostFilterActivity.getText().toString().equals("") ? -1 : Integer.parseInt(binding.minCommentsTextInputEditTextCustomizePostFilterActivity.getText().toString());
        postFilter.maxAwards = -1;
        postFilter.minAwards = -1;
        postFilter.postTitleExcludesRegex = binding.titleExcludesRegexTextInputEditTextCustomizePostFilterActivity.getText().toString();
        Pattern.compile(postFilter.postTitleExcludesRegex);
        postFilter.postTitleContainsRegex = binding.titleContainsRegexTextInputEditTextCustomizePostFilterActivity.getText().toString();
        Pattern.compile(postFilter.postTitleContainsRegex);
        postFilter.postTitleExcludesStrings = binding.titleExcludesStringsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.postTitleContainsStrings = binding.titleContainsStringsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.excludeSubreddits = binding.excludesSubredditsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.excludeUsers = binding.excludesUsersTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.excludeFlairs = binding.excludesFlairsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.containFlairs = binding.containsFlairsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.excludeDomains = binding.excludeDomainsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.containDomains = binding.containDomainsTextInputEditTextCustomizePostFilterActivity.getText().toString();
        postFilter.containTextType = binding.postTypeTextCheckBoxCustomizePostFilterActivity.isChecked();
        postFilter.containLinkType = binding.postTypeLinkCheckBoxCustomizePostFilterActivity.isChecked();
        postFilter.containImageType = binding.postTypeImageCheckBoxCustomizePostFilterActivity.isChecked();
        postFilter.containGifType = binding.postTypeGifCheckBoxCustomizePostFilterActivity.isChecked();
        postFilter.containVideoType = binding.postTypeVideoCheckBoxCustomizePostFilterActivity.isChecked();
        postFilter.containGalleryType = binding.postTypeGalleryCheckBoxCustomizePostFilterActivity.isChecked();
        postFilter.onlyNSFW = binding.onlyNsfwSwitchCustomizePostFilterActivity.isChecked();
        postFilter.onlySpoiler = binding.onlySpoilerSwitchCustomizePostFilterActivity.isChecked();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(POST_FILTER_STATE, postFilter);
        outState.putString(ORIGINAL_NAME_STATE, originalName);
    }
}