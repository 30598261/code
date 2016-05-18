package com.ebanswers.attendance;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import com.ebanswers.attendance.R;

public class TableShowView extends View {
	// 如果是想显示歌词则继承TextView并复写ondraw方法。
	// 开启一个线程不断的调用ondraw方法去更改你所写的继承自TextView的内容
	// 这里随便写了个集成自view的= =这个不是重点
	Context						c;
	WindowManager				mWM;		// WindowManager
	WindowManager.LayoutParams	mWMParams;	// WindowManager参数
	View						win;
	public ScrollForeverTextView m_textView;
	//private boolean m_mode = false;

	public TableShowView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		c = context;
	}
	
	public void fun(int x, int y, int width, int height, boolean format) {
		// 设置载入view WindowManager参数
		mWM = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		win = LayoutInflater.from(c).inflate(R.layout.ctrl_window, null);
		win.setBackgroundColor(Color.TRANSPARENT);
		m_textView = (ScrollForeverTextView) win.findViewById(R.id.marquee);
		m_textView.setSpeed(20);
		WindowManager wm = mWM;
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		mWMParams = wmParams;
		wmParams.type = 2003; // type是关键，这里的2002表示系统级窗口，你也可以试试2003。		
		wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		wmParams.format = -3;  //透明
		
		if(format == false)
		{
			wmParams.windowAnimations =android.R.style.Animation_Translucent;
			win.setBackgroundDrawable(getResources().getDrawable(R.drawable.paper));
			m_textView.setGravity(Gravity.CENTER);
			//m_textView.setY(-15);
		 	LinearLayout liner=new LinearLayout(this.getContext());
		 	liner.setOrientation(LinearLayout.HORIZONTAL);
		 	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,height);  
		 	lp.gravity = Gravity.CENTER;  
		 	m_textView.setLayoutParams(lp); 
		 	m_textView.setSingleLine(false);
		 	lp.width = 1200;
		 	m_textView.setTextSize(23);
		 	HideWindow();
		}
		else
		{
			win.setBackgroundColor(Color.WHITE);
			m_textView.setY(3);
		}
		wmParams.width = width;
		wmParams.height = height;
		wmParams.x = x;
		wmParams.y = y;      
		wm.addView(win, wmParams);// 这句是重点 给WindowManager中丢入刚才设置的值
		
	}
	
	public void startShow()
	{
		m_textView.startScrollShow();
		ShowWindow(1);
	}
	
	public void ShowWindow(int format)
	{
		if(format == 1)
		{
			m_textView.post(new Runnable() {  
		    @Override  
		    public void run() {  
		    	m_textView.scrollTo(-1500, 0);  
		    }   
		});
		}
		win.setVisibility(View.VISIBLE);	
	}
	
	public void HideWindow()
	{
		win.setVisibility(View.GONE);	
	}
	
	public void SetMsgStr(String msg)
	{
		m_textView.SetMsg(msg);
	}
	/*
	public void SetShowMode(boolean mode)
	{
		m_mode = mode;
	}
	*/
}
