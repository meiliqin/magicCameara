package com.martin.ads.omoshiroilib.ui.module;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.martin.ads.omoshiroilib.R;
import com.martin.ads.omoshiroilib.util.DisplayUtils;

public class RecordButton
        extends View {

    private static final String TAG = "RecordButton";
    public static final long TIME_TO_START_RECORD = 500L;
    public static final float TIME_LIMIT_IN_MILS = 10000.0F;
    public static final float PROGRESS_LIM_TO_FINISH_STARTING_ANIM = 0.1F;
    private int BOUNDING_BOX_SIZE;
    private int OUT_CIRCLE_WIDTH;
    private int OUTER_CIRCLE_WIDTH_INC;
    private float INNER_CIRCLE_RADIUS;

    private TouchTimeHandler touchTimeHandler;
    private boolean touchable;
    private boolean recordable;

    private Paint centerCirclePaint;
    private Paint outBlackCirclePaint;
    private Paint outMostBlackCirclePaint;
    private float innerCircleRadiusToDraw;
    private RectF outMostCircleRect;
    private float outBlackCircleRadius;
    private float outMostBlackCircleRadius;
    private int colorWhite;
    private int colorRecord;
    private int colorWhiteP60;
    private int colorBlackP40;
    private int colorBlackP80;
    private int colorTranslucent;
    //top
    private float startAngle270;
    private float percentInDegree;
    private float centerX;
    private float centerY;
    private Paint processBarPaint;
    private Paint outMostWhiteCirclePaint;
    private Paint translucentPaint;
    private Context mContext;
    private int translucentCircleRadius = 0;
    private float outMostCircleRadius;
    private float innerCircleRadiusWhenRecord;
    private long btnPressTime;
    private int outBlackCircleRadiusInc;
    private ClickListener clickListener;
    private int recordState;
    public static final int RECORD_NOT_STARTED=0;
    public static final int RECORD_STARTED=1;
    public static final int RECORD_ENDED=2;

    private TouchTimeHandler.Task updateUITask = new TouchTimeHandler.Task() {
        public void run() {
            long timeLapse = System.currentTimeMillis() - btnPressTime;
            float percent = (float) (timeLapse - TIME_TO_START_RECORD) / TIME_LIMIT_IN_MILS;
            if (timeLapse >= TIME_TO_START_RECORD) {
                synchronized (RecordButton.this){
                    if(recordState==RECORD_NOT_STARTED){
                        recordState = RECORD_STARTED;
                        if(clickListener!=null){
                            clickListener.onLongClickStart();
                        }
                    }
                }
                if(!recordable) return;
                centerCirclePaint.setColor(colorRecord);
                outMostWhiteCirclePaint.setColor(colorWhite);
                percentInDegree = (360.0F * percent);
                if (percent <= 1.0F) {
                    if (percent <= PROGRESS_LIM_TO_FINISH_STARTING_ANIM) {
                        float calPercent=percent / PROGRESS_LIM_TO_FINISH_STARTING_ANIM;
                        float outIncDis = outBlackCircleRadiusInc * calPercent;
                        float curOutCircleWidth = OUT_CIRCLE_WIDTH + OUTER_CIRCLE_WIDTH_INC * calPercent;
                        processBarPaint.setStrokeWidth(curOutCircleWidth);
                        outMostWhiteCirclePaint.setStrokeWidth(curOutCircleWidth);
                        outBlackCircleRadius = (outMostCircleRadius + outIncDis - curOutCircleWidth / 2.0F);
                        outMostBlackCircleRadius = (curOutCircleWidth / 2.0F + (outMostCircleRadius + outIncDis));
                        outMostCircleRect = new RectF(centerX - outMostCircleRadius - outIncDis, centerY - outMostCircleRadius - outIncDis, centerX + outMostCircleRadius + outIncDis, centerY + outMostCircleRadius + outIncDis);
                        translucentCircleRadius = (int) (outIncDis + outMostCircleRadius);
                        innerCircleRadiusToDraw = calPercent * innerCircleRadiusWhenRecord;
                    }
                    invalidate();
                }else{
                    reset();
                }
            }
        }
    };

    public RecordButton(Context paramContext) {
        super(paramContext);
        mContext = paramContext;
        init();
    }

    public RecordButton(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        mContext = paramContext;
        init();
    }

    public RecordButton(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        mContext = paramContext;
        init();
    }

    void init() {
        touchable=recordable=true;
        BOUNDING_BOX_SIZE = DisplayUtils.getRefLength(mContext,100.0F);
        OUT_CIRCLE_WIDTH = DisplayUtils.getRefLength(mContext,2.3F);
        OUTER_CIRCLE_WIDTH_INC=DisplayUtils.getRefLength(mContext,4.3F);
        INNER_CIRCLE_RADIUS = DisplayUtils.getRefLength(mContext,32.0F);
        colorRecord = getResources().getColor(R.color.app_color);
        colorWhite = getResources().getColor(R.color.white);
        colorWhiteP60 = getResources().getColor(R.color.white_sixty_percent);
        colorBlackP40 = getResources().getColor(R.color.black_forty_percent);
        colorBlackP80 = getResources().getColor(R.color.black_eighty_percent);
        colorTranslucent = getResources().getColor(R.color.circle_shallow_translucent_bg);
        processBarPaint = new Paint();
        processBarPaint.setColor(colorRecord);
        processBarPaint.setAntiAlias(true);
        processBarPaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        processBarPaint.setStyle(Style.STROKE);
        processBarPaint.setStrokeCap(Cap.ROUND);
        outMostWhiteCirclePaint = new Paint();
        outMostWhiteCirclePaint.setColor(colorWhite);
        outMostWhiteCirclePaint.setAntiAlias(true);
        outMostWhiteCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outMostWhiteCirclePaint.setStyle(Style.STROKE);
        centerCirclePaint = new Paint();
        centerCirclePaint.setColor(colorWhiteP60);
        centerCirclePaint.setAntiAlias(true);
        centerCirclePaint.setStyle(Style.FILL_AND_STROKE);
        outBlackCirclePaint = new Paint();
        outBlackCirclePaint.setColor(colorBlackP40);
        outBlackCirclePaint.setAntiAlias(true);
        outBlackCirclePaint.setStyle(Style.STROKE);
        outBlackCirclePaint.setStrokeWidth(1.0F);
        outMostBlackCirclePaint = new Paint();
        outMostBlackCirclePaint.setColor(colorBlackP80);
        outMostBlackCirclePaint.setAntiAlias(true);
        outMostBlackCirclePaint.setStyle(Style.STROKE);
        outMostBlackCirclePaint.setStrokeWidth(1.0F);
        translucentPaint = new Paint();
        translucentPaint.setColor(colorTranslucent);
        translucentPaint.setAntiAlias(true);
        translucentPaint.setStyle(Style.FILL_AND_STROKE);
        centerX = (BOUNDING_BOX_SIZE / 2);
        centerY = (BOUNDING_BOX_SIZE / 2);
        outMostCircleRadius = DisplayUtils.getRefLength(mContext,37.0F);
        outBlackCircleRadiusInc = DisplayUtils.getRefLength(mContext,7.0F);
        innerCircleRadiusWhenRecord = DisplayUtils.getRefLength(mContext,35.0F);
        innerCircleRadiusToDraw = INNER_CIRCLE_RADIUS;
        outBlackCircleRadius = (outMostCircleRadius - OUT_CIRCLE_WIDTH / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + OUT_CIRCLE_WIDTH / 2.0F);
        startAngle270 = 270.0F;
        percentInDegree = 0.0F;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        touchTimeHandler = new TouchTimeHandler(Looper.getMainLooper(), updateUITask);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, translucentCircleRadius, translucentPaint);

        //center white-p40 circle
        canvas.drawCircle(centerX, centerY, innerCircleRadiusToDraw, centerCirclePaint);

        //static out-most white circle
        canvas.drawArc(outMostCircleRect, startAngle270, 360.0F, false, outMostWhiteCirclePaint);

        //progress bar
        canvas.drawArc(outMostCircleRect, startAngle270, percentInDegree, false, processBarPaint);

        canvas.drawCircle(centerX, centerY, outBlackCircleRadius, outBlackCirclePaint);
        canvas.drawCircle(centerX, centerY, outMostBlackCircleRadius, outMostBlackCirclePaint);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(BOUNDING_BOX_SIZE, BOUNDING_BOX_SIZE);
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (!touchable) {
            return false;
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: down");
                startTicking();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: up");
                reset();
                break;

        }
        return true;
    }

    public void reset() {
        //Log.d(TAG, "reset: "+recordState);
        synchronized (RecordButton.this){
            if(recordState==RECORD_STARTED){
                if(clickListener!=null)
                    clickListener.onLongClickEnd();
                recordState = RECORD_ENDED;
            }else if(recordState==RECORD_ENDED){
                recordState=RECORD_NOT_STARTED;
            }else{
                if(clickListener!=null)
                    clickListener.onClick();
            }
        }
        touchTimeHandler.clearMsg();
        percentInDegree = 0.0F;
        centerCirclePaint.setColor(colorWhiteP60);
        outMostWhiteCirclePaint.setColor(colorWhite);
        innerCircleRadiusToDraw = INNER_CIRCLE_RADIUS;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        translucentCircleRadius = 0;
        processBarPaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outMostWhiteCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outBlackCircleRadius = (outMostCircleRadius - OUT_CIRCLE_WIDTH / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + OUT_CIRCLE_WIDTH / 2.0F);
        invalidate();
    }

    public boolean isTouchable() {
        return touchable;
    }

    public boolean isRecordable() {
        return recordable;
    }

    public void setRecordable(boolean recordable) {
        this.recordable = recordable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    public void startTicking() {
        synchronized (RecordButton.this){
            if(recordState!=RECORD_NOT_STARTED)
                recordState=RECORD_NOT_STARTED;
        }
        btnPressTime = System.currentTimeMillis();
        touchTimeHandler.sendLoopMsg(0L, 16L);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick();
        void onLongClickStart();
        void onLongClickEnd();
    }
}
