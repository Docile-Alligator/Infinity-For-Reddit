package Settings;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AcknowledgementFragment extends Fragment {


  @BindView(R.id.recycler_view_acknowledgement_fragment)
  RecyclerView recyclerView;

  public AcknowledgementFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_acknowledgement, container, false);
    ButterKnife.bind(this, rootView);

    Activity activity = getActivity();

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
    acknowledgements.add(new Acknowledgement("Swipe",
        "Detects swipe events on Android with listener and RxJava Observable",
        Uri.parse("https://github.com/pwittchen/swipe")));
    acknowledgements.add(new Acknowledgement("RxAndroid",
        "Android specific bindings for RxJava 2",
        Uri.parse("https://github.com/ReactiveX/RxAndroid")));
    acknowledgements.add(new Acknowledgement("RxJava",
        "Reactive extensions for the JVM",
        Uri.parse("https://github.com/ReactiveX/RxJava")));
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

    AcknowledgementRecyclerViewAdapter adapter = new AcknowledgementRecyclerViewAdapter(activity,
        acknowledgements);
    recyclerView.setLayoutManager(new LinearLayoutManager(activity));
    recyclerView.setAdapter(adapter);

    return rootView;
  }
}
