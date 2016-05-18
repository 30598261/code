package com.example.util;

import java.util.Locale;

import com.ebanswers.attendance.MessageAction;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
/**
 * 
 * 项目中大部分Activity的抽象父类

 */

public abstract class AbstractActivity extends Activity {
	private Handler handler = new Handler();

	private int layoutResId = -1; // 布局id

	protected View contentView = null;

	private static boolean first = true;

	/**
	 * 获取界面中用到的View
	 */
	public abstract void findView();

	/**
	 * 向界面中填入数据
	 */
	public abstract void fillData();

	/**
	 * 为组件设置监听器
	 */
	public abstract void setListener();

	/**
	 * 暂时保存数据(用于重置界面如更新区域语言)
	 * 
	 * @return 保存的数据
	 * @see #loadData()
	 */
	public abstract Bundle saveData();

	/**
	 * 从保存的数据中恢复
	 * 
	 * @param bundle
	 *            保存的数据
	 * @see #saveData()
	 */
	public abstract void loadData(Bundle bundle);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initAbstractReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(absReceiver);
		
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle bundle = saveData();
		if (bundle != null)
			outState.putAll(bundle);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		foregroundBroadcast();
		super.onResume();
	}

	protected void foregroundBroadcast() {
		
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && first) {
			first = false;
			refreshUI();
		}
	}

	@Override
	protected void onUserLeaveHint() {
	
		super.onUserLeaveHint();
	}

	/**
	 * 初始化界面，使用当前布局id，并且尝试恢复数据
	 * 
	 * @param savedInstanceState
	 *            保存的数据
	 */
	
	public void initUI(Bundle savedInstanceState) {
		if (layoutResId != -1) {		
			contentView = View.inflate(this, layoutResId, null);
			if (contentView instanceof ViewGroup) {
				if (!(contentView instanceof ScrollView)
						&& !(contentView instanceof HorizontalScrollView)) {
					
					View vw = new View(this);
					vw.setFocusable(true);
					vw.setFocusableInTouchMode(true);
					((ViewGroup) contentView).addView(vw, 0, new LayoutParams(0, 0));
				}
			}
			
			
			setContentView(contentView);
			findView();
			setListener();
			fillData();
			loadData(savedInstanceState);
		}
	}

	/**
	 * 初始化界面，使用传入的布局id
	 * 
	 * @param savedInstanceState
	 *            保存的数据
	 * @param resId
	 *            布局id
	 */
	public void initUI(Bundle savedInstanceState, int resId) {
		layoutResId = resId;
		initUI(savedInstanceState);
	}

	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean ret = true;
			try {
				ret = super.onKeyDown(keyCode, event);
			} catch (Exception e) {
				finish();
			}
			return ret;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean ret = true;
			try {
				ret = super.onKeyUp(keyCode, event);
			} catch (Exception e) {
				finish();
			}
			return ret;
		}
		return super.onKeyUp(keyCode, event);
	}
	*/
	/*
	public void updateSkin() {
		if (contentView.getBackground() == null)
			return;
		
	}
	*/	
		
	public void refreshUI() {
		Configuration config = new Configuration();
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		Bundle bundle = saveData();
		initUI(bundle);
		setTitle("刷新界面");
	}
	
	/**
	 * 刷新界面，使用参数布局资源id
	 * 
	 * @param resId
	 *            布局资源id
	 */
	public void refreshUI(int resId) {
		layoutResId = resId;
		refreshUI();
	}

	/**
	 * 获取当前布局id
	 * 
	 * @return 布局id
	 */
	public int getLayoutResId() {
		return layoutResId;
	}

	/**
	 * 设置当前布局id
	 * 
	 * @param layoutResId
	 *            布局id
	 */
	public void setLayoutResId(int layoutResId) {
		this.layoutResId = layoutResId;
	}

	private AbstractReceiver absReceiver = null;

	/**
	 * 
	 * 广播接收器
	 * 
	 * 
	 * 
	 */
	private class AbstractReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
		}

	}

	/**
	 * 初始化广播接收器
	 */
	private void initAbstractReceiver() {
		absReceiver = new AbstractReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MessageAction.MESSAGE_LOCALE);

		registerReceiver(absReceiver, filter);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		handler.postDelayed(configRunnable, 200);
	}

	private Runnable configRunnable = new Runnable() {
		public void run() {
			refreshUI();
		}
	};

	private Runnable logoutRunnable = new Runnable() {
		public void run() {
		}
	};
	
	 public void onUserInteraction() {
		 sendBroadcast(new Intent("com.example.test.no.operation.change"));
	 }

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	 
	public void ResetChangeTime()
    {
    	//发送指纹消息
		Intent intent = new Intent();
		intent.setAction(MessageAction.MESSAGE_NO_OPERATION_CHANGE);
		sendBroadcast(intent);
    }
	
	@Override
	 public boolean onTouchEvent(MotionEvent event) {
	  int action = event.getAction();
	  
	  float x = event.getX();
	  float y = event.getY();
	//  System.out.println( "X = "+x+"  "+"Y = "+y);
	  
	  ResetChangeTime();
	  return super.onTouchEvent(event);
	 }
}
