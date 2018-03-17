package com.shbj.stepviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class StepView extends View {
    private Paint mLinePaint;
    private int mStepCount;
    private float mPreLineLength;
    private int[] mNormalBitmapWH;
    private int[] mPassedBitmapWH;
    private int[] mTargetBitmapWH;
    private Bitmap mNormalBitmap;
    private Bitmap mPassedBitmap;
    private Bitmap mTargetBitmap;
    private int mCurrentStep;
    private int mNormalLineColor;
    private int mPassedLineColor;
    private Paint mTextPaint;
    private RectF[] mStepRectFs;
    private boolean mStepIsTouch;
    private int mNormalTextColor;
    private int mTargetTextColor;
    private String[] mStepTexts;
    private int mTextLineMargin;
    private boolean mTextUpLine;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mPreLineLength = 0;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StepView);
        //获取xml文件中的线的颜色值、size
        mNormalLineColor = typedArray.getColor(R.styleable.StepView_normal_line_color, Color.BLUE);
        mPassedLineColor = typedArray.getColor(R.styleable.StepView_passed_line_color, Color.WHITE);
        int lineSize = (int) typedArray.getDimension(R.styleable.StepView_line_size, 2);
        //获取xml文件中的文本的颜色值、size
        mNormalTextColor = typedArray.getColor(R.styleable.StepView_normal_text_color, Color.BLACK);
        mTargetTextColor = typedArray.getColor(R.styleable.StepView_target_text_color, Color.BLACK);
        int textSize = (int) typedArray.getDimension(R.styleable.StepView_text_size, 10);
        //获取xml文件中的step的size，设置给step图片的高度
        int stepSize = (int) typedArray.getDimension(R.styleable.StepView_step_size, 0);
        //获取xml文件中的文本和线之间的间距
        mTextLineMargin = (int) typedArray.getDimension(R.styleable.StepView_text_line_margin, 3);
        //获取xml文件中的step总数
        mStepCount = typedArray.getInt(R.styleable.StepView_step_count, 0);
        //默认的step图片
        mNormalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal);
        mPassedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_passed);
        mTargetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_target);

        //获取xml文件中的当前step位置
        mCurrentStep = typedArray.getInt(R.styleable.StepView_current_step, 0);
        //获取xml文件中step图片
        BitmapDrawable normalDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.StepView_normal_step_iv);
        BitmapDrawable passedDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.StepView_passed_step_iv);
        BitmapDrawable targetDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.StepView_target_step_iv);
        //获取xml文件中step是否可点击TRUE可以，FALSE不可以，默认为FALSE
        mStepIsTouch = typedArray.getBoolean(R.styleable.StepView_step_is_touch, false);
        //获取xml文件中text是否在线上，TRUE在线上，FALSE不在线上，默认为FALSE
        mTextUpLine = typedArray.getBoolean(R.styleable.StepView_text_up_line, true);
        mTextPaint.setTextSize(textSize);
        mLinePaint.setStrokeWidth(lineSize);
        mNormalBitmap = normalDrawable.getBitmap();//将xml文件中指定的图片赋给对应的bitmap
        mPassedBitmap = passedDrawable.getBitmap();
        mTargetBitmap = targetDrawable.getBitmap();
        mNormalBitmapWH = getBitmapWH(stepSize, mNormalBitmap);
        mPassedBitmapWH = getBitmapWH(stepSize, mPassedBitmap);
        mTargetBitmapWH = getBitmapWH(stepSize, mTargetBitmap);
        if (stepSize != 0) {//如果stepSize不为0，要对其进行压缩处理，使其高度等于stepSize
            mNormalBitmap = zoomImg(mNormalBitmap, mNormalBitmapWH);
            mPassedBitmap = zoomImg(mPassedBitmap, mPassedBitmapWH);
            mTargetBitmap = zoomImg(mTargetBitmap, mPassedBitmapWH);
        }
        mStepRectFs = new RectF[mStepCount];//初始化step所对应的矩阵数组，点击step时会用到，用于确定点击的是哪个step
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = widthSize - getPaddingLeft() - getPaddingRight();//任何模式下with都是父容器给定的with-padding值
        int height = 0;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize - getPaddingTop() - getPaddingBottom();
        } else {
            height = dp2px(getContext(), 80);
        }
        setMeasuredDimension(width, height);
        mPreLineLength = width / (mStepCount + 1);//计算每条线的长度，由于线比step多一个所以加1
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mStepCount != 0) {
            drawLine(canvas);//drawLine和drawStep分两次循环是为了防止部分线覆盖step
            drawStep(canvas);
        }
    }

    private void drawLine(Canvas canvas) {
        float lineStartX = getPaddingLeft();
        float lineStartY = getLineStartY();
        float lineStopX = 0;
        float lineStopY = lineStartY;
        for (int i = 0; i < mStepCount + 1; i++) {
            if (i < mCurrentStep - 1) {
                mLinePaint.setColor(mPassedLineColor);
            } else if (i == mCurrentStep - 1) {
                mLinePaint.setColor(mPassedLineColor);
            } else {
                mLinePaint.setColor(mNormalLineColor);
            }
            lineStopX = lineStartX + mPreLineLength;
            canvas.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, mLinePaint);
            lineStartX = lineStopX;
        }
    }

    private float getLineStartY() {
        //如果没有文本，lineStartY为paddingTop+step的bitmap高度的一半由于所有bitmap的高度都一样所以只需获取NormalBitmap的高度就可以
        float lineStartY = getPaddingTop() + mNormalBitmapWH[1] / 2;
        if (mStepTexts != null) {
            if (mTextUpLine) {
                int th = getTextWH(mStepTexts[0])[1];//文本的高度
                //如果有文本，lineStartY还需要再加上文本的高度和文本与线之间的间距
                lineStartY = lineStartY + th + mTextLineMargin;
            }
        }
        return lineStartY;
    }

    private void drawStep(Canvas canvas) {
        float lineStartX = getPaddingLeft();
        float lineStartY = getLineStartY();
        Bitmap currentBitmap;
        int[] currentBitmapWH;
        float lineStopX;
        float bitmapLeft;
        float bitmapTop;
        for (int i = 0; i < mStepCount; i++) {
            if (i < mCurrentStep - 1) {
                currentBitmap = mPassedBitmap;
                currentBitmapWH = mPassedBitmapWH;
                mTextPaint.setColor(mNormalTextColor);
            } else if (i == mCurrentStep - 1) {
                currentBitmap = mTargetBitmap;
                currentBitmapWH = mTargetBitmapWH;
                mTextPaint.setColor(mTargetTextColor);
            } else {
                currentBitmap = mNormalBitmap;
                currentBitmapWH = mNormalBitmapWH;
                mTextPaint.setColor(mNormalTextColor);
            }
            lineStopX = lineStartX + mPreLineLength;
            bitmapLeft = lineStopX - currentBitmapWH[0] / 2;
            bitmapTop = lineStartY - currentBitmapWH[1] / 2;
            canvas.drawBitmap(currentBitmap, bitmapLeft, bitmapTop, null);
            mStepRectFs[i] = new RectF(bitmapLeft, bitmapTop, bitmapLeft + currentBitmapWH[0], bitmapTop + currentBitmapWH[1]);
            if (mStepTexts != null) {//当没有传入对应的texts时不需要划线
                drawText(canvas, i, bitmapLeft + currentBitmapWH[1] / 2, bitmapTop, currentBitmapWH[1]);
            }
            lineStartX = lineStopX;
        }
    }

    private void drawText(Canvas canvas, int i, float x, float y, float bitmapH) {
        String text = mStepTexts[i];
        int[] textWH = getTextWH(text);
        int textWidth = textWH[0];
        int textHeight = textWH[1];
        float bottom = 0;
        if (mTextUpLine) {//画文本时的基准点是left.bottom,使其中心点与step的中心点对其
            bottom = y - mTextLineMargin;
        } else {
            bottom = y + bitmapH + mTextLineMargin + textHeight;
        }
        canvas.drawText(text, x - textWidth / 2, bottom, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mStepIsTouch) {//不能点击返回FALSE不处理
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                int touchStep = getTouchStep(new PointF(x, y));//获取被点击的点的位置
                if (touchStep != -1) {
                    mCurrentStep = touchStep + 1;
                    invalidate();
                }
                break;
        }
        return true;
    }

    //设置当前step
    public void setCurrentStep(int currentStep) {
        mCurrentStep = currentStep;
        invalidate();
    }

    //设置step对应的texts
    public void setStepTexts(String[] stepTexts) {
        mStepTexts = stepTexts;
        mStepCount = mStepTexts.length;
        mStepRectFs = new RectF[mStepCount];//初始化step所对应的矩阵数组，点击step时会用到，用于确定点击的是哪个step
    }

    public void setStepIsTouch(boolean stepIsTouch) {
        mStepIsTouch = stepIsTouch;
    }

    public void setTextUpLine(boolean isChecked) {
        mTextUpLine = isChecked;
        invalidate();
    }

    private Bitmap zoomImg(Bitmap bm, int[] WH) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) WH[0]) / width;
        float scaleHeight = ((float) WH[1]) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    //获取bitmap的宽高
    private int[] getBitmapWH(int stepSize, Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (stepSize != 0) {
            width = stepSize * width / height;
            height = stepSize;
        }
        int[] bitmapWH = new int[]{width, height};
        return bitmapWH;
    }

    //获取文本的宽高
    private int[] getTextWH(String text) {
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        return new int[]{bounds.width(), bounds.height()};
    }

    //获取被点击的step位置
    private int getTouchStep(PointF pointF) {
        for (int i = 0; i < mStepCount; i++) {
            RectF stepRectF = mStepRectFs[i];
            if (stepRectF.contains(pointF.x, pointF.y)) {
                return i;
            }
        }
        return -1;
    }

    private int dp2px(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * value + 0.5f);
    }
}
