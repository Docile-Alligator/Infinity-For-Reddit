package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityEditProfileBinding;
import ml.docilealligator.infinityforreddit.events.SubmitChangeAvatarEvent;
import ml.docilealligator.infinityforreddit.events.SubmitChangeBannerEvent;
import ml.docilealligator.infinityforreddit.events.SubmitSaveProfileEvent;
import ml.docilealligator.infinityforreddit.services.EditProfileService;
import ml.docilealligator.infinityforreddit.user.UserViewModel;
import ml.docilealligator.infinityforreddit.utils.EditProfileUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class EditProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_BANNER_REQUEST_CODE = 0x401;
    private static final int PICK_IMAGE_AVATAR_REQUEST_CODE = 0x402;

    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutViewEditProfileActivity);
        }

        setSupportActionBar(binding.toolbarViewEditProfileActivity);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        binding.imageViewChangeBannerEditProfileActivity.setOnClickListener(view -> {
            startPickImage(PICK_IMAGE_BANNER_REQUEST_CODE);
        });
        binding.imageViewChangeAvatarEditProfileActivity.setOnClickListener(view -> {
            startPickImage(PICK_IMAGE_AVATAR_REQUEST_CODE);
        });

        final RequestManager glide = Glide.with(this);
        final UserViewModel.Factory userViewModelFactory =
                new UserViewModel.Factory(getApplication(), mRedditDataRoomDatabase, accountName);
        final UserViewModel userViewModel =
                new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);

        userViewModel.getUserLiveData().observe(this, userData -> {
            if (userData == null) {
                return;
            }
            // BANNER
            final String userBanner = userData.getBanner();
            LayoutParams cBannerLp = (LayoutParams) binding.imageViewChangeBannerEditProfileActivity.getLayoutParams();
            if (userBanner == null || userBanner.isEmpty()) {
                binding.imageViewChangeBannerEditProfileActivity.setLongClickable(false);
                cBannerLp.gravity = Gravity.CENTER;
                binding.imageViewChangeBannerEditProfileActivity.setLayoutParams(cBannerLp);
                binding.imageViewChangeBannerEditProfileActivity.setOnLongClickListener(v -> false);
            } else {
                binding.imageViewChangeBannerEditProfileActivity.setLongClickable(true);
                cBannerLp.gravity = Gravity.END | Gravity.BOTTOM;
                binding.imageViewChangeBannerEditProfileActivity.setLayoutParams(cBannerLp);
                glide.load(userBanner).into(binding.imageViewBannerEditProfileActivity);
                binding.imageViewChangeBannerEditProfileActivity.setOnLongClickListener(view -> {
                    if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                        return false;
                    }
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.remove_banner)
                            .setMessage(R.string.are_you_sure)
                            .setPositiveButton(R.string.yes, (dialogInterface, i)
                                    -> EditProfileUtils.deleteBanner(mOauthRetrofit,
                                    accessToken,
                                    accountName,
                                    new EditProfileUtils.EditProfileUtilsListener() {
                                        @Override
                                        public void success() {
                                            Toast.makeText(EditProfileActivity.this,
                                                    R.string.message_remove_banner_success,
                                                    Toast.LENGTH_SHORT).show();
                                            binding.imageViewBannerEditProfileActivity.setImageDrawable(null);//
                                        }

                                        @Override
                                        public void failed(String message) {
                                            Toast.makeText(EditProfileActivity.this,
                                                    getString(R.string.message_remove_banner_failed, message),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }))
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return true;
                });
            }
            // AVATAR
            final String userAvatar = userData.getIconUrl();
            glide.load(userAvatar)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                    .into(binding.imageViewAvatarEditProfileActivity);
            LayoutParams cAvatarLp = (LayoutParams) binding.imageViewChangeAvatarEditProfileActivity.getLayoutParams();
            if (userAvatar.contains("avatar_default_")) {
                binding.imageViewChangeAvatarEditProfileActivity.setLongClickable(false);
                binding.imageViewChangeAvatarEditProfileActivity.setOnLongClickListener(v -> false);
            } else {
                binding.imageViewChangeAvatarEditProfileActivity.setLongClickable(true);
                binding.imageViewChangeAvatarEditProfileActivity.setOnLongClickListener(view -> {
                    if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                        return false;
                    }
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.remove_avatar)
                            .setMessage(R.string.are_you_sure)
                            .setPositiveButton(R.string.yes, (dialogInterface, i)
                                    -> EditProfileUtils.deleteAvatar(mOauthRetrofit,
                                    accessToken,
                                    accountName,
                                    new EditProfileUtils.EditProfileUtilsListener() {
                                        @Override
                                        public void success() {
                                            Toast.makeText(EditProfileActivity.this,
                                                    R.string.message_remove_avatar_success,
                                                    Toast.LENGTH_SHORT).show();//
                                        }

                                        @Override
                                        public void failed(String message) {
                                            Toast.makeText(EditProfileActivity.this,
                                                    getString(R.string.message_remove_avatar_failed, message),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }))
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return true;
                });
            }

            binding.editTextAboutYouEditProfileActivity.setText(userData.getDescription());
            binding.editTextDisplayNameEditProfileActivity.setText(userData.getTitle());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            return;
        }
        Intent intent = new Intent(this, EditProfileService.class);
        intent.setData(data.getData());
        intent.putExtra(EditProfileService.EXTRA_ACCOUNT_NAME, accountName);
        intent.putExtra(EditProfileService.EXTRA_ACCESS_TOKEN, accessToken);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        switch (requestCode) {
            case PICK_IMAGE_BANNER_REQUEST_CODE:
                intent.putExtra(EditProfileService.EXTRA_POST_TYPE, EditProfileService.EXTRA_POST_TYPE_CHANGE_BANNER);
                ContextCompat.startForegroundService(this, intent);
                break;
            case PICK_IMAGE_AVATAR_REQUEST_CODE:
                intent.putExtra(EditProfileService.EXTRA_POST_TYPE, EditProfileService.EXTRA_POST_TYPE_CHANGE_AVATAR);
                ContextCompat.startForegroundService(this, intent);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save_edit_profile_activity) {
            String displayName = null;
            if (binding.editTextDisplayNameEditProfileActivity.getText() != null) {
                displayName = binding.editTextDisplayNameEditProfileActivity.getText().toString();
            }
            String aboutYou = null;
            if (binding.editTextAboutYouEditProfileActivity.getText() != null) {
                aboutYou = binding.editTextAboutYouEditProfileActivity.getText().toString();
            }
            if (aboutYou == null || displayName == null) return false; //

            Intent intent = new Intent(this, EditProfileService.class);
            intent.putExtra(EditProfileService.EXTRA_ACCOUNT_NAME, accountName);
            intent.putExtra(EditProfileService.EXTRA_ACCESS_TOKEN, accessToken);
            intent.putExtra(EditProfileService.EXTRA_DISPLAY_NAME, displayName); //
            intent.putExtra(EditProfileService.EXTRA_ABOUT_YOU, aboutYou); //
            intent.putExtra(EditProfileService.EXTRA_POST_TYPE, EditProfileService.EXTRA_POST_TYPE_SAVE_EDIT_PROFILE);

            ContextCompat.startForegroundService(this, intent);
            return true;
        }
        return false;
    }

    @Subscribe
    public void onSubmitChangeAvatar(SubmitChangeAvatarEvent event) {
        if (event.isSuccess) {
            Toast.makeText(this, R.string.message_change_avatar_success, Toast.LENGTH_SHORT).show();
        } else {
            String message = getString(R.string.message_change_avatar_failed, event.errorMessage);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onSubmitChangeBanner(SubmitChangeBannerEvent event) {
        if (event.isSuccess) {
            Toast.makeText(this, R.string.message_change_banner_success, Toast.LENGTH_SHORT).show();
        } else {
            String message = getString(R.string.message_change_banner_failed, event.errorMessage);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onSubmitSaveProfile(SubmitSaveProfileEvent event) {
        if (event.isSuccess) {
            Toast.makeText(this, R.string.message_save_profile_success, Toast.LENGTH_SHORT).show();
        } else {
            String message = getString(R.string.message_save_profile_failed, event.errorMessage);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutViewEditProfileActivity, binding.collapsingToolbarLayoutEditProfileActivity, binding.toolbarViewEditProfileActivity);
        binding.rootLayoutViewEditProfileActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        changeColorTextView(binding.contentViewEditProfileActivity, mCustomThemeWrapper.getPrimaryTextColor());
        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.rootLayoutViewEditProfileActivity, typeface);
        }
    }

    private void changeColorTextView(ViewGroup viewGroup, int color) {
        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                changeColorTextView((ViewGroup) child, color);
            } else if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
        }
    }

    private void startPickImage(int requestId) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_from_gallery)),
                requestId);
    }
}
