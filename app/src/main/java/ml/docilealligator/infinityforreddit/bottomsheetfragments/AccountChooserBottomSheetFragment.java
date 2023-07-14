package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountViewModel;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.AccountChooserRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class AccountChooserBottomSheetFragment extends LandscapeExpandedBottomSheetDialogFragment {

    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    @Named("security")
    SharedPreferences sharedPreferences;
    BaseActivity activity;
    RecyclerView recyclerView;
    AccountChooserRecyclerViewAdapter adapter;
    AccountViewModel accountViewModel;

    public AccountChooserBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account_chooser_bottom_sheet, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        Utils.hideKeyboard(activity);

        recyclerView = rootView.findViewById(R.id.recycler_view_account_chooser_bottom_sheet_fragment);
        adapter = new AccountChooserRecyclerViewAdapter(activity, customThemeWrapper, Glide.with(this),
                account -> {
                    if (activity instanceof AccountChooserListener) {
                        ((AccountChooserListener) activity).onAccountSelected(account);
                    }
                    dismiss();
                });
        recyclerView.setAdapter(adapter);

        if (sharedPreferences.getBoolean(SharedPreferencesUtils.REQUIRE_AUTHENTICATION_TO_GO_TO_ACCOUNT_SECTION_IN_NAVIGATION_DRAWER, false)) {
            BiometricManager biometricManager = BiometricManager.from(activity);
            if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                Executor executor = ContextCompat.getMainExecutor(activity);
                BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                        executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        accountViewModel = new ViewModelProvider(AccountChooserBottomSheetFragment.this,
                                new AccountViewModel.Factory(redditDataRoomDatabase)).get(AccountViewModel.class);
                        accountViewModel.getAllAccountsLiveData().observe(getViewLifecycleOwner(), accounts -> {
                            adapter.changeAccountsDataset(accounts);
                        });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        dismiss();
                    }
                });

                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.unlock))
                        .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                        .build();

                biometricPrompt.authenticate(promptInfo);
            } else {
                dismiss();
            }
        } else {
            accountViewModel = new ViewModelProvider(this,
                    new AccountViewModel.Factory(redditDataRoomDatabase)).get(AccountViewModel.class);
            accountViewModel.getAllAccountsLiveData().observe(getViewLifecycleOwner(), accounts -> {
                adapter.changeAccountsDataset(accounts);
            });
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }

    public interface AccountChooserListener {
        void onAccountSelected(Account account);
    }
}