package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.utils.Utils;

public class CommentIndentationView extends LinearLayout {

    private final Paint paint;
    private int level;
    private int[] colors;
    private ArrayList<Path> paths;
    private final int spacing;
    private final int pathWidth;

    public CommentIndentationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathWidth = (int) Utils.convertDpToPixel(2, context);
        spacing = pathWidth * 6;
        paint.setStrokeWidth(pathWidth);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setPathEffect(new DashPathEffect(new float[] { pathWidth * 2, pathWidth * 2 }, 0));
        paths = new ArrayList<>();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        for (int i = 0; i < level; i++) {
            float startX = spacing * (i + 1) + pathWidth;
            Path path = new Path();
            path.moveTo(startX, 0);
            path.lineTo(startX, getHeight());
            paths.add(path);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < paths.size(); i++) {
            paint.setColor(colors[i % 7]);
            canvas.drawPath(paths.get(i), paint);
        }
    }

    public void setLevelAndColors(int level, int[] colors) {
        paths.clear();
        this.colors = colors;
        this.level = level;
        int indentationSpacing = (int) (level * spacing + pathWidth);
        setPaddingRelative(indentationSpacing, 0, pathWidth, 0);
    }
}
