package com.qiniu.shortvideo.app.view.layer;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.gif.GifDecoder;
import com.qiniu.shortvideo.app.gif.GifFrameLoader;
import com.qiniu.shortvideo.app.utils.Utils;

/**
 * 自定义 ImageView 实现了 GIF 图片的播放、旋转、缩放等操作
 */
public class StickerImageView extends AppCompatImageView {

    /**
     * 图片的最大缩放比例
     */
    public static final float MAX_SCALE = 4.0f;

    /**
     * 图片的最小缩放比例
     */
    public static final float MIN_SCALE = 0.3f;

    /**
     * 控制缩放，旋转图标所在四个点得位置
     */
    public static final int LEFT_TOP = 0;
    public static final int RIGHT_TOP = 1;
    public static final int RIGHT_BOTTOM = 2;
    public static final int LEFT_BOTTOM = 3;

    /**
     * 一些默认的常量
     */
    public static final int DEFAULT_FRAME_PADDING = 0;
    public static final int DEFAULT_FRAME_WIDTH = 2;
    public static final int DEFAULT_FRAME_COLOR = Color.WHITE;
    public static final float DEFAULT_SCALE = 1.0f;
    public static final float DEFAULT_DEGREE = 0;
    public static final int DEFAULT_CONTROL_LOCATION = RIGHT_BOTTOM;
    public static final int DEFAULT_DELETE_LOCATION = LEFT_TOP;
    public static final int DEFAULT_EDIT_LOCATION = RIGHT_TOP;
    public static final boolean DEFAULT_EDITABLE = true;
    public static final int DEFAULT_OTHER_DRAWABLE_WIDTH = 50;
    public static final int DEFAULT_OTHER_DRAWABLE_HEIGHT = 50;

    /**
     * 用于旋转缩放的 Bitmap
     */
    private Bitmap mBitmap;

    /**
     * LayerOperationView 的中心点坐标，相对于其父类布局而言的
     */
    private PointF mCenterPoint = new PointF();

    private boolean mIsMeasured;

    /**
     * View的宽度和高度，随着图片的旋转而变化(不包括控制旋转，缩放图片的宽高)
     */
    private int mViewWidth, mViewHeight;

    /**
     * 图片的旋转角度
     */
    private float mDegree = DEFAULT_DEGREE;

    /**
     * 图片的缩放比例
     */
    private float mScale = DEFAULT_SCALE;

    /**
     * 用于缩放，旋转，平移的矩阵
     */
    private Matrix mTransformMatrix = new Matrix();

    /**
     * LayerOperationView 距离父类布局的左间距
     */
    private int mViewPaddingLeft;

    /**
     * LayerOperationView 距离父类布局的上间距
     */
    private int mViewPaddingTop;

    /**
     * 图片四个点坐标
     */
    private Point mLTPoint;
    private Point mRTPoint;
    private Point mRBPoint;
    private Point mLBPoint;

    /**
     * 用于缩放，旋转的控制点的坐标
     */
    private Point mControlPoint = new Point();

    /**
     * 用于缩放，旋转的图标
     */
    private Drawable mControlDrawable;

    /**
     * 缩放，旋转图标的宽和高
     */
    private int mControlDrawableWidth, mControlDrawableHeight;

    /**
     * 用于编辑的控制点的坐标
     */
    private Point mEditPoint = new Point();
    private Drawable mEditDrawable;
    private int mEditDrawableWidth, mEditDrawableHeight;

    /**
     * 用于删除的控制点的坐标
     */
    private Point mDeletePoint = new Point();
    private Drawable mDeleteDrawable;
    private int mDeleteDrawableWidth, mDeleteDrawableHeight;

    /**
     * 画外围框的 Path
     */
    private Path mPath = new Path();

    /**
     * 画外围框的画笔
     */
    private Paint mPaint;

    /**
     * 初始状态
     */
    public static final int STATUS_INIT = 0;

    /**
     * 拖动状态
     */
    public static final int STATUS_DRAG = 1;

    /**
     * 旋转或者放大状态
     */
    public static final int STATUS_ROTATE_ZOOM = 2;

    /**
     * 点击编辑状态
     */
    private static final int STATUS_EDIT = 3;
    /**
     * 点击删除状态
     */
    private static final int STATUS_DELETE = 4;

    /**
     * 当前所处的状态
     */
    private int mStatus = STATUS_INIT;

    /**
     * 外边框与图片之间的间距, 单位是 dip
     */
    private int mFramePadding = DEFAULT_FRAME_PADDING;

    /**
     * 外边框颜色
     */
    private int mFrameColor = DEFAULT_FRAME_COLOR;

    /**
     * 外边框线条粗细, 单位是 dip
     */
    private int mFrameWidth = DEFAULT_FRAME_WIDTH;

    /**
     * 是否处于可以缩放，平移，旋转状态
     */
    private boolean mIsEditable = DEFAULT_EDITABLE;

    private DisplayMetrics mDisplayMetrics;

    private PointF mPreMovePointF = new PointF();
    private PointF mCurMovePointF = new PointF();

    /**
     * 图片在旋转时 x 方向的偏移量
     */
    private int mOffsetX;

    /**
     * 图片在旋转时 y 方向的偏移量
     */
    private int mOffsetY;

    /**
     * 记录点击事件的起始位置
     */
    private float mDownX, mDownY;

    /**
     * 控制图标所在的位置（比如左上，右上，左下，右下）
     */
    private int mControlLocation = DEFAULT_CONTROL_LOCATION;
    private int mDeleteLocation = DEFAULT_DELETE_LOCATION;
    private int mEditLocation = DEFAULT_EDIT_LOCATION;

    private boolean mShowDelete = true;
    private boolean mShowEdit = true;

    private long mStartTime, mEndTime;

    private int mX;
    private int mY;

    /**
     * Gif 动图相关
     */
    private boolean mIsGifSticker;
    private GifDecoder mGifDecoder;
    private Bitmap mFirstGifFrame;
    private GifFrameLoader mGifFrameLoader;
    private String mGifFilePath;

    private OnStickerOperateListener mClickListener;

    public StickerImageView(Context context) {
        this(context, null);
    }

    public StickerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(attrs);
        init();
    }

    /**
     * 获取自定义属性
     *
     * @param attrs
     */
    private void obtainStyledAttributes(AttributeSet attrs) {
        mDisplayMetrics = getContext().getResources().getDisplayMetrics();
        mFramePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_FRAME_PADDING, mDisplayMetrics);
        mFrameWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_FRAME_WIDTH, mDisplayMetrics);

        TypedArray mTypedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.StickerView);

        Drawable srcDrawable = mTypedArray.getDrawable(R.styleable.StickerView_src_bitmap);
        mBitmap = drawable2Bitmap(srcDrawable);

        mFramePadding = mTypedArray.getDimensionPixelSize(R.styleable.StickerView_framePadding, mFramePadding);
        mFrameWidth = mTypedArray.getDimensionPixelSize(R.styleable.StickerView_frameWidth, mFrameWidth);
        mFrameColor = mTypedArray.getColor(R.styleable.StickerView_frameColor, DEFAULT_FRAME_COLOR);
        mScale = mTypedArray.getFloat(R.styleable.StickerView_scale, DEFAULT_SCALE);
        mDegree = mTypedArray.getFloat(R.styleable.StickerView_degree, DEFAULT_DEGREE);

        mControlDrawable = mTypedArray.getDrawable(R.styleable.StickerView_controlDrawable);
        mControlLocation = mTypedArray.getInt(R.styleable.StickerView_controlLocation, DEFAULT_CONTROL_LOCATION);
        mEditDrawable = mTypedArray.getDrawable(R.styleable.StickerView_editDrawable);
        mEditLocation = mTypedArray.getInt(R.styleable.StickerView_editLocation, RIGHT_TOP);
        mDeleteDrawable = mTypedArray.getDrawable(R.styleable.StickerView_deleteDrawable);
        mDeleteLocation = mTypedArray.getInt(R.styleable.StickerView_deleteLocation, LEFT_TOP);

        mIsEditable = mTypedArray.getBoolean(R.styleable.StickerView_editable, DEFAULT_EDITABLE);

        mTypedArray.recycle();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mFrameColor);
        mPaint.setStrokeWidth(mFrameWidth);
        mPaint.setStyle(Style.STROKE);

        if (mControlDrawable == null) {
            mControlDrawable = getContext().getResources().getDrawable(R.mipmap.qn_rotate);
        }

        if (mDeleteDrawable == null) {
            mDeleteDrawable = getContext().getResources().getDrawable(R.mipmap.qn_delete);
        }

        if (mEditDrawable == null) {
            mEditDrawable = getContext().getResources().getDrawable(R.mipmap.qn_edit);
        }

        mControlDrawableWidth = mControlDrawable.getIntrinsicWidth();
        mControlDrawableHeight = mControlDrawable.getIntrinsicHeight();

        mDeleteDrawableWidth = mDeleteDrawable.getIntrinsicWidth();
        mDeleteDrawableHeight = mDeleteDrawable.getIntrinsicHeight();

        mEditDrawableWidth = mEditDrawable.getIntrinsicWidth();
        mEditDrawableHeight = mEditDrawable.getIntrinsicHeight();

        transformDraw();
    }

    public boolean setGifFile(String filePath) {
        mGifFilePath = filePath;
        mIsGifSticker = true;
        File imageFile = new File(mGifFilePath);
        if (!imageFile.exists()) {
            return false;
        }
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        if (mGifDecoder == null) {
            mGifDecoder = new GifDecoder();
        }
        int code = mGifDecoder.read(fileInputStream, 0);
        if (code != GifDecoder.STATUS_OK) {
            return false;
        }
        mGifDecoder.advance();
        mFirstGifFrame = mGifDecoder.getNextFrame();
        if (mGifFrameLoader == null) {
            mGifFrameLoader = new GifFrameLoader(mGifDecoder, mFirstGifFrame);
            mGifFrameLoader.setFrameCallback(mFrameCallback);
        }
        transformDraw();
        return true;
    }

    public String getFilePath() {
        if (mIsGifSticker) {
            return mGifFilePath;
        }
        return null;
    }

    public void setOnStickerOperateListener(OnStickerOperateListener listener) {
        mClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!mIsMeasured) {
            // 获取 LayerOperationView 所在父布局的中心点
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
        //每次draw之前调整View的位置和大小
        super.onDraw(canvas);

        if (mBitmap == null && !mIsGifSticker) {
            return;
        }
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mTransformMatrix, mPaint);
        } else if (mIsGifSticker) {
            Bitmap currentFrame = mGifFrameLoader.getCurrentFrame();
            canvas.drawBitmap(currentFrame, mTransformMatrix, mPaint);
        }

        //处于可编辑状态才画边框和控制图标
        if (mIsEditable) {
            mPath.reset();
            mPath.moveTo(mLTPoint.x, mLTPoint.y);
            mPath.lineTo(mRTPoint.x, mRTPoint.y);
            mPath.lineTo(mRBPoint.x, mRBPoint.y);
            mPath.lineTo(mLBPoint.x, mLBPoint.y);
            mPath.lineTo(mLTPoint.x, mLTPoint.y);
            mPath.lineTo(mRTPoint.x, mRTPoint.y);
            canvas.drawPath(mPath, mPaint);
            //画旋转, 缩放图标

            mControlDrawable.setBounds(mControlPoint.x - mControlDrawableWidth / 2,
                    mControlPoint.y - mControlDrawableHeight / 2, mControlPoint.x + mControlDrawableWidth
                            / 2, mControlPoint.y + mControlDrawableHeight / 2);
            mControlDrawable.draw(canvas);

            if (mEditDrawable != null && mShowEdit) {
                mEditDrawable.setBounds(mEditPoint.x - mControlDrawableWidth / 2,
                        mEditPoint.y - mControlDrawableHeight / 2, mEditPoint.x + mControlDrawableWidth
                                / 2, mEditPoint.y + mControlDrawableHeight / 2);
                mEditDrawable.draw(canvas);
            }

            if (mDeleteDrawable != null && mShowDelete) {
                mDeleteDrawable.setBounds(mDeletePoint.x - mControlDrawableWidth / 2,
                        mDeletePoint.y - mControlDrawableHeight / 2, mDeletePoint.x + mControlDrawableWidth
                                / 2, mDeletePoint.y + mControlDrawableHeight / 2);
                mDeleteDrawable.draw(canvas);
            }
        }

        adjustLayout();
    }

    /**
     * 调整 View 的大小，位置
     */
    private void adjustLayout() {
        int actualWidth = mViewWidth + mControlDrawableWidth;
        int actualHeight = mViewHeight + mControlDrawableHeight;

        int newPaddingLeft = (int) (mCenterPoint.x - actualWidth / 2);
        int newPaddingTop = (int) (mCenterPoint.y - actualHeight / 2);

        if (mViewPaddingLeft != newPaddingLeft || mViewPaddingTop != newPaddingTop) {
            mViewPaddingLeft = newPaddingLeft;
            mViewPaddingTop = newPaddingTop;
        }

        layout(newPaddingLeft, newPaddingTop, newPaddingLeft + actualWidth, newPaddingTop + actualHeight);

        mX = newPaddingLeft + mControlDrawableWidth / 2;
        mY = newPaddingTop + mControlDrawableHeight / 2;
    }

    /**
     * 设置旋转图
     *
     * @param bitmap
     */
    public void setImageBitamp(Bitmap bitmap) {
        this.mBitmap = bitmap;
        transformDraw();
    }

    /**
     * 设置旋转图
     *
     * @param drawable
     */
    public void setImageDrawable(Drawable drawable) {
        this.mBitmap = drawable2Bitmap(drawable);
        transformDraw();
    }

    /**
     * 从Drawable中获取Bitmap对象
     *
     * @param drawable
     * @return
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        try {
            if (drawable == null) {
                return null;
            }

            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }

            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(
                    intrinsicWidth <= 0 ? DEFAULT_OTHER_DRAWABLE_WIDTH
                            : intrinsicWidth,
                    intrinsicHeight <= 0 ? DEFAULT_OTHER_DRAWABLE_HEIGHT
                            : intrinsicHeight, Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * 根据id设置旋转图
     *
     * @param resId
     */
    public void setImageResource(int resId) {
        Drawable drawable = getContext().getResources().getDrawable(resId);
        setImageDrawable(drawable);
    }

    /**
     * 设置 Matrix, 强制刷新
     */
    private void transformDraw() {
        if (mBitmap == null && mFirstGifFrame == null) {
            return;
        }
        int bitmapWidth;
        int bitmapHeight;
        if (mIsGifSticker) {
            bitmapWidth = (int) (mFirstGifFrame.getWidth() * mScale);
            bitmapHeight = (int) (mFirstGifFrame.getHeight() * mScale);
        } else {
            bitmapWidth = (int) (mBitmap.getWidth() * mScale);
            bitmapHeight = (int) (mBitmap.getHeight() * mScale);
        }
        computeRect(-mFramePadding, -mFramePadding, bitmapWidth + mFramePadding, bitmapHeight + mFramePadding, mDegree);

        //设置缩放比例
        mTransformMatrix.setScale(mScale, mScale);
        //绕着图片中心进行旋转
        mTransformMatrix.postRotate(mDegree % 360, bitmapWidth / 2, bitmapHeight / 2);
        //设置画该图片的起始点
        mTransformMatrix.postTranslate(mOffsetX + mControlDrawableWidth / 2, mOffsetY + mControlDrawableHeight / 2);

        adjustLayout();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEditable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                mStatus = judgeStatus(event.getX(), event.getY());

                if (mStatus == STATUS_DRAG) {
                    mDownX = event.getX();
                    mDownY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mClickListener != null) {
                    // 再次判定抬起点 是否处于 icon 的范围之内
                    int secondJudgeState = judgeStatus(event.getX(), event.getY());
                    // 满足才触发回调
                    if (mStatus == STATUS_EDIT && secondJudgeState == mStatus) {
                        mClickListener.onEditClicked();
                    }
                    if (mStatus == STATUS_DELETE && secondJudgeState == mStatus) {
                        mClickListener.onDeleteClicked();
                    }
                    if (mStatus == STATUS_ROTATE_ZOOM || mStatus == STATUS_DRAG) {
                        mClickListener.onRotateClicked();
                    }
                }
                if (mStatus == STATUS_DRAG && mDownX == event.getX() && mDownY == event.getY()) {
                    // 响应点击事件
                    performClick();
                }
                mStatus = STATUS_INIT;
                break;
            case MotionEvent.ACTION_MOVE:
                mCurMovePointF.set(event.getX() + mViewPaddingLeft, event.getY() + mViewPaddingTop);
                if (mStatus == STATUS_ROTATE_ZOOM) {
                    float scale = 1f;

                    int halfBitmapWidth = 0;
                    int halfBitmapHeight = 0;

                    if (mBitmap != null) {
                        halfBitmapWidth = mBitmap.getWidth() / 2;
                        halfBitmapHeight = mBitmap.getHeight() / 2;
                    } else if (mIsGifSticker) {
                        halfBitmapWidth = mFirstGifFrame.getWidth() / 2;
                        halfBitmapHeight = mFirstGifFrame.getHeight() / 2;
                    }

                    // 图片某个点到图片中心的距离
                    float bitmapToCenterDistance = (float) Math.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);

                    // 移动的点到图片中心的距离
                    float moveToCenterDistance = Utils.distance4PointF(mCenterPoint, mCurMovePointF);

                    // 计算缩放比例
                    scale = moveToCenterDistance / bitmapToCenterDistance;

                    // 缩放比例的界限判断
                    if (scale <= MIN_SCALE) {
                        scale = MIN_SCALE;
                    } else if (scale >= MAX_SCALE) {
                        scale = MAX_SCALE;
                    }

                    // 角度
                    double a = Utils.distance4PointF(mCenterPoint, mPreMovePointF);
                    double b = Utils.distance4PointF(mPreMovePointF, mCurMovePointF);
                    double c = Utils.distance4PointF(mCenterPoint, mCurMovePointF);

                    double cosb = (a * a + c * c - b * b) / (2 * a * c);

                    if (cosb >= 1) {
                        cosb = 1f;
                    }

                    double radian = Math.acos(cosb);
                    float newDegree = (float) Utils.radianToDegree(radian);

                    // center -> preMove 的向量， 我们使用 PointF 来实现
                    PointF centerToPreMove = new PointF((mPreMovePointF.x - mCenterPoint.x), (mPreMovePointF.y - mCenterPoint.y));

                    // center -> curMove 的向量
                    PointF centerToCurMove = new PointF((mCurMovePointF.x - mCenterPoint.x), (mCurMovePointF.y - mCenterPoint.y));

                    // 向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
                    float result = centerToPreMove.x * centerToCurMove.y - centerToPreMove.y * centerToCurMove.x;

                    if (result < 0) {
                        newDegree = -newDegree;
                    }

                    mDegree = mDegree + newDegree;
                    mScale = scale;

                    transformDraw();
                } else if (mStatus == STATUS_DRAG) {
                    // 修改中心点
                    mCenterPoint.x += mCurMovePointF.x - mPreMovePointF.x;
                    mCenterPoint.y += mCurMovePointF.y - mPreMovePointF.y;

                    System.out.println(this + "move = " + mCenterPoint);

                    adjustLayout();
                }

                mPreMovePointF.set(mCurMovePointF);
                break;
        }
        return true;
    }

    /**
     * 获取四个点和 View 的大小
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param degree
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

        // 计算 X 坐标最大的值和最小的值
        int maxCoordinateX = Utils.getMaxValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x);
        int minCoordinateX = Utils.getMinValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x);

        mViewWidth = maxCoordinateX - minCoordinateX;

        // 计算 Y 坐标最大的值和最小的值
        int maxCoordinateY = Utils.getMaxValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y);
        int minCoordinateY = Utils.getMinValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y);

        mViewHeight = maxCoordinateY - minCoordinateY;

        // View 中心点的坐标
        Point viewCenterPoint = new Point((maxCoordinateX + minCoordinateX) / 2, (maxCoordinateY + minCoordinateY) / 2);

        // TODO : 这里的逻辑需要捋一下
        mOffsetX = mViewWidth / 2 - viewCenterPoint.x;
        mOffsetY = mViewHeight / 2 - viewCenterPoint.y;

        int halfDrawableWidth = mControlDrawableWidth / 2;
        int halfDrawableHeight = mControlDrawableHeight / 2;

        // 将 Bitmap 的四个点的X的坐标移动 offsetX + halfDrawableWidth
        mLTPoint.x += (mOffsetX + halfDrawableWidth);
        mRTPoint.x += (mOffsetX + halfDrawableWidth);
        mRBPoint.x += (mOffsetX + halfDrawableWidth);
        mLBPoint.x += (mOffsetX + halfDrawableWidth);

        // 将 Bitmap 的四个点的Y坐标移动 mOffsetY + halfDrawableHeight
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
     *
     * @return
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
        }
        return mLTPoint;
    }

    /**
     * 获取旋转某个角度之后的点
     *
     * @param center
     * @param source
     * @param degree
     * @return
     */
    public static Point obtainRotationPoint(Point center, Point source, float degree) {
        //两者之间的距离
        Point disPoint = new Point();
        disPoint.x = source.x - center.x;
        disPoint.y = source.y - center.y;

        //没旋转之前的弧度
        double originRadian = 0;

        //没旋转之前的角度
        double originDegree = 0;

        //旋转之后的角度
        double resultDegree = 0;

        //旋转之后的弧度
        double resultRadian = 0;

        //经过旋转之后点的坐标
        Point resultPoint = new Point();

        double distance = Math.sqrt(disPoint.x * disPoint.x + disPoint.y * disPoint.y);
        if (disPoint.x == 0 && disPoint.y == 0) {
            return center;
            // 第一象限
        } else if (disPoint.x >= 0 && disPoint.y >= 0) {
            // 计算与x正方向的夹角
            originRadian = Math.asin(disPoint.y / distance);

            // 第二象限
        } else if (disPoint.x < 0 && disPoint.y >= 0) {
            // 计算与x正方向的夹角
            originRadian = Math.asin(Math.abs(disPoint.x) / distance);
            originRadian = originRadian + Math.PI / 2;

            // 第三象限
        } else if (disPoint.x < 0 && disPoint.y < 0) {
            // 计算与x正方向的夹角
            originRadian = Math.asin(Math.abs(disPoint.y) / distance);
            originRadian = originRadian + Math.PI;
        } else if (disPoint.x >= 0 && disPoint.y < 0) {
            // 计算与x正方向的夹角
            originRadian = Math.asin(disPoint.x / distance);
            originRadian = originRadian + Math.PI * 3 / 2;
        }

        // 弧度换算成角度
        originDegree = Utils.radianToDegree(originRadian);
        resultDegree = originDegree + degree;

        // 角度转弧度
        resultRadian = Utils.degreeToRadian(resultDegree);

        resultPoint.x = (int) Math.round(distance * Math.cos(resultRadian));
        resultPoint.y = (int) Math.round(distance * Math.sin(resultRadian));
        resultPoint.x += center.x;
        resultPoint.y += center.y;

        return resultPoint;
    }

    /**
     * 根据点击的位置判断是否点中控制旋转，缩放的图片， 初略的计算
     *
     * @param x
     * @param y
     * @return
     */
    private int judgeStatus(float x, float y) {
        PointF touchPoint = new PointF(x, y);
        PointF controlPointF = new PointF(mControlPoint);

        // 点击的点到控制旋转，缩放点的距离
        float disToControl = Utils.distance4PointF(touchPoint, controlPointF);
        // 如果两者之间的距离小于控制图标的宽度，高度的最小值，则认为点中了控制图标
        if (disToControl < Math.min(mControlDrawableWidth / 2, mControlDrawableHeight / 2)) {
            return STATUS_ROTATE_ZOOM;
        }

        float disToEdit = Utils.distance4PointF(touchPoint, new PointF(mEditPoint));
        if (disToEdit < Math.min(mEditDrawableWidth / 2, mEditDrawableHeight / 2)) {
            return STATUS_EDIT;
        }

        float disToDelete = Utils.distance4PointF(touchPoint, new PointF(mDeletePoint));
        if (disToDelete < Math.min(mDeleteDrawableWidth / 2, mDeleteDrawableHeight / 2)) {
            return STATUS_DELETE;
        }

        return STATUS_DRAG;
    }

    public float getImageDegree() {
        return mDegree;
    }

    /**
     * 设置图片旋转角度
     *
     * @param degree
     */
    public void setImageDegree(float degree) {
        if (this.mDegree != degree) {
            this.mDegree = degree;
            transformDraw();
        }
    }

    public float getImageScale() {
        return mScale;
    }

    /**
     * 设置图片缩放比例
     *
     * @param scale
     */
    public void setImageScale(float scale) {
        if (this.mScale != scale) {
            this.mScale = scale;
            transformDraw();
        }
    }

    public Drawable getControlDrawable() {
        return mControlDrawable;
    }

    public int getImageX() {
        return this.mX;
    }

    public int getImageY() {
        return this.mY;
    }

    public int getImageWidth() {
        return mFirstGifFrame.getWidth();
    }

    public int getImageHeight() {
        return mFirstGifFrame.getHeight();
    }

    public int getViewX() {
        return getLeft() + mDeleteDrawableWidth + mFramePadding;
    }

    public int getViewY() {
        return getTop() + mDeleteDrawableHeight + mFramePadding;
    }

    /**
     * 设置控制图标
     *
     * @param drawable
     */
    public void setControlDrawable(Drawable drawable) {
        this.mControlDrawable = drawable;
        mControlDrawableWidth = drawable.getIntrinsicWidth();
        mControlDrawableHeight = drawable.getIntrinsicHeight();
        transformDraw();
    }

    public int getFramePadding() {
        return mFramePadding;
    }

    public void setFramePadding(int framePadding) {
        if (this.mFramePadding == framePadding)
            return;
        this.mFramePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, framePadding, mDisplayMetrics);
        transformDraw();
    }

    public int getFrameColor() {
        return mFrameColor;
    }

    public void setFrameColor(int frameColor) {
        if (this.mFrameColor == frameColor)
            return;
        this.mFrameColor = frameColor;
        mPaint.setColor(frameColor);
        invalidate();
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        if (this.mFrameWidth == frameWidth)
            return;
        this.mFrameWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, frameWidth, mDisplayMetrics);
        mPaint.setStrokeWidth(frameWidth);
        invalidate();
    }

    /**
     * 设置控制图标的位置, 设置的值只能选择LEFT_TOP ，RIGHT_TOP， RIGHT_BOTTOM，LEFT_BOTTOM
     *
     * @param location
     */
    public void setControlLocation(int location) {
        if (this.mControlLocation == location)
            return;
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
     * 设置图片中心点位置，相对于父布局而言
     *
     * @param mCenterPoint
     */
    public void setCenterPoint(PointF mCenterPoint) {
        this.mCenterPoint = mCenterPoint;
        adjustLayout();
    }

    public boolean isEditable() {
        return mIsEditable;
    }

    /**
     * 设置是否处于可缩放，平移，旋转状态
     *
     * @param isEditable
     */
    public void setEditable(boolean isEditable) {
        this.mIsEditable = isEditable;
        invalidate();
    }

    public float getCenterX() {
        return mCenterPoint.x;
    }

    public float getCenterY() {
        return mCenterPoint.y;
    }

    private float mCenterX, mCenterY;

    public void setCenterX(float x) {
        mCenterX = x;
    }

    public void setCenterY(float y) {
        mCenterY = y;
    }

    public void showDelete(boolean showDelete) {
        mShowDelete = showDelete;
    }

    public void showEdit(boolean showEdit) {
        mShowEdit = showEdit;
    }

    public void setStartTime(long startTime, long endTime) {
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void startGifPlaying() {
        if (mGifFrameLoader == null) {
            return;
        }
        mGifFrameLoader.start();
    }

    public void pauseGifPlaying() {
        mGifFrameLoader.stop();
    }

    public void stopGifPlaying() {
        pauseGifPlaying();
        mGifFrameLoader.setNextStartFromFirstFrame();
    }

    public boolean isRunning() {
        if (!mIsGifSticker || mGifFrameLoader == null) {
            return false;
        }
        return mGifFrameLoader.isRunning();
    }

    private GifFrameLoader.FrameCallback mFrameCallback = new GifFrameLoader.FrameCallback() {
        @Override
        public void onFrameReady() {
            invalidate();
        }
    };
}
