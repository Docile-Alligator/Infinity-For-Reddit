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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class UrlMenuBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_URL = "EU";
    @BindView(R.id.link_text_view_url_menu_bottom_sheet_fragment)
    TextView linkTextView;
    @BindView(R.id.open_link_text_view_url_menu_bottom_sheet_fragment)
    TextView openLinkTextView;
    @BindView(R.id.copy_link_text_view_url_menu_bottom_sheet_fragment)
    TextView copyLinkTextView;
    @BindView(R.id.share_link_text_view_url_menu_bottom_sheet_fragment)
    TextView shareLinkTextView;
    private Activity activity;
    private String url;

    public UrlMenuBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_url_menu_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        url = getArguments().getString(EXTRA_URL);

        Uri uri = Uri.parse(url);
        if (uri.getScheme() == null && uri.getHost() == null) {
            url = "https://www.reddit.com" + url;
        }

        linkTextView.setText(url);

        openLinkTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, LinkResolverActivity.class);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
            dismiss();
        });

        copyLinkTextView.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("simple text", url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.copy_link_failed, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        shareLinkTextView.setOnClickListener(view -> {
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
                Utils.setFontToAllTextViews(rootView, ((BaseActivity) activity).typeface);
            }
        } else if (activity instanceof ViewRedditGalleryActivity) {
            if (((ViewRedditGalleryActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((ViewRedditGalleryActivity) activity).typeface);
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}