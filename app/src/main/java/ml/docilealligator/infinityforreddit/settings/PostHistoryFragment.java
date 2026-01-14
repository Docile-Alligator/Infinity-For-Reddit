package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostHistoryBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostHistoryFragment extends Fragment {

    private FragmentPostHistoryBinding binding;
    @Inject
    @Named("post_history")
    SharedPreferences postHistorySharedPreferences;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    Executor mExecutor;
    private SettingsActivity mActivity;

    public PostHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostHistoryBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        binding.getRoot().setBackgroundColor(mActivity.customThemeWrapper.getBackgroundColor());
        applyCustomTheme();

        if (mActivity.isImmersiveInterfaceRespectForcedEdgeToEdge()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false, mActivity.isForcedImmersiveInterface());
                    binding.getRoot().setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        if (mActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), mActivity.typeface);
        }

        boolean isAnonymous = mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT);
        if (isAnonymous) {
            binding.infoTextViewPostHistoryFragment.setText(R.string.only_for_logged_in_user);
            binding.markPostsAsReadLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.readPostsLimitLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.readPostsLimitTextInputLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.markPostsAsReadAfterVotingLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.markPostsAsReadOnScrollLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyInSubredditsLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyInUsersLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyInSearchLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            return binding.getRoot();
        }

        binding.markPostsAsReadSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false));
        binding.readPostsLimitSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.READ_POSTS_LIMIT_ENABLED, true));
        binding.readPostsLimitTextInputEditTextPostHistoryFragment.setText(String.valueOf(postHistorySharedPreferences.getInt(
                mActivity.accountName + SharedPreferencesUtils.READ_POSTS_LIMIT, 500)));
        binding.markPostsAsReadAfterVotingSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, false));
        binding.markPostsAsReadOnScrollSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, false));
        binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));
        binding.hideReadPostsAutomaticallyInSubredditsSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_IN_SUBREDDITS_BASE, false));
        binding.hideReadPostsAutomaticallyInUsersSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_IN_USERS_BASE, false));
        binding.hideReadPostsAutomaticallyInSearchSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_IN_SEARCH_BASE, false));

        updateOptions();

        binding.markPostsAsReadLinearLayoutPostHistoryFragment.setOnClickListener(view ->
                binding.markPostsAsReadSwitchPostHistoryFragment.performClick());
        binding.markPostsAsReadSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, b).apply();
            updateOptions();
        });


        binding.readPostsLimitLinearLayoutPostHistoryFragment.setOnClickListener(view ->
            binding.readPostsLimitSwitchPostHistoryFragment.performClick());
        binding.readPostsLimitSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.READ_POSTS_LIMIT_ENABLED, b).apply();
            updateOptions();
        });
        binding.readPostsLimitTextInputEditTextPostHistoryFragment.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                String readPostsLimitString = binding.readPostsLimitTextInputEditTextPostHistoryFragment.getText().toString();
                if (readPostsLimitString.isEmpty()) {
                    binding.readPostsLimitTextInputEditTextPostHistoryFragment.setText("500");
                } else {
                    int readPostsLimit = Integer.parseInt(readPostsLimitString);
                    if (readPostsLimit < 100) {
                        binding.readPostsLimitTextInputEditTextPostHistoryFragment.setText("100");
                    }
                }
                postHistorySharedPreferences.edit().putInt(mActivity.accountName + SharedPreferencesUtils.READ_POSTS_LIMIT,
                        Integer.parseInt(binding.readPostsLimitTextInputEditTextPostHistoryFragment.getText().toString())).apply();
            }
        });

        binding.markPostsAsReadAfterVotingLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.markPostsAsReadAfterVotingSwitchPostHistoryFragment.performClick());
        binding.markPostsAsReadAfterVotingSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) ->
                postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, b).apply());

        binding.markPostsAsReadOnScrollLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.markPostsAsReadOnScrollSwitchPostHistoryFragment.performClick());
        binding.markPostsAsReadOnScrollSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, b).apply());

        binding.hideReadPostsAutomaticallyLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.performClick());
        binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, b).apply();
            updateOptions();
        });

        binding.hideReadPostsAutomaticallyInSubredditsLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.hideReadPostsAutomaticallyInSubredditsSwitchPostHistoryFragment.performClick());
        binding.hideReadPostsAutomaticallyInSubredditsSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_IN_SUBREDDITS_BASE, b).apply());

        binding.hideReadPostsAutomaticallyInUsersLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.hideReadPostsAutomaticallyInUsersSwitchPostHistoryFragment.performClick());
        binding.hideReadPostsAutomaticallyInUsersSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_IN_USERS_BASE, b).apply());

        binding.hideReadPostsAutomaticallyInSearchLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.hideReadPostsAutomaticallyInSearchSwitchPostHistoryFragment.performClick());
        binding.hideReadPostsAutomaticallyInSearchSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(mActivity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_IN_SEARCH_BASE, b).apply());

        return binding.getRoot();
    }

    private void updateOptions() {
        if (binding.markPostsAsReadSwitchPostHistoryFragment.isChecked()) {
            binding.readPostsLimitLinearLayoutPostHistoryFragment.setVisibility(View.VISIBLE);
            binding.readPostsLimitTextInputLayoutPostHistoryFragment.setVisibility(View.VISIBLE);
            binding.markPostsAsReadAfterVotingLinearLayoutPostHistoryFragment.setVisibility(View.VISIBLE);
            binding.markPostsAsReadOnScrollLinearLayoutPostHistoryFragment.setVisibility(View.VISIBLE);
            binding.hideReadPostsAutomaticallyLinearLayoutPostHistoryFragment.setVisibility(View.VISIBLE);
            binding.hideReadPostsAutomaticallyInSubredditsLinearLayoutPostHistoryFragment.setVisibility(binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.isChecked() ? View.VISIBLE : View.GONE);
            binding.hideReadPostsAutomaticallyInUsersLinearLayoutPostHistoryFragment.setVisibility(binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.isChecked() ? View.VISIBLE : View.GONE);
            binding.hideReadPostsAutomaticallyInSearchLinearLayoutPostHistoryFragment.setVisibility(binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.isChecked() ? View.VISIBLE : View.GONE);

            boolean limitReadPosts = postHistorySharedPreferences.getBoolean(
                    mActivity.accountName + SharedPreferencesUtils.READ_POSTS_LIMIT_ENABLED, true);
            binding.readPostsLimitTextInputLayoutPostHistoryFragment.setVisibility(limitReadPosts ? View.VISIBLE : View.GONE);
        } else {
            binding.readPostsLimitLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.readPostsLimitTextInputLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.markPostsAsReadAfterVotingLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.markPostsAsReadOnScrollLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyInSubredditsLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyInUsersLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyInSearchLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
        }
    }

    private void applyCustomTheme() {
        binding.infoTextViewPostHistoryFragment.setTextColor(mActivity.customThemeWrapper.getSecondaryTextColor());
        Drawable infoDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_info_preference_day_night_24dp, mActivity.customThemeWrapper.getPrimaryIconColor());
        binding.infoTextViewPostHistoryFragment.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        int primaryTextColor = mActivity.customThemeWrapper.getPrimaryTextColor();
        binding.markPostsAsReadTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.readPostsLimitTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.readPostsLimitTextInputLayoutPostHistoryFragment.setBoxStrokeColor(primaryTextColor);
        binding.readPostsLimitTextInputLayoutPostHistoryFragment.setDefaultHintTextColor(ColorStateList.valueOf(primaryTextColor));
        binding.readPostsLimitTextInputEditTextPostHistoryFragment.setTextColor(primaryTextColor);
        binding.markPostsAsReadAfterVotingTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.markPostsAsReadOnScrollTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.hideReadPostsAutomaticallyTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.hideReadPostsAutomaticallyInSubredditsTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.hideReadPostsAutomaticallyInUsersTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.hideReadPostsAutomaticallyInSearchTextViewPostHistoryFragment.setTextColor(primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.readPostsLimitTextInputLayoutPostHistoryFragment.setCursorColor(ColorStateList.valueOf(primaryTextColor));
        } else {
            setCursorDrawableColor(binding.readPostsLimitTextInputEditTextPostHistoryFragment, primaryTextColor);
        }
    }

    private void setCursorDrawableColor(EditText editText, int color) {
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (SettingsActivity) context;
    }
}