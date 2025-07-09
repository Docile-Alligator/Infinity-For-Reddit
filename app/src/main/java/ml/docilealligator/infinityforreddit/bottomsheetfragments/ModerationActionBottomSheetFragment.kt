package ml.docilealligator.infinityforreddit.bottomsheetfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ml.docilealligator.infinityforreddit.PostModerationActionHandler
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment
import ml.docilealligator.infinityforreddit.databinding.FragmentModerationActionBottomSheetBinding
import ml.docilealligator.infinityforreddit.post.Post


private const val EXTRA_POST = "EP"

/**
 * A simple [Fragment] subclass.
 * Use the [ModerationActionBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ModerationActionBottomSheetFragment : LandscapeExpandedRoundedBottomSheetDialogFragment() {
    private var post: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            post = it.getParcelable(EXTRA_POST)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val binding: FragmentModerationActionBottomSheetBinding = FragmentModerationActionBottomSheetBinding.inflate(inflater, container, false)
        if (parentFragment is PostModerationActionHandler) {
            binding.approveTextViewModerationActionBottomSheetFragment.setOnClickListener {
                (parentFragment as PostModerationActionHandler).approvePost(post)
                dismiss()
            }
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(post: Post) =
            ModerationActionBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_POST, post)
                }
            }
    }
}