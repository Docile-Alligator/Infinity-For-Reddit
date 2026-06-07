package ml.docilealligator.infinityforreddit.markdown.uploadedimage;

import org.commonmark.node.CustomBlock;

import ml.docilealligator.infinityforreddit.thing.UploadedImage;

public class UploadedImageBlock extends CustomBlock {
    public UploadedImage uploadeImage;

    public UploadedImageBlock(UploadedImage uploadeImage) {
        this.uploadeImage = uploadeImage;
    }
}
