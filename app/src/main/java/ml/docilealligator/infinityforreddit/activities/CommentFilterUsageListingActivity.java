package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import ml.docilealligator.infinityforreddit.adapters.CommentFilterUsageRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentFilterUsageOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.NewCommentFilterUsageBottomSheetFragment;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterUsage;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterUsageViewModel;
import ml.docilealligator.infinityforreddit.commentfilter.DeleteCommentFilterUsage;
import ml.docilealligator.infinityforreddit.commentfilter.SaveCommentFilterUsage;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityCommentFilterUsageListingBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CommentFilterUsageListingActivity extends BaseActivity {

    public static final String EXTRA_COMMENT_FILTER = "ECF";
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
    private ActivityCommentFilterUsageListingBinding binding;
    public CommentFilterUsageViewModel commentFilterUsageViewModel;
    private CommentFilterUsageRecyclerViewAdapter adapter;
    private CommentFilter commentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityCommentFilterUsageListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        setSupportActionBar(binding.toolbarCommentFilterUsageListingActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        commentFilter = getIntent().getParcelableExtra(EXTRA_COMMENT_FILTER);

        setTitle(commentFilter.name);

        binding.fabCommentFilterUsageListingActivity.setOnClickListener(view -> {
            NewCommentFilterUsageBottomSheetFragment newCommentFilterUsageBottomSheetFragment = new NewCommentFilterUsageBottomSheetFragment();
            newCommentFilterUsageBottomSheetFragment.show(getSupportFragmentManager(), newCommentFilterUsageBottomSheetFragment.getTag());
        });

        adapter = new CommentFilterUsageRecyclerViewAdapter(this, customThemeWrapper, commentFilterUsage -> {
            CommentFilterUsageOptionsBottomSheetFragment commentFilterUsageOptionsBottomSheetFragment = new CommentFilterUsageOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(CommentFilterUsageOptionsBottomSheetFragment.EXTRA_COMMENT_FILTER_USAGE, commentFilterUsage);
            commentFilterUsageOptionsBottomSheetFragment.setArguments(bundle);
            commentFilterUsageOptionsBottomSheetFragment.show(getSupportFragmentManager(), commentFilterUsageOptionsBottomSheetFragment.getTag());
        });
        binding.recyclerViewCommentFilterUsageListingActivity.setAdapter(adapter);

        commentFilterUsageViewModel = new ViewModelProvider(this,
                new CommentFilterUsageViewModel.Factory(redditDataRoomDatabase, commentFilter.name)).get(CommentFilterUsageViewModel.class);

        commentFilterUsageViewModel.getCommentFilterUsageListLiveData().observe(this, commentFilterUsages -> adapter.setCommentFilterUsages(commentFilterUsages));
    }

    public void newCommentFilterUsage(int type) {
        if (type == CommentFilterUsage.SUBREDDIT_TYPE) {
            editAndCommentFilterUsageNameOfUsage(type, null);
        }
    }

    private void editAndCommentFilterUsageNameOfUsage(int type, String nameOfUsage) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_post_or_comment_filter_name_of_usage, null);
        TextView messageTextView = dialogView.findViewById(R.id.message_text_view_edit_post_or_comment_filter_name_of_usage_dialog);
        messageTextView.setVisibility(View.GONE);
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.text_input_layout_edit_post_or_comment_filter_name_of_usage_dialog);
        TextInputEditText textInputEditText = dialogView.findViewById(R.id.text_input_edit_text_edit_post_or_comment_filter_name_of_usage_dialog);
        int primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        textInputLayout.setBoxStrokeColor(primaryTextColor);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        textInputEditText.setTextColor(primaryTextColor);
        if (nameOfUsage != null) {
            textInputEditText.setText(nameOfUsage);
        }
        textInputEditText.requestFocus();
        int titleStringId = R.string.subreddit;
        if (type == CommentFilterUsage.SUBREDDIT_TYPE) {
            textInputEditText.setHint(R.string.settings_tab_subreddit_name);
        }

        Utils.showKeyboard(this, new Handler(), textInputEditText);
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleStringId)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (editTextDialogInterface, i1)
                        -> {
                    Utils.hideKeyboard(this);

                    CommentFilterUsage commentFilterUsage;
                    if (!textInputEditText.getText().toString().equals("")) {
                        commentFilterUsage = new CommentFilterUsage(commentFilter.name, type, textInputEditText.getText().toString());
                        SaveCommentFilterUsage.saveCommentFilterUsage(redditDataRoomDatabase, executor, commentFilterUsage);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(editTextDialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    public void editCommentFilterUsage(CommentFilterUsage commentFilterUsage) {
        editAndCommentFilterUsageNameOfUsage(commentFilterUsage.usage, commentFilterUsage.nameOfUsage);
    }

    public void deleteCommentFilterUsage(CommentFilterUsage commentFilterUsage) {
        DeleteCommentFilterUsage.deleteCommentFilterUsage(redditDataRoomDatabase, executor, commentFilterUsage);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCommentFilterUsageListingActivity, binding.collapsingToolbarLayoutCommentFilterUsageListingActivity, binding.toolbarCommentFilterUsageListingActivity);
        applyFABTheme(binding.fabCommentFilterUsageListingActivity);
        binding.getRoot().setBackgroundColor(customThemeWrapper.getBackgroundColor());
    }
}