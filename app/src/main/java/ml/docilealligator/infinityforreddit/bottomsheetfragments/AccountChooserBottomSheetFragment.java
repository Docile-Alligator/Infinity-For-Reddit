package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountViewModel;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.AccountChooserRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedBottomSheetDialogFragment;

public class AccountChooserBottomSheetFragment extends LandscapeExpandedBottomSheetDialogFragment {

    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
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

        recyclerView = rootView.findViewById(R.id.recycler_view_account_chooser_bottom_sheet_fragment);
        adapter = new AccountChooserRecyclerViewAdapter(activity, customThemeWrapper, Glide.with(this),
                account -> {
                    if (activity instanceof AccountChooserListener) {
                        ((AccountChooserListener) activity).onAccountSelected(account);
                    }
                    dismiss();
                });
        recyclerView.setAdapter(adapter);

        accountViewModel = new ViewModelProvider(this,
                new AccountViewModel.Factory(redditDataRoomDatabase)).get(AccountViewModel.class);
        accountViewModel.getAllAccountsLiveData().observe(getViewLifecycleOwner(), accounts -> {
            adapter.changeAccountsDataset(accounts);
        });

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