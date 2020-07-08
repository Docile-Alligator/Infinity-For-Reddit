package ml.docilealligator.infinityforreddit.Settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;

class CrashReportsRecyclerViewAdapter extends RecyclerView.Adapter<CrashReportsRecyclerViewAdapter.CrashReportViewHolder> {
    private List<String> crashReports;

    public CrashReportsRecyclerViewAdapter(List<String> crashReports) {
        this.crashReports = crashReports;
    }

    @NonNull
    @Override
    public CrashReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CrashReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crash_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CrashReportViewHolder holder, int position) {
        holder.crashReportTextView.setText(crashReports.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return crashReports == null ? 0 : crashReports.size();
    }

    class CrashReportViewHolder extends RecyclerView.ViewHolder {
        TextView crashReportTextView;

        public CrashReportViewHolder(@NonNull View itemView) {
            super(itemView);
            crashReportTextView = (TextView) itemView;
        }
    }
}
