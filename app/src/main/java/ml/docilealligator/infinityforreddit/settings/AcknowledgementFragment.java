package ml.docilealligator.infinityforreddit.settings;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.AcknowledgementRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;

/**
 * A simple {@link Fragment} subclass.
 */
public class AcknowledgementFragment extends Fragment {

    @BindView(R.id.recycler_view_acknowledgement_fragment)
    RecyclerView recyclerView;
    private BaseActivity activity;

    public AcknowledgementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_acknowledgement, container, false);
        ButterKnife.bind(this, rootView);

        ArrayList<Acknowledgement> acknowledgements = new ArrayList<>();
        acknowledgements.add(new Acknowledgement("ExoPlayer",
                "An application level media player for Android",
                Uri.parse("https://github.com/google/ExoPlayer")));
        acknowledgements.add(new Acknowledgement("GestureViews",
                "ImageView and FrameLayout with gestures control and position animation",
                Uri.parse("https://github.com/alexvasilkov/GestureViews")));
        acknowledgements.add(new Acknowledgement("Glide",
                "A fast and efficient open source media management and image loading framework for Android",
                Uri.parse("https://github.com/bumptech/glide")));
        acknowledgements.add(new Acknowledgement("Retrofit",
                "Type-safe HTTP client for Android and Java by Square, Inc.",
                Uri.parse("https://github.com/square/retrofit")));
        acknowledgements.add(new Acknowledgement("Dagger",
                "A fast dependency injector for Java and Android.",
                Uri.parse("https://github.com/google/dagger")));
        acknowledgements.add(new Acknowledgement("Butter Knife",
                "Field and method binding for Android views",
                Uri.parse("https://github.com/JakeWharton/butterknife")));
        acknowledgements.add(new Acknowledgement("Aspect Ratio ImageView",
                "A simple imageview which scales the width or height aspect with the given ratio",
                Uri.parse("https://github.com/santalu/aspect-ratio-imageview")));
        acknowledgements.add(new Acknowledgement("MaterialLoadingProgressBar",
                "A styled ProgressBar",
                Uri.parse("https://github.com/lsjwzh/MaterialLoadingProgressBar")));
        acknowledgements.add(new Acknowledgement("Markwon",
                "A markdown library for Android",
                Uri.parse("https://github.com/noties/Markwon")));
        acknowledgements.add(new Acknowledgement("android-gif-drawable",
                "Views and Drawable for animated GIFs in Android.",
                Uri.parse("https://github.com/koral--/android-gif-drawable")));
        acknowledgements.add(new Acknowledgement("SimpleSearchView",
                "A simple SearchView for Android based on Material Design",
                Uri.parse("https://github.com/Ferfalk/SimpleSearchView")));
        acknowledgements.add(new Acknowledgement("EventBus",
                "A publish/subscribe event bus for Android and Java",
                Uri.parse("https://github.com/greenrobot/EventBus")));
        acknowledgements.add(new Acknowledgement("Customized and Expandable TextView",
                "Simple library to change the Textview as rectangle, circle and square shapes",
                Uri.parse("https://github.com/Rajagopalr3/CustomizedTextView")));
        acknowledgements.add(new Acknowledgement("Rounded Bottom Sheet",
                "Bottom sheet with rounded corners",
                Uri.parse("https://github.com/Deishelon/RoundedBottomSheet")));
        acknowledgements.add(new Acknowledgement("Bridge",
                "A library for avoiding TransactionTooLargeException during state saving and restoration",
                Uri.parse("https://github.com/livefront/bridge")));
        acknowledgements.add(new Acknowledgement("Android-State",
                "A utility library for Android to save objects in a Bundle without any boilerplate",
                Uri.parse("https://github.com/evernote/android-state")));
        acknowledgements.add(new Acknowledgement("FlowLayout",
                "A FlowLayout for Android, which allows child views flow to next row when there is no enough space.",
                Uri.parse("https://github.com/nex3z/FlowLayout")));
        acknowledgements.add(new Acknowledgement("Gson",
                "Gson is a Java library that can be used to convert Java Objects into their JSON representation.",
                Uri.parse("https://github.com/google/gson")));
        acknowledgements.add(new Acknowledgement("Hauler",
                "Hauler is an Android library containing custom layout which enables to easily create swipe to dismiss Activity.",
                Uri.parse("https://github.com/futuredapp/hauler")));
        acknowledgements.add(new Acknowledgement("Slidr",
                "Easily add slide to dismiss functionality to an Activity",
                Uri.parse("https://github.com/r0adkll/Slidr")));
        acknowledgements.add(new Acknowledgement("commonmark-java",
                "Java library for parsing and rendering Markdown text according to the CommonMark specification (and some extensions).",
                Uri.parse("https://github.com/atlassian/commonmark-java")));
        acknowledgements.add(new Acknowledgement("AndroidFastScroll",
                "Fast scroll for Android RecyclerView and more.",
                Uri.parse("https://github.com/zhanghai/AndroidFastScroll")));
        acknowledgements.add(new Acknowledgement("Subsampling Scale Image View",
                "A custom image view for Android, designed for photo galleries and displaying " +
                        "huge images (e.g. maps and building plans) without OutOfMemoryErrors.",
                Uri.parse("https://github.com/davemorrissey/subsampling-scale-image-view")));
        acknowledgements.add(new Acknowledgement("BigImageViewer",
                "Big image viewer supporting pan and zoom, with very little memory " +
                        "usage and full featured image loading choices. Powered by Subsampling Scale " +
                        "Image View, Fresco, Glide, and Picasso. Even with gif and webp support!",
                Uri.parse("https://github.com/Piasy/BigImageViewer")));
        acknowledgements.add(new Acknowledgement("BetterLinkMovementMethod",
                "Attempts to improve how clickable links are detected, highlighted and handled in TextView.",
                Uri.parse("https://github.com/saket/Better-Link-Movement-Method")));
        acknowledgements.add(new Acknowledgement("ZoomLayout",
                "2D zoom and pan behavior for View hierarchies, images, video streams, and much more, written in Kotlin for Android.",
                Uri.parse("https://github.com/natario1/ZoomLayout")));

        AcknowledgementRecyclerViewAdapter adapter = new AcknowledgementRecyclerViewAdapter(activity, acknowledgements);
        recyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(activity));
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
