package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import com.bumptech.glide.load.resource.gif.GifDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class SaveGIFToFile {

    public static void saveGifToFile(Executor executor, Handler handler, GifDrawable resource,
                                     String cacheDirPath, String fileName,
                                     SaveGIFToFileListener saveGIFToFileListener) {
        executor.execute(() -> {
            try {
                File imageFile = new File(cacheDirPath, fileName);
                ByteBuffer byteBuffer = resource.getBuffer();
                try (OutputStream outputStream = new FileOutputStream(imageFile)) {
                    byte[] bytes = new byte[byteBuffer.capacity()];
                    ((ByteBuffer) byteBuffer.duplicate().clear()).get(bytes);
                    outputStream.write(bytes, 0, bytes.length);
                }

                handler.post(() -> saveGIFToFileListener.saveSuccess(imageFile));
            } catch (IOException e) {
                handler.post(saveGIFToFileListener::saveFailed);
            }
        });
    }

    public interface SaveGIFToFileListener {
        void saveSuccess(File imageFile);
        void saveFailed();
    }
}
