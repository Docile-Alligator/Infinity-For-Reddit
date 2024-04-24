package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
            customThemeViewModel.getOnlineCustomThemes().observe(getViewLifecycleOwner(),
                    customThemePagingData -> adapter.submitData(getViewLifecycleOwner().getLifecycle(), customThemePagingData));
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}