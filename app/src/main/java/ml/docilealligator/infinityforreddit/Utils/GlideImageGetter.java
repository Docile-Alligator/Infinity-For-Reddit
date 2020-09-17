package ml.docilealligator.infinityforreddit.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.lang.ref.WeakReference;

public class GlideImageGetter implements Html.ImageGetter {

    private WeakReference<TextView> container;
    private boolean matchParentWidth;
    private HtmlImagesHandler imagesHandler;
    private float density = 1.0f;
    private float textSize;

    public GlideImageGetter(TextView textView) {
        this(textView, false, false, null);
    }

    public GlideImageGetter(TextView textView, boolean matchParentWidth, HtmlImagesHandler imagesHandler) {
        this(textView, matchParentWidth, false, imagesHandler);
    }

    public GlideImageGetter(TextView textView, boolean matchParentWidth, boolean densityAware,
                            @Nullable HtmlImagesHandler imagesHandler) {
        this.container = new WeakReference<>(textView);
        this.matchParentWidth = matchParentWidth;
        this.imagesHandler = imagesHandler;
        if (densityAware) {
            density = container.get().getResources().getDisplayMetrics().density;
        }
        textSize = container.get().getTextSize();
    }

    @Override
    public Drawable getDrawable(String source) {
        if (imagesHandler != null) {
            imagesHandler.addImage(source);
        }

        BitmapDrawablePlaceholder drawable = new BitmapDrawablePlaceholder(textSize);

        Context context = container.get().getContext();
        if (!(context instanceof Activity && ((Activity) context).isDestroyed())) {
            container.get().post(() -> Glide.with(context)
                    .asBitmap()
                    .load(source)
                    .into(drawable));
        }

        return drawable;
    }

    private class BitmapDrawablePlaceholder extends BitmapDrawable implements Target<Bitmap> {

        protected Drawable drawable;

        BitmapDrawablePlaceholder(float textSize) {
            super(container.get().getResources(),
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888));
        }

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        private void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            int drawableWidth = (int) (drawable.getIntrinsicWidth() * density);
            int drawableHeight = (int) (drawable.getIntrinsicHeight() * density);
            float ratio = (float) drawableWidth / (float) drawableHeight;
            drawableHeight = (int) (textSize * 1.5);
            drawableWidth = (int) (drawableHeight * ratio);
            int maxWidth = container.get().getMeasuredWidth();
            if ((maxWidth > 0 && (drawableWidth > maxWidth)) || matchParentWidth) {
                int calculatedHeight = maxWidth * drawableHeight / drawableWidth;
                drawable.setBounds(0, 0, maxWidth, calculatedHeight);
                setBounds(0, 0, maxWidth, calculatedHeight);
            } else {
                drawable.setBounds(0, 0, drawableWidth, drawableHeight);
                setBounds(0, 0, drawableWidth, drawableHeight);
            }

            container.get().setText(container.get().getText());
        }

        @Override
        public void onLoadStarted(@Nullable Drawable placeholderDrawable) {
            if(placeholderDrawable != null) {
                setDrawable(placeholderDrawable);
            }
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            if (errorDrawable != null) {
                setDrawable(errorDrawable);
            }
        }

        @Override
        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            if (container != null) {
                setDrawable(new BitmapDrawable(container.get().getResources(), bitmap));
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholderDrawable) {
            if(placeholderDrawable != null) {
                setDrawable(placeholderDrawable);
            }
        }

        @Override
        public void getSize(@NonNull SizeReadyCallback cb) {
            cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        }

        @Override
        public void removeCallback(@NonNull SizeReadyCallback cb) {}

        @Override
        public void setRequest(@Nullable Request request) {}

        @Nullable
        @Override
        public Request getRequest() {
            return null;
        }

        @Override
        public void onStart() {}

        @Override
        public void onStop() {}

        @Override
        public void onDestroy() {}

    }

    public interface HtmlImagesHandler {
        void addImage(String uri);
    }
}
