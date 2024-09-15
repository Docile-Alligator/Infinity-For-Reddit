package ml.docilealligator.infinityforreddit.activities;

import ml.docilealligator.infinityforreddit.thing.UploadedImage;

public interface UploadImageEnabledActivity {
    void uploadImage();
    void captureImage();
    void insertImageUrl(UploadedImage uploadedImage);
}
