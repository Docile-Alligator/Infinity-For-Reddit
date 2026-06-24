package ml.docilealligator.infinityforreddit.adapters

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.activities.BaseActivity
import ml.docilealligator.infinityforreddit.adapters.CommentsStatusRecyclerViewAdapter.Constants.VIEW_TYPE_FIRST_LOADING
import ml.docilealligator.infinityforreddit.adapters.CommentsStatusRecyclerViewAdapter.Constants.VIEW_TYPE_FIRST_LOADING_FAILED
import ml.docilealligator.infinityforreddit.adapters.CommentsStatusRecyclerViewAdapter.Constants.VIEW_TYPE_NO_COMMENT_PLACEHOLDER
import ml.docilealligator.infinityforreddit.databinding.ItemLoadCommentsBinding
import ml.docilealligator.infinityforreddit.databinding.ItemLoadCommentsFailedPlaceholderBinding
import ml.docilealligator.infinityforreddit.databinding.ItemNoCommentPlaceholderBinding

class CommentsStatusRecyclerViewAdapter(
    private var activity: BaseActivity,
    private val onChangeToNormalThreadMode: () -> Unit,
    private val onRetryFetchingComments: () -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private object Constants {
        const val VIEW_TYPE_FIRST_LOADING: Int = 9
        const val VIEW_TYPE_FIRST_LOADING_FAILED: Int = 10
        const val VIEW_TYPE_NO_COMMENT_PLACEHOLDER: Int = 11
        const val VIEW_TYPE_VIEW_ALL_COMMENTS: Int = 17
    }

    var isSingleCommentThreadMode: Boolean = false
    var isInitialLoading: Boolean = false
    var isInitialLoadingFailed: Boolean = false
    var emptyComments: Boolean = false

    private val circularProgressBarBackgroundColor = activity.customThemeWrapper.circularProgressBarBackground
    private val colorAccent = activity.customThemeWrapper.colorAccent
    private val secondaryTextColor = activity.customThemeWrapper.secondaryTextColor
    private val commentBackgroundColor = activity.customThemeWrapper.commentBackgroundColor

    override fun getItemViewType(position: Int): Int {
        if (isInitialLoading) {
            return VIEW_TYPE_FIRST_LOADING
        } else if (isInitialLoadingFailed) {
            if (isSingleCommentThreadMode && position == 0) {
                return Constants.VIEW_TYPE_VIEW_ALL_COMMENTS
            }
            return VIEW_TYPE_FIRST_LOADING_FAILED
        } else {
            if (isSingleCommentThreadMode && position == 0) {
                return Constants.VIEW_TYPE_VIEW_ALL_COMMENTS
            }
            return VIEW_TYPE_NO_COMMENT_PLACEHOLDER
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_FIRST_LOADING ->
                return LoadCommentsViewHolder(
                    ItemLoadCommentsBinding.inflate(
                        LayoutInflater.from(
                            parent.getContext()
                        ), parent, false
                    ),
                    circularProgressBarBackgroundColor,
                    colorAccent
                )
            VIEW_TYPE_FIRST_LOADING_FAILED ->
                return LoadCommentsFailedViewHolder(
                    ItemLoadCommentsFailedPlaceholderBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                    ),
                    secondaryTextColor,
                    activity.typeface,
                    onRetryFetchingComments
                )
            VIEW_TYPE_NO_COMMENT_PLACEHOLDER ->
                return NoCommentViewHolder(
                    ItemNoCommentPlaceholderBinding.inflate(
                        LayoutInflater.from(
                            parent.getContext()
                        ), parent, false
                    ),
                    secondaryTextColor,
                    activity.typeface
                )
            else ->
                return ViewAllCommentsViewHolder(
                    LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_view_all_comments, parent, false),
                    commentBackgroundColor,
                    colorAccent,
                    activity.typeface,
                    onChangeToNormalThreadMode
                )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

    }

    override fun getItemCount(): Int {
        if (isInitialLoading) {
            return 1
        }

        if (isInitialLoadingFailed || emptyComments) {
            return if (isSingleCommentThreadMode) 2 else 1
        }

        return if (isSingleCommentThreadMode) 1 else 0
    }

    class LoadCommentsViewHolder internal constructor(
        binding: ItemLoadCommentsBinding,
        circularProgressBarBackgroundColor: Int,
        colorAccent: Int
    ) : RecyclerView.ViewHolder(binding.getRoot()) {
        var binding: ItemLoadCommentsBinding?

        init {
            this.binding = binding
            binding.commentProgressBarItemLoadComments.setBackgroundTintList(
                ColorStateList.valueOf(
                    circularProgressBarBackgroundColor
                )
            )
            binding.commentProgressBarItemLoadComments.setColorSchemeColors(colorAccent)
        }
    }

    class LoadCommentsFailedViewHolder internal constructor(
        binding: ItemLoadCommentsFailedPlaceholderBinding,
        secondaryTextColor: Int,
        typeface: Typeface?,
        onRetryFetchingComments: () -> Unit
    ) : RecyclerView.ViewHolder(binding.getRoot()) {
        var binding: ItemLoadCommentsFailedPlaceholderBinding?

        init {
            this.binding = binding
            itemView.setOnClickListener {
                onRetryFetchingComments()
            }
            typeface?.let {
                binding.errorTextViewItemLoadCommentsFailedPlaceholder.setTypeface(it)
            }
            binding.errorTextViewItemLoadCommentsFailedPlaceholder.setTextColor(secondaryTextColor)
        }
    }

    class NoCommentViewHolder internal constructor(
        binding: ItemNoCommentPlaceholderBinding,
        secondaryTextColor: Int,
        typeface: Typeface?
    ) : RecyclerView.ViewHolder(binding.getRoot()) {
        var binding: ItemNoCommentPlaceholderBinding?

        init {
            this.binding = binding
            typeface?.let {
                binding.errorTextViewItemNoCommentPlaceholder.setTypeface(it)
            }
            binding.errorTextViewItemNoCommentPlaceholder.setTextColor(secondaryTextColor)
        }
    }

    class ViewAllCommentsViewHolder internal constructor(
        itemView: View,
        commentBackgroundColor: Int,
        colorAccent: Int,
        typeface: Typeface?,
        onChangeToNormalThreadMode: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onChangeToNormalThreadMode()
                /*if (activity != null && activity is ViewPostDetailActivity) {
                    isSingleCommentThreadMode = false
                    mSingleCommentId = null
                    notifyItemRemoved(0)
                    mFragment.changeToNormalThreadMode()
                }*/
            }

            typeface?.let {
                (itemView as TextView).setTypeface(it)
            }
            itemView.setBackgroundTintList(ColorStateList.valueOf(commentBackgroundColor))
            (itemView as TextView).setTextColor(colorAccent)
        }
    }
}