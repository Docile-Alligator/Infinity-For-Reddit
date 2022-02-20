package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.recyclerview.widget.ConcatAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;

public class NavigationDrawerRecyclerViewMergedAdapter {
    private HeaderSectionRecyclerViewAdapter headerSectionRecyclerViewAdapter;
    private AccountSectionRecyclerViewAdapter accountSectionRecyclerViewAdapter;
    private RedditSectionRecyclerViewAdapter redditSectionRecyclerViewAdapter;
    private PostSectionRecyclerViewAdapter postSectionRecyclerViewAdapter;
    private PreferenceSectionRecyclerViewAdapter preferenceSectionRecyclerViewAdapter;
    private FavoriteSubscribedSubredditsSectionRecyclerViewAdapter favoriteSubscribedSubredditsSectionRecyclerViewAdapter;
    private SubscribedSubredditsRecyclerViewAdapter subscribedSubredditsRecyclerViewAdapter;
    private AccountManagementSectionRecyclerViewAdapter accountManagementSectionRecyclerViewAdapter;
    private ConcatAdapter mainPageConcatAdapter;

    private Resources resources;
    private ArrayList<Account> accounts;
    private boolean isLoggedIn;
    private boolean isInMainPage = true;

    public NavigationDrawerRecyclerViewMergedAdapter(BaseActivity baseActivity, SharedPreferences sharedPreferences,
                                                     SharedPreferences nsfwAndSpoilerSharedPreferences,
                                                     SharedPreferences navigationDrawerSharedPreferences,
                                                     CustomThemeWrapper customThemeWrapper,
                                                     String accountName,
                                                     ItemClickListener itemClickListener) {
        RequestManager glide = Glide.with(baseActivity);

        headerSectionRecyclerViewAdapter = new HeaderSectionRecyclerViewAdapter(baseActivity, glide, accountName,
                sharedPreferences, navigationDrawerSharedPreferences, new HeaderSectionRecyclerViewAdapter.PageToggle() {
            @Override
            public void openAccountSection() {
                NavigationDrawerRecyclerViewMergedAdapter.this.openAccountSection();
            }

            @Override
            public void closeAccountSectionWithoutChangeIconResource() {
                NavigationDrawerRecyclerViewMergedAdapter.this.closeAccountSectionWithoutChangeIconResource();
            }
        });
        accountSectionRecyclerViewAdapter = new AccountSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                navigationDrawerSharedPreferences, accountName != null, itemClickListener);
        redditSectionRecyclerViewAdapter = new RedditSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                navigationDrawerSharedPreferences, itemClickListener);
        postSectionRecyclerViewAdapter = new PostSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                navigationDrawerSharedPreferences, accountName != null, itemClickListener);
        preferenceSectionRecyclerViewAdapter = new PreferenceSectionRecyclerViewAdapter(baseActivity, customThemeWrapper,
                accountName, nsfwAndSpoilerSharedPreferences, navigationDrawerSharedPreferences, itemClickListener);
        favoriteSubscribedSubredditsSectionRecyclerViewAdapter = new FavoriteSubscribedSubredditsSectionRecyclerViewAdapter(
                baseActivity, glide, customThemeWrapper, navigationDrawerSharedPreferences, itemClickListener);
        subscribedSubredditsRecyclerViewAdapter = new SubscribedSubredditsRecyclerViewAdapter(baseActivity, glide,
                customThemeWrapper, navigationDrawerSharedPreferences, itemClickListener);
        accountManagementSectionRecyclerViewAdapter = new AccountManagementSectionRecyclerViewAdapter(baseActivity,
                customThemeWrapper, glide, accountName != null, itemClickListener);

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
        isInMainPage = false;
    }

    public void closeAccountSectionWithoutChangeIconResource() {
        mainPageConcatAdapter.removeAdapter(accountManagementSectionRecyclerViewAdapter);

        mainPageConcatAdapter.addAdapter(accountSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(redditSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(postSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(preferenceSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(favoriteSubscribedSubredditsSectionRecyclerViewAdapter);
        mainPageConcatAdapter.addAdapter(subscribedSubredditsRecyclerViewAdapter);
    }

    public void closeAccountSectionWithoutChangeIconResource(boolean checkIsInMainPage) {
        closeAccountSectionWithoutChangeIconResource();
        headerSectionRecyclerViewAdapter.closeAccountSectionWithoutChangeIconResource(checkIsInMainPage);
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

    public interface ItemClickListener {
        void onMenuClick(int stringId);
        void onSubscribedSubredditClick(String subredditName);
        void onAccountClick(String accountName);
    }
}
