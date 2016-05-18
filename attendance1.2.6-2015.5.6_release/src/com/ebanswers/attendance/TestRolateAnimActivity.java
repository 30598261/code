package com.ebanswers.attendance;

import com.ebanswers.attendance.R;
import com.ebanswers.wt.WifiAdmin;
import com.example.util.AbstractActivity;
import com.example.util.ToastUtil;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TestRolateAnimActivity extends AbstractActivity {
    /** Called when the activity is first created. */
	 CopyOfMyImageView pwdSetImage, clearImage, aboutImage, fingerSetImage, quitImage, APImage;
	 private InteractionService mBoundService; 
	 private MainReceiver receiver = null;
	 private boolean click_state = false;
	 private AlertDialog apWifiDialog, waitingDialog;
	 private Switch m_switchButton;
	 private EditText m_editAPName, m_editAPPwd; 
	 private TextView m_textWifiName, m_textWifiName1, m_textWifiPwd, m_statTextView;
	 private Button m_curBtn, m_cancelBtn;
	 SharedPreferences sharedPreferences=null;
	 public  Handler handler = new Handler();
	 private  boolean m_buttonStat;
	 LinearLayout liner1, liner2;
	 WifiAdmin m_wifiAdmin;
	 String strSSID ,strPWD;
	 private TextView updateTextView;
	 
	 void SendToastMsg(String str)
	 {
		Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	 }
	 
	 //存储配置文件
	 void SaveConfig(String key ,String value)
	 {
		 if(sharedPreferences == null)
			 sharedPreferences =getSharedPreferences("config", Context.MODE_PRIVATE);
		 Editor editor;
		 editor = sharedPreferences.edit();
		 editor.putString(key, value);
		 editor.commit();
	 }
	 
		public class MainReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(action.equals("com.ebanswers.attendance.wifiWarmOpen"))
				{  
					//wifi热点打开
					waitingDialog.dismiss();
					APImage.setImageResource(R.drawable.wifiopen);
					APImage.postInvalidate();
					SendToastMsg("热点创建成功");
					SendMsg(MessageAction.MESSAGE_PLAY_WIFI_AP_CREATE);
				}
				else if(action.equals("com.ebanswers.attendance.wifiWarmClose"))
				{  //wifi热点关闭
					APImage.setImageResource(R.drawable.wificlose);
					SendMsg(MessageAction.MESSAGE_PLAY_WIFI_AP_STOP);
				//	SendToastMsg("热点关闭");
				}
				else if(action.equals("com.ebanswers.attendance.wifiWarmFailer"))
				{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					waitingDialog.dismiss();
					APImage.setImageResource(R.drawable.wificlose);
					SendToastMsg("热点创建失败");
				}
				else if(action.equals("com.ebanswers.attendance.wifiWarmfailer_disconnect"))
				{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					waitingDialog.dismiss();
					APImage.setImageResource(R.drawable.wificlose);
					
					SendToastMsg("网络没有连接， 热点创建失败");
				}
				else if(action.equals("com.ebanswers.attendance.wifiWarmfailer_wifiopen"))
				{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					waitingDialog.dismiss();
					APImage.setImageResource(R.drawable.wificlose);
					SendToastMsg("wifi打开中，热点创建失败");
				}
				else if(action.equals("com.ebanswers.attendance.wifiWarmHave"))
				{
					//wifi热点打开
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					waitingDialog.dismiss();
					APImage.setImageResource(R.drawable.wifiopen);
					APImage.postInvalidate();
				}
				else if(action.equals("com.ebanswers.attendance.wifiWarmNoHave"))
				{
					//wifi热点打开
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					waitingDialog.dismiss();
					APImage.setImageResource(R.drawable.wificlose);
					APImage.postInvalidate();
				}
				else if(action.equals("com.ebanswers.attendance.updateTime"))
				{
					updateTime();
					updateTextView.postInvalidate();
				}
			}
		}

	 /**
		 * 初始化广播接收器
		 */
		public void initReceiver() {
			receiver = new MainReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.ebanswers.attendance.wifiWarmOpen");
			filter.addAction("com.ebanswers.attendance.wifiWarmClose");
			filter.addAction("com.ebanswers.attendance.wifiWarmFailer");
			filter.addAction("com.ebanswers.attendance.wifiWarmfailer_disconnect");
			filter.addAction("com.ebanswers.attendance.wifiWarmfailer_wifiopen");
			filter.addAction("com.ebanswers.attendance.wifiWarmHave");
			filter.addAction("com.ebanswers.attendance.wifiWarmNoHave");
			filter.addAction("com.ebanswers.attendance.updateTime");

			registerReceiver(receiver, filter);
		}
		
	void SendMsg(String msg)
	{
		Intent intent = new Intent();
		intent.setAction(msg);
		sendBroadcast(intent);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    initUI(savedInstanceState, R.layout.likewin8);
		    initReceiver();
		    bindService(new Intent(TestRolateAnimActivity.this, InteractionService.class), mConnection, Context.BIND_AUTO_CREATE); 
		    SendMsg(MessageAction.MESSAGE_NO_OPERATION_CHANGE);
		    /*
		    if(InteractionService.OPEN_MACHINE_STATIC == 1)
		    {
		    	SendMsg(MessageAction.MESSAGE_WIFI_AP_CHECK);
		    }
		    */
	
		  if(InteractionService.m_wiFiAdmin!=null)
		  {
		    if(InteractionService.m_wiFiAdmin.getWifiApState()==true)
		    	APImage.setImageResource(R.drawable.wifiopen);
		    else
		    	APImage.setImageResource(R.drawable.wificlose);
		  }
		  

    }

    private void ChangeSoftware()
    { 
    	if(InteractionService.checkAPP(getBaseContext(),  InteractionService.SOFT__PACKAGE_NAME))
    	{
			Intent intent = new Intent();
			intent.setAction("com.ebanswers.startDog");
			intent.putExtra("package", "com.dsplayer");
			intent.putExtra("callbackActivity", "com.dsplayer.MainActivity");
			sendBroadcast(intent);  //停止广告软件监控
    		    		
    		ComponentName comp = new ComponentName(InteractionService.SOFT__PACKAGE_NAME, "com.dsplayer.MainActivity");
    		Intent mintent = new Intent(InteractionService.SOFT__PACKAGE_NAME);
    		mintent.setComponent(comp);
    		mintent.setAction("android.intent.action.MAIN");
    		mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		startActivity(mintent);
     	}
		else
		{
			System.out.println("未安装广告软件！");
		}
    }
	private ServiceConnection mConnection = new ServiceConnection() { 
        public void onServiceConnected(ComponentName className, IBinder service) { 
                mBoundService = ((InteractionService.LocalBinder)service).getService();  
        }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
		} 
    };


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		//sendBroadcast(new Intent("com.example.test.no.operation.change"));
		super.onStart();
	}
	

	void setDialogView()
	{
		if(sharedPreferences==null)
			sharedPreferences =getSharedPreferences("config", Context.MODE_PRIVATE);
		String apWifi = sharedPreferences.getString("apWifi", "default");
		if(apWifi.equals("open"))
		{	
			m_switchButton.setChecked(true);
			m_editAPName.setEnabled(true);
			m_editAPPwd.setEnabled(true);
			m_editAPName.setTextColor(Color.WHITE);
			m_editAPPwd.setTextColor(Color.WHITE);
			m_textWifiName1.setTextColor(Color.WHITE);
			m_textWifiName.setTextColor(Color.WHITE);
			m_textWifiPwd.setTextColor(Color.WHITE);
			m_buttonStat  = true;
			APImage.setImageResource(R.drawable.wifiopen);
		}
		else
		{
			m_switchButton.setChecked(false);
			m_editAPName.setEnabled(false);
			m_editAPPwd.setEnabled(false);
			m_editAPName.setTextColor(Color.argb(255,136,136,136));
			m_editAPPwd.setTextColor(Color.argb(255,136,136,136));
			m_textWifiName1.setTextColor(Color.argb(255,136,136,136));
			m_textWifiName.setTextColor(Color.argb(255,136,136,136));
			m_textWifiPwd.setTextColor(Color.argb(255,136,136,136));
			m_buttonStat = false;
			APImage.setImageResource(R.drawable.wificlose);
		}
		liner1.setFocusable(true);
		liner1.setFocusableInTouchMode(true);
		liner1.requestFocus();
		liner1.requestFocusFromTouch();
		
	 
		m_wifiAdmin = WifiAdmin.getInstance(getBaseContext());
		strSSID = m_wifiAdmin.getApSSID();
		strPWD = m_wifiAdmin.getApPwd();
		if(strSSID.indexOf("HOOPSON_")>=0)
		{
			m_editAPName.setText(strSSID.substring(8));
			m_editAPPwd.setText(strPWD);
		}
		else
		{
			m_editAPName.setText("001");
		}
	}
	
	/**
	 * 
	* @Title:       updateTime
	* @Description: 更新上传考勤数据的显示时间
	* @param           
	* @return       void   
	* @throws
	* @date         2015-4-28 上午09:43:56
	 */
	private void updateTime()
	{
		if(sharedPreferences==null)
			sharedPreferences =getSharedPreferences("config", Context.MODE_PRIVATE);
		String updateTime = sharedPreferences.getString("lastUpdateTimer", "00:00:00");
		updateTextView.setText(updateTime);
	}
	
	@Override
	public void findView() {
		updateTextView = (TextView)findViewById(R.id.updateTextView);
		pwdSetImage=(CopyOfMyImageView) findViewById(R.id.pwdID);
		clearImage=(CopyOfMyImageView) findViewById(R.id.clearID);
		aboutImage=(CopyOfMyImageView) findViewById(R.id.aboutID);
		fingerSetImage=(CopyOfMyImageView) findViewById(R.id.fingersetID);
		quitImage=(CopyOfMyImageView) findViewById(R.id.quitID);
		APImage =(CopyOfMyImageView) findViewById(R.id.wifiID);
		
		View popWindow = View.inflate(this, R.layout.apwifipopwindow, null);
		apWifiDialog = new AlertDialog.Builder(this).create();
		apWifiDialog.setView(popWindow);
		liner1 = (LinearLayout)popWindow.findViewById(R.id.liner1);
		liner2 = (LinearLayout)popWindow.findViewById(R.id.liner2);
		m_switchButton = (Switch)popWindow.findViewById(R.id.switch1);
		m_editAPName = (EditText)popWindow.findViewById(R.id.editText2);
		m_editAPPwd = (EditText)popWindow.findViewById(R.id.editText1);
		m_textWifiName = (TextView)popWindow.findViewById(R.id.textWifiName1);
		m_textWifiName1= (TextView)popWindow.findViewById(R.id.textWfifName2);
		m_textWifiPwd  = (TextView)popWindow.findViewById(R.id.textWifiPwd);
		m_curBtn = (Button)popWindow.findViewById(R.id.button1);
		m_cancelBtn = (Button)popWindow.findViewById(R.id.button2);
		
		
		updateTime();
		
		setDialogView();
		
		//检查旋转对话框
		View popProgressbar = View.inflate(this, R.layout.progressbar, null);
		waitingDialog = new AlertDialog.Builder(this).create();	 
		waitingDialog.setView(popProgressbar);
		m_statTextView = (TextView)popProgressbar.findViewById(R.id.statText);
		m_statTextView.setText("正在创建WIFI热点，请稍候。。。");
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
		//  final ContentResolver mContentResolver = getContentResolver();
	}

	@Override
	public void setListener() {
		// TODO Auto-generated method stub	
		pwdSetImage.setOnClickIntent(new CopyOfMyImageView.OnViewClick() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					ResetChangeTime();
					if(click_state == false)
					{
						Intent intent = new Intent();
						intent.setClass( TestRolateAnimActivity.this, CommsetActivity.class);//前面一个是一个Activity后面一个是要跳转的Activity
						startActivityForResult(intent, 0);//开始界面的跳转函数
						click_state = true;
					}
				}
			});
		 
		clearImage.setOnClickIntent(new CopyOfMyImageView.OnViewClick() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					ResetChangeTime();
					if(click_state == false)
					{
						Intent intent = new Intent();
						intent.setClass( TestRolateAnimActivity.this, SystemsetActivity.class);//前面一个是一个Activity后面一个是要跳转的Activity
						startActivityForResult(intent,1);//开始界面的跳转函数
						click_state = true;
					}
				}
			});
		 
		aboutImage.setOnClickIntent(new CopyOfMyImageView.OnViewClick() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					ResetChangeTime();
					if(click_state == false)
					{
						Intent intent = new Intent();
						intent.setClass( TestRolateAnimActivity.this, InformationActivity.class);//前面一个是一个Activity后面一个是要跳转的Activity
						startActivityForResult(intent, 2);//开始界面的跳转函数
						click_state = true;
					}
				}
			});
		 
		fingerSetImage.setOnClickIntent(new CopyOfMyImageView.OnViewClick() {
				@Override
				public void onClick() {
					ResetChangeTime();
					if(click_state == false)
					{
						Intent intent = new Intent();
						intent.setClass( TestRolateAnimActivity.this, fingerTestWeb.class);//前面一个是一个Activity后面一个是要跳转的Activity
						startActivityForResult(intent, 3);//开始界面的跳转函数
						click_state = true;
					}
				}
			});
		
		quitImage.setOnClickIntent(new CopyOfMyImageView.OnViewClick() {
			@Override
			public void onClick() {		
				//Intent intent = new Intent();
				//intent.setAction(MessageAction.MESSAGE_PLAY_QUIT_SOUND);
				//sendBroadcast(intent);
				SendMsg(MessageAction.MESSAGE_PLAY_QUIT_SOUND);
				ToastUtil.showToastTextLong(getApplicationContext(), "正在切换软件，请稍候...", Toast.LENGTH_LONG);
				
				ChangeSoftware();
			}
		});
		
		APImage.setOnClickIntent(new CopyOfMyImageView.OnViewClick() {
			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				ResetChangeTime();
		
				Window w=apWifiDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-200;
				w.setAttributes(lp);  
		
				setDialogView();
				apWifiDialog.show();
				
			}
		});
		
		m_switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
		
				if(isChecked==true)
				{			
					m_switchButton.setChecked(true);
					m_editAPName.setEnabled(true);
					m_editAPPwd.setEnabled(true);
					m_editAPName.setTextColor(Color.WHITE);
					m_textWifiName1.setTextColor(Color.WHITE);
					m_textWifiName.setTextColor(Color.WHITE);
					m_textWifiPwd.setTextColor(Color.WHITE);
					m_editAPPwd.setTextColor(Color.WHITE);
					m_buttonStat = true;
					SendMsg(MessageAction.MESSAGE_PLAY_WIFI_NAME);
				}
				else
				{
					m_switchButton.setChecked(false);
					m_editAPName.setEnabled(false);
					m_editAPPwd.setEnabled(false);
					m_editAPName.setTextColor(Color.argb(255,136,136,136));
					m_textWifiName1.setTextColor(Color.argb(255,136,136,136));
					m_textWifiName.setTextColor(Color.argb(255,136,136,136));
					m_textWifiPwd.setTextColor(Color.argb(255,136,136,136));
					m_editAPPwd.setTextColor(Color.argb(255,136,136,136));
					m_buttonStat  =false;
				}
				liner1.setFocusable(true);
				liner1.setFocusableInTouchMode(true);
				liner1.requestFocus();
				liner1.requestFocusFromTouch();
			}  
        });
		
		m_curBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 ResetChangeTime();
				 apWifiDialog.dismiss();
				 
	
				 if(m_buttonStat == true)
				 {
					//上一次wifi热点打开关闭的状态
					 if(m_editAPPwd.length()>=8)
					 { 
						Window w=waitingDialog.getWindow();
						WindowManager.LayoutParams lp =w.getAttributes();
						lp.x=0;
						lp.y=0;
						w.setAttributes(lp); 
						waitingDialog.show();
						strSSID = m_wifiAdmin.getApSSID();
						strPWD = m_wifiAdmin.getApPwd();
							
						Intent intent = new Intent();
						intent.setAction(MessageAction.MESSAGE_WIFI_AP_CREATE);
						intent.putExtra("apWifiName", m_editAPName.getText().toString());
						intent.putExtra("apWifiPwd", m_editAPPwd.getText().toString());
						
						sendBroadcast(intent);
						
						handler.removeCallbacks(CloseProgessbar);
						handler.postDelayed(CloseProgessbar, 1000*30);
					 }
					 else
					 {
						Toast toast = Toast.makeText(getApplicationContext(), "密码少于8位，无法创建，请重新输入", Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					 }
				 }
				 else
				 {
					SendMsg(MessageAction.MESSAGE_WIFI_AP_CLOSE);
				 }
			}}
	       );
		
		
		m_cancelBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 ResetChangeTime();
				 apWifiDialog.dismiss();
			}}
	       );
	}

	@Override
	public Bundle saveData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadData(Bundle bundle) {
		// TODO Auto-generated method stub
	} 
			
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
		unbindService(mConnection);
		if(apWifiDialog!=null)
			apWifiDialog.dismiss();
	}
	public void onResume()
	{
		super.onResume();
	
		/*
	   if(InteractionService.OPEN_MACHINE_STATIC == 1)
	   {
			SendMsg(MessageAction.MESSAGE_WIFI_AP_CHECK);
	   }
	   */
	}
	/** 
     * 复写onActivityResult，这个方法 
     * 是要等到SimpleTaskActivity点了提交过后才会执行的 
     */  
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  
    {  
    	click_state = false;
        super.onActivityResult(requestCode, resultCode, data);  
    }  
    
	private Runnable CloseProgessbar = new Runnable() {
		@Override
		public void run() {
 			waitingDialog.dismiss();
		}
	};
}