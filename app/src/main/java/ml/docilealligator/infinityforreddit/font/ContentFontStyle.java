package ml.docilealligator.infinityforreddit.font;

import ml.docilealligator.infinityforreddit.R;

public enum ContentFontStyle {
    XSmall(R.style.ContentFontStyle_XSmall, "XSmall"),
    Small(R.style.ContentFontStyle_Small, "Small"),
    Normal(R.style.ContentFontStyle_Normal, "Normal"),
    Large(R.style.ContentFontStyle_Large, "Large"),
    XLarge(R.style.ContentFontStyle_XLarge, "XLarge"),
    XXLarge(R.style.ContentFontStyle_XXLarge, "XXLarge");

    private int resId;
    private String title;

    ContentFontStyle(int resId, String title) {
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
