package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentCopyTextBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CopyTextBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_RAW_TEXT = "ERT";
    public static final String EXTRA_MARKDOWN = "EM";

    private BaseActivity baseActivity;
    private ViewRedditGalleryActivity viewRedditGalleryActivity;
    private String markdownText;

    public CopyTextBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Convenience method for creating the dialog, creating and setting arguments bundle
     * and displaying the dialog
     */
    public static void show(@NonNull FragmentManager fragmentManager,
                            @Nullable String rawText, @Nullable String markdown) {
        Bundle bundle = new Bundle();
        bundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, rawText);
        bundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, markdown);
        CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
        copyTextBottomSheetFragment.setArguments(bundle);
        copyTextBottomSheetFragment.show(fragmentManager, null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCopyTextBottomSheetBinding binding = FragmentCopyTextBottomSheetBinding.inflate(inflater, container, false);

        String rawText = getArguments().getString(EXTRA_RAW_TEXT, null);
        markdownText = getArguments().getString(EXTRA_MARKDOWN, null);

        if (rawText != null) {
            binding.copyRawTextTextViewCopyTextBottomSheetFragment.setOnClickListener(view -> {
                showCopyDialog(rawText);
                dismiss();
            });

            binding.copyAllRawTextTextViewCopyTextBottomSheetFragment.setOnClickListener(view -> {
                copyText(rawText);
                dismiss();
            });
        } else {
            binding.copyRawTextTextViewCopyTextBottomSheetFragment.setVisibility(View.GONE);
            binding.copyAllRawTextTextViewCopyTextBottomSheetFragment.setVisibility(View.GONE);
        }

        if (markdownText != null) {
            binding.copyMarkdownTextViewCopyTextBottomSheetFragment.setOnClickListener(view -> {
                showCopyDialog(markdownText);
                dismiss();
            });

            binding.copyAllMarkdownTextViewCopyTextBottomSheetFragment.setOnClickListener(view -> {
                copyText(markdownText);
                dismiss();
            });
        } else {
            binding.copyMarkdownTextViewCopyTextBottomSheetFragment.setVisibility(View.GONE);
            binding.copyAllMarkdownTextViewCopyTextBottomSheetFragment.setVisibility(View.GONE);
        }

        if (baseActivity != null && baseActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), baseActivity.typeface);
        } else if (viewRedditGalleryActivity != null && viewRedditGalleryActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), viewRedditGalleryActivity.typeface);
        }

        return binding.getRoot();
    }

    private void showCopyDialog(String text) {
        AppCompatActivity activity = baseActivity == null ? viewRedditGalleryActivity : baseActivity;
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.copy_text_material_dialog, null);
        TextView textView = layout.findViewById(R.id.text_view_copy_text_material_dialog);
        textView.setText(text);
        new MaterialAlertDialogBuilder(activity, R.style.CopyTextMaterialAlertDialogTheme)
                .setTitle(R.string.copy_text)
                .setView(layout)
                .setPositiveButton(R.string.copy_all, (dialogInterface, i) -> copyText(text))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void copyText(String text) {
        AppCompatActivity activity = baseActivity == null ? viewRedditGalleryActivity : baseActivity;
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("simple text", text);
            clipboard.setPrimaryClip(clip);
            if (android.os.Build.VERSION.SDK_INT < 33) {
                Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, R.string.copy_link_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        } else {
            viewRedditGalleryActivity = (ViewRedditGalleryActivity) context;
        }
    }
}
