package ml.docilealligator.infinityforreddit.asynctasks;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;

public class SaveBitmapImageToFile {

    public static void SaveBitmapImageToFile(Executor executor, Handler handler, Bitmap resource, String cacheDirPath, String fileName,
                                             SaveBitmapImageToFileListener saveBitmapImageToFileListener) {
        executor.execute(() -> {
            try {
                File imageFile = new File(cacheDirPath, fileName);
                OutputStream outputStream = new FileOutputStream(imageFile);
                resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                handler.post(() -> saveBitmapImageToFileListener.saveSuccess(imageFile));
            } catch (IOException e) {
                handler.post(saveBitmapImageToFileListener::saveFailed);
            }
        });
    }

    public interface SaveBitmapImageToFileListener {
        void saveSuccess(File imageFile);
        void saveFailed();
    }
}
