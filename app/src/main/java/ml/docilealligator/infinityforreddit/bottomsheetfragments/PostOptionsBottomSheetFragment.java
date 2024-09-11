package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.activities.ReportActivity;
import ml.docilealligator.infinityforreddit.activities.SubmitCrosspostActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.post.Post;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostOptionsBottomSheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private static final String EXTRA_POST = "EP";

    private BaseActivity mBaseActivity;
    private Post mPost;
    private FragmentPostOptionsBottomSheetBinding binding;

    public PostOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param post Post
     * @return A new instance of fragment PostOptionsBottomSheetFragment.
     */
    public static PostOptionsBottomSheetFragment newInstance(Post post) {
        PostOptionsBottomSheetFragment fragment = new PostOptionsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPost = getArguments().getParcelable(EXTRA_POST);
        } else {
            dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostOptionsBottomSheetBinding.inflate(inflater, container, false);

        if (mPost != null) {
            switch (mPost.getPostType()) {
                case Post.IMAGE_TYPE:
                    break;
                case Post.GIF_TYPE:
                    break;
                case Post.VIDEO_TYPE:
                    break;
                case Post.GALLERY_TYPE:
                    break;
            }

            binding.addToPostFilterTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                Intent intent = new Intent(mBaseActivity, PostFilterPreferenceActivity.class);
                intent.putExtra(PostFilterPreferenceActivity.EXTRA_POST, mPost);
                startActivity(intent);

                dismiss();
            });

            if (mBaseActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                binding.commentTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
                binding.hidePostTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
                binding.crosspostTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
                binding.reportTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
            } else {
                binding.commentTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Intent intent = new Intent(mBaseActivity, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TITLE_KEY, mPost.getTitle());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, mPost.getSelfText());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, mPost.getSelfTextPlain());
                    intent.putExtra(CommentActivity.EXTRA_SUBREDDIT_NAME_KEY, mPost.getSubredditName());
                    intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                    mBaseActivity.startActivity(intent);

                    dismiss();
                });

                if (mPost.isHidden()) {
                    binding.hidePostTextViewPostOptionsBottomSheetFragment.setText(R.string.action_unhide_post);
                } else {
                    binding.hidePostTextViewPostOptionsBottomSheetFragment.setText(R.string.action_hide_post);
                }

                binding.hidePostTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    if (mBaseActivity instanceof PostOptionsCallback) {
                        ((PostOptionsCallback) mBaseActivity).onOptionClicked(mPost, mPost.isHidden() ? POST_OPTION.UNHIDE_POST : POST_OPTION.HIDE_POST);
                    }

                    dismiss();
                });

                binding.crosspostTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Intent submitCrosspostIntent = new Intent(mBaseActivity, SubmitCrosspostActivity.class);
                    submitCrosspostIntent.putExtra(SubmitCrosspostActivity.EXTRA_POST, mPost);
                    startActivity(submitCrosspostIntent);

                    dismiss();
                });

                binding.reportTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Intent intent = new Intent(mBaseActivity, ReportActivity.class);
                    intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
                    intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, mPost.getFullName());
                    startActivity(intent);
                });
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mBaseActivity = (BaseActivity) context;
    }

    public interface PostOptionsCallback {
        void onOptionClicked(Post post, @POST_OPTION int option);
    }

    @IntDef({POST_OPTION.HIDE_POST, POST_OPTION.UNHIDE_POST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface POST_OPTION {
        int HIDE_POST = 0;
        int UNHIDE_POST = 1;
        int DOWNLOAD_MEDIA = 2;
    }
}