package ml.docilealligator.infinityforreddit.Font;

import ml.docilealligator.infinityforreddit.R;

public enum ContentFontFamily {
    Default(R.style.ContentFontFamily, "Default"),
    BalsamiqSans(R.style.ContentFontFamily_BalsamiqSans, "BalsamiqSans"),
    NotoSans(R.style.ContentFontFamily_NotoSans, "NotoSans"),
    RobotoCondensed(R.style.ContentFontFamily_RobotoCondensed, "RobotoCondensed"),
    HarmoniaSans(R.style.ContentFontFamily_HarmoniaSans, "HarmoniaSans"),
    Inter(R.style.ContentFontFamily_Inter, "Inter"),
    Manrope(R.style.ContentFontFamily_Manrope, "Manrope"),
    BadScript(R.style.ContentFontFamily_BadScript, "BadScript"),
    Sriracha(R.style.ContentFontFamily_Sriracha, "Sriracha");

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
