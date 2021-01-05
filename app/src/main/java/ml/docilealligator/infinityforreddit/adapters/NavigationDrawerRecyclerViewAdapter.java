package ml.docilealligator.infinityforreddit.adapters;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.R;
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
    private static final int CURRENT_MENU_ITEMS = 17;

    private AppCompatActivity appCompatActivity;
    private Resources resources;
    private RequestManager glide;
    private String accountName;
    private String userIconUrl;
    private String userBannerUrl;
    private int karma;
    private boolean isNSFWEnabled;
    private boolean requireAuthToAccountSection;
    private ItemClickListener itemClickListener;
    private boolean isLoggedIn;
    private boolean isInMainPage = true;
    private ArrayList<SubscribedSubredditData> favoriteSubscribedSubreddits;
    private ArrayList<SubscribedSubredditData> subscribedSubreddits;
    private ArrayList<Account> accounts;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int dividerColor;
    private int primaryIconColor;

    public NavigationDrawerRecyclerViewAdapter(AppCompatActivity appCompatActivity, SharedPreferences sharedPreferences,
                                               SharedPreferences nsfwAndSpoilerSharedPreferences,
                                               CustomThemeWrapper customThemeWrapper,
                                               String accountName, String userIconUrl,
                                               String userBannerUrl, int karma,
                                               ItemClickListener itemClickListener) {
        this.appCompatActivity = appCompatActivity;
        resources = appCompatActivity.getResources();
        glide = Glide.with(appCompatActivity);
        this.accountName = accountName;
        this.userIconUrl = userIconUrl;
        this.userBannerUrl = userBannerUrl;
        this.karma = karma;
        isNSFWEnabled = nsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        requireAuthToAccountSection = sharedPreferences.getBoolean(SharedPreferencesUtils.REQUIRE_AUTHENTICATION_TO_GO_TO_ACCOUNT_SECTION_IN_NAVIGATION_DRAWER, false);
        isLoggedIn = accountName != null;
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
                if (position == CURRENT_MENU_ITEMS) {
                    return VIEW_TYPE_MENU_GROUP_TITLE;
                } else if (!favoriteSubscribedSubreddits.isEmpty() && position == CURRENT_MENU_ITEMS + favoriteSubscribedSubreddits.size() + 1) {
                    return VIEW_TYPE_MENU_GROUP_TITLE;
                } else if (position > CURRENT_MENU_ITEMS) {
                    if (!favoriteSubscribedSubreddits.isEmpty() && position <= CURRENT_MENU_ITEMS + favoriteSubscribedSubreddits.size()) {
                        return VIEW_TYPE_FAVORITE_SUBSCRIBED_SUBREDDIT;
                    } else {
                        return VIEW_TYPE_SUBSCRIBED_SUBREDDIT;
                    }
                } else if (position == 0) {
                    return VIEW_TYPE_NAV_HEADER;
                } else if (position == 1 || position == 6 || position == 12) {
                    return VIEW_TYPE_MENU_GROUP_TITLE;
                } else if (position == 16) {
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
            if (isLoggedIn) {
                ((NavHeaderViewHolder) holder).karmaTextView.setText(appCompatActivity.getString(R.string.karma_info, karma));
                ((NavHeaderViewHolder) holder).accountNameTextView.setText(accountName);
                if (userIconUrl != null && !userIconUrl.equals("")) {
                    glide.load(userIconUrl)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0))))
                            .into(((NavHeaderViewHolder) holder).profileImageView);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                            .into(((NavHeaderViewHolder) holder).profileImageView);
                }

                if (userBannerUrl != null && !userBannerUrl.equals("")) {
                    glide.load(userBannerUrl).into(((NavHeaderViewHolder) holder).bannerImageView);
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

            ((NavHeaderViewHolder) holder).itemView.setOnClickListener(view -> {
                if (isInMainPage) {
                    if (requireAuthToAccountSection) {
                        BiometricManager biometricManager = BiometricManager.from(appCompatActivity);
                        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                            Executor executor = ContextCompat.getMainExecutor(appCompatActivity);
                            BiometricPrompt biometricPrompt = new BiometricPrompt(appCompatActivity,
                                    executor, new BiometricPrompt.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationSucceeded(
                                        @NonNull BiometricPrompt.AuthenticationResult result) {
                                    super.onAuthenticationSucceeded(result);
                                    openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                                }
                            });

                            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(appCompatActivity.getString(R.string.unlock_account_section))
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
            if (position == 1) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_account);
            } else if (position == 6) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_post);
            } else if (position == 12) {
                ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_preferences);
            } else {
                if (!favoriteSubscribedSubreddits.isEmpty() && position == CURRENT_MENU_ITEMS) {
                    ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.favorites);
                } else {
                    ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.subscriptions);
                }
            }
        } else if (holder instanceof MenuItemViewHolder) {
            int stringId = 0;
            int drawableId = 0;
            boolean setOnClickListener = true;

            if (isInMainPage) {
                if (isLoggedIn) {
                    switch (position) {
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
                            stringId = R.string.inbox;
                            drawableId = R.drawable.ic_inbox_24dp;
                            break;
                        case 7:
                            stringId = R.string.upvoted;
                            drawableId = R.drawable.ic_arrow_upward_black_24dp;
                            break;
                        case 8:
                            stringId = R.string.downvoted;
                            drawableId = R.drawable.ic_arrow_downward_black_24dp;
                            break;
                        case 9:
                            stringId = R.string.hidden;
                            drawableId = R.drawable.ic_outline_lock_24dp;
                            break;
                        case 10:
                            stringId = R.string.account_saved_thing_activity_label;
                            drawableId = R.drawable.ic_outline_bookmarks_24dp;
                            break;
                        case 11:
                            stringId = R.string.gilded;
                            drawableId = R.drawable.ic_star_border_24dp;
                            break;
                        case 13:
                            if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                                stringId = R.string.dark_theme;
                                drawableId = R.drawable.ic_dark_theme_24dp;
                            } else {
                                stringId = R.string.light_theme;
                                drawableId = R.drawable.ic_light_theme_24dp;
                            }
                            break;
                        case 14:
                            setOnClickListener = false;
                            if (isNSFWEnabled) {
                                stringId = R.string.disable_nsfw;
                                drawableId = R.drawable.ic_nsfw_off_24dp;
                            } else {
                                stringId = R.string.enable_nsfw;
                                drawableId = R.drawable.ic_nsfw_on_24dp;
                            }

                            ((MenuItemViewHolder) holder).itemView.setOnClickListener(view -> {
                                if (isNSFWEnabled) {
                                    isNSFWEnabled = false;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.enable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(appCompatActivity.getDrawable(R.drawable.ic_nsfw_on_24dp));
                                    itemClickListener.onMenuClick(R.string.disable_nsfw);
                                } else {
                                    isNSFWEnabled = true;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.disable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(appCompatActivity.getDrawable(R.drawable.ic_nsfw_off_24dp));
                                    itemClickListener.onMenuClick(R.string.enable_nsfw);
                                }
                            });
                            break;
                        case 15:
                            stringId = R.string.settings;
                            drawableId = R.drawable.ic_settings_24dp;
                    }
                } else {
                    switch (position) {
                        case 1:
                            if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                                stringId = R.string.dark_theme;
                                drawableId = R.drawable.ic_dark_theme_24dp;
                            } else {
                                stringId = R.string.light_theme;
                                drawableId = R.drawable.ic_light_theme_24dp;
                            }
                            break;
                        case 2:
                            setOnClickListener = false;
                            if (isNSFWEnabled) {
                                stringId = R.string.disable_nsfw;
                                drawableId = R.drawable.ic_nsfw_off_24dp;
                            } else {
                                stringId = R.string.enable_nsfw;
                                drawableId = R.drawable.ic_nsfw_on_24dp;
                            }

                            ((MenuItemViewHolder) holder).itemView.setOnClickListener(view -> {
                                if (isNSFWEnabled) {
                                    isNSFWEnabled = false;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.enable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(appCompatActivity.getDrawable(R.drawable.ic_nsfw_on_24dp));
                                    itemClickListener.onMenuClick(R.string.disable_nsfw);
                                } else {
                                    isNSFWEnabled = true;
                                    ((MenuItemViewHolder) holder).menuTextView.setText(R.string.disable_nsfw);
                                    ((MenuItemViewHolder) holder).imageView.setImageDrawable(appCompatActivity.getDrawable(R.drawable.ic_nsfw_off_24dp));
                                    itemClickListener.onMenuClick(R.string.enable_nsfw);
                                }
                            });
                            break;
                        case 3:
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
                ((MenuItemViewHolder) holder).imageView.setImageDrawable(appCompatActivity.getDrawable(drawableId));
                if (setOnClickListener) {
                    int finalStringId = stringId;
                    ((MenuItemViewHolder) holder).itemView.setOnClickListener(view -> itemClickListener.onMenuClick(finalStringId));
                }
            }
        } else if (holder instanceof FavoriteSubscribedThingViewHolder) {
            SubscribedSubredditData subreddit = favoriteSubscribedSubreddits.get(position - CURRENT_MENU_ITEMS - 1);
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

            ((FavoriteSubscribedThingViewHolder) holder).itemView.setOnClickListener(view -> {
                itemClickListener.onSubscribedSubredditClick(subredditName);
            });
        } else if (holder instanceof SubscribedThingViewHolder) {
            SubscribedSubredditData subreddit = favoriteSubscribedSubreddits.isEmpty() ? subscribedSubreddits.get(position - CURRENT_MENU_ITEMS - 1)
                    : subscribedSubreddits.get(position - CURRENT_MENU_ITEMS - favoriteSubscribedSubreddits.size() - 2);
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

            ((SubscribedThingViewHolder) holder).itemView.setOnClickListener(view -> {
                itemClickListener.onSubscribedSubredditClick(subredditName);
            });
        } else if (holder instanceof AccountViewHolder) {
            glide.load(accounts.get(position - 1).getProfileImageUrl())
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                    .into(((AccountViewHolder) holder).profileImageGifImageView);
            ((AccountViewHolder) holder).usernameTextView.setText(accounts.get(position - 1).getUsername());
            ((AccountViewHolder) holder).itemView.setOnClickListener(view ->
                    itemClickListener.onAccountClick(accounts.get(position - 1).getUsername()));
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
                    return CURRENT_MENU_ITEMS + (favoriteSubscribedSubreddits.isEmpty() ? 0 : favoriteSubscribedSubreddits.size() + 1)
                            + (subscribedSubreddits.isEmpty() ? 0 : subscribedSubreddits.size() + 1);
                }
                return CURRENT_MENU_ITEMS - 1;
            } else {
                return 4;
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

    public void updateKarma(int karma) {
        this.karma = karma;
        notifyItemChanged(0);
    }

    public void setNSFWEnabled(boolean isNSFWEnabled) {
        this.isNSFWEnabled = isNSFWEnabled;
        if (isInMainPage) {
            if (isLoggedIn) {
                notifyItemChanged(CURRENT_MENU_ITEMS - 3);
            } else {
                notifyItemChanged(2);
            }
        }
    }

    public void setRequireAuthToAccountSection(boolean requireAuthToAccountSection) {
        this.requireAuthToAccountSection = requireAuthToAccountSection;
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
        }
    }

    class MenuGroupTitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        MenuGroupTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView;
            titleTextView.setTextColor(secondaryTextColor);
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
            usernameTextView.setTextColor(primaryTextColor);
        }
    }
}
