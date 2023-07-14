package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.PostGalleryActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SetRedditGalleryItemCaptionAndUrlBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_POSITION = "EP";
    public static final String EXTRA_CAPTION = "EC";
    public static final String EXTRA_URL = "EU";

    private PostGalleryActivity mActivity;
    private TextInputLayout captionTextInputLayout;
    private TextInputEditText captionTextInputEditText;
    private TextInputLayout urlTextInputLayout;
    private TextInputEditText urlTextInputEditText;
    private MaterialButton okButton;

    public SetRedditGalleryItemCaptionAndUrlBottomSheetFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_reddit_gallery_item_caption_and_url_bottom_sheet, container, false);

        captionTextInputLayout = rootView.findViewById(R.id.caption_text_input_layout_set_reddit_gallery_item_caption_and_url_bottom_sheet_fragment);
        captionTextInputEditText = rootView.findViewById(R.id.caption_text_input_edit_text_set_reddit_gallery_item_caption_and_url_bottom_sheet_fragment);
        urlTextInputLayout = rootView.findViewById(R.id.url_text_input_layout_set_reddit_gallery_item_caption_and_url_bottom_sheet_fragment);
        urlTextInputEditText = rootView.findViewById(R.id.url_text_input_edit_text_set_reddit_gallery_item_caption_and_url_bottom_sheet_fragment);
        okButton = rootView.findViewById(R.id.ok_button_set_reddit_gallery_item_caption_and_url_bottom_sheet_fragment);

        int primaryTextColor = mActivity.getResources().getColor(R.color.primaryTextColor);
        Drawable cursorDrawable = Utils.getTintedDrawable(mActivity, R.drawable.edit_text_cursor, primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            captionTextInputEditText.setTextCursorDrawable(cursorDrawable);
            urlTextInputEditText.setTextCursorDrawable(cursorDrawable);
        } else {
            setCursorDrawableColor(captionTextInputEditText, primaryTextColor);
            setCursorDrawableColor(urlTextInputEditText, primaryTextColor);
        }

        int position = getArguments().getInt(EXTRA_POSITION, -1);
        String caption = getArguments().getString(EXTRA_CAPTION, "");
        String url = getArguments().getString(EXTRA_URL, "");

        captionTextInputEditText.setText(caption);
        urlTextInputEditText.setText(url);

        okButton.setOnClickListener(view -> {
            mActivity.setCaptionAndUrl(position, captionTextInputEditText.getText().toString(), urlTextInputEditText.getText().toString());
            dismiss();
        });

        if (mActivity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, mActivity.typeface);
        }

        return rootView;
    }

    private void setCursorDrawableColor(EditText editText, int color) {
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[1] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (Throwable ignored) { }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (PostGalleryActivity) context;
    }
}
