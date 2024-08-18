package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.List;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;

public class NavigationDrawerRecyclerViewMergedAdapter {
    private final HeaderSectionRecyclerViewAdapter headerSectionRecyclerViewAdapter;
    private final AccountSectionRecyclerViewAdapter accountSectionRecyclerViewAdapter;
    private final RedditSectionRecyclerViewAdapter redditSectionRecyclerViewAdapter;
    private final PostSectionRecyclerViewAdapter postSectionRecyclerViewAdapter;
    private final PreferenceSectionRecyclerViewAdapter preferenceSectionRecyclerViewAdapter;
    private final FavoriteSubscribedSubredditsSectionRecyclerViewAdapter favoriteSubscribedSubredditsSectionRecyclerViewAdapter;
    private final SubscribedSubredditsRecyclerViewAdapter subscribedSubredditsRecyclerViewAdapter;
    private final AccountManagementSectionRecyclerViewAdapter accountManagementSectionRecyclerViewAdapter;
    private final ConcatAdapter mainPageConcatAdapter;

    public NavigationDrawerRecyclerViewMergedAdapter(BaseActivity baseActivity, SharedPreferences sharedPreferences,
                                                     SharedPreferences nsfwAndSpoilerSharedPreferences,
                                                     SharedPreferences navigationDrawerSharedPreferences,
                                                     SharedPreferences securitySharedPreferences,
                                                     CustomThemeWrapper customThemeWrapper,
                                                     @NonNull String accountName,
                                                     ItemClickListener itemClickListener) {
        RequestManager glide = Glide.with(baseActivity);

        headerSectionRecyclerViewAdapter = new HeaderSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                glide, accountName, sharedPreferences, navigationDrawerSharedPreferences, securitySharedPreferences,
                new HeaderSectionRecyclerViewAdapter.PageToggle() {
                    @Override
                    public void openAccountManagement() {
                        NavigationDrawerRecyclerViewMergedAdapter.this.openAccountSection();
                    }

                    @Override
                    public void closeAccountManagement() {
                        NavigationDrawerRecyclerViewMergedAdapter.this.closeAccountManagement(false);
                    }
                });
        accountSectionRecyclerViewAdapter = new AccountSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                navigationDrawerSharedPreferences, !accountName.equals(Account.ANONYMOUS_ACCOUNT), itemClickListener);
        redditSectionRecyclerViewAdapter = new RedditSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                navigationDrawerSharedPreferences, itemClickListener);
        postSectionRecyclerViewAdapter = new PostSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                navigationDrawerSharedPreferences, !accountName.equals(Account.ANONYMOUS_ACCOUNT), itemClickListener);
        preferenceSectionRecyclerViewAdapter = new PreferenceSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                accountName, nsfwAndSpoilerSharedPreferences, navigationDrawerSharedPreferences, itemClickListener);
        favoriteSubscribedSubredditsSectionRecyclerViewAdapter = new FavoriteSubscribedSubredditsSectionRecyclerViewAdapter(
                baseActivity, glide, customThemeWrapper, navigationDrawerSharedPreferences, itemClickListener);
        subscribedSubredditsRecyclerViewAdapter = new SubscribedSubredditsRecyclerViewAdapter(baseActivity, glide,
                customThemeWrapper, navigationDrawerSharedPreferences, itemClickListener);
        accountManagementSectionRecyclerViewAdapter = new AccountManagementSectionRecyclerViewAdapter(baseActivity,
                customThemeWrapper, glide, !accountName.equals(Account.ANONYMOUS_ACCOUNT), itemClickListener);

        mainPageConcatAdapter = new ConcatAdapter(
                headerSectionRecyclerViewAdapter,
                accountSectionRecyclerViewAdapter,
                redditSectionRecyclerViewAdapter,
                postSectionRecyclerViewAdapter,
                preferenceSectionRecyclerViewAdapter,
                favoriteSubscribedSubredditsSectionRecyclerViewAdapter,
                subscribedSubredditsRecyclerViewAdapter);
    }

    public ConcatAdapter getConcatAdapter() {
        return mainPageConcatAdapter;
    }

    private void openAccountSection() {
        mainPageConcatAdapter.removeAdapter(accountSectionRecyclerViewAdapter);
        mainPageConcatAdapter.removeAdapter(redditSectionRecyclerViewAdapter);
        mainPageConcatAdapter.removeAdapter(postSectionRecyclerViewAdapter);
        mainPageConcatAdapter.removeAdapter(preferenceSectionRecyclerViewAdapter);
        mainPageConcatAdapter.removeAdapter(favoriteSubscribedSubredditsSectionRecyclerViewAdapter);
        mainPageConcatAdapter.removeAdapter(subscribedSubredditsRecyclerViewAdapter);

        mainPageConcatAdapter.addAdapter(accountManagementSectionRecyclerViewAdapter);
    }

    public void closeAccountManagement(boolean refreshHeader) {
        mainPageConcatAdapter.removeAdapter(accountManagementSectionRecyclerViewAdapter);

        mainPageConcatAdapter.addAdapter(accountSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(redditSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(postSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(preferenceSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(favoriteSubscribedSubredditsSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(subscribedSubredditsRecyclerViewAdapter);

        if (refreshHeader) {
            headerSectionRecyclerViewAdapter.closeAccountManagement(true);
        }
    }

    public void updateAccountInfo(String profileImageUrl, String bannerImageUrl, int karma) {
        headerSectionRecyclerViewAdapter.updateAccountInfo(profileImageUrl, bannerImageUrl, karma);
    }

    public void setRequireAuthToAccountSection(boolean requireAuthToAccountSection) {
        headerSectionRecyclerViewAdapter.setRequireAuthToAccountSection(requireAuthToAccountSection);
    }

    public void setShowAvatarOnTheRightInTheNavigationDrawer(boolean showAvatarOnTheRightInTheNavigationDrawer) {
        headerSectionRecyclerViewAdapter.setShowAvatarOnTheRightInTheNavigationDrawer(showAvatarOnTheRightInTheNavigationDrawer);
    }

    public void changeAccountsDataset(List<Account> accounts) {
        accountManagementSectionRecyclerViewAdapter.changeAccountsDataset(accounts);
    }

    public void setInboxCount(int inboxCount) {
        accountSectionRecyclerViewAdapter.setInboxCount(inboxCount);
    }

    public void setNSFWEnabled(boolean isNSFWEnabled) {
        preferenceSectionRecyclerViewAdapter.setNSFWEnabled(isNSFWEnabled);
    }

    public void setFavoriteSubscribedSubreddits(List<SubscribedSubredditData> favoriteSubscribedSubreddits) {
        favoriteSubscribedSubredditsSectionRecyclerViewAdapter.setFavoriteSubscribedSubreddits(favoriteSubscribedSubreddits);
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        subscribedSubredditsRecyclerViewAdapter.setSubscribedSubreddits(subscribedSubreddits);
    }

    public void setHideKarma(boolean hideKarma) {
        headerSectionRecyclerViewAdapter.setHideKarma(hideKarma);
    }

    public interface ItemClickListener {
        void onMenuClick(int stringId);
        void onSubscribedSubredditClick(String subredditName);
        void onAccountClick(@NonNull String accountName);
        void onAccountLongClick(@NonNull String accountName);
    }
}
