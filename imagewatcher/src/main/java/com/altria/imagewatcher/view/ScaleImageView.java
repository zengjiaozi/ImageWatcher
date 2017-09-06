package com.altria.imagewatcher.view;

import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.altria.imagewatcher.R;
import com.altria.imagewatcher.base.OnExitListener;
import com.altria.imagewatcher.entity.ImageTag;
import com.altria.imagewatcher.entity.ImageViewInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by altria on 17-6-13.
 */

public class ScaleImageView extends android.support.v7.widget.AppCompatImageView {
    private float mWidth;
    private float mHeight;
    private float imageWidth;
    private float imageHeight;
    private Matrix originalMatrix;
    private float limit = 10F;
    private GestureDetector gestureDetector;
    private OnExitListener onExitListener;
    private View bgView;

    public ScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setDrawingCacheEnabled(true);
        //缩放类型
        setScaleType(ScaleType.FIT_CENTER);
        //触摸事件监听
        setOnTouchListener(new MatrixOnTouchListener());

        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap drawingCache = getDrawingCache();
        if (drawingCache != null) {
            imageWidth = drawingCache.getWidth();
            imageHeight = drawingCache.getHeight();

            setDrawingCacheEnabled(false);
        }
        super.onDraw(canvas);
    }

    private class MatrixOnTouchListener implements OnTouchListener {
        /**
         * 拖拉照片模式
         */
        private static final int MODE_DRAG = 1;
        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 2;
        //下拉缩放照片模式
        private static final int MODE_DRAG_ZOOM = 3;
        //退出界限
        private float exitLimit = 0.7F;
        /**
         * 最大缩放级别
         */
        float mMaxScale = 6;
        //最小缩放级别
        float mMinScale = 0.3F;
        /**
         * 双击时的缩放级别
         */
        float mDoubleClickScale = 2;
        //当前图片模式
        private int mMode = 0;
        //手指按下第一点
        private PointF pressPoint;
        //缩放中心点
        private PointF center;
        //手指按下时图片的矩阵
        private Matrix currentMatrix = new Matrix();
        //更新矩阵
        private Matrix updateMatrix = new Matrix();
        //双指按下时之前的距离
        private float baseLength;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mMode = MODE_DRAG;

                    pressPoint = new PointF(event.getX(), event.getY());
                    currentMatrix.set(getImageMatrix());
                    updateMatrix.set(getImageMatrix());
                    setScaleType(ScaleType.MATRIX);

                    if (originalMatrix == null) {
                        originalMatrix = new Matrix();
                        originalMatrix.set(getImageMatrix());
                    }

                    //请求所有父控件及祖宗控件不要拦截事件
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mMode = MODE_ZOOM;

                    float xOffset = event.getX(1) - event.getX(0);
                    float yOffset = event.getY(1) - event.getY(0);
                    baseLength = (float) Math.sqrt(xOffset * xOffset + yOffset * yOffset);
                    center = new PointF((event.getX(0) + event.getX(1)) / 2F, (event.getY(0) + event.getY(1)) / 2F);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2) {
                        float xOffsetMove = event.getX(1) - event.getX(0);
                        float yOffsetMove = event.getY(1) - event.getY(0);

                        float currentLength = (float) Math.sqrt(xOffsetMove * xOffsetMove + yOffsetMove * yOffsetMove);
                        float scale = currentLength / baseLength;

                        updateMatrix.set(currentMatrix);
                        updateMatrix.postScale(scale, scale, center.x, center.y);
                        setImageMatrix(updateMatrix);
                    } else if (mMode == MODE_DRAG || mMode == MODE_DRAG_ZOOM) {
                        float dX = event.getX() - pressPoint.x;
                        float dY = event.getY() - pressPoint.y;
              //          Log.e("TAG","dy:"+dY+" dx:"+dX);
                        if (dY >= limit) {//双击冲突
                            if (mMode == MODE_DRAG_ZOOM || !checkScale()) {
                                if (!isScaleLarge()) {
                                    mMode = MODE_DRAG_ZOOM;

                                    updateMatrix.set(currentMatrix);
                                    float scale = (imageHeight / 2F - dY) / (imageHeight / 2F);
                                    scale = scale < mMinScale ? mMinScale : scale;
                                    scale = scale > 1F ? 1F : scale;
                                    updateMatrix.postScale(scale, scale, event.getX(), event.getY());

                                    //背景颜色透明度变化
                                    bgView.setBackgroundColor(mColorEvaluator.evaluate(scale,
                                            getResources().getColor(R.color.image_bg_start),
                                            getResources().getColor(R.color.image_bg_end)));

                                    setImageMatrix(updateMatrix);
                                }
                            }
                        }

                        //左右滑动切换
                        if (mMode == MODE_DRAG && isScaleLarge() && !checkDxBound()){
                            updateMatrix.set(currentMatrix);
                            updateMatrix.postTranslate(dX, dY);
                            setImageMatrix(updateMatrix);
                        }else if (mMode != MODE_DRAG_ZOOM && Math.abs(dX) >= limit) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                            //复原
                            updateMatrix.set(originalMatrix);
                            setImageMatrix(updateMatrix);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //判断当前缩放级别是否小于原始缩放级别
                    float[] values = new float[9];
                    getImageMatrix().getValues(values);
                    float currentScale = values[Matrix.MSCALE_X];
                    originalMatrix.getValues(values);

                    if (currentScale < exitLimit  * values[Matrix.MSCALE_X] && mMode == MODE_DRAG_ZOOM) {
                        //退出
                        onExitListener.exit();
                    }else if (currentScale <= values[Matrix.MSCALE_X]){
                        //复原
                        updateMatrix.set(originalMatrix);
                        setImageMatrix(updateMatrix);
                        bgView.setBackgroundColor(getResources().getColor(R.color.image_bg_end));
                    }
                    mMode = 0;
                    break;
            }
            return gestureDetector.onTouchEvent(event);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            //捕获Down事件
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (checkScale()) {
                setImageMatrix(originalMatrix);
            }
            return true;
        }
    }

    /**
     * X轴方向是否超出边界
     *
     * @return
     */
    private boolean checkDxBound() {
        boolean result = false;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float currentMatransX = values[Matrix.MTRANS_X];
        float currentMscaleX = values[Matrix.MSCALE_X];
        originalMatrix.getValues(values);
        if (currentMatransX >= 0 || currentMatransX <= -(imageWidth * (currentMscaleX - values[Matrix.MSCALE_X]) - mWidth))
            result = true;

        return result;
    }

    /**
     * 当前照片是否缩放了
     *
     * @return
     */
    private boolean checkScale() {
        boolean result = false;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float currentScale = values[Matrix.MSCALE_X];
        originalMatrix.getValues(values);
     //   Log.e("TAG", currentScale +" "+values[Matrix.MSCALE_X]);
        if (currentScale != values[Matrix.MSCALE_X]) {
            result = true;
        }
        return result;
    }

    /**
     * 图片是否处于放大状态
     */
    private boolean isScaleLarge(){
        boolean result = false;
        float[] values = new float[9];
        getImageMatrix().getValues(values);
        float currentScale = values[Matrix.MSCALE_X];
        originalMatrix.getValues(values);
    //    Log.e("TAG", "scale:"+values[Matrix.MSCALE_X]);
        return currentScale > values[Matrix.MSCALE_X];
    }

    TypeEvaluator<Integer> mColorEvaluator = new TypeEvaluator<Integer>() {
        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            int startColor = startValue;
            int endColor = endValue;

            int alpha = (int) (Color.alpha(startColor) + fraction * (Color.alpha(endColor) - Color.alpha(startColor)));
            int red = (int) (Color.red(startColor) + fraction * (Color.red(endColor) - Color.red(startColor)));
            int green = (int) (Color.green(startColor) + fraction * (Color.green(endColor) - Color.green(startColor)));
            int blue = (int) (Color.blue(startColor) + fraction * (Color.blue(endColor) - Color.blue(startColor)));
            return Color.argb(alpha, red, green, blue);
        }
    };

    public void setExitListener(OnExitListener onExitListener) {
        this.onExitListener = onExitListener;
    }

    /**
     * 背景
     * @param bgView
     */
    public void setBgView(View bgView) {
        this.bgView = bgView;
    }
}
