package com.example.yuansen.baiduimaptest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.example.yuansen.baiduimaptest.model.MarkObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuansen on 2015/8/20.
 */
public class TouchImageView extends ImageView {

    Matrix matrix;

    private List<MarkObject> markList = new ArrayList<MarkObject>();
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 3f;
    float[] m;

    private Bitmap mBitmap;

    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;


    ScaleGestureDetector mScaleDetector;  //比例解析
    GestureDetector     mGestureDetector;  //手势解析

    Context context;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context,new ZoomGeture());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("onTouch", "ACTION_DOWN");
                        last.set(curr);
                        start.set(last);
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.e("onTouch", "ACTION_MOVE");
                        if (mode == DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.e("onTouch", "ACTION_UP");
                        mode = NONE;
                        Boolean btnMaker = false;
                        btnMaker = clickAction(event);
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < CLICK && yDiff < CLICK && !btnMaker)
                            performClick();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        Log.e("onTouch", "ACTION_POINT_UP");
                        mode = NONE;
                        break;
                }

                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }

        });
    }

    // 处理点击标记的事件
    private Boolean clickAction(MotionEvent event) {

        int clickX = (int) event.getX();
        int clickY = (int) event.getY();

        for (MarkObject object : markList) {
            Bitmap location = object.getmBitmap();
            int objX = (int) object.getCurrentX();
            int objY = (int) object.getCurrentY();
            // 判断当前object是否包含触摸点，在这里为了得到更好的点击效果，我将标记的区域放大了
            if (objX - location.getWidth() < clickX
                    && objX + location.getWidth() > clickX
                    && objY + location.getHeight() > clickY
                    && objY - location.getHeight() < clickY) {
                if (object.getMarkListener() != null) {
                    object.getMarkListener().onMarkClick(clickX, clickY);
                    return true;
                }
                break;
            }

        }
        return false;
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    public void addMark(MarkObject markObject) {
        markList.add(markObject);
    }
    public void removeMark(MarkObject markObject){
        markList.remove(markObject);
    }

    private class ZoomGeture extends GestureDetector.SimpleOnGestureListener{
        @Override //双击
        public boolean onDoubleTap(MotionEvent e) {
            Log.e("ZoomGeture", "--onDoubleTap---");
            float mScaleFactor =1f ;
            float origScale = saveScale; //当前绝对放大倍数
            if (saveScale < maxScale) {   //如果预想放大结果超过最大倍数、按最大倍数重新计算相对放大比例
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale == maxScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
            }
            else {
                matrix.postScale(mScaleFactor, mScaleFactor, e.getX(), e.getY()); //从两手中间放大
            }
            Log.e("view大小",""+viewWidth+" "+viewHeight);
            Log.e("orig大小",""+origWidth+" "+origHeight);
            fixTrans();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.e("ZoomGeture", "--onDoubleTapEvent---");
            return super.onDoubleTapEvent(e);
        }
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();  //拖动相对放大比例
            float origScale = saveScale; //当前绝对放大倍数
            saveScale *= mScaleFactor;  //预想绝对放大结果
            if (saveScale > maxScale) {   //如果预想放大结果超过最大倍数、按最大倍数重新计算相对放大比例
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            Log.e("onScale","postScale");
            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
            }
            else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY()); //从两手中间放大
            }

            //Log.e("view大小",""+viewWidth+" "+viewHeight);
            //Log.e("orig大小",""+origWidth+" "+origHeight);

            //Log.e("点击位置Focus", "" + detector.getFocusX() + " " + detector.getFocusY());
            //Log.e("点击位置CurrentSpan", "" + detector.getCurrentSpanX() + " " + detector.getCurrentSpanY());
            fixTrans();
            return true;
        }
    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0){
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("TouchImageView","onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        Log.e("oldMeasuredView大小:",""+oldMeasuredWidth+" "+oldMeasuredHeight);

        if (saveScale == 1) {
            //Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            minScale = Math.min(scaleX,scaleY);
            scaleX = 1f;
            scaleY = 1f;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            saveScale = scale;

            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);


            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        Paint paint = new Paint();
        super.onDraw(canvas);

        //画边框
        Rect rec = canvas.getClipBounds();
        rec.bottom--;
        rec.right--;
       // Paint paint = new Paint();
        paint.setColor(Color.GRAY);   //颜色
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(rec, paint);


        float values[] = new float[9];
        matrix.getValues(values);

        float xc,yc,spanx,spany,bmpx,bmpy;
        xc = 0 - values[2] ;
        yc = 0 - values[5];
        spanx = viewWidth ;
        spany = viewHeight ;

        for (MarkObject markObject:markList){
            Bitmap location = markObject.getmBitmap();
            bmpx = markObject.getMapX() * values[0];
            bmpy = markObject.getMapY() * values[0];
            Log.e("当前屏幕",""+xc+" "+yc+" span:"+(xc+spanx)+" "+(yc+spany));
            Log.e("当前位置",""+bmpx+" "+bmpy);
            if ( bmpx >xc && bmpx <xc+spanx && bmpy >yc && bmpy<yc+spany){
                    canvas.drawBitmap(location,bmpx -xc-location.getWidth()/2,bmpy-yc-location.getHeight(),paint);
                    markObject.setCurrent(bmpx -xc-location.getWidth()/2,bmpy-yc-location.getHeight());
            }else {
                markObject.setCurrent(-100,-100);//触摸不到的点
            }
        }

    }
/*
    public void moveImage(float x,float y){
        matrix.reset();
        matrix.setScale(saveScale, saveScale);
        matrix.postTranslate(viewWidth / 2, viewHeight / 2);
        matrix.postTranslate(-x * saveScale, -y * saveScale);
        fixTrans();
        setImageMatrix(matrix);
        invalidate();
    }
*/
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (this.mBitmap == null)
            this.mBitmap = bm;
    }
    private Bitmap setMarker(Bitmap bmp,float x,float y){
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marka).copy(Bitmap.Config.ARGB_8888, true), x, y, paint);
        return bmp;
    }

}