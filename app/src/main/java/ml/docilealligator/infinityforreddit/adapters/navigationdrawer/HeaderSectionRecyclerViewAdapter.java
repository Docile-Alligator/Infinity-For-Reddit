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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;

public class HeaderSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity baseActivity;
    private Resources resources;
    private RequestManager glide;
    private String accountName;
    private String profileImageUrl;
    private String bannerImageUrl;
    private int karma;
    private boolean requireAuthToAccountSection;
    private boolean showAvatarOnTheRightInTheNavigationDrawer;
    private boolean isLoggedIn;
    private boolean isInMainPage = true;
    private PageToggle pageToggle;

    public HeaderSectionRecyclerViewAdapter(BaseActivity baseActivity, RequestManager glide, String accountName,
                                            SharedPreferences sharedPreferences,
                                            SharedPreferences navigationDrawerSharedPreferences,
                                            PageToggle pageToggle) {
        this.baseActivity = baseActivity;
        resources = baseActivity.getResources();
        this.glide = glide;
        this.accountName = accountName;
        isLoggedIn = accountName != null;
        this.pageToggle = pageToggle;
        requireAuthToAccountSection = sharedPreferences.getBoolean(SharedPreferencesUtils.REQUIRE_AUTHENTICATION_TO_GO_TO_ACCOUNT_SECTION_IN_NAVIGATION_DRAWER, false);
        showAvatarOnTheRightInTheNavigationDrawer = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT, false);
        showAvatarOnTheRightInTheNavigationDrawer = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AVATAR_ON_THE_RIGHT, false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NavHeaderViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nav_header_main, parent, false));
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
                ((NavHeaderViewHolder) holder).dropIconImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_arrow_drop_down_24dp));
            } else {
                ((NavHeaderViewHolder) holder).dropIconImageView.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_arrow_drop_up_24dp));
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
                                    pageToggle.openAccountSection();
                                    openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                                }
                            });

                            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(baseActivity.getString(R.string.unlock_account_section))
                                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                                    .build();

                            biometricPrompt.authenticate(promptInfo);
                        } else {
                            pageToggle.openAccountSection();
                            openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                        }
                    } else {
                        pageToggle.openAccountSection();
                        openAccountSection(((NavHeaderViewHolder) holder).dropIconImageView);
                    }
                } else {
                    ((NavHeaderViewHolder) holder).dropIconImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_drop_down_24dp, null));
                    pageToggle.closeAccountSectionWithoutChangeIconResource();
                    closeAccountSectionWithoutChangeIconResource(false);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private void openAccountSection(ImageView dropIconImageView) {
        dropIconImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_drop_up_24dp, null));
        isInMainPage = false;
    }

    public boolean closeAccountSectionWithoutChangeIconResource(boolean checkIsInMainPage) {
        if (!(checkIsInMainPage && isInMainPage)) {
            isInMainPage = true;
            return true;
        }

        notifyItemChanged(0);
        return false;
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

    public interface PageToggle {
        void openAccountSection();
        void closeAccountSectionWithoutChangeIconResource();
    }
}
