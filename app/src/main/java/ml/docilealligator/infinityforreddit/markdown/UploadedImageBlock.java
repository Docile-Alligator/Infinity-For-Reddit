package ml.docilealligator.infinityforreddit.markdown;

import org.commonmark.node.CustomBlock;

import ml.docilealligator.infinityforreddit.UploadedImage;

public class UploadedImageBlock extends CustomBlock {
    public UploadedImage uploadeImage;

    public UploadedImageBlock(UploadedImage uploadeImage) {
        this.uploadeImage = uploadeImage;
    }
}
