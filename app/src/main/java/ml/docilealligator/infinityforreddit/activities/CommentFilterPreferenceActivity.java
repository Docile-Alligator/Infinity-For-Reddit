package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.CommentFilterWithUsageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentFilterOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterWithUsageViewModel;
import ml.docilealligator.infinityforreddit.commentfilter.DeleteCommentFilter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityCommentFilterPreferenceBinding;

public class CommentFilterPreferenceActivity extends BaseActivity {

    private ActivityCommentFilterPreferenceBinding binding;

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
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

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityCommentFilterPreferenceBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ButterKnife.bind(this);

        applyCustomTheme();

        setSupportActionBar(binding.toolbarCommentFilterPreferenceActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.fabCommentFilterPreferenceActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, CustomizeCommentFilterActivity.class);
            intent.putExtra(CustomizeCommentFilterActivity.EXTRA_FROM_SETTINGS, true);
            startActivity(intent);
        });

        adapter = new CommentFilterWithUsageRecyclerViewAdapter(this, commentFilter -> {
            CommentFilterOptionsBottomSheetFragment commentFilterOptionsBottomSheetFragment = new CommentFilterOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(CommentFilterOptionsBottomSheetFragment.EXTRA_POST_FILTER, commentFilter);
            commentFilterOptionsBottomSheetFragment.setArguments(bundle);
            commentFilterOptionsBottomSheetFragment.show(getSupportFragmentManager(), commentFilterOptionsBottomSheetFragment.getTag());
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

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
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