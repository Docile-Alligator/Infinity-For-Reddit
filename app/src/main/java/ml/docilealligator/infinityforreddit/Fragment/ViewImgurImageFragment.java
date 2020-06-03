package ml.docilealligator.infinityforreddit.Fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ImgurMedia;
import ml.docilealligator.infinityforreddit.R;

public class ViewImgurImageFragment extends Fragment {

    public static final String EXTRA_IMGUR_IMAGES = "EII";

    @BindView(R.id.progress_bar_view_imgur_image_fragment)
    ProgressBar progressBar;
    @BindView(R.id.image_view_view_imgur_image_fragment)
    GestureImageView imageView;
    @BindView(R.id.load_image_error_linear_layout_view_imgur_image_fragment)
    LinearLayout errorLinearLayout;

    private Activity activity;
    private RequestManager glide;
    private ImgurMedia imgurMedia;

    public ViewImgurImageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_imgur_images, container, false);

        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        imgurMedia = getArguments().getParcelable(EXTRA_IMGUR_IMAGES);
        glide = Glide.with(activity);
        loadImage();

        return rootView;
    }

    private void loadImage() {
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
                return true;
        }

        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
