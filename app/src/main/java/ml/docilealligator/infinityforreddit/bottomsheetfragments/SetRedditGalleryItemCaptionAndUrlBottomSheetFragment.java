package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
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

import java.lang.reflect.Field;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.PostGalleryActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSetRedditGalleryItemCaptionAndUrlBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SetRedditGalleryItemCaptionAndUrlBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_POSITION = "EP";
    public static final String EXTRA_CAPTION = "EC";
    public static final String EXTRA_URL = "EU";

    private PostGalleryActivity mActivity;

    public SetRedditGalleryItemCaptionAndUrlBottomSheetFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSetRedditGalleryItemCaptionAndUrlBottomSheetBinding binding = FragmentSetRedditGalleryItemCaptionAndUrlBottomSheetBinding.inflate(inflater, container, false);

        int primaryTextColor = mActivity.getResources().getColor(R.color.primaryTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.captionTextInputLayoutSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.setCursorColor(ColorStateList.valueOf(primaryTextColor));
            binding.urlTextInputLayoutSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.setCursorColor(ColorStateList.valueOf(primaryTextColor));
        } else {
            setCursorDrawableColor(binding.captionTextInputEditTextSetRedditGalleryItemCaptionAndUrlBottomSheetFragment, primaryTextColor);
            setCursorDrawableColor(binding.urlTextInputEditTextSetRedditGalleryItemCaptionAndUrlBottomSheetFragment, primaryTextColor);
        }

        int position = getArguments().getInt(EXTRA_POSITION, -1);
        String caption = getArguments().getString(EXTRA_CAPTION, "");
        String url = getArguments().getString(EXTRA_URL, "");

        binding.captionTextInputEditTextSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.setText(caption);
        binding.urlTextInputEditTextSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.setText(url);

        binding.okButtonSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.setOnClickListener(view -> {
            mActivity.setCaptionAndUrl(position, binding.captionTextInputEditTextSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.getText().toString(), binding.urlTextInputEditTextSetRedditGalleryItemCaptionAndUrlBottomSheetFragment.getText().toString());
            dismiss();
        });

        if (mActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), mActivity.typeface);
        }

        return binding.getRoot();
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
