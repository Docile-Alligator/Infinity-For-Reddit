package ml.docilealligator.infinityforreddit;

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;

public class SaveMemoryCenterInisdeDownsampleStrategy extends DownsampleStrategy {

    private int threshold;

    public SaveMemoryCenterInisdeDownsampleStrategy(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth, int requestedHeight) {
        int originalSourceWidth = sourceWidth;
        int originalSourceHeight = sourceHeight;
        if (sourceWidth * sourceHeight > threshold) {
            int divisor = 2;
            do {
                sourceWidth /= divisor;
                sourceHeight /= divisor;
            } while (sourceWidth * sourceHeight > threshold);
        }

        float widthPercentage = (float) requestedWidth / (float) sourceWidth;
        float heightPercentage = (float) requestedHeight / (float) sourceHeight;

        return Math.min((float) sourceWidth / (float) originalSourceWidth, (float) sourceHeight / (float) originalSourceHeight) * Math.min(1.f, Math.min(widthPercentage, heightPercentage));
    }

    @Override
    public SampleSizeRounding getSampleSizeRounding(int sourceWidth, int sourceHeight, int requestedWidth, int requestedHeight) {
        return SampleSizeRounding.MEMORY;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
