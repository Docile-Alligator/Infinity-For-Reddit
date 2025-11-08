package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.SettingsActivity;
import ml.docilealligator.infinityforreddit.databinding.FragmentNsfwAndSpoilerBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class NsfwAndSpoilerFragment extends Fragment {

    private FragmentNsfwAndSpoilerBinding binding;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences nsfwAndBlurringSharedPreferences;

    private SettingsActivity activity;
    private boolean blurNsfw;
    private boolean doNotBlurNsfwInNsfwSubreddits;
    private boolean disableNsfwForever;
    private boolean manuallyCheckDisableNsfwForever = true;

    public NsfwAndSpoilerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNsfwAndSpoilerBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        applyCustomTheme();

        if (activity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    binding.getRoot().setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        binding.getRoot().setBackgroundColor(activity.customThemeWrapper.getBackgroundColor());

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        boolean enableNsfw = nsfwAndBlurringSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        blurNsfw = nsfwAndBlurringSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        doNotBlurNsfwInNsfwSubreddits = nsfwAndBlurringSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        boolean blurSpoiler = nsfwAndBlurringSharedPreferences.getBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
        disableNsfwForever = sharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false);

        if (enableNsfw) {
            binding.blurNsfwLinearLayoutNsfwAndSpoilerFragment.setVisibility(View.VISIBLE);
            binding.doNotBlurNsfwInNsfwSubredditsLinearLayoutNsfwAndSpoilerFragment.setVisibility(View.VISIBLE);
        }

        binding.enableNsfwSwitchNsfwAndSpoilerFragment.setChecked(enableNsfw);
        binding.blurNsfwSwitchNsfwAndSpoilerFragment.setChecked(blurNsfw);
        binding.doNotBlurNsfwInNsfwSubredditsSwitchNsfwAndSpoilerFragment.setChecked(doNotBlurNsfwInNsfwSubreddits);
        binding.blurSpoilerSwitchNsfwAndSpoilerFragment.setChecked(blurSpoiler);
        binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setChecked(disableNsfwForever);
        binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setEnabled(!disableNsfwForever);
        if (disableNsfwForever) {
            binding.disableNsfwForeverTextViewNsfwAndSpoilerFragment.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
            binding.disableNsfwForeverLinearLayoutNsfwAndSpoilerFragment.setEnabled(false);
        }

        binding.enableNsfwLinearLayoutNsfwAndSpoilerFragment.setOnClickListener(view -> binding.enableNsfwSwitchNsfwAndSpoilerFragment.performClick());
        binding.enableNsfwSwitchNsfwAndSpoilerFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.NSFW_BASE, b).apply();
            if (b) {
                binding.blurNsfwLinearLayoutNsfwAndSpoilerFragment.setVisibility(View.VISIBLE);
                binding.doNotBlurNsfwInNsfwSubredditsLinearLayoutNsfwAndSpoilerFragment.setVisibility(View.VISIBLE);
            } else {
                binding.blurNsfwLinearLayoutNsfwAndSpoilerFragment.setVisibility(View.GONE);
                binding.doNotBlurNsfwInNsfwSubredditsLinearLayoutNsfwAndSpoilerFragment.setVisibility(View.GONE);
            }
            EventBus.getDefault().post(new ChangeNSFWEvent(b));
        });

        binding.blurNsfwLinearLayoutNsfwAndSpoilerFragment.setOnClickListener(view -> binding.blurNsfwSwitchNsfwAndSpoilerFragment.performClick());
        binding.blurNsfwSwitchNsfwAndSpoilerFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(b, doNotBlurNsfwInNsfwSubreddits));
        });

        binding.doNotBlurNsfwInNsfwSubredditsLinearLayoutNsfwAndSpoilerFragment.setOnClickListener(view -> {
            binding.doNotBlurNsfwInNsfwSubredditsSwitchNsfwAndSpoilerFragment.performClick();
        });
        binding.doNotBlurNsfwInNsfwSubredditsSwitchNsfwAndSpoilerFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, b).apply();
            EventBus.getDefault().post(new ChangeNSFWBlurEvent(blurNsfw, b));
        });

        binding.blurSpoilerLinearLayoutNsfwAndSpoilerFragment.setOnClickListener(view -> binding.blurSpoilerSwitchNsfwAndSpoilerFragment.performClick());
        binding.blurSpoilerSwitchNsfwAndSpoilerFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            nsfwAndBlurringSharedPreferences.edit().putBoolean((activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : activity.accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, b).apply();
            EventBus.getDefault().post(new ChangeSpoilerBlurEvent(b));
        });

        binding.disableNsfwForeverLinearLayoutNsfwAndSpoilerFragment.setOnClickListener(view -> {
            binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.performClick();
        });
        binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setOnCheckedChangeListener((compoundButton, b) -> {
            if (manuallyCheckDisableNsfwForever) {
                manuallyCheckDisableNsfwForever = false;
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.disable_over_18_forever_message)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> {
                            sharedPreferences.edit().putBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, true).apply();
                            disableNsfwForever = true;
                            binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setEnabled(false);
                            binding.disableNsfwForeverLinearLayoutNsfwAndSpoilerFragment.setEnabled(false);
                            binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setChecked(true);
                            binding.disableNsfwForeverTextViewNsfwAndSpoilerFragment.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
                            EventBus.getDefault().post(new ChangeNSFWEvent(false));
                        })
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                            binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setChecked(false);
                            manuallyCheckDisableNsfwForever = true;
                        })
                        .setOnDismissListener(dialogInterface -> {
                            if (!disableNsfwForever) {
                                binding.disableNsfwForeverSwitchNsfwAndSpoilerFragment.setChecked(false);
                            }
                            manuallyCheckDisableNsfwForever = true;
                        })
                        .show();
            }
        });

        TextView messageTextView = new TextView(activity);
        int padding = (int) Utils.convertDpToPixel(24, activity);
        messageTextView.setPaddingRelative(padding, padding, padding, padding);
        SpannableString message = new SpannableString(getString(R.string.warning_message_sensitive_content, "https://www.redditinc.com/policies/user-agreement", "https://docile-alligator.github.io"));
        Linkify.addLinks(message, Linkify.WEB_URLS);
        messageTextView.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
            Intent intent = new Intent(activity, LinkResolverActivity.class);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            return true;
        }));
        messageTextView.setLinkTextColor(getResources().getColor(R.color.colorAccent));
        messageTextView.setText(message);
        new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                .setTitle(getString(R.string.warning))
                .setView(messageTextView)
                .setPositiveButton(R.string.agree, (dialogInterface, i) -> dialogInterface.dismiss())
                .setNegativeButton(R.string.do_not_agree, (dialogInterface, i) -> activity.triggerBackPress())
                .setCancelable(false)
                .show();

        return binding.getRoot();
    }

    private void applyCustomTheme() {
        int primaryTextColor = activity.customThemeWrapper.getPrimaryTextColor();
        binding.enableNsfwTextViewNsfwAndSpoilerFragment.setCompoundDrawablesWithIntrinsicBounds(Utils.getTintedDrawable(activity, R.drawable.ic_nsfw_on_day_night_24dp, activity.customThemeWrapper.getPrimaryIconColor()), null, null, null);
        binding.enableNsfwTextViewNsfwAndSpoilerFragment.setTextColor(primaryTextColor);
        binding.blurNsfwTextViewNsfwAndSpoilerFragment.setTextColor(primaryTextColor);
        binding.doNotBlurNsfwTextViewNsfwAndSpoilerFragment.setTextColor(primaryTextColor);
        binding.blurSpoilerTextViewNsfwAndSpoilerFragment.setTextColor(primaryTextColor);
        binding.dangerousTextViewNsfwAndSpoilerFragment.setTextColor(primaryTextColor);
        binding.disableNsfwForeverTextViewNsfwAndSpoilerFragment.setTextColor(primaryTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (SettingsActivity) context;
    }
}