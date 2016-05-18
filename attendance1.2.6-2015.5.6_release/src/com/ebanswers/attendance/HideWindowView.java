package com.ebanswers.attendance;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.ebanswers.attendance.R;

public class HideWindowView extends View {
	// 如果是想显示歌词则继承TextView并复写ondraw方法。
	// 开启一个线程不断的调用ondraw方法去更改你所写的继承自TextView的内容
	// 这里随便写了个集成自view的= =这个不是重点

	Context						c;
	WindowManager				mWM;		// WindowManager
	WindowManager.LayoutParams	mWMParams;	// WindowManager参数
	View						win;
	ImageButton m_imageButton;

	public HideWindowView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		c = context;
	}
	
	public void fun(int x, int y, int width, int height, boolean format) {
		// 设置载入view WindowManager参数
		mWM = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		win = LayoutInflater.from(c).inflate(R.layout.hidewindow, null);
		win.setBackgroundColor(Color.TRANSPARENT);
		
		WindowManager wm = mWM;
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		mWMParams = wmParams;
		wmParams.type = 2003; // type是关键，这里的2002表示系统级窗口，你也可以试试2003。
		wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |LayoutParams.FLAG_NOT_FOCUSABLE ;
	//	wmParams.format = -3;  //透明
		
		win.setBackgroundColor(Color.argb(255,12,14,16));
	//	m_imageButton = (ImageButton) win.findViewById(R.id.imageButton);
	//	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);  
	//	m_imageButton.setLayoutParams(lp); 
	//	lp.width = 100;
	//	lp.height = 100;
		wmParams.gravity=Gravity.LEFT;
		wmParams.width = width;
		wmParams.height = height;
		wmParams.x = x;
		wmParams.y = y;      
		wm.addView(win, wmParams);// 这句是重点 给WindowManager中丢入刚才设置的值
		HideWindow();
		
	}
		
	public void ShowWindow()
	{
		
		win.setVisibility(View.VISIBLE);	
	}
	
	public void HideWindow()
	{
		win.setVisibility(View.GONE);	
	}
	}
