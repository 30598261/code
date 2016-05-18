package com.ebanswers.attendance;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.ebanswers.attendance.R;

public class ScrollForeverTextView extends TextView implements Runnable {
	private WindowManager.LayoutParams wmParams;
	private WindowManager wm;
	View	win;
	private int circleTimes = 1;
	private int hasCircled = 0;
	public int currentScrollPos = -1300;
	private int circleSpeed = 1000;
	public int textWidth = 0;
	private boolean isMeasured = false;
	private Handler handler = new Handler();
	private boolean flag = false;
	private boolean isRun = false;
	private String msg = null;
	Timer timer=null;;

	public ScrollForeverTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scrollTo(currentScrollPos,0);
		// TODO Auto-generated constructor stub
	}
	
		
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(!isMeasured){
			getTextWidth();
			isMeasured = true;
		}
	}
	
	@Override
	public void setVisibility(int visibility){
		//二次进入时初始化成员变量
		flag = false;
		isMeasured = false;
		this.hasCircled = 0;
		super.setVisibility(visibility);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
	/**
	 * 获取文本显示长度
	 */
	private void getTextWidth() {
		Paint paint = this.getPaint();
		String str = this.getText().toString();
		if(str == null) {
			textWidth = 0;
		}		
		textWidth = (int)paint.measureText(str);
	}
	/**
	 * 设置滚动次数，达到次数后设置不可见
	 * @param circleTimes
	 */
	public void setCircleTimes(int circleTimes){
		this.circleTimes = circleTimes;
	}

	public void setSpeed(int speed) {
		this.circleSpeed = speed;
	}
	 
	static ScheduledThreadPoolExecutor stpe = null;
	static boolean runTag = false;
	public void startScrollShow() {
		stopScroll();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		runTag = true;
		getTextWidth();
		currentScrollPos = -1300;
		isRun = true;
		flag = false;
		hasCircled = 0;
		
		/*
		if(timer!=null)
		{
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
		public void run() {
			// TODO Auto-generated method stub
			//起始滚动位置
			
			currentScrollPos += 2;
			scrollTo(currentScrollPos,0);
			//判断滚动一次
			if(currentScrollPos >= textWidth) {
				//从屏幕右侧开始出现
				currentScrollPos = -1300;	
			}
			}
		}, 1000, 20);
		 */
	    
         
         
	
		/*
		try   {     
            if(stpe!=null)
    		{
    			stpe.shutdownNow();

    			try {
					stpe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
  
    			stpe = null;
    		}
         
            stpe = new ScheduledThreadPoolExecutor(1);  
    	    stpe.scheduleWithFixedDelay(new Runnable(){
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    			//	System.out.println("###   正常运行  in ### ");
    				currentScrollPos += 2;
    				
    				//currentScrollPos =getScrollX()+2;
    				scrollTo(currentScrollPos,0);

    				//判断滚动一次
    				if(currentScrollPos >= textWidth) {
    					//从屏幕右侧开始出现
    					currentScrollPos = -1300;
    				}
    			//	System.out.println("###   正常运行  out ### ");
    			}
    	    },  1000, 20, TimeUnit.MILLISECONDS);
    	    
            }   catch(RuntimeException   ex)   {     
               System.out.println("##### 捕获到异常了--------------------");  
        }     		
        */
		
		/*
		new Thread(){
			public void run() {
				try   {     
		            if(stpe!=null)
		    		{
		    			stpe.shutdownNow();

		    			try {
							stpe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}   
		  
		    			stpe = null;
		    		}
		         
		            stpe = new ScheduledThreadPoolExecutor(1);  
		    	    stpe.scheduleWithFixedDelay(new Runnable(){
		    			@Override
		    			public void run() {
		    				// TODO Auto-generated method stub
		    			//	System.out.println("###   正常运行  in ### ");
		    				currentScrollPos += 2;
		    				
		    				//currentScrollPos =getScrollX()+2;
		    				scrollTo(currentScrollPos,0);

		    				//判断滚动一次
		    				if(currentScrollPos >= textWidth) {
		    					//从屏幕右侧开始出现
		    					currentScrollPos = -1300;
		    				}
		    			//	System.out.println("###   正常运行  out ### ");
		    			}
		    	    },  1000, 20, TimeUnit.MILLISECONDS);
		    	    
		            }   catch(RuntimeException   ex)   {     
		               System.out.println("##### 捕获到异常了--------------------");  
		        }     		
			};
		}.start();
		*/
		
		new Thread(){
			public void run() {
				while(runTag)
				{
    				currentScrollPos += 2;
    				scrollTo(currentScrollPos,0);

    				//判断滚动一次
    				if(currentScrollPos >= textWidth) {
    					//从屏幕右侧开始出现
    					currentScrollPos = -1300;
    				}
    				
    				try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	void stopScroll() {
		//用现成播放消息
		
		/*
		//用定时器播放插播消息
		if(timer!=null)
		{
			timer.cancel();
			timer = null;
		}
		*/
		runTag = false;
		/*
		if(stpe!=null)
		{
			stpe.shutdownNow();

			try {
				stpe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   

			stpe = null;
		}
		*/
		isRun = false;
		currentScrollPos = -1300;
		
		scrollTo(currentScrollPos,0);
		
	}
	
	void SetCurrentScrollPos(int x)
	{
		currentScrollPos = x;
		scrollTo(currentScrollPos,0);
	}
	
	void SetMsg(String msgStr)
	{
		this.msg = msgStr;
	}
}
