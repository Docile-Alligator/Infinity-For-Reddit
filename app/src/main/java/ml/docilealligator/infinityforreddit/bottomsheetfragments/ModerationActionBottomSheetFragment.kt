package ml.docilealligator.infinityforreddit.bottomsheetfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import ml.docilealligator.infinityforreddit.PostModerationActionHandler
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment
import ml.docilealligator.infinityforreddit.databinding.FragmentModerationActionBottomSheetBinding
import ml.docilealligator.infinityforreddit.post.Post
import org.checkerframework.checker.units.qual.A


private const val EXTRA_POST = "EP"
private const val EXTRA_POSITION = "EPO"

/**
 * A simple [Fragment] subclass.
 * Use the [ModerationActionBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ModerationActionBottomSheetFragment : LandscapeExpandedRoundedBottomSheetDialogFragment() {
    private var post: Post? = null
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            post = it.getParcelable(EXTRA_POST)
            position = it.getInt(EXTRA_POSITION, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentModerationActionBottomSheetBinding = FragmentModerationActionBottomSheetBinding.inflate(inflater, container, false)
        post?.let { post ->
            if (parentFragment is PostModerationActionHandler) {
                binding.approveTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).approvePost(post, position)
                    dismiss()
                }

                binding.removeTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).removePost(post, position, false)
                    dismiss()
                }

                binding.spamTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).removePost(post, position, true)
                    dismiss()
                }

                activity?.let {
                    binding.toggleStickyTextViewModerationActionBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(it, if (post.isStickied) R.drawable.ic_unstick_post_24dp else R.drawable.ic_stick_post_24dp), null, null, null
                    )
                    binding.toggleLockTextViewModerationActionBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(it, if (post.isLocked) R.drawable.ic_unlock_24dp else R.drawable.ic_lock_day_night_24dp), null, null, null
                    )
                    binding.toggleNsfwTextViewModerationActionBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(it, if (post.isNSFW) R.drawable.ic_unmark_nsfw_24dp else R.drawable.ic_mark_nsfw_24dp), null, null, null
                    )
                    binding.toggleSpoilerTextViewModerationActionBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(it, if (post.isLocked) R.drawable.ic_unmark_spoiler_24dp else R.drawable.ic_spoiler_24dp), null, null, null
                    )
                }

                binding.toggleStickyTextViewModerationActionBottomSheetFragment.setText(if (post.isStickied) R.string.unset_sticky_post else R.string.set_sticky_post)
                binding.toggleStickyTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).toggleSticky(post, position)
                    dismiss()
                }

                binding.toggleLockTextViewModerationActionBottomSheetFragment.setText(if (post.isLocked) R.string.unlock else R.string.lock)
                binding.toggleLockTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).toggleLock(post, position)
                    dismiss()
                }

                binding.toggleNsfwTextViewModerationActionBottomSheetFragment.setText(if (post.isNSFW) R.string.action_unmark_nsfw else R.string.action_mark_nsfw)
                binding.toggleNsfwTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).toggleNSFW(post, position)
                    dismiss()
                }

                binding.toggleSpoilerTextViewModerationActionBottomSheetFragment.setText(if (post.isSpoiler) R.string.action_unmark_spoiler else R.string.action_mark_spoiler)
                binding.toggleSpoilerTextViewModerationActionBottomSheetFragment.setOnClickListener {
                    (parentFragment as PostModerationActionHandler).toggleSpoiler(post, position)
                    dismiss()
                }
            }
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(post: Post, position: Int) =
            ModerationActionBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_POST, post)
                    putInt(EXTRA_POSITION, position)
                }
            }
    }
}