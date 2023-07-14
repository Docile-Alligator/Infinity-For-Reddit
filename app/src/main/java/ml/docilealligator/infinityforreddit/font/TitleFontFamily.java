package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum TitleFontFamily {
    Default(R.style.TitleFontFamily, "Default"),
    BalsamiqSans(R.style.TitleFontFamily_BalsamiqSans, "BalsamiqSans"),
    BalsamiqSansBold(R.style.TitleFontFamily_BalsamiqSansBold, "BalsamiqSansBold"),
    NotoSans(R.style.TitleFontFamily_NotoSans, "NotoSans"),
    NotoSansBold(R.style.TitleFontFamily_NotoSansBold, "NotoSansBold"),
    RobotoCondensed(R.style.TitleFontFamily_RobotoCondensed, "RobotoCondensed"),
    RobotoCondensedBold(R.style.TitleFontFamily_RobotoCondensedBold, "RobotoCondensedBold"),
    HarmoniaSans(R.style.TitleFontFamily_HarmoniaSans, "HarmoniaSans"),
    HarmoniaSansBold(R.style.TitleFontFamily_HarmoniaSansBold, "HarmoniaSansBold"),
    Inter(R.style.TitleFontFamily_Inter, "Inter"),
    InterBold(R.style.TitleFontFamily_InterBold, "InterBold"),
    Manrope(R.style.TitleFontFamily_Manrope, "Manrope"),
    ManropeBold(R.style.TitleFontFamily_ManropeBold, "ManropeBold"),
    Sriracha(R.style.TitleFontFamily_Sriracha, "Sriracha"),
    AtkinsonHyperlegible(R.style.TitleFontFamily_AtkinsonHyperlegible, "AtkinsonHyperlegible"),
    AtkinsonHyperlegibleBold(R.style.TitleFontFamily_AtkinsonHyperlegibleBold, "AtkinsonHyperlegibleBold"),
    Custom(R.style.TitleFontFamily, "Custom");

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
