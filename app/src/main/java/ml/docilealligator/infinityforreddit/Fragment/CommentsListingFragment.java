package ml.docilealligator.infinityforreddit.Fragment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import ml.docilealligator.infinityforreddit.Adapter.CommentsListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CommentViewModel;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_USERNAME = "EN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ARE_SAVED_COMMENTS = "EISC";

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";

    @BindView(R.id.coordinator_layout_comments_listing_fragment)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_comments_listing_fragment)
    RecyclerView mCommentRecyclerView;
    @BindView(R.id.progress_bar_comments_listing_fragment)
    CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_comments_info_linear_layout_comments_listing_fragment)
    LinearLayout mFetchCommentInfoLinearLayout;
    @BindView(R.id.fetch_comments_info_image_view_comments_listing_fragment)
    ImageView mFetchCommentInfoImageView;
    @BindView(R.id.fetch_comments_info_text_view_comments_listing_fragment)
    TextView mFetchCommentInfoTextView;
    CommentViewModel mCommentViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private RequestManager mGlide;
    private Activity activity;
    private CommentsListingRecyclerViewAdapter mAdapter;

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

        if (savedInstanceState == null) {
            getCurrentAccountAndBindView(resources);
        } else {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView(resources);
            } else {
                bindView(resources);
            }
        }

        return rootView;
    }

    private void getCurrentAccountAndBindView(Resources resources) {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
            bindView(resources);
        }).execute();
    }

    private void bindView(Resources resources) {
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        boolean voteButtonsOnTheRight = mSharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mAdapter = new CommentsListingRecyclerViewAdapter(activity, mOauthRetrofit,
                getArguments().getString(EXTRA_ACCESS_TOKEN), getArguments().getString(EXTRA_ACCOUNT_NAME),
                voteButtonsOnTheRight, () -> mCommentViewModel.retryLoadingMore());

        String username = getArguments().getString(EXTRA_USERNAME);
        String sort = mSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_USER_COMMENT, SortType.Type.NEW.value);
        SortType sortType;
        if(sort.equals(SortType.Type.CONTROVERSIAL.value) || sort.equals(SortType.Type.TOP.value)) {
            String sortTime = mSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_USER_COMMENT, SortType.Time.ALL.value);
            sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()), SortType.Time.valueOf(sortTime.toUpperCase()));
        } else {
            sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));
        }

        mCommentRecyclerView.setAdapter(mAdapter);

        CommentViewModel.Factory factory;

        if (mAccessToken == null) {
            factory = new CommentViewModel.Factory(mRetrofit,
                    resources.getConfiguration().locale, null, username, sortType,
                    getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS));
        } else {
            factory = new CommentViewModel.Factory(mOauthRetrofit,
                    resources.getConfiguration().locale, mAccessToken, username, sortType,
                    getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS));
        }

        mCommentViewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);
        mCommentViewModel.getComments().observe(this, comments -> mAdapter.submitList(comments));

        mCommentViewModel.hasComment().observe(this, hasComment -> {
            mProgressBar.setVisibility(View.GONE);
            if (hasComment) {
                mFetchCommentInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchCommentInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_comments);
            }
        });

        mCommentViewModel.getInitialLoadingState().observe(this, networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mProgressBar.setVisibility(View.GONE);
                mFetchCommentInfoLinearLayout.setOnClickListener(view -> refresh());
                showErrorView(R.string.load_comments_failed);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mCommentViewModel.getPaginationNetworkState().observe(this, networkState -> mAdapter.setNetworkState(networkState));
    }

    public void changeSortType(SortType sortType) {
        mCommentViewModel.changeSortType(sortType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
    }

    @Override
    public void refresh() {
        mFetchCommentInfoLinearLayout.setVisibility(View.GONE);
        mCommentViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    private void showErrorView(int stringResId) {
        if (activity != null && isAdded()) {
            mProgressBar.setVisibility(View.GONE);
            mFetchCommentInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchCommentInfoTextView.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(mFetchCommentInfoImageView);
        }
    }
}
