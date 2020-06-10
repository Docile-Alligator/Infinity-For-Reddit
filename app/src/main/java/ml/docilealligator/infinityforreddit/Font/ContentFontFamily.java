package ml.docilealligator.infinityforreddit.Font;

import ml.docilealligator.infinityforreddit.R;

public enum ContentFontFamily {
    Default(R.style.ContentFontFamily, "Default"),
    BalsamiqSans(R.style.ContentFontFamily_BalsamiqSans, "BalsamiqSans"),
    NotoSans(R.style.ContentFontFamily_NotoSans, "NotoSans"),
    RobotoCondensed(R.style.ContentFontFamily_RobotoCondensed, "RobotoCondensed"),
    HarmoniaSans(R.style.ContentFontFamily_HarmoniaSans, "HarmoniaSans"),
    Selawk(R.style.ContentFontFamily_Selawk, "Selawk"),
    Inter(R.style.ContentFontFamily_Inter, "Inter");

    private int resId;
    private String title;

    ContentFontFamily(int resId, String title) {
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
