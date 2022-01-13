package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.InboxActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class NavigationDrawerRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface ItemClickListener {
        void onMenuClick(int stringId);
        void onSubscribedSubredditClick(String subredditName);
        void onAccountClick(String accountName);
    }

    private static final int VIEW_TYPE_NAV_HEADER = 0;
    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_MENU_ITEM = 2;
    private static final int VIEW_TYPE_DIVIDER = 3;
    private static final int VIEW_TYPE_FAVORITE_SUBSCRIBED_SUBREDDIT = 4;
    private static final int VIEW_TYPE_SUBSCRIBED_SUBREDDIT = 5;
    private static final int VIEW_TYPE_ACCOUNT = 6;
    private static final int CURRENT_MENU_ITEMS = 20;
    private static final int ACCOUNT_SECTION_ITEMS = 4;
    private static final int REDDIT_SECTION_ITEMS = 2;
    private static final int POST_SECTION_ITEMS = 5;
    private static final int PREFERENCES_SECTION_ITEMS = 3;

    private BaseActivity baseActivity;
    private Resources resources;
    private RequestManager glide;
    private String accountName;
    private String profileImageUrl;
    private String bannerImageUrl;
    private int karma;
    private int inboxCount;
    private boolean isNSFWEnabled;
    private boolean requireAuthToAccountSection;
    private boolean showAvatarOnTheRightInTheNavigationDrawer;
    private ItemClickListener itemClickListener;
    private boolean isLoggedIn;
    private boolean isInMainPage = true;
    private boolean collapseAccountSection;
    private boolean collapseRedditSection;
    private boolean collapsePostSection;
    private boolean collapsePreferencesSection;
    private boolean collapseFavoriteSubredditsSection;
    private boolean collapseSubscribedSubredditsSection;
    private boolean hideFavoriteSubredditsSection;
    private boolean hideSubscribedSubredditsSection;
    private ArrayList<SubscribedSubredditData> favoriteSubscribedSubreddits;
    private ArrayList<SubscribedSubredditData> subscribedSubreddits;
    private ArrayList<Account> accounts;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int dividerColor;
    private int primaryIconColor;

    public NavigationDrawerRecyclerViewAdapter(BaseActivity baseActivity, SharedPreferences sharedPreferences,
                                               SharedPreferences nsfwAndSpoilerSharedPreferences,
                                               SharedPreferences navigationDrawerSharedPreferences,
                                               CustomThemeWrapper customThemeWrapper,
                                               String accountName,
                                               ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        resources = baseActivity.getResources();
        glide = Glide.with(baseActivity);
        this.accountName = accountName;
        isNSFWEnabled = nsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        requireAuthToAccountSection = sharedPreferences.getBoolean(SharedPreferencesUtils.REQUIRE_AUTHENTICATION_TO_GO_TO_ACCOUNT_SECTION_IN_NAVIGATION_DRAWER, false);
        showAvatarOnTheRightInTheNavigationDrawer = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT, false);
        isLoggedIn = accountName != null;
        collapseAccountSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_ACCOUNT_SECTION, false);
        collapseRedditSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_REDDIT_SECTION, false);
        collapsePostSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_POST_SECTION, false);
        collapsePreferencesSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_PREFERENCES_SECTION, false);
        collapseFavoriteSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_FAVORITE_SUBREDDITS_SECTION, false);
        collapseSubscribedSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_SUBSCRIBED_SUBREDDITS_SECTION, false);
        hideFavoriteSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_FAVORITE_SUBREDDITS_SECTION, false);
        hideSubscribedSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_SUBSCRIBED_SUBREDDITS_SECTIONS, false);
        showAvatarOnTheRightInTheNavigationDrawer = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT, false);

        this.itemClickListener = itemClickListener;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        dividerColor = customThemeWrapper.getDividerColor();
        primaryIconColor = customThemeWrapper.getPrimaryIconColor();
        favoriteSubscribedSubreddits = new ArrayList<>();
        subscribedSubreddits = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (isInMainPage) {
            if (isLoggedIn) {
                if (position == CURRENT_MENU_ITEMS - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                        - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)) {
                    return VIEW_TYPE_MENU_GROUP_TITLE;
                } else if (!hideFavoriteSubredditsSection && !favoriteSubscribedSubreddits.isEmpty() && position == CURRENT_MENU_ITEMS
                        - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                        - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)
                        + (collapseFavoriteSubredditsSection ? 0 : favoriteSubscribedSubreddits.size()) + 1) {
                    return VIEW_TYPE_MENU_GROUP_TITLE;
                } else if (position > CURRENT_MENU_ITEMS - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                        - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)) {
                    if (!favoriteSubscribedSubreddits.isEmpty() && !hideFavoriteSubredditsSection &&
                            !collapseFavoriteSubredditsSection && position <= CURRENT_MENU_ITEMS
                            - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                            - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                            - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                            - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)
                            + favoriteSubscribedSubreddits.size()) {
                        return VIEW_TYPE_FAVORITE_SUBSCRIBED_SUBREDDIT;
                    } else {
                        return VIEW_TYPE_SUBSCRIBED_SUBREDDIT;
                    }
                } else if (position == 0) {
                    return VIEW_TYPE_NAV_HEADER;
                } else if (position == 1
                        || position == (ACCOUNT_SECTION_ITEMS + 2) - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        || position == (ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS + 3)
                        - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        || position == (ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS + POST_SECTION_ITEMS + 4)
                        - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)) {
                    return VIEW_TYPE_MENU_GROUP_TITLE;
                } else if (position == (CURRENT_MENU_ITEMS - 1) - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                        - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)) {
                    return VIEW_TYPE_DIVIDER;
                } else {
                    return VIEW_TYPE_MENU_ITEM;
                }
            } else {
                if (position == 0) {
                    return VIEW_TYPE_NAV_HEADER;
                } else {
                    return VIEW_TYPE_MENU_ITEM;
                }
            }
        } else {
            if (position == 0) {
                return VIEW_TYPE_NAV_HEADER;
            }
            if (accounts != null) {
                if (position > accounts.size()) {
                    return VIEW_TYPE_MENU_ITEM;
                } else {
                    return VIEW_TYPE_ACCOUNT;
                }
            } else {
                return VIEW_TYPE_MENU_ITEM;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NAV_HEADER:
                return new NavHeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.nav_header_main, parent, false));
            case VIEW_TYPE_MENU_GROUP_TITLE:
                return new MenuGroupTitleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_nav_drawer_menu_group_title, parent, false));
            case VIEW_TYPE_MENU_ITEM:
                return new MenuItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_nav_drawer_menu_item, parent, false));
            case VIEW_TYPE_DIVIDER:
                return new DividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_nav_drawer_divider, parent, false));
            case VIEW_TYPE_ACCOUNT:
                return new AccountViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_nav_drawer_account, parent, false));
            case VIEW_TYPE_FAVORITE_SUBSCRIBED_SUBREDDIT:
                return new FavoriteSubscribedThingViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_nav_drawer_subscribed_thing, parent, false));
            default:
                return new SubscribedThingViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_nav_drawer_subscribed_thing, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NavHeaderViewHolder) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((NavHeaderViewHolder) holder).profileImageView.getLayoutParams();
            if (showAvatarOnTheRightInTheNavigationDrawer) {
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            }
            ((NavHeaderViewHolder) holder).profileImageView.setLayoutParams(params);
            if (isLoggedIn) {
                ((NavHeaderViewHolder) holder).karmaTextView.setText(baseActivity.getString(R.string.karma_info, karma));
                ((NavHeaderViewHolder) holder).accountNameTextView.setText(accountName);
                if (profileImageUrl != null && !profileImageUrl.equals("")) {
                    glide.load(profileImageUrl)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0))))
                            .into(((NavHeaderViewHolder) holder).profileImageView);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                            .into(((NavHeaderViewHolder) holder).profileImageView);
                }

                if (bannerImageUrl != null && !bannerImageUrl.equals("")) {
                    glide.load(bannerImageUrl).into(((NavHeaderViewHolder) holder).bannerImageView);
                }
            } else {
                ((NavHeaderViewHolder) holder).karmaTextView.setText(R.string.press_here_to_login);
                ((NavHeaderViewHolder) holder).accountNameTextView.setText(R.string.anonymous_account);
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                        .into(((NavHeaderViewHolder) holder).profileImageView);
            }

            if (isInMainPage) {
                ((NavHeaderViewHolder) holder).dropIconImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_arrow_drop_down_24px));
            } else {
                ((NavHeaderViewHolder) holder).dropIconImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_arrow_drop_up_24px));
            }

            holder.itemView.setOnClickListener(view -> {
                if (isInMainPage) {
                    if (requireAuthToAccountSection) {
                        BiometricManager biometricManager = BiometricManager.from(baseActivity);
                        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                            Executor executor = ContextCompat.getMainExecutor(baseActivity);
                            BiometricPrompt biometricPrompt = new BiometricPrompt(baseActivity,
                                    executor, new BiometricPrompt.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationSucceeded(
                                        @NonNull BiometricPrompt.AuthenticationResult result) {
                                    super.onAuthenticationSucceeded(result);
                                    openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                                }
                            });

                            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(baseActivity.getString(R.string.unlock_account_section))
                                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                                    .build();

                            biometricPrompt.authenticate(promptInfo);
                        } else {
                            openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                        }
                    } else {
                        openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                    }
                } else {
                    ((NavHeaderViewHolder) holder).dropIconImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_drop_down_24px, null));
                    closeAccountSectionWithoutChangeIconResource(false);
                }
            });
        } else if (holder instanceof MenuGroupTitleViewHolder) {
            int type;
            if (position == 1) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_account);
                if (collapseAccountSection) {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24px);
                } else {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24px);
                }
                type = 0;
            } else if (position == (ACCOUNT_SECTION_ITEMS + 2) - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_reddit);
                if (collapseRedditSection) {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24px);
                } else {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24px);
                }
                type = 1;
            } else if (position == (ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS + 3)
                    - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                    - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_post);
                if (collapsePostSection) {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24px);
                } else {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24px);
                }
                type = 2;
            } else if (position == (ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS + POST_SECTION_ITEMS + 4)
                    - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                    - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                    - (collapsePostSection ? POST_SECTION_ITEMS : 0)) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_preferences);
                if (collapsePreferencesSection) {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24px);
                } else {
                    ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24px);
                }
                type = 3;
            } else {
                if (!hideFavoriteSubredditsSection && !favoriteSubscribedSubreddits.isEmpty() && position == CURRENT_MENU_ITEMS
                        - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                        - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)) {
                    ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.favorites);
                    if (collapseFavoriteSubredditsSection) {
                        ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24px);
                    } else {
                        ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24px);
                    }
                    type = 4;
                } else {
                    ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.subscriptions);
                    if (collapseSubscribedSubredditsSection) {
                        ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24px);
                    } else {
                        ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24px);
                    }
                    type = 5;
                }
            }

            holder.itemView.setOnClickListener(view -> {
                switch (type) {
                    case 0:
                        if (collapseAccountSection) {
                            collapseAccountSection = !collapseAccountSection;
                            notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, ACCOUNT_SECTION_ITEMS);
                        } else {
                            collapseAccountSection = !collapseAccountSection;
                            notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, ACCOUNT_SECTION_ITEMS);
                        }
                        break;
                    case 1:
                        if (collapseRedditSection) {
                            collapseRedditSection = !collapseRedditSection;
                            notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, REDDIT_SECTION_ITEMS);
                        } else {
                            collapseRedditSection = !collapseRedditSection;
                            notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, REDDIT_SECTION_ITEMS);
                        }
                        break;
                    case 2:
                        if (collapsePostSection) {
                            collapsePostSection = !collapsePostSection;
                            notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, POST_SECTION_ITEMS);
                        } else {
                            collapsePostSection = !collapsePostSection;
                            notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, POST_SECTION_ITEMS);
                        }
                        break;
                    case 3:
                        if (collapsePreferencesSection) {
                            collapsePreferencesSection = !collapsePreferencesSection;
                            notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, PREFERENCES_SECTION_ITEMS);
                        } else {
                            collapsePreferencesSection = !collapsePreferencesSection;
                            notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, PREFERENCES_SECTION_ITEMS);
                        }
                        break;
                    case 4:
                        if (collapseFavoriteSubredditsSection) {
                            collapseFavoriteSubredditsSection = !collapseFavoriteSubredditsSection;
                            notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, favoriteSubscribedSubreddits.size());
                        } else {
                            collapseFavoriteSubredditsSection = !collapseFavoriteSubredditsSection;
                            notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, favoriteSubscribedSubreddits.size());
                        }
                        break;
                    case 5:
                        if (collapseSubscribedSubredditsSection) {
                            collapseSubscribedSubredditsSection = !collapseSubscribedSubredditsSection;
                            notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, subscribedSubreddits.size());
                        } else {
                            collapseSubscribedSubredditsSection = !collapseSubscribedSubredditsSection;
                            notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, subscribedSubreddits.size());
                        }
                        break;
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (holder instanceof MenuItemViewHolder) {
            int stringId = 0;
            int drawableId = 0;
            boolean setOnClickListener = true;

            if (isInMainPage) {
                if (isLoggedIn) {
                    int pseudoPosition = position;
                    if (collapseAccountSection && collapseRedditSection && collapsePostSection) {
                        pseudoPosition += ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS + POST_SECTION_ITEMS;
                    } else if (collapseAccountSection && collapseRedditSection) {
                        pseudoPosition += ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS;
                    } else if (collapseAccountSection && collapsePostSection) {
                        if (position > REDDIT_SECTION_ITEMS + 2) {
                            pseudoPosition += ACCOUNT_SECTION_ITEMS + POST_SECTION_ITEMS;
                        } else {
                            pseudoPosition += ACCOUNT_SECTION_ITEMS;
                        }
                    } else if (collapseRedditSection && collapsePostSection) {
                        if (position > ACCOUNT_SECTION_ITEMS + 1) {
                            pseudoPosition += REDDIT_SECTION_ITEMS + POST_SECTION_ITEMS;
                        }
                    } else if (collapseAccountSection) {
                        pseudoPosition += ACCOUNT_SECTION_ITEMS;
                    } else if (collapseRedditSection) {
                        if (position > ACCOUNT_SECTION_ITEMS + 1) {
                            pseudoPosition += REDDIT_SECTION_ITEMS;
                        }
                    } else if (collapsePostSection) {
                        if (position > ACCOUNT_SECTION_ITEMS + REDDIT_SECTION_ITEMS + 2) {
                            pseudoPosition += POST_SECTION_ITEMS;
                        }
                    }
                    switch (pseudoPosition) {
                        case 2:
                            stringId = R.string.profile;
                            drawableId = R.drawable.ic_account_circle_24dp;
                            break;
                        case 3:
                            stringId = R.string.subscriptions;
                            drawableId = R.drawable.ic_subscritptions_bottom_app_bar_24dp;
                            break;
                        case 4:
                            stringId = R.string.multi_reddit;
                            drawableId = R.drawable.ic_multi_reddit_24dp;
                            break;
                        case 5:
                            setOnClickListener = false;
                            if (inboxCount > 0) {
                                ((MenuItemViewHolder) holder).menuTextView.setText(baseActivity.getString(R.string.inbox_with_count, inboxCount));
                            } else {
                                ((MenuItemViewHolder) holder).menuTextView.setText(R.string.inbox);
                            }
                            ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_inbox_24dp));
                            holder.itemView.setOnClickListener(view -> {
                                Intent intent = new Intent(baseActivity, InboxActivity.class);
                                baseActivity.startActivity(intent);
                            });
                            break;
                        case 7:
                            stringId = R.string.rpan;
                            drawableId = R.drawable.ic_rpan_24dp;
                            break;
                        case 8:
                            stringId = R.string.trending;
                            drawableId = R.drawable.ic_trending_24dp;
                            break;
                        case 10:
                            stringId = R.string.upvoted;
                            drawableId = R.drawable.ic_arrow_upward_black_24dp;
                            break;
                        case 11:
                            stringId = R.string.downvoted;
                            drawableId = R.drawable.ic_arrow_downward_black_24dp;
                            break;
                        case 12:
                            stringId = R.string.hidden;
                            drawableId = R.drawable.ic_outline_lock_24dp;
                            break;
                        case 13:
                            stringId = R.string.account_saved_thing_activity_label;
                            drawableId = R.drawable.ic_outline_bookmarks_24dp;
                            break;
                        case 14:
                            stringId = R.string.gilded;
                            drawableId = R.drawable.ic_star_border_24dp;
                            break;
                        case 16:
                            if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                                stringId = R.string.dark_theme;
                                drawableId = R.drawable.ic_dark_theme_24dp;
                            } else {
                                stringId = R.string.light_theme;
                                drawableId = R.drawable.ic_light_theme_24dp;
                            }
                            break;
                        case 17:
                            setOnClickListener = false;
                            if (isNSFWEnabled) {
                                stringId = R.string.disable_nsfw;
                                drawableId = R.drawable.ic_nsfw_off_24dp;
                            } else {
                                stringId = R.string.enable_nsfw;
                                drawableId = R.drawable.ic_nsfw_on_24dp;
                            }

                            holder.itemView.setOnClickListener(view -> {
                                if (isNSFWEnabled) {
                                    isNSFWEnabled = false;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.enable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_nsfw_on_24dp));
                                    itemClickListener.onMenuClick(R.string.disable_nsfw);
                                } else {
                                    isNSFWEnabled = true;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.disable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_nsfw_off_24dp));
                                    itemClickListener.onMenuClick(R.string.enable_nsfw);
                                }
                            });
                            break;
                        case 18:
                            stringId = R.string.settings;
                            drawableId = R.drawable.ic_settings_24dp;
                    }
                } else {
                    switch (position) {
                        case 1:
                            stringId = R.string.subscriptions;
                            drawableId = R.drawable.ic_subscritptions_bottom_app_bar_24dp;
                            break;
                        case 2:
                            stringId = R.string.rpan;
                            drawableId = R.drawable.ic_rpan_24dp;
                            break;
                        case 3:
                            stringId = R.string.trending;
                            drawableId = R.drawable.ic_trending_24dp;
                            break;
                        case 4:
                            if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                                stringId = R.string.dark_theme;
                                drawableId = R.drawable.ic_dark_theme_24dp;
                            } else {
                                stringId = R.string.light_theme;
                                drawableId = R.drawable.ic_light_theme_24dp;
                            }
                            break;
                        case 5:
                            setOnClickListener = false;
                            if (isNSFWEnabled) {
                                stringId = R.string.disable_nsfw;
                                drawableId = R.drawable.ic_nsfw_off_24dp;
                            } else {
                                stringId = R.string.enable_nsfw;
                                drawableId = R.drawable.ic_nsfw_on_24dp;
                            }

                            holder.itemView.setOnClickListener(view -> {
                                if (isNSFWEnabled) {
                                    isNSFWEnabled = false;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.enable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_nsfw_on_24dp));
                                    itemClickListener.onMenuClick(R.string.disable_nsfw);
                                } else {
                                    isNSFWEnabled = true;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.disable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_nsfw_off_24dp));
                                    itemClickListener.onMenuClick(R.string.enable_nsfw);
                                }
                            });
                            break;
                        case 6:
                            stringId = R.string.settings;
                            drawableId = R.drawable.ic_settings_24dp;
                    }
                }
            } else {
                if (isLoggedIn) {
                    int offset = accounts == null ? 0 : accounts.size();
                    if (position == offset + 1) {
                        stringId = R.string.add_account;
                        drawableId = R.drawable.ic_outline_add_circle_outline_24dp;
                    } else if (position == offset + 2) {
                        stringId = R.string.anonymous_account;
                        drawableId = R.drawable.ic_anonymous_24dp;
                    } else if (position == offset + 3) {
                        stringId = R.string.log_out;
                        drawableId = R.drawable.ic_log_out_24dp;
                    }
                } else {
                    stringId = R.string.add_account;
                    drawableId = R.drawable.ic_outline_add_circle_outline_24dp;
                }
            }

            if (stringId != 0) {
                ((MenuItemViewHolder) holder).menuTextView.setText(stringId);
                ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, drawableId));
                if (setOnClickListener) {
                    int finalStringId = stringId;
                    holder.itemView.setOnClickListener(view -> itemClickListener.onMenuClick(finalStringId));
                }
            }
        } else if (holder instanceof FavoriteSubscribedThingViewHolder) {
            SubscribedSubredditData subreddit = favoriteSubscribedSubreddits.get(position - (CURRENT_MENU_ITEMS
                    - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                    - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                    - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                    - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0))
                    - 1);
            String subredditName = subreddit.getName();
            String iconUrl = subreddit.getIconUrl();
            ((FavoriteSubscribedThingViewHolder) holder).subredditNameTextView.setText(subredditName);
            if (iconUrl != null && !iconUrl.equals("")) {
                glide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((FavoriteSubscribedThingViewHolder) holder).iconGifImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((FavoriteSubscribedThingViewHolder) holder).iconGifImageView);
            }

            holder.itemView.setOnClickListener(view -> {
                itemClickListener.onSubscribedSubredditClick(subredditName);
            });
        } else if (holder instanceof SubscribedThingViewHolder) {
            SubscribedSubredditData subreddit = favoriteSubscribedSubreddits.isEmpty() || hideFavoriteSubredditsSection ? subscribedSubreddits.get(position - (CURRENT_MENU_ITEMS
                    - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                    - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                    - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                    - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0))
                    - 1)
                    : subscribedSubreddits.get(position - (CURRENT_MENU_ITEMS
                    - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                    - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                    - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                    - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0))
                    - (collapseFavoriteSubredditsSection ? 0 : favoriteSubscribedSubreddits.size())
                    - 2);
            String subredditName = subreddit.getName();
            String iconUrl = subreddit.getIconUrl();
            ((SubscribedThingViewHolder) holder).subredditNameTextView.setText(subredditName);
            if (iconUrl != null && !iconUrl.equals("")) {
                glide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((SubscribedThingViewHolder) holder).iconGifImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((SubscribedThingViewHolder) holder).iconGifImageView);
            }

            holder.itemView.setOnClickListener(view -> {
                itemClickListener.onSubscribedSubredditClick(subredditName);
            });
        } else if (holder instanceof AccountViewHolder) {
            glide.load(accounts.get(position - 1).getProfileImageUrl())
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                    .into(((AccountViewHolder) holder).profileImageGifImageView);
            ((AccountViewHolder) holder).usernameTextView.setText(accounts.get(position - 1).getAccountName());
            holder.itemView.setOnClickListener(view ->
                    itemClickListener.onAccountClick(accounts.get(position - 1).getAccountName()));
        }
    }

    public boolean closeAccountSectionWithoutChangeIconResource(boolean checkIsInMainPage) {
        if (!(checkIsInMainPage && isInMainPage)) {
            notifyItemRangeRemoved(1, getItemCount() - 1);
            if (isLoggedIn) {
                notifyItemRangeInserted(1, (favoriteSubscribedSubreddits.isEmpty() ? 0 : favoriteSubscribedSubreddits.size() + 1)
                        + (subscribedSubreddits.isEmpty() ? 0 : subscribedSubreddits.size() + 1) + CURRENT_MENU_ITEMS - 1);
            } else {
                notifyItemRangeInserted(1, 2);
            }
            isInMainPage = true;

            return true;
        }

        return false;
    }

    private void openAccountSection(ImageView dropIconImageView) {
        dropIconImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_drop_up_24px, null));
        notifyItemRangeRemoved(1, getItemCount() - 1);
        if (accounts != null) {
            notifyItemRangeInserted(1, accounts.size() + 3);
        } else {
            if (isLoggedIn) {
                notifyItemRangeInserted(1, 3);
            } else {
                notifyItemInserted(1);
            }
        }
        isInMainPage = false;
    }

    @Override
    public int getItemCount() {
        if (isInMainPage) {
            if (isLoggedIn) {
                if (!(favoriteSubscribedSubreddits.isEmpty() && subscribedSubreddits.isEmpty())) {
                    if (hideFavoriteSubredditsSection && hideSubscribedSubredditsSection) {
                        return CURRENT_MENU_ITEMS
                                - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                                - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                                - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                                - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0);
                    } else if (hideFavoriteSubredditsSection) {
                        return CURRENT_MENU_ITEMS
                                + (subscribedSubreddits.isEmpty() ? 0 : subscribedSubreddits.size() + 1)
                                - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                                - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                                - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                                - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)
                                - (collapseSubscribedSubredditsSection ? subscribedSubreddits.size() : 0);
                    } else if (hideSubscribedSubredditsSection) {
                        return CURRENT_MENU_ITEMS + (favoriteSubscribedSubreddits.isEmpty() ? 0 : favoriteSubscribedSubreddits.size() + 1)
                                - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                                - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                                - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                                - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)
                                - (collapseFavoriteSubredditsSection ? favoriteSubscribedSubreddits.size() : 0);
                    }
                    return CURRENT_MENU_ITEMS + (favoriteSubscribedSubreddits.isEmpty() ? 0 : favoriteSubscribedSubreddits.size() + 1)
                            + (subscribedSubreddits.isEmpty() ? 0 : subscribedSubreddits.size() + 1)
                            - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                            - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                            - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                            - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0)
                            - (collapseFavoriteSubredditsSection ? favoriteSubscribedSubreddits.size() : 0)
                            - (collapseSubscribedSubredditsSection ? subscribedSubreddits.size() : 0);
                }
                return CURRENT_MENU_ITEMS - 1
                        - (collapseAccountSection ? ACCOUNT_SECTION_ITEMS : 0)
                        - (collapseRedditSection ? REDDIT_SECTION_ITEMS : 0)
                        - (collapsePostSection ? POST_SECTION_ITEMS : 0)
                        - (collapsePreferencesSection ? PREFERENCES_SECTION_ITEMS : 0);
            } else {
                return 7;
            }
        } else {
            if (isLoggedIn) {
                if (accounts != null && !accounts.isEmpty()) {
                    return 4 + accounts.size();
                } else {
                    return 4;
                }
            } else {
                if (accounts != null && !accounts.isEmpty()) {
                    return 2 + accounts.size();
                } else {
                    return 2;
                }
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubscribedThingViewHolder) {
            glide.clear(((SubscribedThingViewHolder) holder).iconGifImageView);
        }
    }

    public void setFavoriteSubscribedSubreddits(List<SubscribedSubredditData> favoriteSubscribedSubreddits) {
        this.favoriteSubscribedSubreddits = (ArrayList<SubscribedSubredditData>) favoriteSubscribedSubreddits;
        if (isInMainPage) {
            notifyDataSetChanged();
        }
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        this.subscribedSubreddits = (ArrayList<SubscribedSubredditData>) subscribedSubreddits;
        if (isInMainPage) {
            notifyDataSetChanged();
        }
    }

    public void changeAccountsDataset(List<Account> accounts) {
        this.accounts = (ArrayList<Account>) accounts;
        if (!isInMainPage) {
            notifyDataSetChanged();
        }
    }

    public void updateAccountInfo(String profileImageUrl, String bannerImageUrl, int karma) {
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.karma = karma;
        notifyItemChanged(0);
    }

    public void setNSFWEnabled(boolean isNSFWEnabled) {
        this.isNSFWEnabled = isNSFWEnabled;
        if (isInMainPage) {
            if (isLoggedIn) {
                notifyItemChanged(CURRENT_MENU_ITEMS - 3);
            } else {
                notifyItemChanged(3);
            }
        }
    }

    public void setRequireAuthToAccountSection(boolean requireAuthToAccountSection) {
        this.requireAuthToAccountSection = requireAuthToAccountSection;
    }

    public void setShowAvatarOnTheRightInTheNavigationDrawer(boolean showAvatarOnTheRightInTheNavigationDrawer) {
        this.showAvatarOnTheRightInTheNavigationDrawer = showAvatarOnTheRightInTheNavigationDrawer;
    }

    public void setInboxCount(int inboxCount) {
        this.inboxCount = inboxCount;
        notifyDataSetChanged();
    }

    class NavHeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name_text_view_nav_header_main)
        TextView accountNameTextView;
        @BindView(R.id.karma_text_view_nav_header_main)
        TextView karmaTextView;
        @BindView(R.id.profile_image_view_nav_header_main)
        GifImageView profileImageView;
        @BindView(R.id.banner_image_view_nav_header_main)
        ImageView bannerImageView;
        @BindView(R.id.account_switcher_image_view_nav_header_main)
        ImageView dropIconImageView;

        NavHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (baseActivity.typeface != null) {
                accountNameTextView.setTypeface(baseActivity.typeface);
                karmaTextView.setTypeface(baseActivity.typeface);
            }
        }
    }

    class MenuGroupTitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title_text_view_item_nav_drawer_menu_group_title)
        TextView titleTextView;
        @BindView(R.id.collapse_indicator_image_view_item_nav_drawer_menu_group_title)
        ImageView collapseIndicatorImageView;

        MenuGroupTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                titleTextView.setTypeface(baseActivity.typeface);
            }
            titleTextView.setTextColor(secondaryTextColor);
            collapseIndicatorImageView.setColorFilter(secondaryTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view_item_nav_drawer_menu_item)
        ImageView imageView;
        @BindView(R.id.text_view_item_nav_drawer_menu_item)
        TextView menuTextView;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                menuTextView.setTypeface(baseActivity.typeface);
            }
            menuTextView.setTextColor(primaryTextColor);
            imageView.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class DividerViewHolder extends RecyclerView.ViewHolder {

        DividerViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setBackgroundColor(dividerColor);
        }
    }

    class FavoriteSubscribedThingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thing_icon_gif_image_view_item_nav_drawer_subscribed_thing)
        GifImageView iconGifImageView;
        @BindView(R.id.thing_name_text_view_item_nav_drawer_subscribed_thing)
        TextView subredditNameTextView;

        FavoriteSubscribedThingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                subredditNameTextView.setTypeface(baseActivity.typeface);
            }
            subredditNameTextView.setTextColor(primaryTextColor);
        }
    }

    class SubscribedThingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thing_icon_gif_image_view_item_nav_drawer_subscribed_thing)
        GifImageView iconGifImageView;
        @BindView(R.id.thing_name_text_view_item_nav_drawer_subscribed_thing)
        TextView subredditNameTextView;

        SubscribedThingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                subredditNameTextView.setTypeface(baseActivity.typeface);
            }
            subredditNameTextView.setTextColor(primaryTextColor);
        }
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.profile_image_item_account)
        GifImageView profileImageGifImageView;
        @BindView(R.id.username_text_view_item_account)
        TextView usernameTextView;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                usernameTextView.setTypeface(baseActivity.typeface);
            }
            usernameTextView.setTextColor(primaryTextColor);
        }
    }
}
