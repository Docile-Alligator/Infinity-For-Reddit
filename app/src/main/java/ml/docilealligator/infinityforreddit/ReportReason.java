package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ReportReason implements Parcelable {
    public static final String REASON_TYPE_SITE_REASON = "site_reason";
    public static final String REASON_TYPE_RULE_REASON = "rule_reason";
    public static final String REASON_TYPE_OTHER_REASON = "other_reason";
    public static final String REASON_SITE_REASON_SELECTED = "site_reason_selected";
    public static final String REASON_RULE_REASON_SELECTED = "rule_reason_selected";
    public static final String REASON_OTHER = "other";

    private String reportReason;
    private String reasonType;
    private boolean isSelected;

    public ReportReason(String reportReason, String reasonType) {
        this.reportReason = reportReason;
        this.reasonType = reasonType;
        this.isSelected = false;
    }

    protected ReportReason(Parcel in) {
        reportReason = in.readString();
        reasonType = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<ReportReason> CREATOR = new Creator<ReportReason>() {
        @Override
        public ReportReason createFromParcel(Parcel in) {
            return new ReportReason(in);
        }

        @Override
        public ReportReason[] newArray(int size) {
            return new ReportReason[size];
        }
    };

    public String getReportReason() {
        return reportReason;
    }

    public String getReasonType() {
        return reasonType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(reportReason);
        parcel.writeString(reasonType);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }

    public static ArrayList<ReportReason> getGeneralReasons(Context context) {
        ArrayList<ReportReason> reportReasons = new ArrayList<>();
        reportReasons.add(new ReportReason(context.getString(R.string.report_reason_general_spam), REASON_TYPE_SITE_REASON));
        reportReasons.add(new ReportReason(context.getString(R.string.report_reason_general_copyright_issue), REASON_TYPE_SITE_REASON));
        reportReasons.add(new ReportReason(context.getString(R.string.report_reason_general_child_pornography), REASON_TYPE_SITE_REASON));
        reportReasons.add(new ReportReason(context.getString(R.string.report_reason_general_abusive_content), REASON_TYPE_SITE_REASON));
        return reportReasons;
    }

    public static ArrayList<ReportReason> convertRulesToReasons(ArrayList<Rule> rules) {
        ArrayList<ReportReason> reportReasons = new ArrayList<>();
        for (Rule r : rules) {
            reportReasons.add(new ReportReason(r.getShortName(), REASON_TYPE_RULE_REASON));
        }

        return reportReasons;
    }
}
