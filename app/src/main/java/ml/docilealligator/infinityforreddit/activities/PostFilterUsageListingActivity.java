package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.PostFilterUsageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.NewPostFilterUsageBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostFilterUsageOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityPostFilterApplicationBinding;
import ml.docilealligator.infinityforreddit.postfilter.DeletePostFilterUsage;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsageViewModel;
import ml.docilealligator.infinityforreddit.postfilter.SavePostFilterUsage;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostFilterUsageListingActivity extends BaseActivity {

    public static final String EXTRA_POST_FILTER = "EPF";

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor executor;
    public PostFilterUsageViewModel postFilterUsageViewModel;
    private PostFilterUsageRecyclerViewAdapter adapter;
    private PostFilter postFilter;
    private ActivityPostFilterApplicationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivityPostFilterApplicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutPostFilterApplicationActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);

                    setMargins(binding.toolbarPostFilterApplicationActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.recyclerViewPostFilterApplicationActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom
                    );

                    setMargins(binding.fabPostFilterApplicationActivity,
                            BaseActivity.IGNORE_MARGIN,
                            BaseActivity.IGNORE_MARGIN,
                            (int) Utils.convertDpToPixel(16, PostFilterUsageListingActivity.this) + allInsets.right,
                            (int) Utils.convertDpToPixel(16, PostFilterUsageListingActivity.this) + allInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbarPostFilterApplicationActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postFilter = getIntent().getParcelableExtra(EXTRA_POST_FILTER);

        setTitle(postFilter.name);

        binding.fabPostFilterApplicationActivity.setOnClickListener(view -> {
            NewPostFilterUsageBottomSheetFragment newPostFilterUsageBottomSheetFragment = new NewPostFilterUsageBottomSheetFragment();
            newPostFilterUsageBottomSheetFragment.show(getSupportFragmentManager(), newPostFilterUsageBottomSheetFragment.getTag());
        });

        adapter = new PostFilterUsageRecyclerViewAdapter(this, customThemeWrapper, postFilterUsage -> {
            PostFilterUsageOptionsBottomSheetFragment postFilterUsageOptionsBottomSheetFragment = new PostFilterUsageOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(PostFilterUsageOptionsBottomSheetFragment.EXTRA_POST_FILTER_USAGE, postFilterUsage);
            postFilterUsageOptionsBottomSheetFragment.setArguments(bundle);
            postFilterUsageOptionsBottomSheetFragment.show(getSupportFragmentManager(), postFilterUsageOptionsBottomSheetFragment.getTag());
        });
        binding.recyclerViewPostFilterApplicationActivity.setAdapter(adapter);

        postFilterUsageViewModel = new ViewModelProvider(this,
                new PostFilterUsageViewModel.Factory(redditDataRoomDatabase, postFilter.name)).get(PostFilterUsageViewModel.class);

        postFilterUsageViewModel.getPostFilterUsageListLiveData().observe(this, postFilterUsages -> adapter.setPostFilterUsages(postFilterUsages));
    }

    public void newPostFilterUsage(int type) {
        switch (type) {
            case PostFilterUsage.HOME_TYPE:
            case PostFilterUsage.SEARCH_TYPE:
                PostFilterUsage postFilterUsage = new PostFilterUsage(postFilter.name, type, PostFilterUsage.NO_USAGE);
                SavePostFilterUsage.savePostFilterUsage(redditDataRoomDatabase, executor, postFilterUsage);
                break;
            case PostFilterUsage.SUBREDDIT_TYPE:
            case PostFilterUsage.USER_TYPE:
            case PostFilterUsage.MULTIREDDIT_TYPE:
                editAndPostFilterUsageNameOfUsage(type, null);
                break;
        }
    }

    private void editAndPostFilterUsageNameOfUsage(int type, String nameOfUsage) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_post_or_comment_filter_name_of_usage, null);
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.text_input_layout_edit_post_or_comment_filter_name_of_usage_dialog);
        TextInputEditText textInputEditText = dialogView.findViewById(R.id.text_input_edit_text_edit_post_or_comment_filter_name_of_usage_dialog);
        int primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        textInputLayout.setBoxStrokeColor(primaryTextColor);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        textInputEditText.setTextColor(primaryTextColor);
        if (nameOfUsage != null && !nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
            textInputEditText.setText(nameOfUsage);
        }
        textInputEditText.requestFocus();
        int titleStringId = R.string.subreddit;
        switch (type) {
            case PostFilterUsage.SUBREDDIT_TYPE:
                textInputEditText.setHint(R.string.settings_tab_subreddit_name);
                break;
            case PostFilterUsage.USER_TYPE:
                textInputEditText.setHint(R.string.settings_tab_username);
                titleStringId = R.string.user;
                break;
            case PostFilterUsage.MULTIREDDIT_TYPE:
                textInputEditText.setHint(R.string.settings_tab_multi_reddit_name);
                titleStringId = R.string.multi_reddit;
                break;
        }

        Utils.showKeyboard(this, new Handler(), textInputEditText);
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleStringId)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (editTextDialogInterface, i1)
                        -> {
                    Utils.hideKeyboard(this);

                    PostFilterUsage postFilterUsage;
                    if (textInputEditText.getText().toString().equals("")) {
                        postFilterUsage = new PostFilterUsage(postFilter.name, type, PostFilterUsage.NO_USAGE);
                    } else {
                        postFilterUsage = new PostFilterUsage(postFilter.name, type, textInputEditText.getText().toString());
                    }

                    SavePostFilterUsage.savePostFilterUsage(redditDataRoomDatabase, executor, postFilterUsage);
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(editTextDialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    public void editPostFilterUsage(PostFilterUsage postFilterUsage) {
        editAndPostFilterUsageNameOfUsage(postFilterUsage.usage, postFilterUsage.nameOfUsage);
    }

    public void deletePostFilterUsage(PostFilterUsage postFilterUsage) {
        DeletePostFilterUsage.deletePostFilterUsage(redditDataRoomDatabase, executor, postFilterUsage);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return customThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostFilterApplicationActivity,
                binding.collapsingToolbarLayoutPostFilterApplicationActivity, binding.toolbarPostFilterApplicationActivity);
        applyFABTheme(binding.fabPostFilterApplicationActivity);
        binding.getRoot().setBackgroundColor(customThemeWrapper.getBackgroundColor());
    }
}