package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.Executor;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.NavHeaderMainBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class HeaderSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final BaseActivity baseActivity;
    private final CustomThemeWrapper customThemeWrapper;
    private final Resources resources;
    private final RequestManager glide;
    private final String accountName;
    private String profileImageUrl;
    private String bannerImageUrl;
    private int karma;
    private boolean requireAuthToAccountSection;
    private boolean showAvatarOnTheRightInTheNavigationDrawer;
    private final boolean isLoggedIn;
    private boolean isInMainPage = true;
    private final PageToggle pageToggle;
    private boolean hideKarma;

    public HeaderSectionRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                            RequestManager glide, @NonNull String accountName,
                                            SharedPreferences sharedPreferences,
                                            SharedPreferences navigationDrawerSharedPreferences,
                                            SharedPreferences securitySharedPreferences,
                                            PageToggle pageToggle) {
        this.baseActivity = baseActivity;
        this.customThemeWrapper = customThemeWrapper;
        resources = baseActivity.getResources();
        this.glide = glide;
        this.accountName = accountName;
        isLoggedIn = !accountName.equals(Account.ANONYMOUS_ACCOUNT);
        this.pageToggle = pageToggle;
        requireAuthToAccountSection = securitySharedPreferences.getBoolean(SharedPreferencesUtils.REQUIRE_AUTHENTICATION_TO_GO_TO_ACCOUNT_SECTION_IN_NAVIGATION_DRAWER, false);
        showAvatarOnTheRightInTheNavigationDrawer = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT, false);
        showAvatarOnTheRightInTheNavigationDrawer = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT, false);
        this.hideKarma = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_ACCOUNT_KARMA_NAV_BAR, false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NavHeaderViewHolder(NavHeaderMainBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NavHeaderViewHolder) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((NavHeaderViewHolder) holder).binding.profileImageViewNavHeaderMain.getLayoutParams();
            if (showAvatarOnTheRightInTheNavigationDrawer) {
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            }
            ((NavHeaderViewHolder) holder).binding.profileImageViewNavHeaderMain.setLayoutParams(params);
            if (isLoggedIn) {
                if (hideKarma) {
                    int karmaTextHeight = ((NavHeaderViewHolder) holder).binding.karmaTextViewNavHeaderMain.getHeight();
                    ((NavHeaderViewHolder) holder).binding.karmaTextViewNavHeaderMain.setVisibility(View.GONE);
                    ((NavHeaderViewHolder) holder).binding.nameTextViewNavHeaderMain.setTranslationY(karmaTextHeight / 2);
                } else {
                    ((NavHeaderViewHolder) holder).binding.karmaTextViewNavHeaderMain.setVisibility(View.VISIBLE);
                    ((NavHeaderViewHolder) holder).binding.karmaTextViewNavHeaderMain.setText(baseActivity.getString(R.string.karma_info, karma));
                    ((NavHeaderViewHolder) holder).binding.nameTextViewNavHeaderMain.setTranslationY(0);
                }
                ((NavHeaderViewHolder) holder).binding.nameTextViewNavHeaderMain.setText(accountName);
                if (profileImageUrl != null && !profileImageUrl.equals("")) {
                    glide.load(profileImageUrl)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0))))
                            .into(((NavHeaderViewHolder) holder).binding.profileImageViewNavHeaderMain);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                            .into(((NavHeaderViewHolder) holder).binding.profileImageViewNavHeaderMain);
                }

                if (bannerImageUrl != null && !bannerImageUrl.equals("")) {
                    glide.load(bannerImageUrl).into(((NavHeaderViewHolder) holder).binding.bannerImageViewNavHeaderMain);
                }
            } else {
                ((NavHeaderViewHolder) holder).binding.karmaTextViewNavHeaderMain.setText(R.string.press_here_to_login);
                ((NavHeaderViewHolder) holder).binding.nameTextViewNavHeaderMain.setText(R.string.anonymous_account);
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                        .into(((NavHeaderViewHolder) holder).binding.profileImageViewNavHeaderMain);
            }

            if (isInMainPage) {
                ((NavHeaderViewHolder) holder).binding.accountSwitcherImageViewNavHeaderMain.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_arrow_drop_down_24dp));
            } else {
                ((NavHeaderViewHolder) holder).binding.accountSwitcherImageViewNavHeaderMain.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_arrow_drop_up_24dp));
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
                                    pageToggle.openAccountManagement();
                                    openAccountManagement(((NavHeaderViewHolder) holder).binding.accountSwitcherImageViewNavHeaderMain);
                                }
                            });

                            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(baseActivity.getString(R.string.unlock_account_section))
                                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                                    .build();

                            biometricPrompt.authenticate(promptInfo);
                        } else {
                            pageToggle.openAccountManagement();
                            openAccountManagement(((NavHeaderViewHolder) holder).binding.accountSwitcherImageViewNavHeaderMain);
                        }
                    } else {
                        pageToggle.openAccountManagement();
                        openAccountManagement(((NavHeaderViewHolder) holder).binding.accountSwitcherImageViewNavHeaderMain);
                    }
                } else {
                    ((NavHeaderViewHolder) holder).binding.accountSwitcherImageViewNavHeaderMain.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_drop_down_24dp, null));
                    pageToggle.closeAccountManagement();
                    closeAccountManagement(false);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private void openAccountManagement(ImageView dropIconImageView) {
        dropIconImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_drop_up_24dp, null));
        isInMainPage = false;
    }

    public void closeAccountManagement(boolean notifyItemChanged) {
        isInMainPage = true;
        if (notifyItemChanged) {
            notifyItemChanged(0);
        }
    }

    public void updateAccountInfo(String profileImageUrl, String bannerImageUrl, int karma) {
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.karma = karma;
        notifyItemChanged(0);
    }

    public void setRequireAuthToAccountSection(boolean requireAuthToAccountSection) {
        this.requireAuthToAccountSection = requireAuthToAccountSection;
    }

    public void setShowAvatarOnTheRightInTheNavigationDrawer(boolean showAvatarOnTheRightInTheNavigationDrawer) {
        this.showAvatarOnTheRightInTheNavigationDrawer = showAvatarOnTheRightInTheNavigationDrawer;
    }

    public void setHideKarma(boolean hideKarma) {
        this.hideKarma = hideKarma;
        notifyItemChanged(0);
    }

    class NavHeaderViewHolder extends RecyclerView.ViewHolder {
        NavHeaderMainBinding binding;

        NavHeaderViewHolder(@NonNull NavHeaderMainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            if (baseActivity.typeface != null) {
                binding.nameTextViewNavHeaderMain.setTypeface(baseActivity.typeface);
                binding.karmaTextViewNavHeaderMain.setTypeface(baseActivity.typeface);
            }

            itemView.setBackgroundColor(customThemeWrapper.getColorPrimary());
            binding.nameTextViewNavHeaderMain.setTextColor(customThemeWrapper.getToolbarPrimaryTextAndIconColor());
            binding.karmaTextViewNavHeaderMain.setTextColor(customThemeWrapper.getToolbarSecondaryTextColor());
            binding.accountSwitcherImageViewNavHeaderMain.setColorFilter(customThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    public interface PageToggle {
        void openAccountManagement();
        void closeAccountManagement();
    }
}
