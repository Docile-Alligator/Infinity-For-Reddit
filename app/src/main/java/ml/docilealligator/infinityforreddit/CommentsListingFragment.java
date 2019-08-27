package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsListingFragment extends Fragment implements FragmentCommunicator {

    static final String EXTRA_USERNAME_KEY = "ENK";
    static final String EXTRA_ACCESS_TOKEN = "EAT";
    static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.coordinator_layout_comments_listing_fragment) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_comments_listing_fragment) RecyclerView mCommentRecyclerView;
    @BindView(R.id.progress_bar_comments_listing_fragment) CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_comments_info_linear_layout_comments_listing_fragment) LinearLayout mFetchCommentInfoLinearLayout;
    @BindView(R.id.fetch_comments_info_image_view_comments_listing_fragment) ImageView mFetchCommentInfoImageView;
    @BindView(R.id.fetch_comments_info_text_view_comments_listing_fragment) TextView mFetchCommentInfoTextView;

    private RequestManager mGlide;

    private Activity activity;

    private CommentsListingRecyclerViewAdapter mAdapter;

    CommentViewModel mCommentViewModel;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    public CommentsListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments_listing, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        mGlide = Glide.with(activity);

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    mCommentRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
                }
            }
        }

        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        mAdapter = new CommentsListingRecyclerViewAdapter(activity, mOauthRetrofit,
                getArguments().getString(EXTRA_ACCESS_TOKEN), getArguments().getString(EXTRA_ACCOUNT_NAME),
                () -> mCommentViewModel.retryLoadingMore());

        String username = getArguments().getString(EXTRA_USERNAME_KEY);

        mCommentRecyclerView.setAdapter(mAdapter);

        CommentViewModel.Factory factory = new CommentViewModel.Factory(mRetrofit,
                resources.getConfiguration().locale, username, PostDataSource.SORT_TYPE_NEW);
        mCommentViewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);
        mCommentViewModel.getComments().observe(this, comments -> mAdapter.submitList(comments));

        mCommentViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchCommentInfoLinearLayout.setOnClickListener(view -> mCommentViewModel.retry());
                showErrorView(R.string.load_comments_failed);
            } else {
                mFetchCommentInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mCommentViewModel.hasComment().observe(this, hasComment -> {
            if(hasComment) {
                mFetchCommentInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchCommentInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_comments);
            }
        });

        mCommentViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        return rootView;
    }

    void changeSortType(String sortType) {
        mCommentViewModel.changeSortType(sortType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void refresh() {
        mCommentViewModel.refresh();
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        if(activity != null && isAdded()) {
            mFetchCommentInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchCommentInfoTextView.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(mFetchCommentInfoImageView);
        }
    }
}
