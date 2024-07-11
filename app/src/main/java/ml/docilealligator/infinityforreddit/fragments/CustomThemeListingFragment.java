package ml.docilealligator.infinityforreddit.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.adapters.CustomThemeListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.OnlineCustomThemeListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeViewModel;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.FragmentCustomThemeListingBinding;
import retrofit2.Retrofit;

public class CustomThemeListingFragment extends Fragment {

    public static final String EXTRA_IS_ONLINE = "EIO";

    @Inject
    @Named("online_custom_themes")
    Retrofit onlineCustomThemesRetrofit;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    @Named("light_theme")
    SharedPreferences lightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences darkThemeSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences amoledThemeSharedPreferences;
    @Inject
    Executor executor;
    public CustomThemeViewModel customThemeViewModel;
    private BaseActivity activity;
    private FragmentCustomThemeListingBinding binding;
    private boolean isOnline;
    @Nullable
    private ActivityResultLauncher<Intent> customizeThemeActivityResultLauncher;

    public CustomThemeListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        // Inflate the layout for this fragment
        binding = FragmentCustomThemeListingBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            isOnline = getArguments().getBoolean(EXTRA_IS_ONLINE);
        }

        binding.recyclerViewCustomizeThemeListingActivity.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    ((RecyclerViewContentScrollingInterface) activity).contentScrollDown();
                } else if (dy < 0) {
                    ((RecyclerViewContentScrollingInterface) activity).contentScrollUp();
                }
            }
        });

        if (isOnline) {
            OnlineCustomThemeListingRecyclerViewAdapter adapter = new OnlineCustomThemeListingRecyclerViewAdapter(activity);
            binding.recyclerViewCustomizeThemeListingActivity.setAdapter(adapter);

            customThemeViewModel = new ViewModelProvider(this,
                    new CustomThemeViewModel.Factory(executor, onlineCustomThemesRetrofit, redditDataRoomDatabase))
                    .get(CustomThemeViewModel.class);
            customThemeViewModel.getOnlineCustomThemeMetadata().observe(getViewLifecycleOwner(),
                    customThemePagingData -> adapter.submitData(getViewLifecycleOwner().getLifecycle(), customThemePagingData));

            customizeThemeActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
                if (activityResult.getResultCode() == Activity.RESULT_OK) {
                    Intent data = activityResult.getData();
                    int index = data.getIntExtra(CustomizeThemeActivity.RETURN_EXTRA_INDEX_IN_THEME_LIST, -1);
                    String themeName = data.getStringExtra(CustomizeThemeActivity.RETURN_EXTRA_THEME_NAME);
                    String primaryColorHex = data.getStringExtra(CustomizeThemeActivity.RETURN_EXTRA_PRIMARY_COLOR);

                    adapter.updateMetadata(index, themeName, primaryColorHex);
                }
            });
        } else {
            CustomThemeListingRecyclerViewAdapter adapter = new CustomThemeListingRecyclerViewAdapter(activity,
                    CustomThemeWrapper.getPredefinedThemes(activity));
            binding.recyclerViewCustomizeThemeListingActivity.setAdapter(adapter);

            customThemeViewModel = new ViewModelProvider(this,
                    new CustomThemeViewModel.Factory(redditDataRoomDatabase))
                    .get(CustomThemeViewModel.class);
            customThemeViewModel.getAllCustomThemes().observe(getViewLifecycleOwner(), adapter::setUserThemes);
        }

        return binding.getRoot();
    }

    @Nullable
    public ActivityResultLauncher<Intent> getCustomizeThemeActivityResultLauncher() {
        return customizeThemeActivityResultLauncher;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}