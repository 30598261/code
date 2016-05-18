package com.ebanswers.attendance;

import java.net.SocketException;

import com.ebanswers.attendance.R;
import com.example.util.AbstractActivity;
import com.example.util.NetStat;
import com.example.util.ToastUtil;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;  

public class CommsetActivity extends AbstractActivity {
    private Button m_btnCancel, setCurBtn, setCancelButton, setChangeBtn, setChangeCancelBtn;
    private defineImageButton setPwdButton, setSoundButton, checkInternetButton, changeWifiSetButton;
    private IPTextView textView1;
    private TextView  textView2;
    Context mContext; 
    SharedPreferences sharedPreferences;
	private AlertDialog popupWindow, popSoundWindow , popProgressbarWindow, popChangeWindow;
	private int buttonState;
	private Editor editor;
	private EditText editText;
	private SeekBar m_soundBar;
	private int maxVolume, currentVolume;
	public  AudioManager audiomanage;
	public  Handler handler = new Handler();
	public  MyReceiver receiver=null;
	public static boolean isActive = false;
	public String m_statText = null;
	public int m_windowStat=0;
	private String text1 = new String("进入“设置”后，请在“无线和网络”进行网络设置，完成后以管理员指纹认证退回考勤管理界面");
	private String text2 = new String("进入“设置”后，禁止对“无线与网络”以外进行操作，以免引起考勤软件无法使用");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        initUI(savedInstanceState, R.layout.commset);
  
        RegBroadcast();
    }
    public  void onResume()
    {
    	super.onResume();
    }
    public void RegBroadcast()
    {
    	  //注册广播接收器
        receiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.ljq.activity.CountService");
        registerReceiver(receiver,filter);
        isActive = true;
        
    }

    @Override
    public void onDestroy() {
    	unregisterReceiver(receiver);
    	isActive = false;
    	super.onDestroy();
        
    }

    void UpdateNetStat()
    {
    	 if(InteractionService.m_warningMsgTag==true)
	        {
	        	try {
	        		String ip=PooledRemoteFileServer.getLocalIpAddress();
	        		if(ip==null)
	        		{
	        			textView1.setText("考勤机IP","未连接");
						textView1.setColor(Color.RED);
						return;
	        		}
	        		if(ip.equals("")==true)
					{
						 textView1.setText("考勤机IP", "未连接");
						 textView1.setColor(Color.RED);
					}
					else
					{	
						String ipStr = getWifiIPAddress(getApplicationContext());
						
						if(InteractionService.netConnectType == 1)
			        	{
							if(ipStr!=null)
							{
								textView1.setText("考勤机IP",ipStr);
								textView1.setColor(Color.rgb(163, 165, 170));
							}
							else
							{
								textView1.setText("考勤机IP","未连接");
								textView1.setColor(Color.RED);
							}
			        	}
			        	else if(InteractionService.netConnectType==9)
			        	{	
			        		String str = PooledRemoteFileServer.getLocalIpAddress();
			        		textView1.setText("考勤机IP", str);
							textView1.setColor(Color.rgb(163, 165, 170));
			        	}

					}
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	        
	        }
	        else
	        {
	        	textView1.setText("考勤机IP", "未连接");
				textView1.setColor(Color.RED);
	        }
    }
	@Override
	public void findView() {
		// TODO Auto-generated method stub		
			textView1 = (IPTextView)findViewById(R.id.textview1);
	        textView1.setPosition(100, 80);
	        textView1.setState(1);
	        UpdateNetStat();
	        sharedPreferences = mContext.getSharedPreferences("config", Context.MODE_WORLD_READABLE);
			setPwdButton = (defineImageButton)findViewById(R.id.setPwdButtonID);
			setPwdButton.setPosition(100, 80);
			setPwdButton.setState(6);
			setPwdButton.setText("连接密码");
			String str= sharedPreferences.getString("PassWord", "");
			if(str.equals(""))
			{   
				setPwdButton.setInternetText("空");
			}
			else
			{   
				setPwdButton.setInternetText(str);
			}
			setSoundButton = (defineImageButton)findViewById(R.id.setSoundButtonID);
			setSoundButton.setPosition(100, 80);
			setSoundButton.setState(6);
			setSoundButton.setText("音量设置");
			checkInternetButton = (defineImageButton)findViewById(R.id.checkInternet);
			checkInternetButton.setPosition(100, 80);
			checkInternetButton.setState(5);
		    if(InteractionService.m_warningMsgTag==true)
		    {
		       	m_statText = "已连接";
		    }
		    else
		    {
		        m_statText = "未连接";
		    }
		    checkInternetButton.setInternetText(m_statText);
			checkInternetButton.setText("检查网络连接");
	        m_btnCancel = (Button) findViewById(R.id.btnCancel);
	    	
			//弹出密码对话框
			View popWindowView = View.inflate(this, R.layout.netsetpopwindow, null);
			popupWindow = new AlertDialog.Builder(this).create();
			popupWindow.setView(popWindowView);
			setCurBtn = (Button) popWindowView.findViewById(R.id.button1);
			setCancelButton = (Button) popWindowView.findViewById(R.id.button2);
			editText = (EditText)popWindowView.findViewById(R.id.editText1);
			editor = sharedPreferences.edit(); 
			editText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
			
			View popChangeWindowView = View.inflate(this, R.layout.wifiset, null);
			popChangeWindow = new AlertDialog.Builder(this).create();
			popChangeWindow.setView(popChangeWindowView);
			setChangeBtn = (Button) popChangeWindowView.findViewById(R.id.button1);
			setChangeCancelBtn = (Button) popChangeWindowView.findViewById(R.id.button2);
			 textView2 = (TextView)popChangeWindowView.findViewById(R.id.textView2);
			textView2.setText("进入“设置”后，请在“无线和网络”进行网络设置，完成后以管理员指纹认证退回考勤管理界面");
			
			//音量对话框
			View popSoundWindowView = View.inflate(this, R.layout.seekbar, null);
			popSoundWindow = new AlertDialog.Builder(this).create();
			popSoundWindow.setView(popSoundWindowView);
			m_soundBar = (SeekBar) popSoundWindowView.findViewById(R.id.soundBar1);
			audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);   //获取系统最大音量  
			m_soundBar.setMax(maxVolume);   //拖动条最高值与系统最大声匹配
			currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值  
			String volumeTmpstr = ""+currentVolume*100/maxVolume+"%";
			setSoundButton.setInternetText(volumeTmpstr);
			m_soundBar.setProgress(currentVolume);
			m_soundBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
					audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
					audiomanage.setStreamVolume(AudioManager.STREAM_SYSTEM, progress/2, 0);
					currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值  
					m_soundBar.setProgress(currentVolume); 
					String volumeTmpstr = ""+currentVolume*100/maxVolume+"%";
					setSoundButton.setInternetText(volumeTmpstr);
					setSoundButton.postInvalidate();
					editor.putInt("voice", currentVolume);
					editor.commit();
					SendSoundMsg(MessageAction.MESSAGE_PLAY_DING_SOUND);
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
					// TODO Auto-generated method stub
				}
			});
			
			changeWifiSetButton = (defineImageButton)findViewById(R.id.changeWifiSet);
			changeWifiSetButton.setPosition(100, 80);
			changeWifiSetButton.setText("网络设置");
			
			//检查旋转对话框
			View popProgressbar = View.inflate(this, R.layout.progressbar, null);
			popProgressbarWindow = new AlertDialog.Builder(this).create();	 
			popProgressbarWindow.setView(popProgressbar);
	}

	@Override
	public void fillData() {
		getApplicationContext();
	}

	@Override
	public void setListener() {	
		
		setCurBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				switch(buttonState )
				{
				case 1:
					if("".equals(editText.getText().toString().trim()))
					{
						editor.putInt("Port", -1);
					}
					else
					{
				    	editor.putInt("Port", new Integer(editText.getText().toString()));
					}
					ToastUtil.showToast3Text(getApplicationContext(), "服务端口号设置成功!", null,null,Toast.LENGTH_SHORT, false);
					 editor.commit();
					 popupWindow.dismiss();
					break;
				case 2:
					if("".equals(editText.getText().toString().trim()))
					{
						editor.putString("PassWord",  "");
					}
					else
					{
						editor.putString("PassWord",  editText.getText().toString());
					}
					editor.commit();
					ToastUtil.showToastTextLong(getApplicationContext(), "连接密码设置成功!", Toast.LENGTH_LONG);
					String str = sharedPreferences.getString("PassWord", "");
					if("".equals(str))
					{
						setPwdButton.setInternetText("空");
					}
					else
					{
						setPwdButton.setInternetText(str);
					}
					setPwdButton.postInvalidate();
					SendSoundMsg(MessageAction.MESSAGE_PLAY_OPERATIONSUCC_SOUND);
					popupWindow.dismiss();
					break;
				default:
					break;
				}
				ResetChangeTime();
			}
		}
	    );
		checkInternetButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {	
				
				popProgressbarWindow.dismiss();
	         	Window w=popProgressbarWindow.getWindow();
				 WindowManager.LayoutParams lp =w.getAttributes();
				 lp.x=0;
				 lp.y=0;
				 w.setAttributes(lp); 
				 popProgressbarWindow.show();
				 
				 handler.removeCallbacks(CloseProgessbar);
				 handler.postDelayed(CloseProgessbar, 1000*21);
				 
				new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            {  
		            	int tag = 0;
		            	boolean ret = false;
		            	if(InteractionService.isNetworkConnected(getApplicationContext())==false)
		            	{
		            		m_statText="未连接";
		            		InteractionService.m_warningMsgTag = false;
		            		textView1.setText("考勤机IP","未连接");
		            		tag = 1;
		            	}
		            	else
		            	{
		            		ret = NetStat.ping();
							if(ret==false)
							{
								InteractionService.m_warningMsgTag = false;
								m_statText="未连接";
								textView1.setText("考勤机IP","未连接");
								tag = 1;
							}
							else if(ret==true)
							{
								InteractionService.m_warningMsgTag = true;	
								m_statText="已连接";
								try {
									textView1.setText("考勤机IP", PooledRemoteFileServer.getLocalIpAddress());
								} catch (SocketException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								tag =2;
							}
		            	}
		            	
		            	try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ResetChangeTime();
		            	checkInternetButton.setInternetText(m_statText);
		            	handler.removeCallbacks(CloseProgessbar);									
						checkInternetButton.postInvalidate();
						textView1.postInvalidate();
						popProgressbarWindow.dismiss();
													
		            }  
		        }).start();  
			}}
	       );
		
		setCancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ResetChangeTime();
				popupWindow.dismiss();
			}}
	       );
		
		
		setPwdButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ResetChangeTime();
		             buttonState=2;
		     		 editText.setText("");
					 Window w=popupWindow.getWindow();
					 WindowManager.LayoutParams lp =w.getAttributes();
					 lp.x=0;
					 lp.y=-170;
					 w.setAttributes(lp); 
					 popupWindow.show();
					
					 SendSoundMsg(MessageAction.MESSAGE_PLAY_INPUTPWD_SOUND);
				}}
		       );
		
		       m_btnCancel.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ResetChangeTime();
					finish();
				}
		       });
		       
		       setSoundButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ResetChangeTime();
			     		 editText.setText("");
						 Window w=popSoundWindow.getWindow();
						 WindowManager.LayoutParams lp =w.getAttributes();
						 lp.x=0;
						 lp.y=0;
						 w.setAttributes(lp); 
						 popSoundWindow.show();
						handler.removeCallbacks(closeRunable);
						handler.postDelayed(closeRunable, 1000*2);
					}}
			       );
			
			       m_btnCancel.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ResetChangeTime();
						finish();
					}
			       });	
			       
			       changeWifiSetButton.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							/*
							 if(android.os.Build.VERSION.SDK_INT > 10) {
								// 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面 
									 startActivity(new Intent( android.provider.Settings.ACTION_SETTINGS)); 
								} else { 
									startActivity(new Intent( android.provider.Settings.ACTION_WIRELESS_SETTINGS)); 
								}
							*/
						//	 SendSoundMsg(MessageAction.MESSAGE_SHOW_HIDEVIEW_WINDOW);
							ResetChangeTime();
							
							Window w=popChangeWindow.getWindow();
								 WindowManager.LayoutParams lp =w.getAttributes();
								 lp.x=0;
								 lp.y=-170;
								 w.setAttributes(lp); 
								 popChangeWindow.show();
						}}
				       );
			       
			       
			       setChangeBtn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ResetChangeTime();
						switch(m_windowStat)
						{
						case 0:
							m_windowStat = 1;
							textView2.setText(text2);
							break;
						case 1:
							textView2.setText(text1);
							m_windowStat = 0;
							popChangeWindow.dismiss();
							if(android.os.Build.VERSION.SDK_INT > 10) {
								// 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面 
									 startActivity(new Intent( android.provider.Settings.ACTION_SETTINGS)); 
								} else { 
									startActivity(new Intent( android.provider.Settings.ACTION_WIRELESS_SETTINGS)); 
								}
							break;
							default:
								m_windowStat = 0;
								textView2.setText(text1);
								break;
						}
					}
				}
			    );
			       
			    setChangeCancelBtn.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							ResetChangeTime();
							m_windowStat = 0;
							textView2.setText(text1);
							popChangeWindow.dismiss();
						}
					}
			);
	}

	
	@Override
	public void loadData(Bundle bundle) {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public Bundle saveData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	void SendSoundMsg(String action)
	{
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent);
	}
	
	private Runnable closeRunable = new Runnable() {
		@Override
		public void run() {
			 popSoundWindow.dismiss();
		}
	};
	
	private Runnable CloseProgessbar = new Runnable() {
		@Override
		public void run() {
			m_statText="已连接";
			//checkInternetButton.setInternetText(m_statText);
 			checkInternetButton.postInvalidate();
 			popProgressbarWindow.dismiss();
		}
	};
	
	 /**
     * 获取广播数据
     * 
     * @author jiqinlin
     *
     */
    public class MyReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		intent.getExtras();
    		if(InteractionService.m_warningMsgTag==true)
  		    {
  		       	m_statText = "已连接";
  		      //textView1.setText("考勤机IP", PooledRemoteFileServer.getLocalIpAddress());
  		       	UpdateNetStat();
  		    }
  		    else
  		    {
  		        m_statText = "未连接";
  		        textView1.setText("考勤机IP","未连接");
  		    }
  		    checkInternetButton.setInternetText(m_statText);
  		    checkInternetButton.postInvalidate();
  		    textView1.postInvalidate();
    	}
    }
    
 
	    public String getWifiIPAddress(Context ctx)
	    {     
	    	 ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	         NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	         if(wifiNetworkInfo.isConnected())
	         { 
	        	WifiManager wifi_service = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);  
		        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();  
		        WifiInfo wifiinfo = wifi_service.getConnectionInfo();  
		        int d = dhcpInfo.ipAddress>>24&0xff;
		        int c = dhcpInfo.ipAddress>>16&0xff;
		        int b = dhcpInfo.ipAddress>>8&0xff;
		        int a = dhcpInfo.ipAddress&0xff;
		       
		        return(""+a+"."+b+"."+c+"."+d);      
	         }
	         else
	        	 return null;

	    }  

	    void SendMsg(String msg)
		{
			Intent intent = new Intent();
			intent.setAction(msg);
			sendBroadcast(intent);
		}
}
