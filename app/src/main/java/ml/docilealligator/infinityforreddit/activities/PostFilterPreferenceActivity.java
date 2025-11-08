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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.PostFilterWithUsageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostFilterOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityPostFilterPreferenceBinding;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.postfilter.DeletePostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterWithUsage;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterWithUsageViewModel;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostFilterPreferenceActivity extends BaseActivity {

    public static final String EXTRA_POST = "EP";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_USER_NAME = "EUN";

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
    public PostFilterWithUsageViewModel postFilterWithUsageViewModel;
    private PostFilterWithUsageRecyclerViewAdapter adapter;
    private ActivityPostFilterPreferenceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivityPostFilterPreferenceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutPostFilterPreferenceActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);

                    setMargins(binding.toolbarPostFilterPreferenceActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.recyclerViewPostFilterPreferenceActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom
                    );

                    setMargins(binding.fabPostFilterPreferenceActivity,
                            BaseActivity.IGNORE_MARGIN,
                            BaseActivity.IGNORE_MARGIN,
                            (int) Utils.convertDpToPixel(16, PostFilterPreferenceActivity.this) + allInsets.right,
                            (int) Utils.convertDpToPixel(16, PostFilterPreferenceActivity.this) + allInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbarPostFilterPreferenceActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Post post = getIntent().getParcelableExtra(EXTRA_POST);
        String subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
        String username = getIntent().getStringExtra(EXTRA_USER_NAME);

        binding.fabPostFilterPreferenceActivity.setOnClickListener(view -> {
            if (post != null) {
                showPostFilterOptions(post, null);
            } else if (subredditName != null) {
                excludeSubredditInFilter(subredditName, null);
            } else if (username != null) {
                excludeUserInFilter(username, null);
            } else {
                Intent intent = new Intent(PostFilterPreferenceActivity.this, CustomizePostFilterActivity.class);
                intent.putExtra(CustomizePostFilterActivity.EXTRA_FROM_SETTINGS, true);
                startActivity(intent);
            }
        });

        adapter = new PostFilterWithUsageRecyclerViewAdapter(this, customThemeWrapper, postFilter -> {
            if (post != null) {
                showPostFilterOptions(post, postFilter);
            } else if (subredditName != null) {
                excludeSubredditInFilter(subredditName, postFilter);
            } else if (username != null) {
                excludeUserInFilter(username, postFilter);
            } else {
                PostFilterOptionsBottomSheetFragment postFilterOptionsBottomSheetFragment = new PostFilterOptionsBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(PostFilterOptionsBottomSheetFragment.EXTRA_POST_FILTER, postFilter);
                postFilterOptionsBottomSheetFragment.setArguments(bundle);
                postFilterOptionsBottomSheetFragment.show(getSupportFragmentManager(), postFilterOptionsBottomSheetFragment.getTag());
            }
        });

        binding.recyclerViewPostFilterPreferenceActivity.setAdapter(adapter);

        postFilterWithUsageViewModel = new ViewModelProvider(this,
                new PostFilterWithUsageViewModel.Factory(redditDataRoomDatabase)).get(PostFilterWithUsageViewModel.class);

        postFilterWithUsageViewModel.getPostFilterWithUsageListLiveData().observe(this, new Observer<List<PostFilterWithUsage>>() {
            @Override
            public void onChanged(List<PostFilterWithUsage> postFilterWithUsages) {
                adapter.setPostFilterWithUsageList(postFilterWithUsages);
            }
        });
    }

    public void showPostFilterOptions(Post post, @Nullable PostFilter postFilter) {
        String[] options = getResources().getStringArray(R.array.add_to_post_filter_options);
        boolean[] selectedOptions = new boolean[]{false, false, false, false, false, false, false, false};
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.select)
                .setMultiChoiceItems(options, selectedOptions, (dialogInterface, i, b) -> selectedOptions[i] = b)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    Intent intent = new Intent(PostFilterPreferenceActivity.this, CustomizePostFilterActivity.class);
                    if (postFilter != null) {
                        intent.putExtra(CustomizePostFilterActivity.EXTRA_POST_FILTER, postFilter);
                    }
                    intent.putExtra(CustomizePostFilterActivity.EXTRA_FROM_SETTINGS, true);
                    for (int j = 0; j < selectedOptions.length; j++) {
                        if (selectedOptions[j]) {
                            switch (j) {
                                case 0:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_EXCLUDE_SUBREDDIT, post.getSubredditName());
                                    break;
                                case 1:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_EXCLUDE_USER, post.getAuthor());
                                    break;
                                case 2:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_EXCLUDE_FLAIR, post.getFlair());
                                    break;
                                case 3:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_CONTAIN_FLAIR, post.getFlair());
                                    break;
                                case 4:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_EXCLUDE_DOMAIN, post.getUrl());
                                    break;
                                case 5:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_CONTAIN_DOMAIN, post.getUrl());
                                    break;
                                case 6:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_CONTAIN_SUBREDDIT, post.getSubredditName());
                                    break;
                                case 7:
                                    intent.putExtra(CustomizePostFilterActivity.EXTRA_CONTAIN_USER, post.getAuthor());
                                    break;
                            }
                        }
                    }
                    startActivity(intent);
                })
                .show();
    }

    public void excludeSubredditInFilter(String subredditName, PostFilter postFilter) {
        Intent intent = new Intent(this, CustomizePostFilterActivity.class);
        intent.putExtra(CustomizePostFilterActivity.EXTRA_EXCLUDE_SUBREDDIT, subredditName);
        if (postFilter != null) {
            intent.putExtra(CustomizePostFilterActivity.EXTRA_POST_FILTER, postFilter);
        }
        startActivity(intent);
    }

    public void excludeUserInFilter(String username, PostFilter postFilter) {
        Intent intent = new Intent(this, CustomizePostFilterActivity.class);
        intent.putExtra(CustomizePostFilterActivity.EXTRA_EXCLUDE_USER, username);
        if (postFilter != null) {
            intent.putExtra(CustomizePostFilterActivity.EXTRA_POST_FILTER, postFilter);
        }
        startActivity(intent);
    }

    public void editPostFilter(PostFilter postFilter) {
        Intent intent = new Intent(PostFilterPreferenceActivity.this, CustomizePostFilterActivity.class);
        intent.putExtra(CustomizePostFilterActivity.EXTRA_POST_FILTER, postFilter);
        intent.putExtra(CustomizePostFilterActivity.EXTRA_FROM_SETTINGS, true);
        startActivity(intent);
    }

    public void applyPostFilterTo(PostFilter postFilter) {
        Intent intent = new Intent(this, PostFilterUsageListingActivity.class);
        intent.putExtra(PostFilterUsageListingActivity.EXTRA_POST_FILTER, postFilter);
        startActivity(intent);
    }

    public void deletePostFilter(PostFilter postFilter) {
        DeletePostFilter.deletePostFilter(redditDataRoomDatabase, executor, postFilter);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostFilterPreferenceActivity,
                binding.collapsingToolbarLayoutPostFilterPreferenceActivity, binding.toolbarPostFilterPreferenceActivity);
        applyFABTheme(binding.fabPostFilterPreferenceActivity);
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