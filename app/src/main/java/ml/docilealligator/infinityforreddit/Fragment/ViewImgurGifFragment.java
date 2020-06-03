package ml.docilealligator.infinityforreddit.Fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.AsyncTask.SaveGIFToFileAsyncTask;
import ml.docilealligator.infinityforreddit.BuildConfig;
import ml.docilealligator.infinityforreddit.ImgurMedia;
import ml.docilealligator.infinityforreddit.R;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewImgurGifFragment extends Fragment {

    public static final String EXTRA_IMGUR_GIF = "EIG";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @BindView(R.id.progress_bar_view_imgur_gif_fragment)
    ProgressBar progressBar;
    @BindView(R.id.image_view_view_imgur_gif_fragment)
    GifImageView imageView;
    @BindView(R.id.load_image_error_linear_layout_view_imgur_gif_fragment)
    LinearLayout errorLinearLayout;

    private Activity activity;
    private RequestManager glide;
    private ImgurMedia imgurMedia;
    private boolean isActionBarHidden = false;
    private boolean isDownloading = false;

    public ViewImgurGifFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_imgur_gif, container, false);

        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        imgurMedia = getArguments().getParcelable(EXTRA_IMGUR_GIF);
        glide = Glide.with(activity);
        loadGif();

        return rootView;
    }

    private void loadGif() {
        glide.load(imgurMedia.getLink()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                errorLinearLayout.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(imageView);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.view_imgur_image_fragments, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download_view_imgur_image_fragments:
                return true;
            case R.id.action_share_view_imgur_image_fragments:
                Toast.makeText(activity, R.string.save_gif_before_sharing, Toast.LENGTH_SHORT).show();
                glide.asGif().load(imgurMedia.getLink()).listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (activity.getExternalCacheDir() != null) {
                            new SaveGIFToFileAsyncTask(resource, activity.getExternalCacheDir().getPath(),
                                    imgurMedia.getFileName(),
                                    new SaveGIFToFileAsyncTask.SaveGIFToFileAsyncTaskListener() {
                                        @Override
                                        public void saveSuccess(File imageFile) {
                                            Uri uri = FileProvider.getUriForFile(activity,
                                                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
                                            Intent shareIntent = new Intent();
                                            shareIntent.setAction(Intent.ACTION_SEND);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                            shareIntent.setType("image/*");
                                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                                        }

                                        @Override
                                        public void saveFailed() {
                                            Toast.makeText(activity,
                                                    R.string.cannot_save_gif, Toast.LENGTH_SHORT).show();
                                        }
                                    }).execute();
                        } else {
                            Toast.makeText(activity,
                                    R.string.cannot_get_storage, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }).submit();
                return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(activity, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && isDownloading) {
                download();
            }
            isDownloading = false;
        }
    }

    private void download() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgurMedia.getLink()));
        request.setTitle(imgurMedia.getFileName());

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Android Q support
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, imgurMedia.getFileName());
        } else {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File directory = new File(path + "/Infinity/");
            boolean saveToInfinityFolder = true;
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    saveToInfinityFolder = false;
                }
            } else {
                if (directory.isFile()) {
                    if (!(directory.delete() && directory.mkdir())) {
                        saveToInfinityFolder = false;
                    }
                }
            }

            if (saveToInfinityFolder) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/Infinity/", imgurMedia.getFileName());
            } else {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, imgurMedia.getFileName());
            }
        }

        DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

        if (manager == null) {
            Toast.makeText(activity, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        manager.enqueue(request);
        Toast.makeText(activity, R.string.download_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
