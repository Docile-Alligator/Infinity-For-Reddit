package ml.docilealligator.infinityforreddit.Font;

import ml.docilealligator.infinityforreddit.R;

public enum FontFamily {
    Default(R.style.FontFamily, "Default"),
    BalsamiqSans(R.style.FontFamily_BalsamiqSans, "BalsamiqSans"),
    NotoSans(R.style.FontFamily_NotoSans, "NotoSans"),
    RobotoCondensed(R.style.FontFamily_RobotoCondensed, "RobotoCondensed"),
    HarmoniaSans(R.style.FontFamily_HarmoniaSans, "HarmoniaSans"),
    Selawk(R.style.FontFamily_Selawk, "Selawk"),
    Inter(R.style.FontFamily_Inter, "Inter");

    private int resId;
    private String title;

    FontFamily(int resId, String title) {
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
