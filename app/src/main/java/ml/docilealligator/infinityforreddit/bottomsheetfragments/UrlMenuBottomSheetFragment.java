package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentUrlMenuBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class UrlMenuBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_URL = "EU";

    private Activity activity;
    private String url;

    public UrlMenuBottomSheetFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static UrlMenuBottomSheetFragment newInstance(String url) {
        UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url);
        urlMenuBottomSheetFragment.setArguments(bundle);
        return urlMenuBottomSheetFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentUrlMenuBottomSheetBinding binding = FragmentUrlMenuBottomSheetBinding.inflate(inflater, container, false);

        url = getArguments().getString(EXTRA_URL);

        Uri uri = Uri.parse(url);
        if (uri.getScheme() == null && uri.getHost() == null) {
            url = "https://www.reddit.com" + url;
        }

        binding.linkTextViewUrlMenuBottomSheetFragment.setText(url);

        binding.openLinkTextViewUrlMenuBottomSheetFragment.setOnClickListener(view -> {
            Intent intent = new Intent(activity, LinkResolverActivity.class);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
            dismiss();
        });

        binding.copyLinkTextViewUrlMenuBottomSheetFragment.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("simple text", url);
                clipboard.setPrimaryClip(clip);
                if (android.os.Build.VERSION.SDK_INT < 33) {
                    Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, R.string.copy_link_failed, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        binding.shareLinkTextViewUrlMenuBottomSheetFragment.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, url);
            try {
                Intent shareIntent = Intent.createChooser(intent, null);
                startActivity(shareIntent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.no_app, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        if (activity instanceof BaseActivity) {
            if (((BaseActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((BaseActivity) activity).typeface);
            }
        } else if (activity instanceof ViewRedditGalleryActivity) {
            if (((ViewRedditGalleryActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewRedditGalleryActivity) activity).typeface);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}