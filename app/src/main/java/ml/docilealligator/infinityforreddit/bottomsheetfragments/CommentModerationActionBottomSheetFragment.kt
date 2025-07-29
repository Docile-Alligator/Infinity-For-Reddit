package ml.docilealligator.infinityforreddit.bottomsheetfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import ml.docilealligator.infinityforreddit.CommentModerationActionHandler
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment
import ml.docilealligator.infinityforreddit.databinding.FragmentCommentModerationActionBottomSheetBinding

private const val EXTRA_COMMENT = "EP"
private const val EXTRA_POSITION = "EPO"

/**
 * A simple [Fragment] subclass.
 * Use the [CommentModerationActionBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommentModerationActionBottomSheetFragment : LandscapeExpandedRoundedBottomSheetDialogFragment() {
    private var comment: Comment? = null
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            comment = it.getParcelable(EXTRA_COMMENT)
            position = it.getInt(EXTRA_POSITION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentCommentModerationActionBottomSheetBinding.inflate(
            inflater,
            container,
            false
        )

        comment?.let { comment ->
            if (comment.isApproved) {
                binding.approveTextViewCommentModerationActionBottomSheetFragment.visibility = View.GONE
            } else {
                binding.approveTextViewCommentModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as CommentModerationActionHandler).approveComment(comment, position)
                    dismiss()
                }
            }

            if (comment.isRemoved) {
                binding.removeTextViewCommentModerationActionBottomSheetFragment.visibility = View.GONE
                binding.spamTextViewCommentModerationActionBottomSheetFragment.visibility = View.GONE
            } else {
                binding.removeTextViewCommentModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as CommentModerationActionHandler).removeComment(comment, position, false)
                    dismiss()
                }

                binding.spamTextViewCommentModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as CommentModerationActionHandler).removeComment(comment, position, true)
                    dismiss()
                }
            }

            activity?.let {
                binding.toggleLockTextViewCommentModerationActionBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(it, if (comment.isLocked) R.drawable.ic_unlock_24dp else R.drawable.ic_lock_day_night_24dp), null, null, null
                )
            }

            binding.toggleLockTextViewCommentModerationActionBottomSheetFragment.setText(if (comment.isLocked) R.string.unlock else R.string.lock)
            binding.toggleLockTextViewCommentModerationActionBottomSheetFragment.setOnClickListener {
                (parentFragment as CommentModerationActionHandler).toggleLock(comment, position)
                dismiss()
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(comment: Comment, position: Int) =
            CommentModerationActionBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_COMMENT, comment)
                    putInt(EXTRA_POSITION, position)
                }
            }
    }
}