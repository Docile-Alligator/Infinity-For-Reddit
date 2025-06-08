package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.metadata.ImageInfoExtractor;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.ImageViewFactory;

import java.io.File;

import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import pl.droidsonroids.gif.GifImageView;

public class GlideGifImageViewFactory extends ImageViewFactory {
    private SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInisdeDownsampleStrategy;

    public GlideGifImageViewFactory(SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInisdeDownsampleStrategy) {
        this.saveMemoryCenterInisdeDownsampleStrategy = saveMemoryCenterInisdeDownsampleStrategy;
    }

    @Override
    protected final View createAnimatedImageView(final Context context, final int imageType,
                                                 final int initScaleType) {
        switch (imageType) {
            case ImageInfoExtractor.TYPE_GIF:
            case ImageInfoExtractor.TYPE_ANIMATED_WEBP: {
                final GifImageView imageView = new GifImageView(context);
                imageView.setScaleType(BigImageView.scaleType(initScaleType));
                return imageView;
            }
            default:
                return super.createAnimatedImageView(context, imageType, initScaleType);
        }
    }

    @Override
    public final void loadAnimatedContent(final View view, final int imageType,
                                          final File imageFile) {
        switch (imageType) {
            case ImageInfoExtractor.TYPE_GIF:
            case ImageInfoExtractor.TYPE_ANIMATED_WEBP: {
                if (view instanceof GifImageView) {
                    Glide.with(view.getContext())
                            .load(imageFile)
                            .centerInside()
                            .downsample(saveMemoryCenterInisdeDownsampleStrategy)
                            .into((GifImageView) view);
                }
                break;
            }

            default:
                super.loadAnimatedContent(view, imageType, imageFile);
        }
    }

    @Override
    public void loadThumbnailContent(final View view, final Uri thumbnail) {
        if (view instanceof GifImageView) {
            Glide.with(view.getContext())
                    .load(thumbnail)
                    .into((GifImageView) view);
        }
    }
}
