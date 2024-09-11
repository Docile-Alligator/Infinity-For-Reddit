package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
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
    private SettingsActivity activity;

    public PostHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostHistoryBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        binding.getRoot().setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());
        applyCustomTheme();

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        if (activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.infoTextViewPostHistoryFragment.setText(R.string.only_for_logged_in_user);
            binding.markPostsAsReadLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.markPostsAsReadAfterVotingLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.markPostsAsReadOnScrollLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            binding.hideReadPostsAutomaticallyLinearLayoutPostHistoryFragment.setVisibility(View.GONE);
            return binding.getRoot();
        }

        binding.markPostsAsReadSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                activity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false));
        binding.markPostsAsReadAfterVotingSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                activity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, false));
        binding.markPostsAsReadOnScrollSwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                activity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, false));
        binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.setChecked(postHistorySharedPreferences.getBoolean(
                activity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));

        binding.markPostsAsReadLinearLayoutPostHistoryFragment.setOnClickListener(view -> {
            binding.markPostsAsReadSwitchPostHistoryFragment.performClick();
        });

        binding.markPostsAsReadSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) ->
                postHistorySharedPreferences.edit().putBoolean(activity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, b).apply());

        binding.markPostsAsReadAfterVotingLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.markPostsAsReadAfterVotingSwitchPostHistoryFragment.performClick());

        binding.markPostsAsReadAfterVotingSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) ->
                postHistorySharedPreferences.edit().putBoolean(activity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, b).apply());

        binding.markPostsAsReadOnScrollLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.markPostsAsReadOnScrollSwitchPostHistoryFragment.performClick());

        binding.markPostsAsReadOnScrollSwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(activity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, b).apply());

        binding.hideReadPostsAutomaticallyLinearLayoutPostHistoryFragment.setOnClickListener(view -> binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.performClick());

        binding.hideReadPostsAutomaticallySwitchPostHistoryFragment.setOnCheckedChangeListener((compoundButton, b) -> postHistorySharedPreferences.edit().putBoolean(activity.accountName + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, b).apply());

        return binding.getRoot();
    }

    private void applyCustomTheme() {
        binding.infoTextViewPostHistoryFragment.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
        Drawable infoDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_day_night_24dp, activity.customThemeWrapper.getPrimaryIconColor());
        binding.infoTextViewPostHistoryFragment.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        binding.markPostsAsReadTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.markPostsAsReadAfterVotingTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.markPostsAsReadOnScrollTextViewPostHistoryFragment.setTextColor(primaryTextColor);
        binding.hideReadPostsAutomaticallyTextViewPostHistoryFragment.setTextColor(primaryTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (SettingsActivity) context;
    }
}