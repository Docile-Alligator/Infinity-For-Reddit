package ml.docilealligator.infinityforreddit.asynctasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SaveBitmapImageToFileAsyncTask extends AsyncTask<Void, Void, Void> {
    private Bitmap resource;
    private String cacheDirPath;
    private String fileName;
    private SaveBitmapImageToFileAsyncTaskListener saveBitmapImageToFileAsyncTaskListener;
    private boolean saveSuccess = true;
    private File imageFile;

    public SaveBitmapImageToFileAsyncTask(Bitmap resource, String cacheDirPath, String fileName,
                                          SaveBitmapImageToFileAsyncTaskListener saveBitmapImageToFileAsyncTaskListener) {
        this.resource = resource;
        this.cacheDirPath = cacheDirPath;
        this.fileName = fileName;
        this.saveBitmapImageToFileAsyncTaskListener = saveBitmapImageToFileAsyncTaskListener;
    }

    public interface SaveBitmapImageToFileAsyncTaskListener {
        void saveSuccess(File imageFile);
        void saveFailed();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            imageFile = new File(cacheDirPath, fileName);
            OutputStream outputStream = new FileOutputStream(imageFile);
            resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
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
            saveBitmapImageToFileAsyncTaskListener.saveSuccess(imageFile);
        } else {
            saveBitmapImageToFileAsyncTaskListener.saveFailed();
        }
    }
}
