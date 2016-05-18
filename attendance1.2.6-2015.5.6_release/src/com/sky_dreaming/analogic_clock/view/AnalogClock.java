package com.sky_dreaming.analogic_clock.view;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

import java.io.InputStream;
import java.util.TimeZone;

import com.ebanswers.attendance.R;

@RemoteView
public class AnalogClock extends View {
    private Time mCalendar;
    private Bitmap mDial;
    private BitmapDrawable mDialDrawable;
    private BitmapDrawable mHourHandDrawable;
    private BitmapDrawable mMinuteHandDrawable;
    private BitmapDrawable mSecondHandDrawable;
    private int mDialWidth;
    private int mDialHeight;
    private boolean mAttached = false;
    private float mHours;
    private float mMinutes;
    private float mSeconds;
    private String time_zone;
    
    public String getTime_zone() {
		return time_zone;
	}

	public void setTime_zone(String timeZone) {
		time_zone = timeZone;
	}
	
	/**
     * 标志时间、时区、时钟布局大小等是否有改变
     */
    private boolean mChanged;
    
    /**
     * 线程队列管理,消息传递和处理机制
     */
    private Handler loopHandler = new Handler();
    
    /**
     * 标志页面刷新线程尚未执行
     */
    private boolean isRun = false;
    
    /**
     * 时钟运行
     */
    private void run()
    {
    	/**
    	 * 将线程加入队列
    	 */
    	loopHandler.post(tickRunnable);
    }
    private Runnable tickRunnable = new Runnable() {   
        public void run() {
        	/**
        	 * 在非UI线程调用，强制刷新界面
        	 */
        	postInvalidate();
        	/**
        	 * 将线程加入队列，1000毫秒后启动
        	 */
            loopHandler.postDelayed(tickRunnable, 1000);   
        }   
    };   
	/**
	 * 构造方法
	 */
    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        
        /**
         * 初始化日历对象
         */
        mCalendar = new Time();
        time_zone = mCalendar.timezone;
        
        Resources r = this.getContext().getResources();
		InputStream is =null;
		
		/**
		 * 初始化表盘，时针，分针， 秒针
		 */
		is = r.openRawResource(R.drawable.clock);
		mDialDrawable = new BitmapDrawable(is);
		mDial = mDialDrawable.getBitmap();
		
		is = r.openRawResource(R.drawable.hands);
		mHourHandDrawable = new BitmapDrawable(is);
		
		is = r.openRawResource(R.drawable.hands);
		mMinuteHandDrawable = new BitmapDrawable(is);
		
		is = r.openRawResource(R.drawable.hands);
		mSecondHandDrawable = new BitmapDrawable(is);
		
        /**
         * 获取表盘有效像素宽高
         */
        mDialWidth = mDialDrawable.getIntrinsicWidth();
        mDialHeight = mDialDrawable.getIntrinsicHeight();
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter, null, loopHandler);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
                resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isRun)
        {
        	run();
        	isRun = true;
        	return;
        }
        onTimeChanged();
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = mDial.getWidth();
        int availableHeight = mDial.getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2; 

        final Drawable dial = mDialDrawable;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();
        
        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                                   (float) availableHeight / (float) h);
            canvas.save();

            canvas.scale(scale, scale, x, y);
        }

        if (changed) {

            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();

        canvas.rotate(mHours / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHandDrawable;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h * 2 / 3), x + (w / 2), y + (h / 3));
        }
        hourHand.draw(canvas);

        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHandDrawable;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h  * 4 / 5), x + (w / 2), y + (h / 5));
        }
        minuteHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mSeconds / 60.0f * 360.0f, x, y);

        final Drawable scendHand = mSecondHandDrawable;
        if (changed) {
            w = scendHand.getIntrinsicWidth();
            h = scendHand.getIntrinsicHeight();
            scendHand.setBounds(x - (w / 2), y - h, x + (w / 2), y);
        }
        scendHand.draw(canvas);
        canvas.restore();

        if (scaled) {
            canvas.restore();
        }
    }

    private void onTimeChanged() {

        mCalendar.setToNow();

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        int second = mCalendar.second;
        
        mSeconds = second;
        mMinutes = minute + second / 60.0f;
        mHours = hour + mMinutes / 60.0f;
        
        mChanged = true;
    }
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String tz = "";
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
                time_zone = mCalendar.timezone;
            }
            Log.i("********************",tz);
            onTimeChanged();
            invalidate();
        }
    };
}
