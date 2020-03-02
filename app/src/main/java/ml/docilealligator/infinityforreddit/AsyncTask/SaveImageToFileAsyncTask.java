package ml.docilealligator.infinityforreddit.AsyncTask;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SaveImageToFileAsyncTask extends AsyncTask<Void, Void, Void> {
    private Bitmap resource;
    private String cacheDirPath;
    private SaveImageToFileAsyncTaskListener saveImageToFileAsyncTaskListener;
    private boolean saveSuccess = true;
    private File imageFile;

    public SaveImageToFileAsyncTask(Bitmap resource, String cacheDirPath,
                                    SaveImageToFileAsyncTaskListener saveImageToFileAsyncTaskListener) {
        this.resource = resource;
        this.cacheDirPath = cacheDirPath;
        this.saveImageToFileAsyncTaskListener = saveImageToFileAsyncTaskListener;
    }

    public interface SaveImageToFileAsyncTaskListener {
        void saveSuccess(File imageFile);
        void saveFailed();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            imageFile = new File(cacheDirPath, "shared.jpg");
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
            saveImageToFileAsyncTaskListener.saveSuccess(imageFile);
        } else {
            saveImageToFileAsyncTaskListener.saveFailed();
        }
    }
}
