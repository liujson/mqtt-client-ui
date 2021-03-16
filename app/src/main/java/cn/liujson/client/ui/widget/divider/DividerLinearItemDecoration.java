package cn.liujson.client.ui.widget.divider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



/**
 * @author liujson
 * @date 2020/11/14.
 */
public class DividerLinearItemDecoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    // 线性列表 方向
    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    private Drawable mDivider;
    private int mOrientation;
    private Paint mPaint;
    private int mDividerSize = 2;

    private int paddingStart = 0;
    private int paddingEnd = 0;

    /**
     * 默认样式分割线
     * 宽度为2 颜色为灰色
     *
     * @param context
     * @param orientation
     */
    public DividerLinearItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation     列表方向
     * @param dividerDrawable 分割线图片
     */
    public DividerLinearItemDecoration(Context context, int orientation, Drawable dividerDrawable) {
        this(context, orientation);
        mDivider = dividerDrawable;
        mDividerSize = mDivider.getIntrinsicHeight();
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation  列表方向
     * @param dividerSize  分割线高度
     * @param dividerColor 分割线颜色
     */
    public DividerLinearItemDecoration(Context context, int orientation, int dividerSize, @ColorRes int dividerColor) {
        this(context, orientation);
        mDividerSize = dividerSize;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(context.getResources().getColor(dividerColor));
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    public void setPaddingStart(int paddingStart) {
        this.paddingStart = Math.max(paddingStart, 0);
    }

    public void setPaddingEnd(int paddingEnd) {
        this.paddingEnd = Math.max(paddingEnd, 0);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    // 绘制垂直排列的分割线
    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView v = new RecyclerView(parent.getContext());
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDividerSize;
            if (mPaint != null) {
                c.drawRect(left + paddingStart, top, right - paddingEnd, bottom, mPaint);
                continue;
            }
            if (mDivider != null) {
                mDivider.setBounds(left + paddingStart, top, right - paddingEnd, bottom);
                mDivider.draw(c);
            }
        }
    }

    // 绘制水平排列的分割线
    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDividerSize;
            if (mPaint != null) {
                c.drawRect(left, top + paddingStart, right, bottom + paddingStart, mPaint);
                continue;
            }
            if (mDivider != null) {
                mDivider.setBounds(left, top + paddingStart, right, bottom + paddingStart);
                mDivider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDividerSize);
        } else {
            outRect.set(0, 0, mDividerSize, 0);
        }
    }
}
