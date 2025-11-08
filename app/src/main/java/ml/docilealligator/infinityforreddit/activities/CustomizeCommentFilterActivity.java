package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter;
import ml.docilealligator.infinityforreddit.commentfilter.SaveCommentFilter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityCustomizeCommentFilterBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomizeCommentFilterActivity extends BaseActivity {

    public static final String EXTRA_COMMENT_FILTER = "ECF";
    public static final String EXTRA_FROM_SETTINGS = "EFS";
    public static final String EXTRA_EXCLUDE_USER = "EEU";
    public static final String RETURN_EXTRA_COMMENT_FILTER = "RECF";
    private static final String COMMENT_FILTER_STATE = "CFS";
    private static final String ORIGINAL_NAME_STATE = "ONS";
    private static final String DISPLAY_MODE_SELECTED_ITEM_INDEX_STATE = "DMSIIS";
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
    private CommentFilter commentFilter;
    private boolean fromSettings;
    private String originalName;
    private ActivityCustomizeCommentFilterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);
        binding = ActivityCustomizeCommentFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutCustomizeCommentFilterActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets windowInsets = Utils.getInsets(insets, false);
                    Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

                    setMargins(binding.toolbarCustomizeCommentFilterActivity,
                            windowInsets.left,
                            windowInsets.top,
                            windowInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.contentWrapperViewCustomizeCommentFilterActivity.setPadding(
                            windowInsets.left,
                            0,
                            windowInsets.right,
                            windowInsets.bottom
                    );

                    setMargins(binding.contentWrapperViewCustomizeCommentFilterActivity,
                            BaseActivity.IGNORE_MARGIN,
                            BaseActivity.IGNORE_MARGIN,
                            BaseActivity.IGNORE_MARGIN,
                            imeInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbarCustomizeCommentFilterActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarCustomizeCommentFilterActivity);

        ActivityResultLauncher<Intent> requestAddUsersLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (data == null) {
                return;
            }

            ArrayList<String> usernames = data.getStringArrayListExtra(SearchActivity.RETURN_EXTRA_SELECTED_USERNAMES);
            String currentUsers = binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.getText().toString().trim();
            if (usernames != null && !usernames.isEmpty()) {
                if (!currentUsers.isEmpty() && currentUsers.charAt(currentUsers.length() - 1) != ',') {
                    String newString = currentUsers + ",";
                    binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.setText(newString);
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : usernames) {
                    stringBuilder.append(s).append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.append(stringBuilder.toString());
            }
        });

        binding.addUsersImageViewCustomizeCommentFilterActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_USERS, true);
            intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
            requestAddUsersLauncher.launch(intent);
        });

        fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);

        if (savedInstanceState != null) {
            commentFilter = savedInstanceState.getParcelable(COMMENT_FILTER_STATE);
            originalName = savedInstanceState.getString(ORIGINAL_NAME_STATE);
            binding.displayModeSpinnerCustomizeCommentFilterActivity.setSelection(savedInstanceState.getInt(DISPLAY_MODE_SELECTED_ITEM_INDEX_STATE), false);
        } else {
            commentFilter = getIntent().getParcelableExtra(EXTRA_COMMENT_FILTER);
            if (commentFilter == null) {
                commentFilter = new CommentFilter();
                originalName = "";
            } else {
                if (!fromSettings) {
                    originalName = "";
                } else {
                    originalName = commentFilter.name;
                }
            }
            bindView();
        }
    }

    private void bindView() {
        binding.nameTextInputEditTextCustomizeCommentFilterActivity.setText(commentFilter.name);
        binding.displayModeSpinnerCustomizeCommentFilterActivity.setSelection(commentFilter.displayMode == CommentFilter.DisplayMode.REMOVE_COMMENT ? 0 : 1);
        binding.excludeStringsTextInputEditTextCustomizeCommentFilterActivity.setText(commentFilter.excludeStrings);
        binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.setText(commentFilter.excludeUsers);
        binding.minVoteTextInputEditTextCustomizeCommentFilterActivity.setText(Integer.toString(commentFilter.minVote));
        binding.maxVoteTextInputEditTextCustomizeCommentFilterActivity.setText(Integer.toString(commentFilter.maxVote));

        Intent intent = getIntent();
        String excludeUser = intent.getStringExtra(EXTRA_EXCLUDE_USER);

        if (excludeUser != null && !excludeUser.equals("")) {
            if (!binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.getText().toString().equals("")) {
                binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.append(",");
            }
            binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.append(excludeUser);
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
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCustomizeCommentFilterActivity, binding.collapsingToolbarLayoutCustomizeCommentFilterActivity, binding.toolbarCustomizeCommentFilterActivity);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int primaryIconColor = mCustomThemeWrapper.getPrimaryIconColor();
        int filledCardViewBackgroundColor = mCustomThemeWrapper.getFilledCardViewBackgroundColor();

        binding.nameCardViewCustomizeCommentFilterActivity.setCardBackgroundColor(filledCardViewBackgroundColor);
        binding.nameExplanationTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.nameTextInputLayoutCustomizeCommentFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.nameTextInputLayoutCustomizeCommentFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.nameTextInputEditTextCustomizeCommentFilterActivity.setTextColor(primaryTextColor);

        binding.displayModeCardViewCustomizeCommentFilterActivity.setCardBackgroundColor(filledCardViewBackgroundColor);
        binding.displayModeExplanationTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.displayModeTitleTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.displayModeSpinnerCustomizeCommentFilterActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                View child = parent.getChildAt(0);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(primaryTextColor);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.excludeStringsCardViewCustomizeCommentFilterActivity.setCardBackgroundColor(filledCardViewBackgroundColor);
        binding.excludeStringsExplanationTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.excludeStringsTextInputLayoutCustomizeCommentFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.excludeStringsTextInputLayoutCustomizeCommentFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludeStringsTextInputEditTextCustomizeCommentFilterActivity.setTextColor(primaryTextColor);

        binding.excludeUsersCardViewCustomizeCommentFilterActivity.setCardBackgroundColor(filledCardViewBackgroundColor);
        binding.excludeUsersExplanationTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.excludeUsersTextInputLayoutCustomizeCommentFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.excludeUsersTextInputLayoutCustomizeCommentFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.addUsersImageViewCustomizeCommentFilterActivity.setImageDrawable(Utils.getTintedDrawable(this, R.drawable.ic_add_24dp, primaryIconColor));

        binding.voteCardViewCustomizeCommentFilterActivity.setCardBackgroundColor(filledCardViewBackgroundColor);
        binding.minVoteExplanationTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.minVoteTextInputLayoutCustomizeCommentFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.minVoteTextInputLayoutCustomizeCommentFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.minVoteTextInputEditTextCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.maxVoteExplanationTextViewCustomizeCommentFilterActivity.setTextColor(primaryTextColor);
        binding.maxVoteTextInputLayoutCustomizeCommentFilterActivity.setBoxStrokeColor(primaryTextColor);
        binding.maxVoteTextInputLayoutCustomizeCommentFilterActivity.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.maxVoteTextInputEditTextCustomizeCommentFilterActivity.setTextColor(primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.nameTextInputLayoutCustomizeCommentFilterActivity.setCursorColor(ColorStateList.valueOf(primaryTextColor));
            binding.excludeStringsTextInputLayoutCustomizeCommentFilterActivity.setCursorColor(ColorStateList.valueOf(primaryTextColor));
            binding.excludeUsersTextInputLayoutCustomizeCommentFilterActivity.setCursorColor(ColorStateList.valueOf(primaryTextColor));
            binding.minVoteTextInputLayoutCustomizeCommentFilterActivity.setCursorColor(ColorStateList.valueOf(primaryTextColor));
            binding.maxVoteTextInputLayoutCustomizeCommentFilterActivity.setCursorColor(ColorStateList.valueOf(primaryTextColor));
        } else {
            setCursorDrawableColor(binding.nameTextInputEditTextCustomizeCommentFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.excludeStringsTextInputEditTextCustomizeCommentFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.minVoteTextInputEditTextCustomizeCommentFilterActivity, primaryTextColor);
            setCursorDrawableColor(binding.maxVoteTextInputEditTextCustomizeCommentFilterActivity, primaryTextColor);
        }

        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), typeface);
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
        getMenuInflater().inflate(R.menu.customize_comment_filter_activity, menu);
        if (fromSettings) {
            menu.findItem(R.id.action_save_customize_comment_filter_activity).setVisible(false);
        }
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save_customize_comment_filter_activity) {
            try {
                constructCommentFilter();
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RETURN_EXTRA_COMMENT_FILTER, commentFilter);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } catch (PatternSyntaxException e) {
                Toast.makeText(this, R.string.invalid_regex, Toast.LENGTH_SHORT).show();
            }

            return true;
        } else if (item.getItemId() == R.id.action_save_to_database_customize_comment_filter_activity) {
            try {
                constructCommentFilter();

                if (!commentFilter.name.equals("")) {
                    saveCommentFilter(originalName);
                } else {
                    Toast.makeText(CustomizeCommentFilterActivity.this, R.string.comment_filter_requires_a_name, Toast.LENGTH_LONG).show();
                }
            } catch (PatternSyntaxException e) {
                Toast.makeText(this, R.string.invalid_regex, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private void saveCommentFilter(String originalName) {
        SaveCommentFilter.saveCommentFilter(mExecutor, new Handler(), mRedditDataRoomDatabase, commentFilter, originalName,
                new SaveCommentFilter.SaveCommentFilterListener() {
                    @Override
                    public void success() {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(RETURN_EXTRA_COMMENT_FILTER, commentFilter);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }

                    @Override
                    public void duplicate() {
                        new MaterialAlertDialogBuilder(CustomizeCommentFilterActivity.this, R.style.MaterialAlertDialogTheme)
                                .setTitle(getString(R.string.duplicate_comment_filter_dialog_title, commentFilter.name))
                                .setMessage(R.string.duplicate_comment_filter_dialog_message)
                                .setPositiveButton(R.string.override, (dialogInterface, i) -> saveCommentFilter(commentFilter.name))
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                });
    }

    private void constructCommentFilter() throws PatternSyntaxException {
        commentFilter.name = binding.nameTextInputEditTextCustomizeCommentFilterActivity.getText().toString();
        commentFilter.displayMode = binding.displayModeSpinnerCustomizeCommentFilterActivity.getSelectedItemPosition() == 0 ?
                CommentFilter.DisplayMode.REMOVE_COMMENT : CommentFilter.DisplayMode.COLLAPSE_COMMENT;
        commentFilter.excludeStrings = binding.excludeStringsTextInputEditTextCustomizeCommentFilterActivity.getText().toString();
        commentFilter.excludeUsers = binding.excludeUsersTextInputEditTextCustomizeCommentFilterActivity.getText().toString();
        commentFilter.maxVote = binding.maxVoteTextInputEditTextCustomizeCommentFilterActivity.getText() == null || binding.maxVoteTextInputEditTextCustomizeCommentFilterActivity.getText().toString().equals("") ? -1 : Integer.parseInt(binding.maxVoteTextInputEditTextCustomizeCommentFilterActivity.getText().toString());
        commentFilter.minVote = binding.minVoteTextInputEditTextCustomizeCommentFilterActivity.getText() == null || binding.minVoteTextInputEditTextCustomizeCommentFilterActivity.getText().toString().equals("") ? -1 : Integer.parseInt(binding.minVoteTextInputEditTextCustomizeCommentFilterActivity.getText().toString());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(COMMENT_FILTER_STATE, commentFilter);
        outState.putString(ORIGINAL_NAME_STATE, originalName);
        outState.putInt(DISPLAY_MODE_SELECTED_ITEM_INDEX_STATE, binding.displayModeSpinnerCustomizeCommentFilterActivity.getSelectedItemPosition());
    }
}