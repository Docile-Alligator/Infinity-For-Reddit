package ml.docilealligator.infinityforreddit;

public enum ContentFontStyle {
    Small(R.style.ContentFontStyle_Small, "Small"),
    Normal(R.style.ContentFontStyle_Normal, "Normal"),
    Large(R.style.ContentFontStyle_Large, "Large"),
    XLarge(R.style.ContentFontStyle_XLarge, "XLarge"),
    XXLarge(R.style.ContentFontStyle_XXLarge, "XXLarge");

    private int resId;
    private String title;

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }

    ContentFontStyle(int resId, String title) {
        this.resId = resId;
        this.title = title;
    }
}
