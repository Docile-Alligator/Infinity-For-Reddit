package ml.docilealligator.infinityforreddit.Font;

import ml.docilealligator.infinityforreddit.R;

public enum TitleFontFamily {
    Default(R.style.TitleFontFamily, "Default"),
    BalsamiqSans(R.style.TitleFontFamily_BalsamiqSans, "BalsamiqSans"),
    NotoSans(R.style.TitleFontFamily_NotoSans, "NotoSans"),
    RobotoCondensed(R.style.TitleFontFamily_RobotoCondensed, "RobotoCondensed"),
    HarmoniaSans(R.style.TitleFontFamily_HarmoniaSans, "HarmoniaSans"),
    Selawk(R.style.TitleFontFamily_Selawk, "Selawk"),
    Inter(R.style.TitleFontFamily_Inter, "Inter"),
    Manrope(R.style.TitleFontFamily_Manrope, "Manrope"),
    Rubik(R.style.TitleFontFamily_Rubik, "Rubik");

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
