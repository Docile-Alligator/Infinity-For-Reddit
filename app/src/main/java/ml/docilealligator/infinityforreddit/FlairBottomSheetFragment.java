package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class FlairBottomSheetFragment extends BottomSheetDialogFragment {

    interface FlairSelectionCallback {
        void flairSelected(Flair flair);
    }

    static final String EXTRA_ACCESS_TOKEN = "EAT";
    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    @BindView(R.id.progress_bar_flair_bottom_sheet_fragment) ProgressBar progressBar;
    @BindView(R.id.error_text_view_flair_bottom_sheet_fragment) TextView errorTextView;
    @BindView(R.id.recycler_view_bottom_sheet_fragment) RecyclerView recyclerView;

    private String mAccessToken;
    private String mSubredditName;

    private Activity mAcitivity;
    private FlairBottomSheetRecyclerViewAdapter mAdapter;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    public FlairBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flair_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        mAcitivity = getActivity();

        ((Infinity) mAcitivity.getApplication()).getAppComponent().inject(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        mAdapter = new FlairBottomSheetRecyclerViewAdapter(flair -> {
            ((FlairSelectionCallback) mAcitivity).flairSelected(flair);
            dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        mAccessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        mSubredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);

        fetchFlairs();

        return rootView;
    }

    private void fetchFlairs() {
        FetchFlairs.fetchFlairsInSubreddit(mOauthRetrofit, mAccessToken,
                mSubredditName, new FetchFlairs.FetchFlairsInSubredditListener() {
            @Override
            public void fetchSuccessful(ArrayList<Flair> flairs) {
                progressBar.setVisibility(View.GONE);
                if(flairs == null || flairs.size() == 0) {
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(R.string.no_flair);
                } else {
                    errorTextView.setVisibility(View.GONE);
                    mAdapter.changeDataset(flairs);
                }
            }

            @Override
            public void fetchFailed() {
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.VISIBLE);
                errorTextView.setText(R.string.error_loading_flairs);
                errorTextView.setOnClickListener(view -> fetchFlairs());
            }
        });
    }
}
