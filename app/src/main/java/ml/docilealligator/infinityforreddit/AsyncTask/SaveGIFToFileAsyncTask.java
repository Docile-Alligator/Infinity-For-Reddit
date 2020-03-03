package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import com.bumptech.glide.load.resource.gif.GifDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SaveGIFToFileAsyncTask extends AsyncTask<Void, Void, Void> {
    private GifDrawable resource;
    private String cacheDirPath;
    private SaveGIFToFileAsyncTaskListener saveImageToFileAsyncTaskListener;
    private boolean saveSuccess = true;
    private File imageFile;

    public SaveGIFToFileAsyncTask(GifDrawable resource, String cacheDirPath,
                                    SaveGIFToFileAsyncTaskListener saveImageToFileAsyncTaskListener) {
        this.resource = resource;
        this.cacheDirPath = cacheDirPath;
        this.saveImageToFileAsyncTaskListener = saveImageToFileAsyncTaskListener;
    }

    public interface SaveGIFToFileAsyncTaskListener {
        void saveSuccess(File imageFile);
        void saveFailed();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            imageFile = new File(cacheDirPath, "shared.gif");
            ByteBuffer byteBuffer = resource.getBuffer();
            OutputStream outputStream = new FileOutputStream(imageFile);
            byte[] bytes = new byte[byteBuffer.capacity()];
            ((ByteBuffer) byteBuffer.duplicate().clear()).get(bytes);
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
        } catch (IOException e) {
            saveSuccess = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (saveSuccess) {
            saveImageToFileAsyncTaskListener.saveSuccess(imageFile);
        } else {
            saveImageToFileAsyncTaskListener.saveFailed();
        }
    }
}
