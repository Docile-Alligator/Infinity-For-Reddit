package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import app.futured.hauler.DragDirection;
import app.futured.hauler.HaulerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.ImgurMedia;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;
import ml.docilealligator.infinityforreddit.WallpaperSetter;
import ml.docilealligator.infinityforreddit.apis.ImgurAPI;
import ml.docilealligator.infinityforreddit.customviews.ViewPagerBugFixed;
import ml.docilealligator.infinityforreddit.font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontFamily;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.fragments.ViewImgurImageFragment;
import ml.docilealligator.infinityforreddit.fragments.ViewImgurVideoFragment;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewImgurMediaActivity extends AppCompatActivity implements SetAsWallpaperCallback, CustomFontReceiver {

    public static final String EXTRA_IMGUR_TYPE = "EIT";
    public static final String EXTRA_IMGUR_ID = "EII";
    public static final int IMGUR_TYPE_GALLERY = 0;
    public static final int IMGUR_TYPE_ALBUM = 1;
    public static final int IMGUR_TYPE_IMAGE = 2;
    private static final String IMGUR_IMAGES_STATE = "IIS";

    @BindView(R.id.hauler_view_view_imgur_media_activity)
    HaulerView haulerView;
    @BindView(R.id.progress_bar_view_imgur_media_activity)
    ProgressBar progressBar;
    @BindView(R.id.view_pager_view_imgur_media_activity)
    ViewPagerBugFixed viewPager;
    @BindView(R.id.load_image_error_linear_layout_view_imgur_media_activity)
    LinearLayout errorLinearLayout;
    public Typeface typeface;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ArrayList<ImgurMedia> images;
    private boolean useBottomAppBar;
    @Inject
    @Named("imgur")
    Retrofit imgurRetrofit;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        getTheme().applyStyle(R.style.Theme_Normal, true);

        getTheme().applyStyle(FontStyle.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(FontFamily.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(sharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        setContentView(R.layout.activity_view_imgur_media);

        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        useBottomAppBar = sharedPreferences.getBoolean(SharedPreferencesUtils.USE_BOTTOM_TOOLBAR_IN_MEDIA_VIEWER, false);

        if (!useBottomAppBar) {
            ActionBar actionBar = getSupportActionBar();
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparentActionBarAndExoPlayerControllerColor)));
            setTitle(" ");
        } else {
            getSupportActionBar().hide();
        }

        String imgurId = getIntent().getStringExtra(EXTRA_IMGUR_ID);
        if (imgurId == null) {
            finish();
            return;
        }

        if (savedInstanceState != null) {
            images = savedInstanceState.getParcelableArrayList(IMGUR_IMAGES_STATE);
        }

        if (sharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_VERTICALLY_TO_GO_BACK_FROM_MEDIA, true)) {
            haulerView.setOnDragDismissedListener(dragDirection -> {
                int slide = dragDirection == DragDirection.UP ? R.anim.slide_out_up : R.anim.slide_out_down;
                finish();
                overridePendingTransition(0, slide);
            });
        } else {
            haulerView.setDragEnabled(false);
        }

        if (images == null) {
            fetchImgurMedia(imgurId);
        } else {
            progressBar.setVisibility(View.GONE);
            setupViewPager();
        }

        errorLinearLayout.setOnClickListener(view -> fetchImgurMedia(imgurId));
    }

    public boolean isUseBottomAppBar() {
        return useBottomAppBar;
    }

    private void fetchImgurMedia(String imgurId) {
        errorLinearLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        switch (getIntent().getIntExtra(EXTRA_IMGUR_TYPE, IMGUR_TYPE_IMAGE)) {
            case IMGUR_TYPE_GALLERY:
                imgurRetrofit.create(ImgurAPI.class).getGalleryImages(APIUtils.IMGUR_CLIENT_ID, imgurId)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.isSuccessful()) {
                                    new ParseImgurImagesAsyncTask(response.body(), new ParseImgurImagesAsyncTask.ParseImgurImagesAsyncTaskListener() {
                                        @Override
                                        public void success(ArrayList<ImgurMedia> images) {
                                            ViewImgurMediaActivity.this.images = images;
                                            progressBar.setVisibility(View.GONE);
                                            errorLinearLayout.setVisibility(View.GONE);
                                            setupViewPager();
                                        }

                                        @Override
                                        public void failed() {
                                            progressBar.setVisibility(View.GONE);
                                            errorLinearLayout.setVisibility(View.VISIBLE);
                                        }
                                    }).execute();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    errorLinearLayout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                errorLinearLayout.setVisibility(View.VISIBLE);
                            }
                        });
                break;
            case IMGUR_TYPE_ALBUM:
                imgurRetrofit.create(ImgurAPI.class).getAlbumImages(APIUtils.IMGUR_CLIENT_ID, imgurId)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.isSuccessful()) {
                                    new ParseImgurImagesAsyncTask(response.body(), new ParseImgurImagesAsyncTask.ParseImgurImagesAsyncTaskListener() {
                                        @Override
                                        public void success(ArrayList<ImgurMedia> images) {
                                            ViewImgurMediaActivity.this.images = images;
                                            progressBar.setVisibility(View.GONE);
                                            errorLinearLayout.setVisibility(View.GONE);
                                            setupViewPager();
                                        }

                                        @Override
                                        public void failed() {
                                            progressBar.setVisibility(View.GONE);
                                            errorLinearLayout.setVisibility(View.VISIBLE);
                                        }
                                    }).execute();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    errorLinearLayout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                errorLinearLayout.setVisibility(View.VISIBLE);
                            }
                        });
                break;
            case IMGUR_TYPE_IMAGE:
                imgurRetrofit.create(ImgurAPI.class).getImage(APIUtils.IMGUR_CLIENT_ID, imgurId)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.isSuccessful()) {
                                    new ParseImgurImageAsyncTask(response.body(), new ParseImgurImageAsyncTask.ParseImgurImageAsyncTaskListener() {
                                        @Override
                                        public void success(ImgurMedia image) {
                                            ViewImgurMediaActivity.this.images = new ArrayList<>();
                                            ViewImgurMediaActivity.this.images.add(image);
                                            progressBar.setVisibility(View.GONE);
                                            errorLinearLayout.setVisibility(View.GONE);
                                            setupViewPager();
                                        }

                                        @Override
                                        public void failed() {
                                            progressBar.setVisibility(View.GONE);
                                            errorLinearLayout.setVisibility(View.VISIBLE);
                                        }
                                    }).execute();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    errorLinearLayout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                errorLinearLayout.setVisibility(View.VISIBLE);
                            }
                        });
                break;
        }
    }

    private void setupViewPager() {
        if (!useBottomAppBar) {
            setToolbarTitle(0);
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    setToolbarTitle(position);
                }
            });
        }
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
    }

    private void setToolbarTitle(int position) {
        if (images != null && position >= 0 && position < images.size()) {
            if (images.get(position).getType() == ImgurMedia.TYPE_VIDEO) {
                setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.view_imgur_media_activity_video_label, position + 1, images.size()) + "</font>")));
            } else {
                setTitle(Utils.getTabTextWithCustomFont(typeface, Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.view_imgur_media_activity_image_label, position + 1, images.size()) + "</font>")));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(IMGUR_IMAGES_STATE, images);
    }

    @Override
    public void setToHomeScreen(int viewPagerPosition) {
        if (images != null && viewPagerPosition >= 0 && viewPagerPosition < images.size()) {
            WallpaperSetter.set(executor, new Handler(), images.get(viewPagerPosition).getLink(), WallpaperSetter.HOME_SCREEN, this,
                    new WallpaperSetter.SetWallpaperListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewImgurMediaActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewImgurMediaActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void setToLockScreen(int viewPagerPosition) {
        if (images != null && viewPagerPosition >= 0 && viewPagerPosition < images.size()) {
            WallpaperSetter.set(executor, new Handler(), images.get(viewPagerPosition).getLink(), WallpaperSetter.LOCK_SCREEN, this,
                    new WallpaperSetter.SetWallpaperListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewImgurMediaActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewImgurMediaActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void setToBoth(int viewPagerPosition) {
        if (images != null && viewPagerPosition >= 0 && viewPagerPosition < images.size()) {
            WallpaperSetter.set(executor, new Handler(), images.get(viewPagerPosition).getLink(), WallpaperSetter.BOTH_SCREENS, this,
                    new WallpaperSetter.SetWallpaperListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewImgurMediaActivity.this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewImgurMediaActivity.this, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public int getCurrentPagePosition() {
        return viewPager.getCurrentItem();
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            ImgurMedia imgurMedia = images.get(position);
            if (imgurMedia.getType() == ImgurMedia.TYPE_VIDEO) {
                ViewImgurVideoFragment fragment = new ViewImgurVideoFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(ViewImgurVideoFragment.EXTRA_IMGUR_VIDEO, imgurMedia);
                bundle.putInt(ViewImgurVideoFragment.EXTRA_INDEX, position);
                bundle.putInt(ViewImgurVideoFragment.EXTRA_MEDIA_COUNT, images.size());
                fragment.setArguments(bundle);
                return fragment;
            } else {
                ViewImgurImageFragment fragment = new ViewImgurImageFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(ViewImgurImageFragment.EXTRA_IMGUR_IMAGES, imgurMedia);
                bundle.putInt(ViewImgurImageFragment.EXTRA_INDEX, position);
                bundle.putInt(ViewImgurImageFragment.EXTRA_MEDIA_COUNT, images.size());
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }

    private static class ParseImgurImagesAsyncTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private ArrayList<ImgurMedia> images;
        private boolean parseFailed = false;
        private ParseImgurImagesAsyncTaskListener parseImgurImagesAsyncTaskListener;

        interface ParseImgurImagesAsyncTaskListener {
            void success(ArrayList<ImgurMedia> images);

            void failed();
        }

        ParseImgurImagesAsyncTask(String response, ParseImgurImagesAsyncTaskListener parseImgurImagesAsyncTaskListener) {
            this.response = response;
            this.parseImgurImagesAsyncTaskListener = parseImgurImagesAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray jsonArray = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.IMAGES_KEY);
                images = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject image = jsonArray.getJSONObject(i);
                    String type = image.getString(JSONUtils.TYPE_KEY);
                    if (type.contains("gif")) {
                        images.add(new ImgurMedia(image.getString(JSONUtils.ID_KEY),
                                image.getString(JSONUtils.TITLE_KEY), image.getString(JSONUtils.DESCRIPTION_KEY),
                                "video/mp4", image.getString(JSONUtils.MP4_KEY)));
                    } else {
                        images.add(new ImgurMedia(image.getString(JSONUtils.ID_KEY),
                                image.getString(JSONUtils.TITLE_KEY), image.getString(JSONUtils.DESCRIPTION_KEY),
                                type, image.getString(JSONUtils.LINK_KEY)));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (parseFailed) {
                parseImgurImagesAsyncTaskListener.failed();
            } else {
                parseImgurImagesAsyncTaskListener.success(images);
            }
        }
    }

    private static class ParseImgurImageAsyncTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private ImgurMedia image;
        private boolean parseFailed = false;
        private ParseImgurImageAsyncTaskListener parseImgurImageAsyncTaskListener;

        interface ParseImgurImageAsyncTaskListener {
            void success(ImgurMedia image);

            void failed();
        }

        ParseImgurImageAsyncTask(String response, ParseImgurImageAsyncTaskListener parseImgurImageAsyncTaskListener) {
            this.response = response;
            this.parseImgurImageAsyncTaskListener = parseImgurImageAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject image = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);
                String type = image.getString(JSONUtils.TYPE_KEY);
                if (type.contains("gif")) {
                    this.image = new ImgurMedia(image.getString(JSONUtils.ID_KEY),
                            image.getString(JSONUtils.TITLE_KEY), image.getString(JSONUtils.DESCRIPTION_KEY),
                            "video/mp4", image.getString(JSONUtils.MP4_KEY));
                } else {
                    this.image = new ImgurMedia(image.getString(JSONUtils.ID_KEY),
                            image.getString(JSONUtils.TITLE_KEY), image.getString(JSONUtils.DESCRIPTION_KEY),
                            type, image.getString(JSONUtils.LINK_KEY));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (parseFailed) {
                parseImgurImageAsyncTaskListener.failed();
            } else {
                parseImgurImageAsyncTaskListener.success(image);
            }
        }
    }
}
