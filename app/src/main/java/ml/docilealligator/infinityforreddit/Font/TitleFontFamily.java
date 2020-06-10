package ml.docilealligator.infinityforreddit.Font;

import ml.docilealligator.infinityforreddit.R;

public enum TitleFontFamily {
    Default(R.style.TitleFontFamily, "Default"),
    BalsamiqSans(R.style.TitleFontFamily_BalsamiqSans, "BalsamiqSans"),
    NotoSans(R.style.TitleFontFamily_NotoSans, "NotoSans");

    private int resId;
    private String title;

    TitleFontFamily(int resId, String title) {
        this.resId = resId;
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }
}
