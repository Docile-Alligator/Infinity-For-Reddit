package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.CommentFilterWithUsageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentFilterOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterWithUsageViewModel;
import ml.docilealligator.infinityforreddit.commentfilter.DeleteCommentFilter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityCommentFilterPreferenceBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CommentFilterPreferenceActivity extends BaseActivity {

    public static final String EXTRA_COMMENT = "EC";

    private ActivityCommentFilterPreferenceBinding binding;

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
    public CommentFilterWithUsageViewModel commentFilterWithUsageViewModel;
    private CommentFilterWithUsageRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);
        binding = ActivityCommentFilterPreferenceBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        applyCustomTheme();

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutCommentFilterPreferenceActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);

                    setMargins(binding.toolbarCommentFilterPreferenceActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.recyclerViewCommentFilterPreferenceActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom
                    );

                    setMargins(binding.fabCommentFilterPreferenceActivity,
                            BaseActivity.IGNORE_MARGIN,
                            BaseActivity.IGNORE_MARGIN,
                            (int) Utils.convertDpToPixel(16, CommentFilterPreferenceActivity.this) + allInsets.right,
                            (int) Utils.convertDpToPixel(16, CommentFilterPreferenceActivity.this) + allInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbarCommentFilterPreferenceActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Comment comment = getIntent().getParcelableExtra(EXTRA_COMMENT);

        binding.fabCommentFilterPreferenceActivity.setOnClickListener(view -> {
            if (comment != null) {
                showCommentFilterOptions(comment, null);
            } else {
                Intent intent = new Intent(this, CustomizeCommentFilterActivity.class);
                intent.putExtra(CustomizeCommentFilterActivity.EXTRA_FROM_SETTINGS, true);
                startActivity(intent);
            }
        });

        adapter = new CommentFilterWithUsageRecyclerViewAdapter(this, commentFilter -> {
            if (comment != null) {
                showCommentFilterOptions(comment, commentFilter);
            } else {
                CommentFilterOptionsBottomSheetFragment commentFilterOptionsBottomSheetFragment = new CommentFilterOptionsBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(CommentFilterOptionsBottomSheetFragment.EXTRA_COMMENT_FILTER, commentFilter);
                commentFilterOptionsBottomSheetFragment.setArguments(bundle);
                commentFilterOptionsBottomSheetFragment.show(getSupportFragmentManager(), commentFilterOptionsBottomSheetFragment.getTag());
            }
        });

        binding.recyclerViewCommentFilterPreferenceActivity.setAdapter(adapter);

        commentFilterWithUsageViewModel = new ViewModelProvider(this,
                new CommentFilterWithUsageViewModel.Factory(redditDataRoomDatabase)).get(CommentFilterWithUsageViewModel.class);

        commentFilterWithUsageViewModel.getCommentFilterWithUsageListLiveData().observe(this, commentFilterWithUsages -> adapter.setCommentFilterWithUsageList(commentFilterWithUsages));
    }

    public void editCommentFilter(CommentFilter commentFilter) {
        Intent intent = new Intent(this, CustomizeCommentFilterActivity.class);
        intent.putExtra(CustomizeCommentFilterActivity.EXTRA_COMMENT_FILTER, commentFilter);
        intent.putExtra(CustomizeCommentFilterActivity.EXTRA_FROM_SETTINGS, true);
        startActivity(intent);
    }

    public void applyCommentFilterTo(CommentFilter commentFilter) {
        Intent intent = new Intent(this, CommentFilterUsageListingActivity.class);
        intent.putExtra(CommentFilterUsageListingActivity.EXTRA_COMMENT_FILTER, commentFilter);
        startActivity(intent);
    }

    public void deleteCommentFilter(CommentFilter commentFilter) {
        DeleteCommentFilter.deleteCommentFilter(redditDataRoomDatabase, executor, commentFilter);
    }

    public void showCommentFilterOptions(Comment comment, @Nullable CommentFilter commentFilter) {
        String[] options = getResources().getStringArray(R.array.add_to_comment_filter_options);
        boolean[] selectedOptions = new boolean[]{false};
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.select)
                .setMultiChoiceItems(options, selectedOptions, (dialogInterface, i, b) -> selectedOptions[i] = b)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    Intent intent = new Intent(CommentFilterPreferenceActivity.this, CustomizeCommentFilterActivity.class);
                    if (commentFilter != null) {
                        intent.putExtra(CustomizeCommentFilterActivity.EXTRA_COMMENT_FILTER, commentFilter);
                    }
                    intent.putExtra(CustomizeCommentFilterActivity.EXTRA_FROM_SETTINGS, true);
                    for (int j = 0; j < selectedOptions.length; j++) {
                        if (selectedOptions[j]) {
                            if (j == 0) {
                                intent.putExtra(CustomizeCommentFilterActivity.EXTRA_EXCLUDE_USER, comment.getAuthor());
                            }
                        }
                    }
                    startActivity(intent);
                })
                .show();
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCommentFilterPreferenceActivity, binding.collapsingToolbarLayoutCommentFilterPreferenceActivity, binding.toolbarCommentFilterPreferenceActivity);
        applyFABTheme(binding.fabCommentFilterPreferenceActivity);
        binding.getRoot().setBackgroundColor(customThemeWrapper.getBackgroundColor());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}