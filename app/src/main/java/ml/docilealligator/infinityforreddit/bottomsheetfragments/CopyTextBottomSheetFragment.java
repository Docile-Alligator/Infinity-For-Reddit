package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.app.Activity;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class CopyTextBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_RAW_TEXT = "ERT";
    public static final String EXTRA_MARKDOWN = "EM";

    @BindView(R.id.copy_raw_text_text_view_copy_text_bottom_sheet_fragment)
    TextView copyRawTextTextView;
    @BindView(R.id.copy_markdown_text_view_copy_text_bottom_sheet_fragment)
    TextView copyMarkdownTextView;
    @BindView(R.id.copy_all_raw_text_text_view_copy_text_bottom_sheet_fragment)
    TextView copyAllRawTextTextView;
    @BindView(R.id.copy_all_markdown_text_view_copy_text_bottom_sheet_fragment)
    TextView copyAllMarkdownTextView;

    private Activity activity;
    private String markdownText;

    public CopyTextBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_copy_text_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        String rawText = getArguments().getString(EXTRA_RAW_TEXT, null);
        markdownText = getArguments().getString(EXTRA_MARKDOWN, null);

        if (rawText != null) {
            copyRawTextTextView.setOnClickListener(view -> {
                showCopyDialog(rawText);
                dismiss();
            });

            copyAllRawTextTextView.setOnClickListener(view -> {
                copyText(rawText);
                dismiss();
            });
        } else {
            copyRawTextTextView.setVisibility(View.GONE);
            copyAllRawTextTextView.setVisibility(View.GONE);
        }

        if (markdownText != null) {
            markdownText = markdownText.replaceAll("<sup>", "^").replaceAll("</sup>", "");
            copyMarkdownTextView.setOnClickListener(view -> {
                showCopyDialog(markdownText);
                dismiss();
            });

            copyAllMarkdownTextView.setOnClickListener(view -> {
                copyText(markdownText);
                dismiss();
            });
        } else {
            copyMarkdownTextView.setVisibility(View.GONE);
            copyAllMarkdownTextView.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void showCopyDialog(String text) {
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
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("simple text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, R.string.copy_link_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
