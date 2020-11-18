package ml.docilealligator.infinityforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.adapters.SubredditFilterRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.subredditfilter.DeleteSubredditFilter;
import ml.docilealligator.infinityforreddit.subredditfilter.InsertSubredditFilter;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilter;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilterViewModel;

public class SubredditFilterPopularAndAllActivity extends BaseActivity {

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;
    @BindView(R.id.coordinator_layout_subreddit_filter_popular_and_all_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_subreddit_filter_popular_and_all_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_subreddit_filter_popular_and_all_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_subreddit_filter_popular_and_all_activity)
    RecyclerView recyclerView;
    @BindView(R.id.fab_subreddit_filter_popular_and_all_activity)
    FloatingActionButton fab;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    SubredditFilterViewModel subredditFilterViewModel;
    private SubredditFilterRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_filter_popular_and_all);

        ButterKnife.bind(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new SubredditFilterRecyclerViewAdapter(subredditFilter -> DeleteSubredditFilter.deleteSubredditFilter(redditDataRoomDatabase, subredditFilter, () -> {}));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab.hide();
                } else if (dy < 0) {
                    fab.show();
                }
            }
        });

        subredditFilterViewModel = new ViewModelProvider(this,
                new SubredditFilterViewModel.Factory(redditDataRoomDatabase))
                .get(SubredditFilterViewModel.class);
        subredditFilterViewModel.getSubredditFilterLiveData().observe(this, subredditFilters -> adapter.updateSubredditsName(subredditFilters));

        fab.setOnClickListener(view -> {
            EditText thingEditText = (EditText) getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, null);
            thingEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.choose_a_subreddit)
                    .setView(thingEditText)
                    .setPositiveButton(R.string.ok, (dialogInterface, i)
                            -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                        }
                        SubredditFilter subredditFilter = new SubredditFilter(thingEditText.getText().toString(), SubredditFilter.TYPE_POPULAR_AND_ALL);
                        InsertSubredditFilter.insertSubredditFilter(redditDataRoomDatabase, subredditFilter,
                                () -> {});
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.search, (dialogInterface, i) -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                        }

                        Intent intent = new Intent(this, SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, true);
                        startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
                    })
                    .setOnDismissListener(dialogInterface -> {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                        }
                    })
                    .show();
        });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            SubredditFilter subredditFilter = new SubredditFilter(data.getStringExtra(SearchActivity.EXTRA_RETURN_SUBREDDIT_NAME), SubredditFilter.TYPE_POPULAR_AND_ALL);
            InsertSubredditFilter.insertSubredditFilter(redditDataRoomDatabase, subredditFilter,
                    () -> {});
        }
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
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
    }
}