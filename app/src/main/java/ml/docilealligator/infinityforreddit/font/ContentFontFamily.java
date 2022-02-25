package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum ContentFontFamily {
    Default(R.style.ContentFontFamily, "Default"),
    BalsamiqSans(R.style.ContentFontFamily_BalsamiqSans, "BalsamiqSansBold"),
    BalsamiqSansBold(R.style.ContentFontFamily_BalsamiqSansBold, "BalsamiqSansBold"),
    NotoSans(R.style.ContentFontFamily_NotoSans, "NotoSans"),
    NotoSansBold(R.style.ContentFontFamily_NotoSansBold, "NotoSansBold"),
    RobotoCondensed(R.style.ContentFontFamily_RobotoCondensed, "RobotoCondensed"),
    RobotoCondensedBold(R.style.ContentFontFamily_RobotoCondensedBold, "RobotoCondensedBold"),
    HarmoniaSans(R.style.ContentFontFamily_HarmoniaSans, "HarmoniaSans"),
    HarmoniaSansBold(R.style.ContentFontFamily_HarmoniaSansBold, "HarmoniaSansBold"),
    Inter(R.style.ContentFontFamily_Inter, "Inter"),
    InterBold(R.style.ContentFontFamily_InterBold, "InterBold"),
    Manrope(R.style.ContentFontFamily_Manrope, "Manrope"),
    ManropeBold(R.style.ContentFontFamily_ManropeBold, "ManropeBold"),
    Sriracha(R.style.ContentFontFamily_Sriracha, "Sriracha"),
    AtkinsonHyperlegible(R.style.ContentFontFamily_AtkinsonHyperlegible, "AtkinsonHyperlegible"),
    AtkinsonHyperlegibleBold(R.style.ContentFontFamily_AtkinsonHyperlegibleBold, "AtkinsonHyperlegibleBold"),
    Custom(R.style.ContentFontFamily, "Custom");

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
