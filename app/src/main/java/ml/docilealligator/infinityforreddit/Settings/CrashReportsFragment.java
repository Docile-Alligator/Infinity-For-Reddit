package ml.docilealligator.infinityforreddit.Settings;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;

public class CrashReportsFragment extends Fragment {

    @BindView(R.id.recycler_view_crash_reports_fragment)
    RecyclerView recyclerView;
    private Activity activity;
    private CrashReportsRecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    public CrashReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_crash_reports, container, false);

        ButterKnife.bind(this, rootView);

        adapter = new CrashReportsRecyclerViewAdapter(CrashyReporter.INSTANCE.getLogsAsStrings());
        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }
}