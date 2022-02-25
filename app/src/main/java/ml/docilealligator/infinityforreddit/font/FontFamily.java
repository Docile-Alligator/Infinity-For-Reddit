package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum FontFamily {
    Default(R.style.FontFamily, "Default"),
    BalsamiqSans(R.style.FontFamily_BalsamiqSans, "BalsamiqSans"),
    BalsamiqSansBold(R.style.FontFamily_BalsamiqSansBold, "BalsamiqSansBold"),
    NotoSans(R.style.FontFamily_NotoSans, "NotoSans"),
    NotoSansBold(R.style.FontFamily_NotoSansBold, "NotoSansBold"),
    RobotoCondensed(R.style.FontFamily_RobotoCondensed, "RobotoCondensed"),
    RobotoCondensedBold(R.style.FontFamily_RobotoCondensedBold, "RobotoCondensedBold"),
    HarmoniaSans(R.style.FontFamily_HarmoniaSans, "HarmoniaSans"),
    HarmoniaSansBold(R.style.FontFamily_HarmoniaSansBold, "HarmoniaSansBold"),
    Inter(R.style.FontFamily_Inter, "Inter"),
    InterBold(R.style.FontFamily_InterBold, "InterBold"),
    Manrope(R.style.FontFamily_Manrope, "Manrope"),
    ManropeBold(R.style.FontFamily_ManropeBold, "ManropeBold"),
    Sriracha(R.style.FontFamily_Sriracha, "Sriracha"),
    AtkinsonHyperlegible(R.style.FontFamily_AtkinsonHyperlegible, "AtkinsonHyperlegible"),
    AtkinsonHyperlegibleBold(R.style.FontFamily_AtkinsonHyperlegibleBold, "AtkinsonHyperlegibleBold"),
    Custom(R.style.FontFamily, "Custom");

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
