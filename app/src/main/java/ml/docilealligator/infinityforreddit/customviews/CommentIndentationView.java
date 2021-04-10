package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.utils.Utils;

public class CommentIndentationView extends LinearLayout {

    private final Paint paint;
    private int level;
    private Integer[] colors;
    private ArrayList<Integer> startXs;
    private final int spacing;
    private final int pathWidth;

    public CommentIndentationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        setWillNotDraw(false);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathWidth = (int) Utils.convertDpToPixel(2, context);
        spacing = pathWidth * 6;
        paint.setStrokeWidth(pathWidth);
        paint.setStyle(Paint.Style.STROKE);
        startXs = new ArrayList<>();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        startXs.clear();
        for (int i = 0; i < level; i++) {
            startXs.add(spacing * (i + 1) + pathWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < startXs.size(); i++) {
            paint.setColor(colors[i % 7]);
            canvas.drawLine(startXs.get(i), 0, startXs.get(i), getHeight(), paint);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState myState = new SavedState(parcelable);
        myState.startXs = this.startXs;
        myState.colors = this.colors;
        return parcelable;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;

        super.onRestoreInstanceState(savedState.getSuperState());

        this.startXs = savedState.startXs;
        this.colors = savedState.colors;

        invalidate();
    }

    public void setLevelAndColors(int level, Integer[] colors) {
        this.colors = colors;
        this.level = level;
        int indentationSpacing = (level * spacing + pathWidth);
        setPaddingRelative(indentationSpacing, 0, pathWidth, 0);
        invalidate();
    }

    private static class SavedState extends BaseSavedState {
        ArrayList<Integer> startXs;
        Integer[] colors;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            startXs = in.readArrayList(SavedState.class.getClassLoader());
            colors = (Integer[]) in.readArray(SavedState.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(startXs);
            out.writeArray(colors);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
