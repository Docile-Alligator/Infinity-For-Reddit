package ml.docilealligator.infinityforreddit.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crazylegend.crashyreporter.CrashyReporter;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.adapters.CrashReportsRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CrashReportsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrashReportsFragment extends Fragment {

    private Activity activity;

    public CrashReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_crash_reports, container, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new CrashReportsRecyclerViewAdapter(CrashyReporter.INSTANCE.getLogsAsStrings()));

        return recyclerView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}