package com.qiniu.pili.droid.shortvideo.demo.view.sticker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLTextView;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Utils;
import com.qiniu.pili.droid.shortvideo.demo.view.OnStickerOperateListener;

/**
 * 自定义文字贴图控件，实现了文字的旋转、缩放等功能
 */
public class StickerTextView extends PLTextView {

    /**
     * 文字的最大、最小缩放比例
     */
    private static final float MAX_SCALE = 4.0f;
    private static final float MIN_SCALE = 0.3f;

    /**
     * 控制缩放，旋转图标所在四个点得位置
     */
    private static final int LEFT_TOP = 0;
    private static final int RIGHT_TOP = 1;
    private static final int RIGHT_BOTTOM = 2;
    private static final int LEFT_BOTTOM = 3;

    /**
     * 一些默认的常量
     */
    private static final int DEFAULT_FRAME_PADDING = 0;
    private static final int DEFAULT_FRAME_WIDTH = 2;
    private static final int DEFAULT_FRAME_COLOR = Color.WHITE;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final float DEFAULT_DEGREE = 0;
    private static final int DEFAULT_CONTROL_LOCATION = RIGHT_BOTTOM;
    private static final int DEFAULT_DELETE_LOCATION = LEFT_TOP;
    private static final int DEFAULT_EDIT_LOCATION = RIGHT_TOP;
    private static final boolean DEFAULT_EDITABLE = true;

    //初始状态
    public static final int STATUS_INIT = 0;
    //拖动状态
    public static final int STATUS_DRAG = 1;
    //旋转或者放大状态
    public static final int STATUS_ROTATE_ZOOM = 2;
    //点击编辑状态
    private static final int STATUS_EDIT = 3;
    //点击删除状态
    private static final int STATUS_DELETE = 4;
    //当前所处的状态
    private int mStatus = STATUS_INIT;

    /**
     * 中心点坐标，相对于其父类布局而言的
     */
    private PointF mCenterPoint = new PointF();

    private boolean mIsMeasured;

    /**
     * View 的宽度和高度，随着文字的旋转而变化(不包括控制旋转，缩放图片的宽高)
     */
    private int mViewWidth, mViewHeight;

    /**
     * 文字的旋转角度和缩放比例
     */
    private float mDegree = DEFAULT_DEGREE;
    private float mScale = DEFAULT_SCALE;

    /**
     * 距离父类布局的左间距和上间距
     */
    private int mViewPaddingLeft;
    private int mViewPaddingTop;

    /**
     * view 四个点坐标
     */
    private Point mLTPoint;
    private Point mRTPoint;
    private Point mRBPoint;
    private Point mLBPoint;

    /**
     * 用于缩放，旋转的控制点的信息
     */
    private Point mControlPoint = new Point();
    private Drawable mControlDrawable;
    private int mControlDrawableWidth, mControlDrawableHeight;

    /**
     * 用于编辑的控制点的信息
     */
    private Point mEditPoint = new Point();
    private Drawable mEditDrawable;
    private int mEditDrawableWidth, mEditDrawableHeight;

    /**
     * 用于删除的控制点的信息
     */
    private Point mDeletePoint = new Point();
    private Drawable mDeleteDrawable;
    private int mDeleteDrawableWidth, mDeleteDrawableHeight;

    /**
     * 外边框
     */
    //外边框的 path
    private final Path mPath = new Path();
    //外边框的画笔
    private Paint mPaint;
    //外边框与图片之间的间距, 单位是 dip
    private int mFramePadding;
    //外边框颜色
    private int mFrameColor;
    //外边框线条粗细, 单位是 dip
    private int mFrameWidth;

    /**
     * 是否处于可以缩放，平移，旋转状态
     */
    private boolean mIsEditable = DEFAULT_EDITABLE;

    /**
     * 是否可触摸
     */
    private boolean mIsTouchable = true;

    private DisplayMetrics mDisplayMetrics;

    /**
     * 触摸落点位置
     */
    private final PointF mPreMovePointF = new PointF();
    private final PointF mCurMovePointF = new PointF();

    /**
     * 文字在旋转时 x 和 y 方向的偏移量
     */
    private int mOffsetX, mOffsetY;

    /**
     * 控制图标所在的位置（比如左上，右上，左下，右下）
     */
    private int mControlLocation = DEFAULT_CONTROL_LOCATION;
    private int mDeleteLocation = DEFAULT_DELETE_LOCATION;
    private int mEditLocation = DEFAULT_EDIT_LOCATION;

    /**
     * 是否显示删除图标，是否显示编辑图标
     */
    private boolean mShowDelete = true;
    private boolean mShowEdit = true;

    /**
     * 文字相关
     */
    private final Paint mTextPaint = new Paint();
    private final Paint mStrokePaint = new Paint();
    private float mTextWidth;
    private float mTextHeight;
    //文字的 X 、 Y 坐标
    private int mTextX, mTextY;
    // 记录当前文字贴图的位置信息
    private Rect mTextPosition;

    private OnStickerOperateListener mClickListener;

    public StickerTextView(Context context) {
        this(context, null);
    }

    public StickerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainStyledAttributes(attrs);
        init();
    }

    /**
     * 获取自定义属性
     */
    private void obtainStyledAttributes(AttributeSet attrs) {
        mDisplayMetrics = getContext().getResources().getDisplayMetrics();
        TypedArray mTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.StickerView);
        mFramePadding = mTypedArray.getDimensionPixelSize(R.styleable.StickerView_framePadding, DEFAULT_FRAME_PADDING);
        mFrameWidth = mTypedArray.getDimensionPixelSize(R.styleable.StickerView_frameWidth, DEFAULT_FRAME_WIDTH);
        mFrameColor = mTypedArray.getColor(R.styleable.StickerView_frameColor, DEFAULT_FRAME_COLOR);
        mScale = mTypedArray.getFloat(R.styleable.StickerView_scale, DEFAULT_SCALE);
        mDegree = mTypedArray.getFloat(R.styleable.StickerView_degree, DEFAULT_DEGREE);
        mIsEditable = mTypedArray.getBoolean(R.styleable.StickerView_editable, DEFAULT_EDITABLE);
        mControlDrawable = mTypedArray.getDrawable(R.styleable.StickerView_controlDrawable);
        mControlLocation = mTypedArray.getInt(R.styleable.StickerView_controlLocation, DEFAULT_CONTROL_LOCATION);
        mEditDrawable = mTypedArray.getDrawable(R.styleable.StickerView_editDrawable);
        mEditLocation = mTypedArray.getInt(R.styleable.StickerView_editLocation, RIGHT_TOP);
        mDeleteDrawable = mTypedArray.getDrawable(R.styleable.StickerView_deleteDrawable);
        mDeleteLocation = mTypedArray.getInt(R.styleable.StickerView_deleteLocation, LEFT_TOP);
        mTypedArray.recycle();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mFrameColor);
        mPaint.setStrokeWidth(mFrameWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setColor(getCurrentTextColor());
        mTextPaint.setShadowLayer(getShadowRadius(), getShadowDx(), getShadowDy(), getShadowColor());
        mTextPaint.setTypeface(getTypeface());
        mTextPaint.setAlpha((int) (getAlpha() * 255));

        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setTextSize(getTextSize());
        mStrokePaint.setTypeface(getTypeface());
        mStrokePaint.setAlpha((int) (getAlpha() * 255));

        if (mControlDrawable == null) {
            mControlDrawable = getContext().getResources().getDrawable(R.drawable.ic_rotation);
        }

        if (mDeleteDrawable == null) {
            mDeleteDrawable = getContext().getResources().getDrawable(R.drawable.ic_delete_sticker);
        }

        if (mEditDrawable == null) {
            mEditDrawable = getContext().getResources().getDrawable(R.drawable.ic_edit);
        }

        mControlDrawableWidth = mControlDrawable.getIntrinsicWidth();
        mControlDrawableHeight = mControlDrawable.getIntrinsicHeight();

        mDeleteDrawableWidth = mDeleteDrawable.getIntrinsicWidth();
        mDeleteDrawableHeight = mDeleteDrawable.getIntrinsicHeight();

        mEditDrawableWidth = mEditDrawable.getIntrinsicWidth();
        mEditDrawableHeight = mEditDrawable.getIntrinsicHeight();

        String[] strings = getText().toString().split("\n");
        mTextWidth = 0;
        for (String s : strings) {
            float width = mTextPaint.measureText(s);
            if (width > mTextWidth) {
                mTextWidth = width;
            }
        }
        mTextHeight = (mTextPaint.descent() - mTextPaint.ascent()) * strings.length;

        transformDraw();
    }

    public void setOnStickerOperateListener(OnStickerOperateListener listener) {
        mClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mIsMeasured) {
            //获取所在父布局的中心点
            ViewGroup mViewGroup = (ViewGroup) getParent();
            if (null != mViewGroup) {
                int parentWidth = mViewGroup.getWidth();
                int parentHeight = mViewGroup.getHeight();
                mCenterPoint.set(parentWidth / 2, parentHeight / 2);
            }
            mIsMeasured = true;
            adjustLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //处于可编辑状态才画边框和控制图标
        if (mIsEditable) {
            mPath.reset();
            mPath.moveTo(mLTPoint.x, mLTPoint.y);
            mPath.lineTo(mRTPoint.x, mRTPoint.y);
            mPath.lineTo(mRBPoint.x, mRBPoint.y);
            mPath.lineTo(mLBPoint.x, mLBPoint.y);
            mPath.lineTo(mLTPoint.x, mLTPoint.y);
            canvas.drawPath(mPath, mPaint);
            //画旋转，缩放，删除图标
            mControlDrawable.setBounds(mControlPoint.x - mControlDrawableWidth / 2, mControlPoint.y - mControlDrawableHeight / 2, mControlPoint.x + mControlDrawableWidth / 2, mControlPoint.y + mControlDrawableHeight / 2);
            mControlDrawable.draw(canvas);

            if (mEditDrawable != null && mShowEdit) {
                mEditDrawable.setBounds(mEditPoint.x - mControlDrawableWidth / 2, mEditPoint.y - mControlDrawableHeight / 2, mEditPoint.x + mControlDrawableWidth / 2, mEditPoint.y + mControlDrawableHeight / 2);
                mEditDrawable.draw(canvas);
            }

            if (mDeleteDrawable != null && mShowDelete) {
                mDeleteDrawable.setBounds(mDeletePoint.x - mControlDrawableWidth / 2, mDeletePoint.y - mControlDrawableHeight / 2, mDeletePoint.x + mControlDrawableWidth / 2, mDeletePoint.y + mControlDrawableHeight / 2);
                mDeleteDrawable.draw(canvas);
            }
        }

        canvas.save();
        canvas.translate(mLTPoint.x, mLTPoint.y);
        canvas.rotate(mDegree);

        float singleHeight = mTextPaint.descent() - mTextPaint.ascent();
        float yPostion = singleHeight - mTextPaint.descent();
        String[] strings = getText().toString().split("\n");
        for (String s : strings) {
            if (mStrokePaint.getStrokeWidth() != 0) {
                canvas.drawText(s, 0, yPostion, mStrokePaint);
            }
            canvas.drawText(s, 0, yPostion, mTextPaint);
            yPostion += singleHeight;
        }

        canvas.restore();

        adjustLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsTouchable) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                mStatus = judgeStatus(event.getX(), event.getY());
                if (mClickListener != null) {
                    mClickListener.onStickerSelected();
                }
                mIsEditable = true;
                break;
            case MotionEvent.ACTION_UP:
                if (mClickListener != null) {
                    //再次判定抬起点，是否处于 icon 的范围之内
                    int secondJudgeState = judgeStatus(event.getX(), event.getY());
                    //满足才触发回调
                    if (mStatus == STATUS_EDIT && secondJudgeState == mStatus) {
                        mClickListener.onEditClicked();
                    }
                    if (mStatus == STATUS_DELETE && secondJudgeState == mStatus) {
                        mClickListener.onDeleteClicked();
                    }
                }
                mStatus = STATUS_INIT;
                break;
            case MotionEvent.ACTION_MOVE:
                mCurMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                if (mStatus == STATUS_ROTATE_ZOOM) {
                    //文字对角线一半的距离
                    float halfTextDiagonal = (float) Math.sqrt(Math.pow(mTextWidth / 2, 2) + Math.pow(mTextHeight / 2, 2));
                    //当前的点到 view 中心的距离
                    float curPointToViewCenterDistance = Utils.getTwoPointsDistance(mCenterPoint, mCurMovePointF);
                    //之前的点到 view 中心的距离
                    float prePointToCenterDistance = Utils.getTwoPointsDistance(mCenterPoint, mPreMovePointF);
                    //计算缩放幅度
                    float changeScale = (curPointToViewCenterDistance - prePointToCenterDistance) * 2 / halfTextDiagonal;
                    mScale += changeScale;
                    //缩放比例的界限判断
                    if (mScale <= MIN_SCALE) {
                        mScale = MIN_SCALE;
                    } else if (mScale >= MAX_SCALE) {
                        mScale = MAX_SCALE;
                    }

                    //角度 先利用三角函数求出旋转角度，再利用向量判别旋转方向
                    double a = Utils.getTwoPointsDistance(mCenterPoint, mPreMovePointF);
                    double b = Utils.getTwoPointsDistance(mPreMovePointF, mCurMovePointF);
                    double c = Utils.getTwoPointsDistance(mCenterPoint, mCurMovePointF);
                    double cosb = (a * a + c * c - b * b) / (2 * a * c);
                    if (cosb >= 1) {
                        cosb = 1f;
                    }
                    double radian = Math.acos(cosb);
                    float newDegree = (float) Utils.radianToDegree(radian);

                    //center -> proMove 的向量
                    PointF centerToProMove = new PointF((mPreMovePointF.x - mCenterPoint.x), (mPreMovePointF.y - mCenterPoint.y));
                    //center -> curMove 的向量
                    PointF centerToCurMove = new PointF((mCurMovePointF.x - mCenterPoint.x), (mCurMovePointF.y - mCenterPoint.y));
                    //向量叉乘结果, 如果结果为负数，表示为逆时针，结果为正数表示顺时针
                    float result = centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;
                    if (result < 0) {
                        newDegree = -newDegree;
                    }

                    mDegree = mDegree + newDegree;
                    mTextPaint.setTextSize(getTextSize() * mScale);
                    mStrokePaint.setTextSize(getTextSize() * mScale);
                    transformDraw();
                } else if (mStatus == STATUS_DRAG) {
                    // 修改中心点
                    mCenterPoint.x += mCurMovePointF.x - mPreMovePointF.x;
                    mCenterPoint.y += mCurMovePointF.y - mPreMovePointF.y;

                    adjustLayout();
                }

                mPreMovePointF.set(mCurMovePointF);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 避免非预期的布局变化
        boolean viewLocationChanged = changed && mTextPosition != null
                && (left != mTextPosition.left || top != mTextPosition.top
                || right != mTextPosition.right || bottom != mTextPosition.bottom);
        if (viewLocationChanged) {
            layout(mTextPosition.left, mTextPosition.top, mTextPosition.right, mTextPosition.bottom);
        }
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
        if (mTextPaint != null && mStrokePaint != null) {
            mTextPaint.setTypeface(tf);
            mStrokePaint.setTypeface(tf);
            transformDraw();
        }
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mTextPaint != null && mStrokePaint != null) {
            mTextPaint.setColor(color);
            invalidate();
        }
    }

    @Override
    public void setShadowLayer(float shadowRadius, float shadowDx, float shadowDy, int shadowColor) {
        super.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
        if (mTextPaint != null && mStrokePaint != null) {
            mTextPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
            invalidate();
        }
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (mTextPaint != null && mStrokePaint != null) {
            mTextPaint.setAlpha((int) (alpha * 255));
            mStrokePaint.setAlpha((int) (alpha * 255));
            invalidate();
        }
    }

    public void setStrokeColor(int color) {
        mStrokePaint.setColor(color);
        invalidate();
    }

    public void setStrokeWidth(float width) {
        mStrokePaint.setStrokeWidth(width);
        invalidate();
    }

    @Override
    public void setSelected(boolean isEditable) {
        this.mIsEditable = isEditable;
        invalidate();
    }

    /**
     * 调整 View 的大小，位置
     */
    private void adjustLayout() {
        int actualWidth = mViewWidth + mControlDrawableWidth;
        int actualHeight = mViewHeight + mControlDrawableHeight;

        int newPaddingLeft = (int) (mCenterPoint.x - actualWidth / 2);
        int newPaddingTop = (int) (mCenterPoint.y - actualHeight / 2);

        mViewPaddingLeft = newPaddingLeft;
        mViewPaddingTop = newPaddingTop;

        if (mTextPosition == null) {
            mTextPosition = new Rect();
        }
        mTextPosition.left = newPaddingLeft;
        mTextPosition.top = newPaddingTop;
        mTextPosition.right = newPaddingLeft + actualWidth;
        mTextPosition.bottom = newPaddingTop + actualHeight;

        layout(newPaddingLeft, newPaddingTop, newPaddingLeft + actualWidth, newPaddingTop + actualHeight);

        mTextX = newPaddingLeft + mControlDrawableWidth / 2;
        mTextY = newPaddingTop + mControlDrawableHeight / 2;
    }

    /**
     * 设置 Matrix , 强制刷新
     */
    private void transformDraw() {
        //测量文字宽高
        String[] strings = getText().toString().split("\n");
        mTextWidth = 0;
        for (String s : strings) {
            float width = mTextPaint.measureText(s);
            if (width > mTextWidth) {
                mTextWidth = width;
            }
        }
        mTextHeight = (mTextPaint.descent() - mTextPaint.ascent()) * strings.length;
        //计算旋转后的矩形位置和宽高
        computeRect(mFramePadding, mFramePadding, (int) mTextWidth + mFramePadding, (int) mTextHeight + mFramePadding, mDegree);
        //调整布局
        adjustLayout();
    }

    /**
     * 获取文字四个点和 View 的大小
     */
    private void computeRect(int left, int top, int right, int bottom, float degree) {
        Point lt = new Point(left, top);
        Point rt = new Point(right, top);
        Point rb = new Point(right, bottom);
        Point lb = new Point(left, bottom);
        Point cp = new Point((left + right) / 2, (top + bottom) / 2);
        mLTPoint = obtainRotationPoint(cp, lt, degree);
        mRTPoint = obtainRotationPoint(cp, rt, degree);
        mRBPoint = obtainRotationPoint(cp, rb, degree);
        mLBPoint = obtainRotationPoint(cp, lb, degree);

        //计算 X 坐标最大的值和最小的值
        int maxCoordinateX = Utils.getMaxValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x);
        int minCoordinateX = Utils.getMinValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x);

        mViewWidth = maxCoordinateX - minCoordinateX;

        //计算 Y 坐标最大的值和最小的值
        int maxCoordinateY = Utils.getMaxValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y);
        int minCoordinateY = Utils.getMinValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y);

        mViewHeight = maxCoordinateY - minCoordinateY;

        mControlPoint = LocationToPoint(mControlLocation);
        mDeletePoint = LocationToPoint(mDeleteLocation);
        mEditPoint = LocationToPoint(mEditLocation);

        mOffsetX = mViewWidth / 2 - cp.x;
        mOffsetY = mViewHeight / 2 - cp.y;

        int halfDrawableWidth = mControlDrawableWidth / 2;
        int halfDrawableHeight = mControlDrawableHeight / 2;

        //将 Bitmap 的四个点的 X 的坐标移动 mOffsetX + halfDrawableWidth
        mLTPoint.x += (mOffsetX + halfDrawableWidth);
        mRTPoint.x += (mOffsetX + halfDrawableWidth);
        mRBPoint.x += (mOffsetX + halfDrawableWidth);
        mLBPoint.x += (mOffsetX + halfDrawableWidth);

        //将 Bitmap 的四个点的 Y 坐标移动 mOffsetY + halfDrawableHeight
        mLTPoint.y += (mOffsetY + halfDrawableHeight);
        mRTPoint.y += (mOffsetY + halfDrawableHeight);
        mRBPoint.y += (mOffsetY + halfDrawableHeight);
        mLBPoint.y += (mOffsetY + halfDrawableHeight);

        mControlPoint = LocationToPoint(mControlLocation);
        mDeletePoint = LocationToPoint(mDeleteLocation);
        mEditPoint = LocationToPoint(mEditLocation);
    }

    /**
     * 根据位置判断控制图标处于那个点
     */
    private Point LocationToPoint(int location) {
        switch (location) {
            case LEFT_TOP:
                return mLTPoint;
            case RIGHT_TOP:
                return mRTPoint;
            case RIGHT_BOTTOM:
                return mRBPoint;
            case LEFT_BOTTOM:
                return mLBPoint;
            default:
                break;
        }
        return mLTPoint;
    }

    /**
     * 获取旋转某个角度之后的点
     */
    public static Point obtainRotationPoint(Point center, Point source, float degree) {
        //两者之间的距离
        Point disPoint = new Point();
        disPoint.x = source.x - center.x;
        disPoint.y = source.y - center.y;

        //旋转之前的弧度和旋转之后的弧度
        double originRadian, resultRadian;

        //经过旋转之后点的坐标
        Point resultPoint = new Point();

        double distance = Math.sqrt(disPoint.x * disPoint.x + disPoint.y * disPoint.y);
        if (disPoint.x == 0 && disPoint.y == 0) {
            return center;

        } else if (disPoint.x >= 0 && disPoint.y >= 0) {
            // 第一象限
            // 计算与 x 正方向的夹角
            originRadian = Math.asin(disPoint.y / distance);
        } else if (disPoint.x < 0 && disPoint.y >= 0) {
            // 第二象限
            // 计算与 x 正方向的夹角
            originRadian = Math.asin(Math.abs(disPoint.x) / distance);
            originRadian = originRadian + Math.PI / 2;
        } else if (disPoint.x < 0) {
            // 第三象限
            // 计算与 x 正方向的夹角
            originRadian = Math.asin(Math.abs(disPoint.y) / distance);
            originRadian = originRadian + Math.PI;
        } else {
            // 第四象限
            // 计算与 x 正方向的夹角
            originRadian = Math.asin(disPoint.x / distance);
            originRadian = originRadian + Math.PI * 3 / 2;
        }

        //弧度调整
        resultRadian = originRadian + Utils.degreeToRadian(degree);

        resultPoint.x = (int) Math.round(distance * Math.cos(resultRadian));
        resultPoint.y = (int) Math.round(distance * Math.sin(resultRadian));
        resultPoint.x += center.x;
        resultPoint.y += center.y;
        return resultPoint;
    }

    /**
     * 根据点击的位置判断是否点中控制旋转，缩放的图标， 初略的计算
     */
    private int judgeStatus(float x, float y) {
        PointF touchPoint = new PointF(x, y);

        //点击的点到控制旋转，缩放点的距离
        float disToControl = Utils.getTwoPointsDistance(touchPoint, new PointF(mControlPoint));

        //如果两者之间的距离小于 控制图标的宽度，高度的最小值，则认为点中了控制图标
        if (disToControl < Math.min(mControlDrawableWidth / 2, mControlDrawableHeight / 2)) {
            return STATUS_ROTATE_ZOOM;
        }

        float disToEdit = Utils.getTwoPointsDistance(touchPoint, new PointF(mEditPoint));
        if (disToEdit < Math.min(mEditDrawableWidth / 2, mEditDrawableHeight / 2)) {
            return STATUS_EDIT;
        }

        float disToDelete = Utils.getTwoPointsDistance(touchPoint, new PointF(mDeletePoint));
        if (disToDelete < Math.min(mDeleteDrawableWidth / 2, mDeleteDrawableHeight / 2)) {
            return STATUS_DELETE;
        }

        return STATUS_DRAG;
    }

    public void setText(String text) {
        super.setText(text);
        transformDraw();
    }

    public float getDegree() {
        return mDegree;
    }

    public void setDegree(float degree) {
        if (this.mDegree != degree) {
            this.mDegree = degree;
            transformDraw();
        }
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
        transformDraw();
    }

    public int getTextX() {
        return this.mTextX;
    }

    public int getTextY() {
        return this.mTextY;
    }

    public int getViewX() {
        return getLeft() + mDeleteDrawableWidth + mFramePadding;
    }

    public int getViewY() {
        return getTop() + mDeleteDrawableHeight + mFramePadding;
    }

    public void setControlDrawable(Drawable drawable) {
        this.mControlDrawable = drawable;
        mControlDrawableWidth = drawable.getIntrinsicWidth();
        mControlDrawableHeight = drawable.getIntrinsicHeight();
        transformDraw();
    }

    public Drawable getControlDrawable() {
        return mControlDrawable;
    }

    public int getFramePadding() {
        return mFramePadding;
    }

    public void setFramePadding(int framePadding) {
        if (this.mFramePadding == framePadding) {
            return;
        }
        this.mFramePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, framePadding, mDisplayMetrics);
        transformDraw();
    }

    public int getFrameColor() {
        return mFrameColor;
    }

    public void setFrameColor(int frameColor) {
        if (this.mFrameColor == frameColor) {
            return;
        }
        this.mFrameColor = frameColor;
        mPaint.setColor(frameColor);
        invalidate();
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        if (this.mFrameWidth == frameWidth) {
            return;
        }
        this.mFrameWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, frameWidth, mDisplayMetrics);
        mPaint.setStrokeWidth(frameWidth);
        invalidate();
    }

    /**
     * 设置控制图标的位置
     * 设置的值只能选择 LEFT_TOP ，RIGHT_TOP ，RIGHT_BOTTOM ，LEFT_BOTTOM
     */
    public void setControlLocation(int location) {
        if (this.mControlLocation == location) {
            return;
        }
        this.mControlLocation = location;
        transformDraw();
    }

    public int getControlLocation() {
        return mControlLocation;
    }

    public PointF getCenterPoint() {
        return mCenterPoint;
    }

    /**
     * 设置文字中心点位置，相对于父布局而言
     */
    public void setCenterPoint(PointF mCenterPoint) {
        this.mCenterPoint = mCenterPoint;
        adjustLayout();
    }

    public boolean isEditable() {
        return mIsEditable;
    }

    public void showDelete(boolean showDelete) {
        mShowDelete = showDelete;
    }

    public void showEdit(boolean showEdit) {
        mShowEdit = showEdit;
    }

    public void setTouchable(boolean touchable) {
        mIsTouchable = touchable;
    }
}
