package com.ebanswers.attendance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import com.ebanswers.attendance.R;
import com.ebanswers.object.Person;
import com.ebanswers.object.PersonEx;
import com.ebanswers.object.SaveConnectionClass;
import com.ebanswers.object.attendanceTimer;
import com.ebanswers.update.GData;
import com.ebanswers.update.IniReader;
import com.ebanswers.wt.WifiAdmin;
import com.example.util.DataRecvProc;
import com.example.util.NetStat;
import com.example.util.RequestUtil;
import com.example.util.ToastUtil;
import com.example.util.ToastWaring;
import com.example.db.Attendance;
import com.example.db.DBAdapter;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Secure;
import android.serialport.api.sample.Application;
import android.util.Log;
import android.widget.Toast;
import android_serialport_api.SerialPort;

/**
 * 后台交互服务，用于上层各Activity间以及上层与底层间的交互
 */
public class InteractionService extends Service implements  DataRecvProc{
	private static int progress;
	public static Handler  testHandler;
	//创建服务端ServerSocket对象
    private boolean serverRuning = false;
    public static final int SERVERPORT = 31344;  //注意这里和上面重定向的配置是有关系的�?
    static BufferedReader mBufferedReaderServer	= null;
	private Thread mThreadServer, checkWifiThread;
	static  PooledRemoteFileServer m_PooledRemoteFileServer=null;
	protected SerialPort mSerialPort;
	boolean mByteReceivedBack;
	Object mByteReceivedBackSemaphore = new Object();
	SharedPreferences sharedPreferences;
	protected Application mApplication;
	protected OutputStream mOutputStream;
	private InteractionReceiver receiver = null;
	GetFingerThread m_getFingerThread;
	NET_MSG_Proc m_netMsgProcThread;
	static int g_userID;
	Object curFinagerObj = new Object();
	Object tcpServerThreadObj = new Object();
	Object apWifiCreateObj = new Object();
	Object netReCreateObj = new Object();
	private int m_admin = 0;
	private boolean  m_netStat = false;   //��ǰ����״??  true:��?? false:�Ͽ�   Ĭ��Ϊtrue״??
	static Queue<String>  msgQueue;
	byte mValueToSend;
	Timer timer = null;
	Timer m_tcpConnectTimer = null;
	TableShowView m_tableShowView, popTextView;
	private boolean tcp_thread_stat = false;
	private boolean tcp_heart = false;
	static boolean m_play = false;
	static Context g_context;
	static int g_attendance_timer=0;
	static attendanceTimer g_attendanceTimer1 = new attendanceTimer();
	static attendanceTimer g_attendanceTimer2 = new attendanceTimer();
	final static String SOFT__PACKAGE_NAME = "com.dsplayer";
	public String play_id=null;
	String msgStr=null;
	int msgTag = 0;
	static int posCount=0;
	static boolean m_warningMsgTag = true;  // false,网络不通， true, 网络畅通
	ScheduledThreadPoolExecutor stpe = null; 
	byte[] buf;
	int g_id;
	public static int netConnectType = 0;
	static int midTime1, midTime2, midTime3;
	String settingStr = null;
	public  Map<String, Socket> m_map = new HashMap<String, Socket>();
	public static int m_preConnectNetType=-1;
	public static WifiAdmin m_wiFiAdmin=null;
	static int openWifiTimes=0;
	private CreateAPProcess m_createAPProcess;
	public static  String WIFI_AP_HEADER = "001";
	public static  String WIFI_AP_PASSWORD ="12345678";
	String strSSID = null;
	String strPWD = null;
	static int OPEN_MACHINE_STATIC = 0;
	SoundPool soundPool;
	Map<Integer, Integer> soundMap = new HashMap<Integer, Integer>();  
	static boolean checkNetTag = false;
	private MediaPlayer mp=null;
	//自动更新升级程序
	private static String apkUrl = "http://cloud.hoopson.com/Project/Hoopson/";
	private static String versionUrl = "http://cloud.hoopson.com/Project/Hoopson/version.exe";
	private static final String savePath = "/sdcard/updateApk/";
	private static String saveFileNameUpdateAPK = savePath + "AttendanceUpdate.apk";
	private static final String saveFileNameIni = savePath + "version.ini";
	private Context mContext = this;
	boolean birthdayTag = false;
	String happyBirthdayStr=null;	
	
	
	//jni  本地操作指纹模块接口
	static {
         System.loadLibrary("kaoqinji");
	 }
	
	public native int RegFinger(int id);
	public native int RegFingerOne(int id);
	public native int RegFingerTwo(int id);
	public native int CurFinger();
	public native int ClearAllFinger();
	public native void SetFlagSwitch(boolean flag);
	public native int OpenDevice();
	public native int CloseDevice();
	public native int DelFinger(int num);
	public native int OpenLed(int id);
	public native int UpFinger(int id);
	
	public static Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				GData.setDownload(false);
				// 下载完毕
				if(GData.getFileType()!=2)
				{
					try {
						new IniReader(saveFileNameIni, null);	
						int version =  g_context.getPackageManager().getPackageInfo(g_context.getPackageName(), 0).versionCode;
						if(version/10 < Integer.parseInt(IniReader.androidVersion))
						{
							GData.setFileType(2);
							progress = 0;
							startDownload();
							
						//	System.out.println(" 发现新版本");
						}
						else
						{
					//		System.out.println(" 没有新版本 ");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					GData.setFileType(0);
					installApk();
				}
				break;
			case 2:
				GData.setDownload(false);
				// 这里是用户界面手动取消，所以会经过activity的onDestroy();方法
				break;
			case 1:
				int rate = msg.arg1;
				GData.setDownload(true);
				if (rate < 100) {
					;
				} else {
				//	System.out.println("下载完毕!");
				}
				break;
			}
		}
	};
	
	
	
	/**
	 * 
	* @Title:       updateMsg
	* @Description: 显示插播消息
	* @param           
	* @return       void   
	* @throws
	* @date         2015-4-15 下午01:47:16
	 */
	public void updateMsg()
	{
		sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		//插播消息
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTimeStr = sdf.format(calendar.getTime());
		int colorR = sharedPreferences.getInt("COLOR_R", 255);
		int colorG = sharedPreferences.getInt("COLOR_G", 0);
		int colorB = sharedPreferences.getInt("COLOR_B", 0);
		int timeCount = sharedPreferences.getInt("timeCount", 0);
		int appendTag = sharedPreferences.getInt("APPEND_TAG", 0);
		String msgStr = sharedPreferences.getString("MSG", null);
		String startTimeStr = sharedPreferences.getString("startTime",curTimeStr);
		long time = 0;
		try {
			time = DateCompare(curTimeStr, startTimeStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(appendTag!=0)
		{
			if (time != 0) // 有设置过插播消息
			{
				if (timeCount > time) 
				{
					int tmpTag = (appendTag&0x3)>>1;
					if(tmpTag == 1)
					{  //pop消息
						popTextView.m_textView.setTextColor(Color.rgb(colorR,colorG, colorB));
						popTextView.m_textView.setText(msgStr);
					}
					tmpTag = (appendTag&0x4)>>2;
					if(tmpTag==1)
					{  //滚动消息
						m_tableShowView.m_textView.setTextColor(Color.rgb(colorR,colorG, colorB));
						m_tableShowView.m_textView.setText(msgStr);
						m_tableShowView.startShow();
					}
							
					handler.removeCallbacks(NetMsgRuanable);
					handler.postDelayed(NetMsgRuanable, (long)(1000 * (timeCount-time)));
				}
			}
		}
	}
	

	public boolean CheckInternetStat()
	{	
		if(checkNetTag == false)
		{
			checkNetTag = true;
			// TODO Auto-generated method stub
			if(isNetworkConnected(getApplicationContext())==false)
			{ //网络断开
				m_warningMsgTag = false;
				m_tableShowView.m_textView.stopScroll();
				m_tableShowView.HideWindow();
				popTextView.HideWindow();
			}
			else
			{
				boolean ret = false;
				
				
				/*ret = NetStat.ping();
				if(ret==false)
				{ //网络断开
					m_warningMsgTag = false;
					m_tableShowView.m_textView.stopScroll();
					m_tableShowView.HideWindow();
					popTextView.HideWindow();
				}
				else if(ret==true)
				{  //网络连接
					m_warningMsgTag = true;
					updateMsg();
				}*/
				m_warningMsgTag = true;
				updateMsg();
			}
			if(CommsetActivity.isActive == true)
			{
				//发送广播
				Intent intent=new Intent();
				intent.setAction("com.ljq.activity.CountService");
				sendBroadcast(intent);
			}
			
			/*
			if(m_warningMsgTag == false)
			{
				Intent intent=new Intent();
				intent.setAction(MessageAction.MESSAGE_CONNECT_DISCONNECT);
				sendBroadcast(intent);
			}
			*/
			checkNetTag = false;
		}
		return m_warningMsgTag;
	}
	
	private Runnable checkNetAble = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(CheckInternetStat()==true)
			{   //当前网络畅通，1小时后再进行测试
				stpe.schedule(checkNetAble, 60*60, TimeUnit.SECONDS);
			}
			else
			{
				//当前网络不通，5分钟后测试
				stpe.schedule(checkNetAble, 5*60, TimeUnit.SECONDS);
			}	
		}
	};
	
	/**
	 * 
	* @Title:       CheckInternetConnecting
	* @Description: 检测网络连接状态
	* @param           
	* @return       void   
	* @throws
	* @date         2015-4-15 下午01:50:41
	 */
	private void CheckInternetConnecting()
	{
		stpe = new ScheduledThreadPoolExecutor(1);
		stpe.schedule(checkNetAble, 10, TimeUnit.SECONDS);
	}

	public static long DateCompare(String s1,String s2) throws java.text.ParseException  {
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		   java.util.Date now = df.parse(s1);
		   java.util.Date date=df.parse(s2);
		   long l=now.getTime()-date.getTime();
		   long day=l/(24*60*60*1000);
		   long hour=(l/(60*60*1000)-day*24);
		   long min=((l/(60*1000))-day*24*60-hour*60);
		   return l/1000;
	}

	public static class GetRandom {
		// 返回ch1和ch2之间(包括ch1,ch2)的任意一个字符,如果ch1 > ch2，返回'\0'
		public static char getRandomChar(char ch1, char ch2) {
			if (ch1 > ch2)
				return 0;
			// 下面两种形式等价
			return (char) (ch1 + Math.random() * (ch2 - ch1 + 1));
		}

		// 返回a到b之間(包括a,b)的任意一個自然数,如果a > b || a < 0，返回-1
		public static int getRandomInt(int a, int b) {
			if (a > b || a < 0)
				return -1;
			// 下面两种形式等价
			return a + (int) (Math.random() * (b - a + 1));
		}
	}
	
	    private void RandomUpdateTimer(int startTime, int endTime)  
	    {
	    	//计算随机时间，并保存   //得到当前time
			Calendar c = Calendar.getInstance();
			int mHour = c.get(Calendar.HOUR_OF_DAY) * 60 * 60;
			int mMinute = c.get(Calendar.MINUTE) * 60;
			int mSecond = c.get(Calendar.SECOND);
			 //当天当前时间的总秒数
			int curTotalTime = mHour + mMinute + mSecond;  
			//获得一个指定时间内的随机时间  getRandomInt 获得一个范围内的随机数字
			int updateTime =GetRandom.getRandomInt(startTime, endTime);   
			
			long delayTime=0;
			if(curTotalTime<updateTime)
			{
				delayTime =  ((long)updateTime-(long)curTotalTime)+30*60;
			}
			else
			{
				//计算上传数据，还要等多久
				delayTime = ((long)curTotalTime-(long)updateTime)%30*60;// 
			}
	//		Log.e("tag",  " updatetime : "+updateTime+"curTotalTime :  " +curTotalTime+ "delayTime : "+delayTime);
			
	    	handler.postDelayed(UpdateTimerRunnable, delayTime*1000);  	
	    }
	    
		    
	  /**
	   *  计算+上传 考勤数据到蓝色互动服务器
	   */
		private void RandomUpdateAttendance()
		{	
			RandomUpdateTimer(6*60*60, 6*60*60+30*60);//计算随机上传时间，启动定时器   取值在6点到6点半之间
		}

		/**
		 * 
		* @Title:       initFingerThread
		* @Description: 初始化指纹模块操作线程
		* @param           
		* @return       void   
		* @throws
		* @date         2015-4-15 下午01:45:13
		 */
		void initFingerThread()
		{
			m_getFingerThread = new GetFingerThread();
			m_getFingerThread.setPriority(Thread.MAX_PRIORITY); // 1
			m_getFingerThread.start();
			m_getFingerThread.threadStatic = 2;
			serverRuning = true;
		}
		
		/**
		* @Title:       initDoorControl
		* @Description: 初始化门禁功能
		* @param           
		* @return       void   
		* @throws
		* @date         2015-4-15 下午01:46:43
		 */
		void initDoorControl()
		{
			//门禁功能
			try {
				mSerialPort = mApplication.getSerialPort();
				mOutputStream = mSerialPort.getOutputStream();
			} catch (SecurityException e) {
				DisplayError(R.string.error_security);
			} catch (IOException e) {
				DisplayError(R.string.error_unknown);
			} catch (InvalidParameterException e) {
				DisplayError(R.string.error_configuration);
			}
		}
		
		/**
		 * 
		* @Title:       initDB
		* @Description: 初始化数据库
		* @param           
		* @return       void   
		* @throws
		* @date         2015-4-15 下午01:49:34
		 */
		void initDB()
		{
			//创建数据库
			DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
			dbAdapter.open();
			dbAdapter.createDB();
			dbAdapter.close();
			dbAdapter = null;
		}
		
		/**
		 * 
		* @Title:       initSound
		* @Description: 初始化音量大小
		* @param           
		* @return       void   
		* @throws
		* @date         2015-4-15 下午01:51:27
		 */
		void initSound()
		{
			int m_voiceValue = sharedPreferences.getInt("voice", 4);
			//音量调节
			AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, m_voiceValue, 0);
			audiomanage.setStreamVolume(AudioManager.STREAM_SYSTEM, m_voiceValue/2, 0);
		}
		
	@Override	
	public void onCreate() {
		super.onCreate();
		
		initReceiver();
				
		// 打开指纹模块
		OpenDevice();
	
		initFingerThread();
		
		mApplication = (Application) getApplication();
		msgQueue = new LinkedList<String>();  
		g_context = getApplicationContext();
	
		sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
		
	//	initDoorControl();
		
		popTextView = new TableShowView(this);
		popTextView.fun(-200, -400, 1280, 160, false);
		// tcp服务器密码验证
		m_tableShowView = new TableShowView(this);
		m_tableShowView.fun(0, 700, 1280, 50, true);
		m_tableShowView.HideWindow();
		 
		updateMsg();  
		
		initSound();
		RegPlaySoundFile();
		
		initDB();
		
	//	GetAttendanceTimer();  //获得考勤时间规则
	//	new UpdateAttendanceAsyncTask().execute();   //立即上传考勤数据
		//上传考勤数据到服务器,蓝色互动服务
		RandomUpdateAttendance();
		
		mThreadServer = new Thread(mcreateRunnable);
		mThreadServer.start();
		
		m_netMsgProcThread = new NET_MSG_Proc();
		m_netMsgProcThread.start();    
	    		
		// 访问量相关  星际服务器
		// sendBroadcast(new Intent(MessageAction.MESSAGE_SEND_PAGEVIEWS));
		sendBroadcast(new Intent(MessageAction.MESSAGE_NO_OPERATION_CHANGE));
		
		// 检查wifi 并打开热点
		checkWifiThread = new Thread(checkWifiConnectStat);
		checkWifiThread.start();
		
		CheckInternetConnecting();
				
		//获得一个指定时间内的随机时间  getRandomInt 获得一个范围内的随机数字
		int data = GetRandom.getRandomInt(1, 6*60*60);  //开机之后6小时内，检测升级
		handler.postDelayed(CheckUpdateAPK, data*1000);  //hcm  检测升级+自动升级
	}
	
	/**
	 * 
	* @Title:       RegPlaySoundFile
	* @Description: 注册音频播放文件
	* @param           
	* @return       void   
	* @throws
	* @date         2015-4-15 下午01:48:58
	 */
	void RegPlaySoundFile()
	{
		soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0); 	
		soundMap.put(1, soundPool.load( this, R.raw.right, 1));   
        soundMap.put(2, soundPool.load( this, R.raw.failer, 1));  
        soundMap.put(3, soundPool.load( this, R.raw.exit, 1));  
        soundMap.put(4, soundPool.load( this, R.raw.limit, 1)); 
        soundMap.put(5, soundPool.load( this, R.raw.please, 1));   
        soundMap.put(6, soundPool.load( this, R.raw.recov, 1));   
        soundMap.put(7, soundPool.load( this, R.raw.notes, 1));
        soundMap.put(8, soundPool.load( this, R.raw.careful, 1));   
        soundMap.put(9, soundPool.load( this, R.raw.delsucc, 1));   
        soundMap.put(10, soundPool.load( this, R.raw.inputid, 1));   
        soundMap.put(11, soundPool.load( this, R.raw.inputpwd, 1));   
        soundMap.put(12, soundPool.load( this, R.raw.nohaveid, 1));  
        soundMap.put(13, soundPool.load( this, R.raw.operationsucc, 1));  
        soundMap.put(14, soundPool.load( this, R.raw.regfailer, 1));
        soundMap.put(15, soundPool.load( this, R.raw.regsucc, 1));   
        soundMap.put(16, soundPool.load( this, R.raw.server, 1));   
        soundMap.put(17, soundPool.load( this, R.raw.welcome, 1));
        soundMap.put(18, soundPool.load( this, R.raw.isrecov, 1));
        soundMap.put(19, soundPool.load( this, R.raw.addlimit, 1));
        soundMap.put(20, soundPool.load( this, R.raw.dellimit, 1));
        soundMap.put(21, soundPool.load( this, R.raw.ding, 1));
        soundMap.put(22, soundPool.load( this, R.raw.notconnect, 1));
        soundMap.put(23, soundPool.load( this, R.raw.longmsg, 1));
        soundMap.put(24, soundPool.load( this, R.raw.shortmsg, 1));
        soundMap.put(25, soundPool.load( this, R.raw.noattendance, 1));
        soundMap.put(26, soundPool.load( this, R.raw.haveamsg, 1));
        soundMap.put(27, soundPool.load( this, R.raw.inputname, 1));
        soundMap.put(28, soundPool.load( this, R.raw.wifistart, 1));
        soundMap.put(29, soundPool.load( this, R.raw.wifistop, 1));
        soundMap.put(30, soundPool.load( this, R.raw.wifiname, 1));
        soundMap.put(31, soundPool.load( this, R.raw.early, 1));
        soundMap.put(32, soundPool.load( this, R.raw.goodbye, 1));
        soundMap.put(34, soundPool.load( this, R.raw.jiaban, 1));
        soundMap.put(35, soundPool.load( this, R.raw.late, 1));
        soundMap.put(36, soundPool.load( this, R.raw.morning, 1));
        soundMap.put(37, soundPool.load( this, R.raw.netok, 1));
        soundMap.put(38, soundPool.load( this, R.raw.netdisconnect, 1));
        soundMap.put(39, soundPool.load( this, R.raw.happy, 1));
	}
	
	
	void GetAttendanceTimer()
	{
		g_attendance_timer = sharedPreferences.getInt("ATTENDANCE_TIMER", 0);
		if(g_attendance_timer == 2)
		{// 双班次考勤
			g_attendanceTimer1.m_startTime = sharedPreferences.getString("startTime1", "00:00")+":00";
			g_attendanceTimer1.m_endTime = sharedPreferences.getString("endTime1", "12:30"+":00");
			g_attendanceTimer1.m_overTime = sharedPreferences.getString("overTime1", null);
			g_attendanceTimer1.m_noLateTime = sharedPreferences.getInt("noLateTime1", 0);
			g_attendanceTimer1.m_beginHour = sharedPreferences.getInt("beginHour1", 9);
			g_attendanceTimer1.m_beginMinute = sharedPreferences.getInt("beginMinute1", 30);
			g_attendanceTimer1.m_endHour = sharedPreferences.getInt("endHour1", 12);
			g_attendanceTimer1.m_endMinute = sharedPreferences.getInt("endMinute1", 0);
			g_attendanceTimer1.m_moreTime1 = sharedPreferences.getInt("moreTime1", 0);
			g_attendanceTimer1.m_moreTime2 = sharedPreferences.getInt("moreTime2", 0);
			g_attendanceTimer1.m_startTimeHour = sharedPreferences.getInt("startTimeHour1", 0);
			g_attendanceTimer1.m_startTimeMinute = sharedPreferences.getInt("startTimeMinute1", 0);
			g_attendanceTimer1.m_endTimeHour = sharedPreferences.getInt("endTimeHour1", 12);
			g_attendanceTimer1.m_endTimeMinute = sharedPreferences.getInt("endTimeMinute1", 30);
			g_attendanceTimer2.m_startTime = sharedPreferences.getString("startTime2", "12:30"+":00");
			g_attendanceTimer2.m_endTime = sharedPreferences.getString("endTime2", "23:59"+":00");
			g_attendanceTimer2.m_overTime = sharedPreferences.getString("overTime2", null);
			g_attendanceTimer2.m_noLateTime = sharedPreferences.getInt("noLateTime2", 0);
			g_attendanceTimer2.m_beginHour = sharedPreferences.getInt("beginHour2", 13);
			g_attendanceTimer2.m_beginMinute = sharedPreferences.getInt("beginMinute2", 0);
			g_attendanceTimer2.m_endHour = sharedPreferences.getInt("endHour2", 17);
			g_attendanceTimer2.m_endMinute = sharedPreferences.getInt("endMinute2", 30);
			g_attendanceTimer2.m_moreTime1 = sharedPreferences.getInt("moreTime3", 0);
			g_attendanceTimer2.m_moreTime2 = sharedPreferences.getInt("moreTime4", 0);
			g_attendanceTimer2.m_startTimeHour = sharedPreferences.getInt("startTimeHour2", 12);
			g_attendanceTimer2.m_startTimeMinute = sharedPreferences.getInt("startTimeMinute2",30);
			g_attendanceTimer2.m_endTimeHour = sharedPreferences.getInt("endTimeHour2", 23);
			g_attendanceTimer2.m_endTimeMinute = sharedPreferences.getInt("endTimeMinute2", 59);
		}
		else
		{// 单班次考勤
			g_attendanceTimer1.m_startTime = sharedPreferences.getString("startTime3", "00:00"+":00");
			g_attendanceTimer1.m_endTime = sharedPreferences.getString("endTime3", "23:59"+":00");
			g_attendanceTimer1.m_overTime = sharedPreferences.getString("overTime3", null);
			g_attendanceTimer1.m_noLateTime = sharedPreferences.getInt("noLateTime3", 0);
			g_attendanceTimer1.m_beginHour = sharedPreferences.getInt("beginHour3", 9);
			g_attendanceTimer1.m_beginMinute = sharedPreferences.getInt("beginMinute3", 0);
			g_attendanceTimer1.m_endHour = sharedPreferences.getInt("endHour3", 17);
			g_attendanceTimer1.m_endMinute = sharedPreferences.getInt("endMinute3", 30);
			g_attendanceTimer1.m_moreTime1 = sharedPreferences.getInt("moreTime5", 0);
			g_attendanceTimer1.m_moreTime2 = sharedPreferences.getInt("moreTime6", 0);
			g_attendanceTimer1.m_startTimeHour = sharedPreferences.getInt("startTimeHour3", 0);
			g_attendanceTimer1.m_startTimeMinute = sharedPreferences.getInt("startTimeMinute3", 0);
			g_attendanceTimer1.m_endTimeHour = sharedPreferences.getInt("endTimeHour3", 23);
			g_attendanceTimer1.m_endTimeMinute = sharedPreferences.getInt("endTimeMinute3", 59);
		}
	}
	
	
	/**
	 * 重新打开wifi， 先关闭，后打开
	* @Title:       reConnect
	* @Description: TODO
	* @param           
	* @return       void   
	* @throws
	* @date         2015-5-6 上午10:27:00
	 */
	/*
	public void reConnect()
	{
		m_wiFiAdmin = WifiAdmin.getInstance(g_context);
		if(m_preConnectNetType == ConnectivityManager.TYPE_WIFI)
		{
			 WifiManager localWifiManager;//提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
			 localWifiManager = (WifiManager)g_context.getSystemService(Context.WIFI_SERVICE);
			 int stat =localWifiManager.getWifiState();  
			
			if(stat == 3)  //当前wifi打开
			{
				SaveConfig("WIFI_ORDER", "begin");   //begin是开始，finish是结束
				m_wiFiAdmin.closeWifi();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SaveConfig("WIFI_ORDER", "finish");
				m_wiFiAdmin.OpenWifi();
			}
		}
	}
	*/

	public static boolean isNetworkConnected(Context context) 
	{  
	    if (context != null) {  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		    NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
		    if (mNetworkInfo != null) {  
		       netConnectType = mNetworkInfo.getType();
		       if(netConnectType == ConnectivityManager.TYPE_WIFI)
		       { //wifi
		    	   m_preConnectNetType = ConnectivityManager.TYPE_WIFI;
		       }
		       else if(netConnectType == ConnectivityManager.TYPE_ETHERNET)
		       { //以太网
		    	   m_preConnectNetType = ConnectivityManager.TYPE_ETHERNET;   
		       }
		       else if(netConnectType==ConnectivityManager.TYPE_MOBILE)
		       {  //3g网卡
		    	   m_preConnectNetType = ConnectivityManager.TYPE_MOBILE;
		       }
		       return mNetworkInfo.isAvailable();  
		    }  
		}  
		return false;  
	} 

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		flags = START_STICKY;
	
	//	MyLog.w("test", "onStartCommand  restart servivce ");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//MyLog.w("test", " onDestroy service ");
		CloseDevice();
		unregisterReceiver(receiver);
		System.exit(0);
		
		soundPool.release();
		
		mApplication.closeSerialPort();
		mSerialPort = null;
		
		Intent localIntent = new Intent();
		localIntent.setClass(this, InteractionService.class); //���ʱ��������Service
		this.startService(localIntent);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	 public void onRebind(Intent intent) {//���°�
		// TODO Auto-generated method stub
		super.onRebind(intent);
	 }
	
	int this_id = -1;
	public void PlaySound(int id)
	{
		if(birthdayTag == true && m_warningMsgTag==true)
		{
			id = 39;
		}
		if(this_id != -1)
		{	
			soundPool.stop(this_id);
		}
		else
			soundPool.stop(0);
		
		if(id==39)
		{
			playHappyBirthday();
		}
		else
		{
			if(mp==null)
				this_id = soundPool.play(soundMap.get(id), 1, 1, 1, 0, 1);
		}
	}
		
	//播放生日快乐音乐
	public void playHappyBirthday()
	{
		try {
			if(mp==null)
			{
		    	mp = MediaPlayer.create(g_context, R.raw.happy);
		    	if(mp!=null)
		    	{
		    		mp.start();
		    	}
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//mp3播放完成
		mp.setOnCompletionListener(new OnCompletionListener(){
	         @Override
	         public void onCompletion(MediaPlayer mpa) {
	             mp.release();
	             
	             mp = null;
	         }
	      });
	}
	
	@Override
    public boolean onUnbind(Intent intent) {//����
         // TODO Auto-generated method stub
		return super.onUnbind(intent);
    }
	
	private void SetMsgID(String id)
	{
		msgQueue.clear();
		SetFlagSwitch(false);
		msgQueue.offer(id); 	
		
	    if(m_getFingerThread.threadStatic == 2)
	    {
		   while(m_getFingerThread.getState() != Thread.State.WAITING)
			{
				try {
				    SetFlagSwitch(false);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
	    if(m_getFingerThread.threadStatic ==2 || m_getFingerThread.threadStatic==4 || m_getFingerThread.threadStatic == 6)
	    {
		   synchronized (curFinagerObj) 
	    	{
	    		curFinagerObj.notify();
	    	} 
	    }	
	}
		
	 //存储配置文件
	  boolean SaveConfig(String key , String value)
	 {
		 SharedPreferences shared = getSharedPreferences("config", Context.MODE_PRIVATE);
		 Editor editor = shared.edit();
		 editor.putString(key, value);
		 if(editor.commit()==true)
		 {
		 }
		 else
		 {
			 return false;
		 }

		 editor.putString("END", value);
		 editor.commit();
		 return true;
	 }
	 
	Timer longMsgTimer=null;
	static int longTimeMsgCount = 0;
	public class InteractionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		
			if (action.equals(MessageAction.MESSAGE_LOCALE)) {
				intent.getExtras();
			}
			else if(action.equals(MessageAction.MESSAGE_TCP_POP_NETMSG))
			{
				if(m_warningMsgTag)
				{
		            if(msgStr==null)
		            {  //这次启动中，没有设置过消息，所以，要从配置文件中读取数据，查看以前启动中，有没有设置过消息。
		            	sharedPreferences = getApplicationContext().getSharedPreferences("config",Context.MODE_PRIVATE);
		            	msgStr = sharedPreferences.getString("MSG", null);
		            	msgTag = sharedPreferences.getInt("APPEND_TAG",0);
		            }
					if((msgTag&0x2)>>1==1 )
					{   //有设置过弹出消息
						popTextView.m_textView.setText(msgStr);
		
						popTextView.ShowWindow(0);
						
						handler.removeCallbacks(StopPopMsgWindow);
						handler.postDelayed(StopPopMsgWindow, 1000 * 5);
					}
				}
			}
			else if (action.equals(MessageAction.MESSAGE_TCP_SET_NETMSG)) {
				Bundle bundle = intent.getExtras();
				int timeCount = bundle.getInt("timeCount");
				int colorR = bundle.getInt("COLOR_R");
				int colorG = bundle.getInt("COLOR_G");
				int colorB = bundle.getInt("COLOR_B");
				msgTag = bundle.getInt("APPEND_TAG");
				
				m_tableShowView.m_textView.setTextColor(Color.rgb(colorR,colorG, colorB));
				popTextView.m_textView.setTextColor(Color.rgb(colorR, colorG, colorB));
				
				sharedPreferences = getApplicationContext().getSharedPreferences("config",Context.MODE_PRIVATE);
				int tmpTag = msgTag&0x1;
				if(tmpTag==0)
				{
					msgStr=bundle.getString("MSG");
				}
				else
				{
					msgStr = sharedPreferences.getString("MSG", "");
					msgStr +="   "+bundle.getString("MSG");
				}
				if(m_warningMsgTag)
				{
					if((msgTag&0x4)>>2==1)
					{   //滚动消息
						m_tableShowView.m_textView.setText(msgStr);
						m_tableShowView.startShow();
					}
					else
					{
						m_tableShowView.m_textView.stopScroll();
						m_tableShowView.HideWindow();
					}
					
					if((msgTag&0x2)>>1==1)
					{  //弹出消息
						popTextView.m_textView.setText(msgStr);
					//	System.out.println("弹出开始");
					}
					else
					{
						popTextView.HideWindow();
					}
				}
				else
				{
					m_tableShowView.m_textView.stopScroll();
					m_tableShowView.HideWindow();
					popTextView.HideWindow();
				}
				handler.removeCallbacks(NetMsgRuanable);
				handler.postDelayed(NetMsgRuanable, (long) 1000*timeCount);
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateStr = sdf.format(calendar.getTime());

				Editor editor;
				editor = sharedPreferences.edit();
				editor.putInt("COLOR_R", colorR);
				editor.putInt("COLOR_G", colorG);
				editor.putInt("COLOR_B", colorB);
				editor.putInt("timeCount", timeCount);
				editor.putString("MSG", msgStr);
				editor.putString("startTime", dateStr);
				editor.putInt("APPEND_TAG", msgTag);
				editor.commit();

			} else if (action.equals(MessageAction.MESSAGE_TCP_STOP_NETMSG)) {
				m_tableShowView.m_textView.stopScroll();
				m_tableShowView.HideWindow();
				popTextView.HideWindow();
				
				sharedPreferences = getSharedPreferences("config",Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.putString("MSG", null);
				editor.putInt("APPEND_TAG", 0);
				editor.commit();
				msgStr=null;
					
			} else if (action.equals(MessageAction.MESSAGE_SHOW_TEST)) {
				ToastUtil.showToast(getApplicationContext(), intent.getExtras().getString("msg"), Toast.LENGTH_LONG);
			} else if (action.equals(MessageAction.MESSAGE_SHOW_TEST_3MSG)) {
				Bundle m_bundle =  intent.getExtras();
				final String msg1 = m_bundle.getString("msg1");
				final String msg2 = m_bundle.getString("msg2");
				final String msg3 = m_bundle.getString("msg3");
				final boolean birthdaTag = m_bundle.getBoolean("birthdayTag");  //是否今天过生日
				final int birthdaySwitch = m_bundle.getInt("birthdaySwitch");
				if(birthdaTag==true && birthdaySwitch==1)
				{  //今天过生日
					if(intent.getExtras().getString("msg1").equals("")==false)
					{
						int colorR = sharedPreferences.getInt("COLOR_R", 255);
						int colorG = sharedPreferences.getInt("COLOR_G", 0);
						int colorB = sharedPreferences.getInt("COLOR_B", 0);
						popTextView.m_textView.setText(happyBirthdayStr);
						popTextView.m_textView.setTextColor(Color.rgb(colorR,colorG, colorB));
						popTextView.ShowWindow(0);
						
						handler.removeCallbacks(StopPopMsgWindow);
						handler.postDelayed(StopPopMsgWindow, 1000 * 7);	
					}
						
			 		if(longMsgTimer!=null)
			 		{
			 			longMsgTimer.cancel();
			 			longMsgTimer=null;
			 		}
			 		longMsgTimer = new Timer();
			 		longMsgTimer.schedule(new TimerTask()
			 		{
						public void run(){
							Intent m_intent = new Intent(MessageAction.MESSAGE_LONG_MSG);
							m_intent.putExtra("msg1", msg1);
							m_intent.putExtra("msg2", msg2);
							m_intent.putExtra("msg3", msg3);
							sendBroadcast(m_intent);
						}
					}, 1, 1000);
				}
				else
				{  //今天不过生日
					ToastUtil.showToast3Text(getApplicationContext(), "认证通过", msg2, msg3, Toast.LENGTH_LONG, birthdayTag);
					PlaySound(1);
					
					
					/*//判断是否需要提示网络未通 
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(m_warningMsgTag==false)
					    	{
					    		Intent intent = new Intent();
								intent.setAction(MessageAction.MESSAGE_TCP_SHOW_WARNING_MSG);
								intent.putExtra("pkg", getPackageName());
								intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); 
							
								sendBroadcast(intent);
								
								PlaySound(24);	
					    	}
						}
					}, 1000*3);*/
				}		
				birthdayTag = false; 
		 		//注册
			} else if (action.equals(MessageAction.MESSAGE_SHOW_TEST_LONG)) {
				String msg1 = intent.getExtras().getString("msg1");
				ToastUtil.showToastTextLong(getApplicationContext(), msg1, Toast.LENGTH_LONG);
				msg1 = null;
			}
			else if (action.equals(MessageAction.MESSAGE_TCP_SHOW_WARNING_MSG))
			{
				ToastUtil.showWarningMsg(context, null, Toast.LENGTH_LONG);
				handler.removeCallbacks(TwiceShowWaringRuanable);
				handler.postDelayed(TwiceShowWaringRuanable, 1000*3);
			}
			else if (action.equals(MessageAction.MESSAGE_SHOW_WARING))
			{
				 String msg1 = intent.getExtras().getString("msg1");
				 String msg2 = intent.getExtras().getString("msg2");
				 String msg3 = intent.getExtras().getString("msg3");
				 String msg4 = intent.getExtras().getString("msg4");
				 ToastWaring.showToast(context, msg1, msg2, msg3, msg4,  Toast.LENGTH_LONG);
			}
			else if (action.equals(MessageAction.MESSAGE_SET_FINGER)) 
			{ 
				m_getFingerThread.curSelectItemStat = 1;
			    m_admin = intent.getExtras().getInt("msg");
				m_getFingerThread.reg_id = intent.getExtras().getInt("fingerID");
				m_getFingerThread.name = intent.getExtras().getString("Name");
			
				SetMsgID("1");
			}
			else if (action.equals(MessageAction.MESSAGE_CLEAR_FINGER)) 
			{
				m_getFingerThread.curSelectItemStat =3;
				
				SetMsgID("3");
			}
			else if (action.equals(MessageAction.MESSAGE_DELETE_FINGER))
			{
				m_getFingerThread.curSelectItemStat =4;
				msgQueue.clear();
				
			    int id = intent.getExtras().getInt("id");
			    g_id = id;
			  
				SetMsgID("5");
			}
			else if (action.equals(MessageAction.MESSAGE_RECOVER_DEVICE)) 
			{   //恢复出厂设置
				SetMsgID("6");
			}
			else if (action.equals(MessageAction.MESSAGE_NO_OPERATION_CHANGE)) 
			{ //60s 
				if(checkAPP(context, SOFT__PACKAGE_NAME))
				{
				//	System.out.println("60s 无操作 跳转");
					handler.removeCallbacks(ChangeViewRunnable);
					handler.postDelayed(ChangeViewRunnable, 1000*60);
				}
				else
				{
					System.out.println("未安装广告软件！");
				}
			}
			
			else if(action.equals(MessageAction.MESSAGE_ACTIVITY_ACT))
			{
				openHolder(false); //停止广播播放软件切换
				//切换界面
				Intent inte=new Intent(getBaseContext(),TestRolateAnimActivity.class);
		        inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  );
		        startActivity(inte);
			}
			else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
			{  //网络状态改变
			//	System.out.println("收到网络状态改变消息");
				boolean tmpStat = isNetworkConnected(getApplicationContext());
				if(tmpStat == true&&m_netStat!=true)
				{  //网络连通
					m_warningMsgTag = true;
					CheckInternetStat();
					if(tcp_thread_stat == true)
					{
						synchronized (tcpServerThreadObj) {
						//	System.out.println("触发网络状态改变，服务器重新启动");
							tcpServerThreadObj.notify();
						}
					}
				}
				else if(tmpStat == false)
				{  //网络不通
			//		System.out.println("网络不通");
					m_warningMsgTag = false;
					m_tableShowView.m_textView.stopScroll();
					m_tableShowView.HideWindow();
					popTextView.HideWindow();
					CheckInternetStat();
					if(m_PooledRemoteFileServer!=null&&m_netStat ==true)
					{//	System.out.println("网络不通，服务器停止");
						m_PooledRemoteFileServer.serverTag = false;
						m_PooledRemoteFileServer.quit();
					}
					m_netStat = false;
					
					/*   //自动连接wifi
					new Thread(new Runnable()  
			        {  
			            @Override  
			            public void run()  
			            {  
			            	synchronized (netReCreateObj) 
			       	    	{
			            		reConnect();
			       	    	} 
			            }  
			        }).start();  
					*/
				}
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_EXIT_SOUND))
			{	
				PlaySound(6);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_NOTES_SOUND))
			{
				PlaySound(7);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_CAREFUL_SOUND))
			{
				PlaySound(8);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_INPUTID_SOUND))
			{
				PlaySound(10);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_INPUTPWD_SOUND))
			{
				PlaySound(11);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_NOHAVEID_SOUND))
			{
				PlaySound(12);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_REGFAILER_SOUND))
			{
				PlaySound(14);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_REGSUCC_SOUND))
			{
				PlaySound(15);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_OPERATIONSUCC_SOUND))
			{
				PlaySound(13);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_RECOVER_DEVICE))
			{
				PlaySound(18);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_ADDLIMIT_SOUND))
			{
				PlaySound(19);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_DELLIMIT_SOUND))
			{
				PlaySound(20);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_DING_SOUND))
			{
				PlaySound(21);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_QUIT_SOUND))
			{
				PlaySound(3);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_INPUT_NAME))
			{
				PlaySound(27);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_WIFI_AP_CREATE))
			{
				PlaySound(28);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_WIFI_AP_STOP))
			{
				PlaySound(29);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_WIFI_NAME))
			{
				PlaySound(30);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_EARLY))
			{
				PlaySound(31);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_GOODBYE))
			{
				PlaySound(32);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_LATE))
			{
				PlaySound(35);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_GOODMORNING))
			{
				PlaySound(36);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_NETOK))
			{
				PlaySound(37);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_NETDISCONNECT))
			{
				PlaySound(38);
			}
			else if(action.equals(MessageAction.MESSAGE_PLAY_SET_SOUND))
			{
				//音量调节
				AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, 4, 0);
				audiomanage.setStreamVolume(AudioManager.STREAM_SYSTEM, 2, 0);
			} 
			else if (action.equals(MessageAction.MESSAGE_SEND_PAGEVIEWS)) 
			{
				Calendar c = Calendar.getInstance();
				int mHour = c.get(Calendar.HOUR_OF_DAY) * 60 * 60;
				int mMinute = c.get(Calendar.MINUTE) * 60;
				int mSecond = c.get(Calendar.SECOND);
				int curTotalTime = mHour + mMinute + mSecond;

				/*
				 * //随机数应在10点-16点之间 10*60*60 16*60*60 //生成随机数 //获得当前时间 int
				 * startTime = 10*60*60;//秒 int endTime = 16*60*60; //秒 int
				 * delayTime=0; if(startTime<=curTotalTime&&
				 * curTotalTime<=endTime) { endTime = endTime-curTotalTime;
				 * delayTime = GetRandom.getRandomInt(0, endTime); } else
				 * if(endTime<curTotalTime) { startTime =
				 * 24*60*60-curTotalTime+startTime; endTime =
				 * 24*60*60-curTotalTime+endTime; delayTime =
				 * GetRandom.getRandomInt(startTime, endTime); }
				 * System.out.println
				 * ("#####                      pageViewCount :  "+delayTime);
				 */

				// 当前时间早于10点，固定时间10点发送;如果当前时间已经超过10点，延迟10秒发送
				int startTime = 10 * 60 * 60;// 秒
				int delayTime = 0;
				if (curTotalTime <= startTime) {
					// 当天10点发送
					delayTime = startTime - curTotalTime;
				} else if (curTotalTime > startTime) {
					delayTime = 24 * 60 * 60 - curTotalTime + startTime;
				}
				handler.postDelayed(pageViewRunalbe, (long)(delayTime * 1000));
			}
			else if(action.equals(MessageAction.MESSAGE_TCP_SHOW_UPDATESTAT))
			{
				if(m_warningMsgTag)
				{
					updateMsg();
				}
				else
				{
					m_tableShowView.m_textView.stopScroll();
					m_tableShowView.HideWindow();
					popTextView.HideWindow();
				}
			}
			else if(action.equals(MessageAction.MESSAGE_WIFI_AP_CREATE))
			{ //创建wifi热点并打开
				WIFI_AP_HEADER=intent.getExtras().getString("apWifiName");
				WIFI_AP_PASSWORD=intent.getExtras().getString("apWifiPwd");
				new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            {  
		            	synchronized (apWifiCreateObj) 
		       	    	{
		            	 	int ret = OpenWifiAP(0);
							switch(ret)
							{
							case 0: //创建热点成功
								SaveConfig("apWifi", "open");
								sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmOpen"));
								break;
							case 2:
								SaveConfig("apWifi", "open");
								sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmHave"));
								break;
							case -1:
								SaveConfig("apWifi", "close");
								sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmFailer")); //创建失败
								break;
							case -2:
								SaveConfig("apWifi", "close");
								sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_disconnect")); //创建失败, 网络不通
								break;
							case -3:
								SaveConfig("apWifi", "close");
								sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_wifiopen")); //创建失败, wifi打开中
								break;
								default:
									break;
							}
		       	    	} 
		            }  
		        }).start();  
			}
			else if(action.equals(MessageAction.MESSAGE_WIFI_AP_CLOSE))
			{  //关闭wifi热点
				if(m_wiFiAdmin.createWiFiAP(m_wiFiAdmin.createWifiInfo(m_wiFiAdmin.getApSSID(), "81028066", 3, "ap"),false)==true)
				{
					System.out.println("关闭热点成功");
					SaveConfig("apWifi","close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmClose"));
					handler.removeCallbacks(SetWifiAPRuannable);
				}
				else
					System.out.println("关闭热点失败");
			}
			else if(action.equals(MessageAction.MESSAGE_WIFI_AP_CHECK))
			{
				//上一次wifi热点打开关闭的状态
				String apWifiStat = sharedPreferences.getString("apWifi", "default"); 
				if(apWifiStat.equals("open") || apWifiStat.equals("default"))
				{ //上次热点打开
					int ret = OpenWifiAP(2);
					switch(ret)
					{
					case 0: //创建热点成功
						SaveConfig("apWifi", "open");
						sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmOpen"));
						break;
					case 2:
						SaveConfig("apWifi", "open");
						sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmHave"));  //已经有热点
						break;
					case 3:
						SaveConfig("apWifi", "close");
						sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmNoHave"));  //没有热点
						break;
					case -1:
						SaveConfig("apWifi", "close");
						sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmFailer")); //创建失败
						break;
					case -2:
						SaveConfig("apWifi", "close");
						sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_disconnect")); //创建失败, 网络不通
						break;
					case -3:
						SaveConfig("apWifi", "close");
						sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_wifiopen")); //创建失败, wifi打开中
						break;
					default:
						break;
					}
				}
				else if(apWifiStat.equals("close"))
				{  //上次热点关闭
					if(m_wiFiAdmin.getWifiApState()==true) 
					{
						sendBroadcast(new Intent(MessageAction.MESSAGE_WIFI_AP_CLOSE)); //创建失败, 网络不通
					}
				}
			}
			/*
			//重新连接wifi 开关wifi
			else if(action.equals(MessageAction.MESSAGE_CONNECT_DISCONNECT))
			{	
			//	System.out.println("### reConnect 2");
				new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            {  
		            	synchronized (netReCreateObj) 
		       	    	{
		            		reConnect();
		       	    	} 
		            }  
		        }).start();  
			}
			*/
			else if(action.equals(MessageAction.MESSAGE_LONG_MSG))
			{
				Bundle m_bundle = intent.getExtras();
				ToastUtil.showToast3TextLongTime(getApplicationContext(), "认证成功", m_bundle.getString("msg2"), m_bundle.getString("msg3"), Toast.LENGTH_SHORT, true);
				longTimeMsgCount++;
				if(longTimeMsgCount>=6)
				{
					if(longMsgTimer!=null)
			 		{
			 			longMsgTimer.cancel();
			 			longMsgTimer=null;
			 			longTimeMsgCount=0;
			 		}
				}
			}
		}
	}
	

	/**
	 * 
	* @Title:       initReceiver
	* @Description: 初始化、注册广播接收器并
	* @param           
	* @return       void   
	* @throws
	* @date         2015-4-15 下午01:52:42
	 */
	public void initReceiver() {
		receiver = new InteractionReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		filter.addAction(MessageAction.MESSAGE_LOCALE);
		filter.addAction(MessageAction.MESSAGE_SET_FINGER);
		filter.addAction(MessageAction.MESSAGE_CUR_FINGER);
		filter.addAction(MessageAction.MESSAGE_CLEAR_FINGER);
		filter.addAction(MessageAction.MESSAGE_DELETE_FINGER);	
		filter.addAction(MessageAction.MESSAGE_SHOW_TEST);
		filter.addAction(MessageAction.MESSAGE_TCP_SET_NETMSG);
		filter.addAction(MessageAction.MESSAGE_SHOW_TEST_3MSG);
		filter.addAction(MessageAction.MESSAGE_NO_OPERATION_CHANGE);
		filter.addAction(MessageAction.MESSAGE_CLOSE_SOCKET);
		filter.addAction(MessageAction.MESSAGE_SERIAL_MESSAGE);
		filter.addAction(MessageAction.MESSAGE_ACTIVITY_ACT);
		filter.addAction(MessageAction.MESSAGE_SHOW_TEST_LONG);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(MessageAction.MESSAGE_SHOW_TEST_LONG);
		filter.addAction(MessageAction.MESSAGE_PLAY_EXIT_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_LIMIT_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_NOTES_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_CAREFUL_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_WELCOME_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_INPUTID_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_INPUTPWD_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_NOHAVEID_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_REGFAILER_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_REGSUCC_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_OPERATIONSUCC_SOUND);
		filter.addAction(MessageAction.MESSAGE_RECOVER_DEVICE);
		filter.addAction(MessageAction.MESSAGE_PLAY_RECOVER_DEVICE);
		filter.addAction(MessageAction.MESSAGE_PLAY_ADDLIMIT_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_DELLIMIT_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_DING_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_QUIT_SOUND);
		filter.addAction(MessageAction.MESSAGE_PLAY_SET_SOUND);
		filter.addAction(MessageAction.MESSAGE_SEND_PAGEVIEWS);
		filter.addAction(MessageAction.MESSAGE_TCP_STOP_NETMSG);
		filter.addAction(MessageAction.MESSAGE_TCP_POP_NETMSG);
		filter.addAction(MessageAction.MESSAGE_TCP_SHOW_WARNING_MSG);
		filter.addAction(MessageAction.MESSAGE_TCP_SHOW_UPDATESTAT);
		filter.addAction(MessageAction.MESSAGE_SHOW_WARING);
		filter.addAction(MessageAction.MESSAGE_SHUT_OEPN_DEVICE);
		filter.addAction(MessageAction.MESSAGE_WIFI_AP_CREATE);
		filter.addAction(MessageAction.MESSAGE_WIFI_AP_CLOSE);
		filter.addAction(MessageAction.MESSAGE_WIFI_AP_CHECK);
		filter.addAction(MessageAction.MESSAGE_CONNECT_DISCONNECT);
		filter.addAction(MessageAction.MESSAGE_PLAY_INPUT_NAME);
		filter.addAction(MessageAction.MESSAGE_PLAY_WIFI_NAME);
		filter.addAction(MessageAction.MESSAGE_PLAY_WIFI_AP_CREATE);
		filter.addAction(MessageAction.MESSAGE_PLAY_WIFI_AP_STOP);
		filter.addAction(MessageAction.MESSAGE_PLAY_NETOK);
		filter.addAction(MessageAction.MESSAGE_PLAY_NETDISCONNECT);
		filter.addAction(MessageAction.MESSAGE_PLAY_LATE);
		filter.addAction(MessageAction.MESSAGE_PLAY_EARLY);
		filter.addAction(MessageAction.MESSAGE_LONG_MSG);
		registerReceiver(receiver, filter);
	}

	public class LocalBinder extends Binder { 
		InteractionService getService() { 
			return InteractionService.this; 
		  } 
		} 
	
	private Runnable	mcreateRunnable	= new Runnable() 
	{
		public void run()
		{	
			synchronized (tcpServerThreadObj) {
				while(m_getFingerThread.getState() != Thread.State.WAITING)
				{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				SleepWape();
				
				String deviceID = Secure.getString(getApplication().getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
				if(isNetworkConnected(getApplicationContext())==true&&m_netStat==false)
				{  //网络已经连通，软件网络不通
				}
				else
				{  //网络不通	
					m_netStat = false;
					try {
						tcp_thread_stat = true;
						tcpServerThreadObj.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		
			while(true)
			{
				m_netStat = true;
				m_PooledRemoteFileServer.serverTag = true;
				getApplicationContext();
				m_PooledRemoteFileServer = new PooledRemoteFileServer(SERVERPORT, 20, getApplicationContext());
				m_PooledRemoteFileServer.setDeviceId(deviceID);
				m_PooledRemoteFileServer.setI(InteractionService.this);
				if(m_PooledRemoteFileServer.GetServerStat() == false)
				{
					try {
					//	System.out.println("#### 打开服务器， 建立监听 ");
						m_PooledRemoteFileServer.acceptConnections();
					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
					
				try {
					tcp_thread_stat = true;
					tcpServerThreadObj.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}
		}
	};
	
	/**
	 * 检测网络连接状态
	 */
	private Runnable	checkWifiConnectStat	= new Runnable() 
	{
		public void run()
		{		
			m_wiFiAdmin = WifiAdmin.getInstance(g_context);
			
			
			//暂时不管wifi状态
			/*
			//上一次wifi打开关闭的状态
			String wifiOrder = sharedPreferences.getString("WIFI_ORDER", "default");
			 WifiManager localWifiManager;//提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
			 localWifiManager = (WifiManager)g_context.getSystemService(Context.WIFI_SERVICE);
			 int stat =localWifiManager.getWifiState();  
			 
			 if(wifiOrder.equals("begin"))
			 {			
				if(stat != 3)
				{
					m_wiFiAdmin.OpenWifi();
					SaveConfig("WIFI_ORDER","finish");
				}
			}
	
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
	
			//上一次wifi热点打开关闭的状态
			String apWifiStat = sharedPreferences.getString("apWifi", "default"); 
			if(apWifiStat.equals("open")==true)
			{ //首次使用软件
				int ret = OpenWifiAP(1);
				switch(ret)
				{
				case 0: //创建热点成功
					SaveConfig("apWifi", "open");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmOpen"));
					break;
				case 2:
					SaveConfig("apWifi", "open");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmHave"));
					break;
				case -1:
					SaveConfig("apWifi", "close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer")); //创建失败
					break;
				case -2:
					SaveConfig("apWifi", "close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_disconnect")); //创建失败, 网络不通
					break;
				case -3:
					SaveConfig("apWifi", "close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_wifiopen")); //创建失败, wifi打开中
					break;
				default:
					break;
				}
			}
			/*
			else if(apWifiStat.equals("default")==true)
			{ //首次使用软件
				int ret = OpenWifiAP(1);
				switch(ret)
				{
				case 0: //创建热点成功
					SaveConfig("apWifi", "open");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmOpen"));
					break;
				case 2:
					SaveConfig("apWifi", "open");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmHave"));
					break;
				case -1:
					SaveConfig("apWifi", "close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer")); //创建失败
					break;
				case -2:
					SaveConfig("apWifi", "close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_disconnect")); //创建失败, 网络不通
					break;
				case -3:
					SaveConfig("apWifi", "close");
					sendBroadcast(new Intent("com.ebanswers.attendance.wifiWarmfailer_wifiopen")); //创建失败, wifi打开中
					break;
				default:
					break;
				}
			}
			*/
			else if(apWifiStat.equals("close"))
			{  //上次热点关闭
				//System.out.println("#### close ");
				sendBroadcast(new Intent(MessageAction.MESSAGE_WIFI_AP_CHECK)); //创建失败, wifi打开中
			}
			else
			{
			}
			
			OPEN_MACHINE_STATIC = 1;
		}
	};
	
	 public static boolean checkAPP(Context context, String packageName) {
		
	        if (packageName == null || "".equals(packageName))
	            return false;
	        try {
	            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
	            return true;
	        } catch (NameNotFoundException e) {
	            return false;
	        }
	    }
	
	private Runnable ChangeViewRunnable = new Runnable() {
		@Override
		public void run() {		
			if(isTopActivity(getApplicationContext()) == false)
			{
				//System.out.println("不跳转");
				return;
			}
			
			openHolder(true); 
		                  	//切换到广告软件中 
            			ComponentName comp = new ComponentName(
            					"com.dsplayer", "com.dsplayer.MainActivity");
            					Intent mintent = new Intent(SOFT__PACKAGE_NAME);	
            					mintent.setComponent(comp);	
            					mintent.setAction("android.intent.action.MAIN");
            					mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            					startActivity(mintent);		
		}
	};

	private Runnable NetMsgRuanable = new Runnable() {
		@Override
		public void run() {
			Intent intent = new Intent();
			intent.setAction(MessageAction.MESSAGE_TCP_STOP_NETMSG);
			sendBroadcast(intent, null); // 广播发送
			handler.removeCallbacks(this);
		//	System.out.println("停止插播消息");
		}
	};
	
	private Runnable TwiceShowWaringRuanable = new Runnable() {
		@Override
		public void run() {
			ToastUtil.showWarningMsg(getBaseContext(), null, Toast.LENGTH_LONG);
		}
	};
	
	private Runnable StopPopMsgWindow = new Runnable() {
		@Override
		public void run() {
			popTextView.HideWindow();
		}
	};

	private Runnable pageViewRunalbe = new Runnable() {
		@Override
		public void run() {
			new Thread() { // 发送post线程
				public void run() {
					Context dsplayerAppContext = null;
					try {
						dsplayerAppContext = getApplicationContext().createPackageContext("com.dsplayer",
										Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(play_id==null)
						play_id = dsplayerAppContext.getSharedPreferences("ebanswers_preferences", Context.MODE_WORLD_READABLE).getString("PLAYER_ID", "");

					DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
					dbAdapter.open();
					long attenDanceCount = dbAdapter.getCount("select count (*) from Attendance");
					dbAdapter.close();
					dbAdapter = null;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("key", "4b99ec1244f5475ab781356aec822463"));
					params.add(new BasicNameValuePair("username", "Ebanswers"));
					NameValuePair par1 = new BasicNameValuePair("type", "访问量");
					NameValuePair par2 = new BasicNameValuePair("player_id", play_id);
					NameValuePair par3 = new BasicNameValuePair("data", "abd");
					String tmp;
					tmp = "" + attenDanceCount;
					NameValuePair par4 = new BasicNameValuePair("code", tmp);
					params.add(par1);
					params.add(par2);
					params.add(par3);
					params.add(par4);
					
				//	String result = RequestUtil.doPost(path, params);
				//	System.out.println("发送后 返回：" + result);
				};
			}.start();

			// 随机数应在10点-16点之间 10*60*60 16*60*60
			// 生成随机数
			// 获得当前时间

			Calendar c = Calendar.getInstance();
			int mHour = c.get(Calendar.HOUR_OF_DAY) * 60 * 60;
			int mMinute = c.get(Calendar.MINUTE) * 60;
			int mSecond = c.get(Calendar.SECOND);
			int delayTime = 24 * 60 * 60;

			handler.removeCallbacks(this);
			handler.postDelayed(this, delayTime);
		};
	};
	
	/*
	boolean UpdateAttendanceAsyncTask()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     
		String date = formatter.format(new java.util.Date());
		System.out.println("立刻上传考勤数据");
		boolean updateTag = false;
		Attendance[] m_Attendance = null;
		long attendanceLen = 0;
		SharedPreferences sharedPreferences = InteractionService.g_context.getSharedPreferences("config",Context.MODE_PRIVATE);
		String lastUpdateTimer = sharedPreferences.getString("lastUpdateTimer", "NULL");
		StringBuffer tmp = new StringBuffer();
		StringBuffer tmpStr = new StringBuffer();
		if(lastUpdateTimer.equals("NULL"))  //没有上传过数据，首次上传
		{	
			tmp.append("select count (*) from Attendance");
			tmpStr.append("select * from Attendance");
		}
		else  //已经上传过数据了，从上次上传数据截止时间上传
		{	
			String m_dateTime1 = "datetime('"+lastUpdateTimer+"')";		    
		    tmp.append("select count (*) from Attendance where datetime(IO_DateTime)>=");
		    tmp.append(m_dateTime1);
		    tmpStr.append("select * from Attendance where datetime(IO_DateTime)>=");
		    tmpStr.append(m_dateTime1);
		}
	
		DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
		dbAdapter.open();
		attendanceLen = dbAdapter.getCount(tmp.toString());
		if (attendanceLen<=0)
		{	
			dbAdapter.close();
			dbAdapter = null;
			return false;
		}
		m_Attendance = new Attendance[(int) (attendanceLen)];
		m_Attendance = dbAdapter.getAttendanceQuery(tmpStr.toString());
		dbAdapter.close();
		dbAdapter = null;
		String path = "http://120.26.113.21:8088/ws/HoopsonAttend.asmx";  //正式
	//	System.out.println("新服 ："+path);
		if(attendanceLen<=0)
		{
			return false;
		}
	
		final int UPDATECOUNT = 500; //单次上传条数
		
		for(int j=0; j<attendanceLen/UPDATECOUNT+1; j++)
		{
			long len=0;
			
			long a=attendanceLen/(long)UPDATECOUNT;
			long b=attendanceLen%(long)UPDATECOUNT;

			if(a==0)
			{
				if(b!=0)
					len = b;
				else
					len = 0;
			}
			else
			{
				if(attendanceLen-(UPDATECOUNT)*(j+1)>=0)
					len = UPDATECOUNT;
				else
				{
					if(b==0)
						return false;
					len = b;
				}
			}	
		
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");        
			Date tmpDate = null;   
			String str = null;  
			
			JSONArray jsonarray = new JSONArray();//json数组  
			int i=0;
			for(i=j*UPDATECOUNT; i<len+j*UPDATECOUNT; i++)
			{
				
				JSONObject jsonObj = new JSONObject();//json形式  
				try {
					if(m_Attendance[i].NAME == null)
					{
						jsonObj.put("UserName", "null");
					}
					else
						jsonObj.put("UserName", m_Attendance[i].NAME);
					jsonObj.put("AttendanceId", Secure.getString(InteractionService.g_context.getContentResolver(),Secure.ANDROID_ID));
					jsonObj.put("AttendanceTime",m_Attendance[i].dateTime);  
					try {   
						tmpDate = format.parse(m_Attendance[i].date);    
					} catch (ParseException e) {    
					    e.printStackTrace();   
					}   
					str = format.format(tmpDate);  

					jsonObj.put("AttendanceDate", str);
					
					
					 //把字符串转化为时间，方法一  
			        SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
			        Date date1 = null;
					try {
						date1 = format1.parse(m_Attendance[i].dateTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
					jsonObj.put("AttendanceNo", ""+	date1.getTime());
			
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}//
				jsonarray.put(jsonObj);
				jsonObj = null;
			}
			System.out.println("----   i --"+i+" ----- j ---- "+j);
	//		System.out.println(jsonarray.toString());
			SoapObject request= new SoapObject ("http://tempuri.org/", "UploadAttendanceInfo");
			request.addProperty("JsonData", jsonarray.toString());
			SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.bodyOut=request;
			envelope.dotNet = true; 
			envelope.setOutputSoapObject(request);
			HttpTransportSE ht=new HttpTransportSE(path);
			try {
				System.out.println("----    6 ---- ");
				ht.call("http://tempuri.org/UploadAttendanceInfo", envelope);
				System.out.println("----    7 ---- ");
			} catch (IOException e1) {
					// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			} catch (XmlPullParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
			System.out.println("----    2 ---- ");		
			 SoapObject result = (SoapObject) envelope.bodyIn;
			 System.out.println("返回值： "+result);
			 String tmp1 =  result.getProperty(0).toString();
			 try {
				JSONObject resultStr = new JSONObject(tmp1);
				int ResultCode = resultStr.getInt("ResultCode");
				resultStr.getString("Msg");
				if(ResultCode == 1)
				{
					updateTag = true;
				}
				else
				{
					updateTag = false;
					return false;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		if(updateTag == true)
		{
			Editor editor;
			editor = sharedPreferences.edit();
			editor.putString("lastUpdateTimer", date);
			editor.commit();
		}
		return true;
	}
	*/
	/**
	 * 上传考勤数据
	 */
	private Runnable UpdateTimerRunnable = new Runnable() {
		@Override
		public void run() {
			new UpdateAttendanceAsyncTask().execute();   //上传考勤数据到服务器
			//每半个小时上传一次
			handler.postDelayed(UpdateTimerRunnable, 30*60*1000);  
			//有可能不太实时
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					sendBroadcast(new Intent("com.ebanswers.attendance.updateTime"));
				}
				
			}, 60*1000);
			
		}
	};
	
	//检查指纹头是否激活，没激活的话，进行激活
	private Runnable SwapFingerThreadRunnable = new Runnable() {
		@Override
		public void run() {
			if(m_getFingerThread.m_runStat!=2)
			{
				SleepWape();
				//SendMessage_LONG_MSG("指纹头没有激活，现在激活");
				handler.postDelayed(SwapFingerThreadRunnable, 10*1000);  //hcm
			}
			else
			{
				System.out.println("### 指纹头已经激活1");
			}
		}
	};

	//检查是否有更新包,并下载
	private Runnable CheckUpdateAPK = new Runnable() {
		@Override
		public void run() {
			Log.e("tag", "检测自动升级");
			try{					
				GData.setDownload(true);
				GData.setFileType(1);
				progress = 0;
				startDownload();
			}finally {
			}
		}
	};
	
	class CreateAPProcess implements Runnable {
		public boolean running = false;
		private long startTime = 0L;
		private Thread thread = null;

		CreateAPProcess() {
		}

		public void run() {
			while (true) {
				if (!this.running)
					return;
				try {
					Thread.sleep(5L);
				} catch (Exception localException) {
				}
			}
		}

		public void start() {
			try {
				thread = new Thread(this);
				running = true;
				startTime = System.currentTimeMillis();
				thread.start();
			} finally {
			}
		}

		public void stop() {
			try {
				this.running = false;
				this.thread = null;
				this.startTime = 0L;
			} finally {
			}
		}
	}
	
	public String getLocalHostName() {
		String str1 = Build.BRAND;
		String str2 = Build.MODEL;
		if (-1 == str2.toUpperCase().indexOf(str1.toUpperCase()))
			str2 = str1 + "_" + str2;
		return str2;
	}
	
	//AP_STAT :  0  一定创建     1  已打开不 创建，未打开，创建    2.只检测，不创建
	int OpenWifiAP(int AP_STAT)
	{		
		int ret = 1;
		openWifiTimes++;
		WifiManager localWifiManager;//提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
		localWifiManager = (WifiManager)g_context.getSystemService(Context.WIFI_SERVICE);
		int stat =localWifiManager.getWifiState();  //wifi当前状态
	
		if(stat == 1)  //wifi关闭状态下
		{
			m_wiFiAdmin = WifiAdmin.getInstance(this);
			if(m_wiFiAdmin.getWifiApState()==true)  
			{  //热点已打开	
				if(AP_STAT!=0)
				{  //缺省状态下，检查AP状态
					ret = 2;
				}
				else
				{
					//关闭热点
					if(m_wiFiAdmin.createWiFiAP(m_wiFiAdmin.createWifiInfo(m_wiFiAdmin.getApSSID(), "81028066", 3, "ap"),false)==true)
					{	
					//	System.out.println("关闭热点成功");
						ret = 1;
					}
					else
					{
						ret = -4; //关闭当前热点失败
					}
				}
			}			
		}	
		else 
		{
			handler.removeCallbacks(SetWifiAPRuannable);
			if(openWifiTimes<=3)
				handler.postDelayed(SetWifiAPRuannable, (long)(1000 * 60));
			else
				handler.postDelayed(SetWifiAPRuannable, (long)(1000 * 60*60));
			return -3;
		}
		/*
		else   //wifi 没有关闭,关闭wifi, 判断网络，网络通，创建热点，网络不通，打开wifi
		{
			if(AP_STAT==2)
			{
				if(m_wiFiAdmin.getWifiApState()==true)  
				{
					handler.removeCallbacks(SetWifiAPRuannable);
					if(openWifiTimes<=3)
						handler.postDelayed(SetWifiAPRuannable, (long)(1000 * 60));
					else
						handler.postDelayed(SetWifiAPRuannable, (long)(1000 * 60*60));
					return  2;
				}
				else
					return 3;
			}
			SaveConfig("WIFI_ORDER","begin");   //begin是开始，finish是结束
			m_wiFiAdmin.closeWifi();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CheckInternetStat();  //检测网络状态
			if(m_warningMsgTag==true)  //网络畅通
			{
				ret = 1;
			}
			else
			{	
				m_wiFiAdmin.OpenWifi();
				ret = -3;  //关闭wifi后，网络不通，无法创建热点
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			SaveConfig("WIFI_ORDER","finish");
		}
		*/
		
		if(ret == 1)
		{	//热点未打开或者被关闭, 创建打开热点
		//	if(m_warningMsgTag==true)  //网络畅通
			{	
				strSSID = m_wiFiAdmin.getApSSID();
				strPWD = m_wiFiAdmin.getApPwd();
				for(int i =0 ; i<3; i++)
				{
					boolean RET = false;
					if(AP_STAT == 0)
					{	
						RET = m_wiFiAdmin.createWiFiAP(m_wiFiAdmin.createWifiInfo("HOOPSON_"+WIFI_AP_HEADER, WIFI_AP_PASSWORD, 3, "ap"),true);
					}
					else
					{
						if(strSSID.indexOf("HOOPSON_")>=0)
						{
							RET = m_wiFiAdmin.createWiFiAP(m_wiFiAdmin.createWifiInfo(strSSID, strPWD, 3, "ap"),true);
						}
						else
						{
							RET = m_wiFiAdmin.createWiFiAP(m_wiFiAdmin.createWifiInfo("HOOPSON_"+WIFI_AP_HEADER, WIFI_AP_PASSWORD, 3, "ap"),true);
						}
					}
				
					if(RET == true)  //创建热点命令成功
					{
					//	System.out.println("热点创建成功");
						//if (AP_STAT == 1)
						{
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
																
							if(m_wiFiAdmin.getWifiApState()==true)
							{
								String strSSID1 = m_wiFiAdmin.getApSSID();
								String strPWD1 = m_wiFiAdmin.getApPwd();
								
								if(strSSID1.equals("AndroidAP")==true)  //创建成功的热点名称与想要创建的热点名称不一样。
								{
									m_wiFiAdmin.createWiFiAP(m_wiFiAdmin.createWifiInfo(m_wiFiAdmin.getApSSID(), "81028066", 3, "ap"),false);
									ret = -1;
									m_wiFiAdmin = WifiAdmin.getInstance(g_context);
									//上一次wifi打开关闭的状态
									strSSID = sharedPreferences.getString("apName", "001");
									strPWD = sharedPreferences.getString("apPwd", "12345678");
									continue ;
								}
								ret = 0;
								SaveConfig("apName",strSSID1);
								SaveConfig("apPwd", strPWD1);
								break;
							}
							else
							{	
								ret = -1;
								if(AP_STAT==1)
									continue;
						
							}
						}
					}
					else
					{
					//	System.out.println("热点创建失败");
						ret = -1;
						if(AP_STAT==1)
							continue;
					}
				}
			}
		}
		
		handler.removeCallbacks(SetWifiAPRuannable);
		if(openWifiTimes<=3)
			handler.postDelayed(SetWifiAPRuannable, (long)(1000 * 60));
		else
			handler.postDelayed(SetWifiAPRuannable, (long)(1000 * 60*60));
		return ret;
	}
	
		
	private Runnable SetWifiAPRuannable = new Runnable() {
		@Override
		public void run() {
			String apWifiStat = sharedPreferences.getString("apWifi", "default"); 
			if(apWifiStat.equals("open"))
			{
				OpenWifiAP(2);
			}
		}
	};
	
	//自动开关机时间设置
	class UpdateShutTime extends Thread{
		@Override
				public void run() {
		//		super.run();
					Context dsplayerAppContext = null;
					try {
						dsplayerAppContext = getApplicationContext().createPackageContext("com.dsplayer",Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(play_id==null)
						play_id = dsplayerAppContext.getSharedPreferences("ebanswers_preferences", Context.MODE_WORLD_READABLE).getString("PLAYER_ID", "");
					String path = "http://cloud.hoopson.com/WebEdit/api/player/timings/add";
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("key", "4b99ec1244f5475ab781356aec822463"));
					params.add(new BasicNameValuePair("username", "Ebanswers"));
					NameValuePair par1 = new BasicNameValuePair("ids", play_id);
					NameValuePair par2 = new BasicNameValuePair("setting", settingStr);
					params.add(par1);
					params.add(par2);
				//	System.out.println("发送 :"+settingStr);
					String result = RequestUtil.doPost(path, params);
					if(result.indexOf("success")>=0)
					{
						byte[] headBuf = new byte[12];
						// 网络数据包包头
						headBuf[0] = -17;
						headBuf[1] = 1;
						headBuf[2] = 0; // device_id
						headBuf[3] = 0; // device_id
						headBuf[4] = 0; // device_id
						headBuf[5] = 0; // deivce_id
						headBuf[6] = 0; // 填位
						headBuf[7] = 0; // 填位
						headBuf[8] = 102; // type
						headBuf[10] = 12;
						headBuf[11] = 0;
						Socket m_socket = m_map.get(""+this.getId());;
						PooledConnectionHandler m_PooledConnectionHandler = PooledRemoteFileServer.m_map.get(m_socket);
						try {
							m_PooledConnectionHandler.sendTcpData(headBuf);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//设置定时开关机时间
					}
					else
					{
						byte[] headBuf = new byte[12];
						// 网络数据包包头
						headBuf[0] = -17;
						headBuf[1] = 1;
						headBuf[2] = 0; // device_id
						headBuf[3] = 0; // device_id
						headBuf[4] = 0; // device_id
						headBuf[5] = 0; // deivce_id
						headBuf[6] = 0; // 填位
						headBuf[7] = 0; // 填位
						headBuf[8] = 101; // type
						headBuf[10] = 12;
						headBuf[11] = 0;
						Socket m_socket = m_map.get(""+this.getId());;
						PooledConnectionHandler m_PooledConnectionHandler = PooledRemoteFileServer.m_map.get(m_socket);
						try {
							m_PooledConnectionHandler.sendTcpData(headBuf);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
					
					m_map.remove(""+this.getId());
					//System.out.println("发送后 返回：" + result);
		}
	}
	 	
	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//SerialPortActivity.this.finish();
			}
		});
		b.show();
	}
	
	/**
	 * 
	* @Title:       SleepWape
	* @Description: 激活指纹头
	* @param           
	* @return       void   
	* @throws
	* @date         2015-4-21 下午03:43:37
	 */
	private void SleepWape()
	{
		msgQueue.offer("2");
		SetFlagSwitch(false);
		synchronized (curFinagerObj) {
			curFinagerObj.notify();
		}
	}
	
	 class NET_MSG_Proc extends Thread {
		 Socket m_socket = null;
		 
		 
			public void run()
			{
				super.run();
				Looper.prepare();
				class M_Handler extends Handler{             
		                public M_Handler(Looper looper){
		                	super (looper);
		                	}
		                
		                public M_Handler(){
		                }
		                public void handleMessage(Message msg) { // 处理消息 
		                	int msg_id =  msg.what;
		                	switch(msg_id)
		                	{
		                	case 0:
		                	case 1:
		                		break;
		                	case 12:
		                	{
		                		Intent intent = new Intent();
								intent.setAction(MessageAction.MESSAGE_ACTIVITY_ACT);
								sendBroadcast(intent, null); // 广播发送
								
								sendBroadcast(new Intent(MessageAction.MESSAGE_NO_OPERATION_CHANGE));
		                		break;
		                	}
		                	case 14:
		                	{
		                		Intent m_intent = new Intent();
		                		m_intent.setAction(MessageAction.MESSAGE_TCP_SET_NETMSG);
		                		m_intent.putExtras(msg.getData());
								sendBroadcast(m_intent, null); // 广播发送
		                		break;
		                	}
		                	case 16:
		                	{
		                		Intent m_intent = new Intent();
		                		m_intent.setAction(MessageAction.MESSAGE_TCP_STOP_NETMSG);
								sendBroadcast(m_intent, null); // 广播发送
		                		break;
		                	}
		                	case 17:
		                	{  //自动开关机设置
		                		UpdateShutTime m_UpdateShutTime = new UpdateShutTime();
		                	
								Bundle bundle = msg.getData();
								settingStr = bundle.getString("setting");	
								SaveConnectionClass m_SaveConnectionClass = (SaveConnectionClass) bundle.getSerializable("connection");
								m_socket = m_SaveConnectionClass.m_connection;
								m_map.put(""+m_UpdateShutTime.getId(), m_socket);
								m_UpdateShutTime.start();
								
		                		break;
		                	}
		                	default:
		                		break;
		                	}
		                }
				   }
				   testHandler  = new M_Handler();
					Looper.loop();
			}
	 }

	 /**
	  * 
	   @Name: InteractionService.java 
	   @Author: 指纹头相关操作    注册，认证等
	   @Date: 2015-4-21 
	   @Description:
	  */
	private class GetFingerThread extends Thread {
		int m_runStat = 1;
		int threadStatic=0; //线程状态  1:注册指纹  2:认证指纹     6:wait 7:run
		int curSelectItemStat = 0;
		int reg_id=-1;
		String name;
		
		// load方法加载音频文件返回对应的ID   		
		@SuppressWarnings("deprecation")
		@Override
		public void run() 
		{
			super.run();
	            synchronized (curFinagerObj) 
	            {
	             	try {
	             		{
							DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
							dbAdapter.open();
							dbAdapter.CreateNew();
							dbAdapter.SetDepartment();
							dbAdapter.close();
							dbAdapter = null;
	             		}

		        		while(true)
		        		{
		        			if(m_runStat == 1 || msgQueue.size()==0)
		        			{
		        				threadStatic = 6;
		        				curFinagerObj.wait();
		        			}
		        			m_runStat = 2;
		        			if(msgQueue.size()>0)
		        			{
		        				String tmpStr = msgQueue.poll();
		        				if(tmpStr.equals("1"))
		        				{
		        					threadStatic = 1;
		        					
		                    		//添加指纹
			        				int ret=0;
			        				if((ret=RegF(name, "", m_getFingerThread.reg_id))==0)
		                    		{	//注册成功
		                    			msgQueue.offer("2");
		                    		}
		                    		else if(ret == -1)
		                    		{
		                    		}
		                    		else 
		                    		{   //注册失败
		                    			msgQueue.offer("2");
		                    		}         		
		        				}
		        				else if(tmpStr.equals("2"))
		        				{
		        					threadStatic = 2;
									if (OpenDevice() == 1 && mp==null)
									{		
										int ret = CurFinger();
										Calendar c = Calendar.getInstance();
										int mYear = c.get(Calendar.YEAR);
										int mMonth = c.get(Calendar.MONTH) + 1;
										int mDay = c.get(Calendar.DAY_OF_MONTH);
										int mHour = c.get(Calendar.HOUR_OF_DAY);
										int mMinute = c.get(Calendar.MINUTE);
										int mSecond = c.get(Calendar.SECOND);
										
										String mHourStr, mMinuteStr, mSecondStr;
										String m_date = "" + mYear + "-" + mMonth + "-" + mDay;
							
										mHourStr=String.format("%02d", mHour);
										mMinuteStr=String.format("%02d", mMinute);
										mSecondStr=String.format("%02d", mSecond);
					
										String m_time = "" + mHourStr + ":" + mMinuteStr + ":" + mSecondStr;
										String m_time1 = "" + mHourStr + ":" + mMinuteStr;
										
										if (ret == 0) 
										{ // 认证成功
											DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
											// 打开数据库
											dbAdapter.open();
											PersonEx[] m_Person = new PersonEx[1];
											Log.e("","----------- id : "+g_userID);
											m_Person = dbAdapter.queryPersonExOneData(g_userID);
											if (m_Person == null) {
												m_getFingerThread.threadStatic = 2;
												SendMessage_LONG_MSG("无此指纹相关信息,请重新注册指纹！");
												msgQueue.offer("2");
											}
											else 
											{
												SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
												
												birthdayTag = false;
												try {

													if(m_Person[0].Birthday!=null&&!m_Person[0].Birthday.isEmpty())
													{
														Date birthDayDate = sdf.parse(m_Person[0].Birthday);
														if(mMonth==birthDayDate.getMonth()+1 && mDay==birthDayDate.getDate())  //今天过生日
															birthdayTag = true;	
													}
												} catch (Exception e) {
													// TODO Auto-generated catch blockd
													e.printStackTrace();
													
												}
																							
												if (m_Person[0].Admin == 1) {
													// 发送切换界面消息
													Intent intent = new Intent();
													intent.setAction(MessageAction.MESSAGE_ACTIVITY_ACT);
													sendBroadcast(intent);
													sendBroadcast(new Intent(MessageAction.MESSAGE_NO_OPERATION_CHANGE));
													PlaySound(17);
												} else {
												   	if(m_warningMsgTag==true)
												   		PlaySound(1);  //打卡成功
												}
																							
												GetAttendanceTimer();
												SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
												String date = sDateFormat.format(new java.util.Date());
												
												int Segment=0;
												if(g_attendance_timer==2)
												{
													Segment=g_attendanceTimer1.IsTimeSegment(g_attendanceTimer2, mHour, mMinute);
													midTime2 = g_attendanceTimer1.getMidTime2();
													midTime3 = g_attendanceTimer1.getMidTime3();
												}
												else
												{
												    Segment= g_attendanceTimer1.IsTimeSegmentOneTime(mHour, mMinute);												
												}
											    midTime1 = g_attendanceTimer1.getMidTime1();
											    
												String m_midHour1, m_midMinute1, m_midHour2, m_midMinute2, m_midHour3, m_midMinute3;
												int midHour1, midMinute1, midHour2, midMinute2, midHour3, midMinute3;
												midHour1 = midTime1/60;
												midMinute1 = midTime1%60;
												midHour2 = midTime2/60;
												midMinute2 = midTime2%60;
												midHour3 = midTime3/60;
												midMinute3 = midTime3%60;
												
												m_midHour1=String.format("%02d", midHour1);
												m_midMinute1=String.format("%02d", midMinute1);
												m_midHour2=String.format("%02d", midHour2);
												m_midMinute2=String.format("%02d", midMinute2);
												m_midHour3=String.format("%02d", midHour3);
												m_midMinute3=String.format("%02d", midMinute3);
												
												String m_midTime1 = "" + m_midHour1 + ":" + m_midMinute1 +":00";
												String m_midTime2 = "" + m_midHour2 + ":" + m_midMinute2 +":00";
												String m_midTime3 = "" + m_midHour3 + ":" + m_midMinute3 +":00";
												
												  //周几
												 Calendar cal=Calendar.getInstance();
												 Date dd = new Date();
												 cal.setTime(dd);
												 int cur_day = cal.get(Calendar.DAY_OF_WEEK);
						
												 
												 switch(cur_day)
												 {
												 case 2:  //周1
													 cur_day = 1;
													 break;
												 case 1:  //周日
												 case 3:  //周2
												 case 4:  //周3
												 case 5:  //周4
												 case 6:  //周5
												 case 7:  //周6
													 cur_day = -1;
													 break;
													 default:
														 cur_day = -1;
														 break;
												 }
												 boolean showMsgTag = false;
											
												 String curDate=sdf.format(new java.util.Date());
												 StringBuffer tmp = new StringBuffer("select count (*) from Attendance where name='");
												 tmp.append(m_Person[0].name+"'");
												 tmp.append(" and datetime(IO_DateTime)>=");
												 if(g_attendance_timer==2)
												 {
													// System.out.println("双班");
													 switch(Segment)
														{
														case 1:
														{   //上午上班打卡
															if(cur_day==1)  //周1早上，不提醒
																break;
													        Calendar calTmp = Calendar.getInstance();
													        //  日期的DATE减去10  就是往后推10 天 同理 +10 就是往后推十天
													        calTmp.add(Calendar.DATE, cur_day);
													        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
													        String m_dateTmp = sf.format(calTmp.getTime());
															String m_dateTime1 = "datetime('"+m_dateTmp+" "+m_midTime3+"')";
															String m_dateTime2 = "datetime('" +m_dateTmp+" "+g_attendanceTimer2.m_endTime + "')";
												
															tmp.append(m_dateTime1);
															tmp.append(" and datetime(IO_DateTime)<=");
															tmp.append(m_dateTime2);
										
														    long len = dbAdapter.getCount(tmp.toString());
														    if(len>=1)
														    {
														    }
														    else
														    {
														    	showMsgTag = true;
														    }
															break;
														}
														case 2:
														{   //上午下班打卡													
									        				String m_dateTime1 = "datetime('" +curDate+" "+g_attendanceTimer1.m_startTime + "')";
									        				String m_dateTime2 = "datetime('"+curDate+" "+m_midTime1+"')";
									        				
															tmp.append(m_dateTime1);
															tmp.append(" and datetime(IO_DateTime)<=");
															tmp.append(m_dateTime2);
								
														    long len = dbAdapter.getCount(tmp.toString());
														    if(len>=1)
														    {
														    }
														    else
														    {
														    	showMsgTag = true;
														    }
															break;
															
														}
														case 3:
														{   //下午上班打卡
									        				String m_dateTime1 = "datetime('"+curDate+" "+m_midTime1+"')";
									        				String m_dateTime2 = "datetime('"+curDate+" "+m_midTime2+"')";
									        				
															tmp.append(m_dateTime1);
															tmp.append(" and datetime(IO_DateTime)<=");
															tmp.append(m_dateTime2);
															
											
														    long len = dbAdapter.getCount(tmp.toString());
														    if(len>=1)
														    {
														    }
														    else
														    {
														    	showMsgTag = true;
														    }
															break;
													
														}
														case 4:
														{   //下午下班打卡													
									        				String m_dateTime1 = "datetime('"+curDate+" "+m_midTime2+"')";
									        				String m_dateTime2 = "datetime('"+curDate+" "+m_midTime3+"')";
									        				
															tmp.append(m_dateTime1);
															tmp.append(" and datetime(IO_DateTime)<=");
															tmp.append(m_dateTime2);
												
														    long len = dbAdapter.getCount(tmp.toString());
														    if(len>=1)
														    {
														    }
														    else
														    {
														    	showMsgTag = true;
														    }
															break;
														}
														default:
															//其他时间打卡
															break;
														}
												 }
												 else
												 {// 单班
										
													 switch(Segment)
													 {
														case 1:
														{  
															if(cur_day==1)  //周1早上，不提醒
																break;
															//上班打卡
													        Calendar calTmp = Calendar.getInstance();
													        //  日期的DATE减去10  就是往后推10 天 同理 +10 就是往后推十天
													        calTmp.add(Calendar.DATE, cur_day);
													        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
													        String m_dateTmp = sf.format(calTmp.getTime());
															String m_dateTime1 = "datetime('"+m_dateTmp+" "+m_midTime1+"')";
															String m_dateTime2 = "datetime('" +m_dateTmp+" "+g_attendanceTimer1.m_endTime + "')";
									
															tmp.append(m_dateTime1);
															tmp.append(" and datetime(IO_DateTime)<=");
															tmp.append(m_dateTime2);
													
														    long len = dbAdapter.getCount(tmp.toString());
														    if(len>=1)
														    {
														    }
														    else
														    {
														    	showMsgTag = true;
														    }
												
															break;
														}
														case 2:
															 //下班打卡													
									        				String m_dateTime1 = "datetime('" +curDate+" "+g_attendanceTimer1.m_startTime + "')";
									        				String m_dateTime2 = "datetime('"+curDate+" "+m_midTime1+"')";
									        				
															tmp.append(m_dateTime1);
															tmp.append(" and datetime(IO_DateTime)<=");
															tmp.append(m_dateTime2);
														
														    long len = dbAdapter.getCount(tmp.toString());
									
														    if(len<1)
														    {
														    	showMsgTag = true;
														    }
													
															break;
															default:
																break;
														}
												 }
												 
												long result = dbAdapter.insertAttendance(g_userID,
															mYear, mMonth, mDay, mHour, mMinute, mSecond,
															m_date, m_time, date, m_Person[0].name);
												if (result <= 0) 
												{
													SendMessage_LONG_MSG("数据保存失败，请重新打卡");
													msgQueue.offer("2");
												}
												else 
												{
													StringBuffer stringBuffer = new StringBuffer("ID : ");
													stringBuffer.append(g_userID);
													stringBuffer.append("   姓名: ");
													stringBuffer.append(m_Person[0].name);
													happyBirthdayStr = m_Person[0].name+" : "+m_Person[0].msg;
													//if(showMsgTag==true&&m_warningMsgTag)
													if(showMsgTag==true)
													{  
														if(birthdayTag == false)
															SendWaringMsg("     上个班次未考勤      ", "认证成功", stringBuffer.toString(), m_time1);
														else
														{
															SendMessage_3MSG(m_Person[0].name+" : "+m_Person[0].msg,stringBuffer.toString(), m_time1, 
																	birthdayTag, m_Person[0].birthdaySwitch);  //生日快乐
														}
														if(m_Person[0].Admin !=1)
															PlaySound(25);
													}
													else
													{  
														SendMessage_3MSG(m_Person[0].name+" : "+m_Person[0].msg, stringBuffer.toString(), m_time1, 
																birthdayTag, m_Person[0].birthdaySwitch);  //生日快乐
																										
														//SendMessage_3MSG("", stringBuffer.toString(), m_time1, 1);  //生日快乐
														/*
														StringBuffer helloMsg = new StringBuffer("select count (*) from Attendance where name='");
														helloMsg.append(m_Person[0].name+"'");
														helloMsg.append(" and datetime(IO_DateTime)>=");
														
														Calendar calTmp = Calendar.getInstance();
													    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
													    String m_dateTmp = sf.format(calTmp.getTime());
													    
														String m_dateTime1 = "datetime('"+m_dateTmp+"00:00:00')";		
														helloMsg.append(m_dateTime1);
														helloMsg.append(" and datetime(IO_DateTime)<=");
														helloMsg.append("datetime('"+m_dateTmp+"23:59:59')");
													    long len = dbAdapter.getCount(helloMsg.toString());
													  
													    if(len==1)
													    {
													    	//早上好
													    	if(mHour*60+mMinute<=g_attendanceTimer1.m_beginHour*60+g_attendanceTimer1.m_beginMinute)
													    		SendMessage_3MSG("认证成功",stringBuffer.toString(), m_time1, 2);  //早上好
													    	else
														     	SendMessage_3MSG("认证成功",stringBuffer.toString(), m_time1, 3);  //你迟到了
													    }
													    else
													    {
													    	Log.w("tag", " mHour: "+mHour+" mMInute: "+mMinute+"  g_attendanceTimer1.m_bgeginHour: "+g_attendanceTimer1.m_beginHour+" g_attendanceTimer1.m_beginMinute : "+g_attendanceTimer1.m_beginMinute);
													    	if(mHour*60+mMinute<=g_attendanceTimer1.m_beginHour*60+g_attendanceTimer1.m_beginMinute)
													    	{
													    		SendMessage_3MSG("认证成功",stringBuffer.toString(), m_time1, 1);  //打卡成功
													    	}
													    	else if(mHour*60+mMinute>=g_attendanceTimer1.m_endHour*60+g_attendanceTimer1.m_endMinute)
													    	{
													     		SendMessage_3MSG("认证成功", stringBuffer.toString(), m_time1, 3);  //再见
													    	}
													    	else
													    	{
													    		SendMessage_3MSG("认证成功",stringBuffer.toString(), m_time1, 4);   //早退
													    	}
													    }
													    */
													}
													
													msgQueue.offer("2");
														
													if(birthdayTag==false)
													{
														Intent intent = new Intent();
														intent.setAction(MessageAction.MESSAGE_TCP_POP_NETMSG);
														sendBroadcast(intent);
													}
												}
												showMsgTag = false;																							    																	
								        								        		
											}
											dbAdapter.close();
											dbAdapter = null;
					           			} 
					        			else if(ret==-1)
					        			{
					        				if(m_getFingerThread.curSelectItemStat != 2)
					        				{
					        					threadStatic = 6;
					        					curFinagerObj.wait();
					        				}
					        			}
					        			else
					        			{	//认证失败
					        				DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
				        			    	//打开数据库
				        				    dbAdapter.open();
				        				    String str = "select count (*) from Person";
				        				    long count = dbAdapter.getCount(str);			
				        				    if(count > 0)
				          				    {  //已经有指纹用户，设备不是空指纹 
				        				    	StringBuffer stringBuffer = new StringBuffer(" 认证未通过");
				        				    	if(m_warningMsgTag==true)
				        				    		PlaySound(2);    //认证未通过
					        		 			stringBuffer.append("\n");
					        		 			stringBuffer.append(m_date);
					        		 			stringBuffer.append("  ");
					        		 			stringBuffer.append(m_time);
					        			    	SendMessage_LONG_MSG(stringBuffer.toString());
					        				    stringBuffer = null;
						        				msgQueue.offer("2");
				        				    }
				        				    else
				        				    {
				        				    	PlaySound(17); 
				        				    	sendBroadcast(new Intent(MessageAction.MESSAGE_NO_OPERATION_CHANGE));
				        				    	SendMessage_LONG_MSG("指纹用户为空");
				        				    	msgQueue.offer("2");
				        				    	
				        				    	Intent intent = new Intent();
						        				intent.setAction(MessageAction.MESSAGE_ACTIVITY_ACT);
						        				sendBroadcast(intent);
						        				intent = new Intent();
						        				intent.setAction(MessageAction.MESSAGE_TCP_POP_NETMSG);
						        				sendBroadcast(intent);
				        				    }
				        				    dbAdapter.close();
				        				    dbAdapter=null;
				        				    
				        				    m_date = null;
				        				    m_time = null;
					        			}
			        				}
									else
									{	
										msgQueue.offer("2");
										Thread.sleep(100);
									}
		        				}
		        				else if(tmpStr.equals("3"))
		        				{ 
		        					threadStatic = 3;
		        					//删除指纹
		        					if(ClearAllFinger() ==0 )
									{
		        						SendMessage_LONG_MSG("清空所有指纹用户成功");
		        						PlaySound(13);
									 }
		        					else
		        					{
		        						SendMessage_LONG_MSG("清空所有指纹用户失败");
		        					}
		        							
		        					msgQueue.offer("2");
		        				}
		        				else if(tmpStr.equals("4"))
		        				{
		        					threadStatic =6;
		        					curFinagerObj.wait();
		        				}
		        				else if(tmpStr.equals("6"))
		        				{
		        					threadStatic = 3;
		        					//删除指纹
		        					if(ClearAllFinger() ==0 )
									{	
		        						SendMessage_LONG_MSG("恢复出厂设置成功");
									 }
		        					else
		        					{
		        						SendMessage_LONG_MSG("恢复出厂设置失败");
		        					}
		        							
		        					msgQueue.offer("2");
		        				}
		        				else if(tmpStr.equals("5"))
		        				{  //删除单个指纹
		        					threadStatic = 5;
		        					msgQueue.offer("2");
		        					// 查询是否有此指纹id
		        					DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
			        		    	//打开数据�?
			        			    dbAdapter.open();
			        				Person[] m_Person = new Person[1];
			        			    m_Person = dbAdapter.queryPersonOneData(g_id);
			        			    if(m_Person == null)
			        			    {
			        			    	SendMessage_LONG_MSG("无此指纹相关信息,无法删除！");
			        			    	PlaySound(12);
			        			    	dbAdapter.close();
		        	      				dbAdapter =null;
			        			    }
			        			    else
			        			    {
			        			    	 SetFlagSwitch(false);
				        				 if(DelFinger(g_id)==0)
				        	        	 {
				        	      			    String sql ="delete from Person where id = "+g_id;
				        	      			    try {
				        	      				   dbAdapter.db.execSQL(sql);
				        	      				} catch (SQLException e) {
				        	      	            }
				        	      				SendMessage_LONG_MSG("删除用户成功！");
				        	      				PlaySound(9); 
				        	        			//删除数据成功
				        	        		}
				        	        		else
				        	        		{ 
				        	        			//删除数据失败
				        	        			SendMessage_LONG_MSG("删除用户失败！");
				        	        		}
				        	        	 	dbAdapter.close();
			        	      				dbAdapter =null;
				        					sendBroadcast(new Intent(MessageAction.MESSAGE_CUR_FINGER));
			        			    }
		        				}
		        		     }
		        			else
		        			{
		        				//System.out.println("##############  msg  queue size : "+msgQueue.size());
		        			//	MyLog.w("test", " msg  queue size :  "+msgQueue.size());
		        			}
		        			}
		        		}
					 catch (InterruptedException e) {
						 //MyLog.w("test", " error  1 ");
					} catch (IllegalStateException e) {
						 //MyLog.w("test", " error  2 ");
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
	            }
		}
	}
		
	 public static boolean isBackground(Context context) {
		    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		    for (RunningAppProcessInfo appProcess : appProcesses) {
		         if (appProcess.processName.equals(context.getPackageName())) {
		                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
		               //           Log.i("后台", appProcess.processName);
		                	return true;
		                }else{
		               //           Log.i("前台", appProcess.processName);
		                	return false;
		                }
		           }
		    }
		    return false;
		}
    
    public static void  SetUserID(int id) {
    	g_userID = id;
		return;
    }
    
    public void SendMessage(String str)
    {
    	Intent intent = new Intent();
		intent.setAction(MessageAction.MESSAGE_SHOW_TEST);
		intent.putExtra("pkg", getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.putExtra("msg", str);
		
		sendBroadcast(intent);
    }
    
    public void SendWaringMsg(String str1, String str2, String str3, String str4)
    {
    	Intent intent = new Intent();
		intent.setAction(MessageAction.MESSAGE_SHOW_WARING);
		intent.putExtra("pkg", getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.putExtra("msg1", str1);
		intent.putExtra("msg2", str2);
		intent.putExtra("msg3", str3);
		intent.putExtra("msg4", str4);
		sendBroadcast(intent);
    }
    
    /**
     * 
    * @Title:       SendMessage_3MSG
    * @Description: 发送指纹认证成功消息
    * @param        @param str1
    * @param        @param str2
    * @param        @param str3
    * @param        @param tag  是否当天过生日   false 未过生日 true 过生日
    * @param        @param birthdaySwitch  是否以插播消息方式提示生日快乐   0：不提示  1：提示
    * @return       void   
    * @throws
    * @date         2015-5-4 下午01:38:31
     */
    public void SendMessage_3MSG(String str1, String str2, String str3, boolean tag,  int birthdaySwitch)
    {
    	/*
	    if(m_warningMsgTag==false)
    	{
    		Intent intent = new Intent();
			intent.setAction(MessageAction.MESSAGE_TCP_SHOW_WARNING_MSG);
			intent.putExtra("pkg", getPackageName());
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		
			sendBroadcast(intent);
			
			PlaySound(24);	
    	}
    	else
    	*/
    	{
    		Intent intent = new Intent();
    		intent.setAction(MessageAction.MESSAGE_SHOW_TEST_3MSG);
    		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    		intent.putExtra("pkg", getPackageName());
    		intent.putExtra("msg1", str1);
    		intent.putExtra("msg2", str2);
    		intent.putExtra("msg3", str3);
    		intent.putExtra("birthdayTag", tag);
    		intent.putExtra("birthdaySwitch", birthdaySwitch);
    	    sendOrderedBroadcast(intent,"msg.per");
    	    /*
    	    if(tag == 1)
			{
				PlaySound(24);  //早上好
			}
			else if(tag ==2)
				PlaySound(36); //正常打卡
			else if(tag == 3)
				PlaySound(35); //迟到
			else if(tag == 4)
			{
				PlaySound(31); //早退
			}
			*/
    	}
    }
    
    protected static boolean isTopActivity(Context context){
    	String packageName = "com.dsplayer";
    	 ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    for (RunningAppProcessInfo appProcess : appProcesses) {
	         if (appProcess.processName.equals(packageName)) {
	                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
	               //   Log.i("后台", appProcess.processName);
	                	return true;
	                }else{
	               //   Log.i("前台", appProcess.processName);
	                    return false;
	                }
	           }
	    }
	    
	    if(checkAPP(context, SOFT__PACKAGE_NAME))
	    {
	    	 return true;
	    }
		
        return false;
    }
    
	public  void openHolder(Boolean flag) {
		Intent intent = new Intent();
		if (flag) {
		//	System.out.println("###   openHolder  启动监控");
			intent.setAction("com.ebanswers.startDog");
		} else {
		//	System.out.println("###   openHolder  停止监控");
			intent.setAction("com.ebanswers.stopDog");
		}
		intent.putExtra("package", "com.dsplayer");
		intent.putExtra("callbackActivity", "com.dsplayer.MainActivity");
		sendBroadcast(intent);
	}
    /*public void SendMessage_LONG_MSG(String str1)
    {
		Intent intent = new Intent();
    	if(m_warningMsgTag==false)
    	{
			intent.setAction(MessageAction.MESSAGE_TCP_SHOW_WARNING_MSG);
			intent.putExtra("pkg", getPackageName());
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			sendBroadcast(intent);
			PlaySound(24);
    	}
    	else
    	{
			intent.setAction(MessageAction.MESSAGE_SHOW_TEST_LONG);
			intent.putExtra("pkg", getPackageName());
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			intent.putExtra("msg1", str1);
			sendBroadcast(intent);
    	}
    }*/

    public void SendMessage_LONG_MSG(String str1)
    {
		Intent intent = new Intent();
    	
			intent.setAction(MessageAction.MESSAGE_SHOW_TEST_LONG);
			intent.putExtra("pkg", getPackageName());
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			intent.putExtra("msg1", str1);
			sendBroadcast(intent);
    	
    }
    
	public  void StartTcpStaticWatch()
	{		
		if(m_tcpConnectTimer!=null)	
		{
			m_tcpConnectTimer.cancel();
			m_tcpConnectTimer=null;
		}
		m_tcpConnectTimer = new Timer();
		m_tcpConnectTimer.schedule(new TimerTask(){
				public void run(){
					if(tcp_heart==false)
					{
						if(m_PooledRemoteFileServer.GetConnectCount()>=1)
						{
							m_PooledRemoteFileServer.CloseTcpConnect();
						}
						m_tcpConnectTimer.cancel();
					}
					else
					{
					//	System.out.println("网络连接断开 3");
						tcp_heart = false;
					}
				}
			}, 1000*50, 1000*70);
	}

	public static byte[] intToBytes(int num) {
		byte[] b = new byte[4];

		b[0] = (byte) (num >>> 0);
		b[1] = (byte) (num >>> 8);
		b[2] = (byte) (num >>> 16);
		b[3] = (byte) (num >>> 24);
		return b;
	}

	public static int BytesToInt(byte[] bRefArr) {
		int iOutcome = 0;
		byte bLoop;

		for (int i = 0; i < bRefArr.length; i++) {
			bLoop = bRefArr[i];
			iOutcome += (bLoop & 0xFF) << (8 * i);
		}
		return iOutcome;
	}

	int  RegF(String name , String department, int id)
	{
		int ret = 0;
		int user_id = -1;
		user_id = id;
		DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
		dbAdapter.open();

		String str = "select count (*) from Person where id=" + id;
		long count = dbAdapter.getCount(str);
		if (count > 0) {
			dbAdapter.close();
			dbAdapter = null;
			SendMessage_LONG_MSG("注册指纹:此ID号已被使用，请换一个ID号");
			m_getFingerThread.threadStatic = 3;
	    	ret = -2;
	    }
	    else
	    {
	    	//long len = dbAdapter.getCount("select count (*) from Person where");
	    	long len = 0;
	    	int finger_count = 0 ;
	 		int left= 0;
		 		
	 		if(user_id == -2)
	 		{   //超级管理员 

	 			len = dbAdapter.getCount("select count (*) from Person where id>=1 and id<=3");
	 			if(len>=3)
	 			{
	 				//注册已满
	 				PlaySound(4); 
	 				SendMessage_LONG_MSG("管理员用户注册已满，注册失败");
	 				return -2;
	 			}
	 			finger_count = 3;
	 			left = 1;
	 		}
	 		else 
	 		{  //普�?用户
	 			len = dbAdapter.getCount("select count (*) from Person where id>=4");
	 			if(len>=496)
	 			{
	 				//注册已满
	 				PlaySound(19);
	 				SendMessage_LONG_MSG("普通用户注册已满，注册失败");
	 				return -2;
	 			}
	 			finger_count = 500;
	 			left = 4;
	 		}
	    	
			Person[] m_Person = new Person[(int)(len)];
	 		if(user_id == -2)
	 		{
	 			m_Person = dbAdapter.getQuery("select * from Person where id>=1 and id<=3");
	 		}
	 		else
	 		{
	 			m_Person = dbAdapter.getQuery("select * from Person where id>=4");
	 		}
	 		/*   
	 		for(int i=1; i<=999; i++)
	 		{
	 			//user_id不为-1时，说明已经找到未被使用的id号
	 			if(user_id != -1)
	 				break;
	 			for(int j=0; j<len; j++)
	 			{
	 				if(m_Person[j].id==i)
	 				{
	 					break;
	 				}
	 				//遍历过所有的记录，没有与当前搜索的id数相匹配的，此id数即为没被分配的，此id保存在user_id中。
	 				if(j==len-1)
	 				{
	 					user_id = i;
	 				}
	 			}
	 		}
	 		*/
	 			 		
	 		for(int i=left; i<=finger_count; i++)
	 		{
	 			//user_id不为-1时，说明已经找到未被使用的id号
	 			if(user_id != -1 && user_id!= -2)
	 				break;
	 			int j=0;
	 			for(j=0; j<len; j++)
	 			{
	 				if(m_Person[j].id==i)
	 				{
	 					break;
	 				}
	 				
	 				//遍历过所有已存在的用户id，没有与当前搜索的id数相匹配的，此id数即为没被分配的，此id保存在user_id中�?
	 				if(j==len-1)
	 				{
	 					user_id = i;
	 				}
	 			}
	 		}
	 		
	 		
	 		if(user_id == -2)
	 		{
	 			user_id = 1;
	 		}
	 		else if(user_id == -1)
	 		{
	 			user_id = 4;
	 		}
	 	
	 		SetFlagSwitch(true);
	 		SendMessage_LONG_MSG("请放上手指,直到注册完成");
	 		PlaySound(5); 
	 		if(timer!=null)
	 		{
	 			timer.cancel();
	 			timer=null;
	 		}
	 		timer = new Timer();
				timer.schedule(new TimerTask(){
					public void run(){
						SendMessage_LONG_MSG("请放上手指,直到注册完成");
					}
				}, 1000, 1000);
	 		//注册
			
	 		ret = RegFingerOne(user_id);
	 	
	 		
	 		//注册
	 		switch(ret)
	 		{
	 		case 0:
	 			ret = RegFingerTwo(user_id);
	 			switch(ret)
	 			{
	 			case 0:

	 				if(dbAdapter.insertPersonEx(name, user_id, 1, department, m_admin+1)>=0)
	 				{
	 					String tmp;
			 			tmp="注册成功       ID : "+user_id;
			 			tmp+="  姓名 ";
			 			tmp +=name;
			 			SendMessage_LONG_MSG(tmp);
			 			PlaySound(15);
	 				}
	 				else
	 				{
	 					SendMessage_LONG_MSG("注册指纹失败，请重新注册");
	 					PlaySound(14);
	 				}
	 				break;
	 		
	 				default:
	 					SendMessage_LONG_MSG("注册指纹失败，请重新注册");
	 					PlaySound(14);
	 					break;
	 			}
	 			break;

	 			default:
	 				SendMessage_LONG_MSG("注册指纹失败，请重新注册");
	 				break;
	 		}
	 	
	 		timer.cancel();
	 		timer=null;
	 		
	 		dbAdapter.close();
	 		dbAdapter = null;
	 		m_Person = null;
	    }
	   
		return ret;
	}
	
	private static void startDownload() {
		// TODO Auto-generated method stub
		downloadApk();
	}

	//
	/**
	 * 下载apk
	 * 
	 * @param url
	 */
	private static Thread downLoadThread;
	private static void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 安装apk
	 * 
	 * @param url
	 */
	private static void installApk() {
		File apkfile = new File(saveFileNameUpdateAPK);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		g_context.startActivity(i);
	}

	private static int lastRate = 0;
	private static Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {

				URL url;
				String apkFile;
				if(GData.getFileType()==2)
				{
					url = new URL(apkUrl+GData.getName());
					saveFileNameUpdateAPK = savePath+ GData.getName();
					apkFile = saveFileNameUpdateAPK;
				}
				else
				{
					url = new URL(versionUrl);
					apkFile = saveFileNameIni;
				}

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				
				int length = conn.getContentLength();
				if(conn.getResponseCode()!=200)
				{
				//	System.out.println(" ####　　下载链接出错， err_no　："+conn.getResponseCode());
					return;
				}
				InputStream is = conn.getInputStream();
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);

				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
				
								
					if (numread <= 0) {
						// 下载完成通知安装
						handler.sendEmptyMessage(0);
						// 下载完了，cancelled也要设置
					//	System.out.println("## canceled set true ");
						break;
					}
					fos.write(buf, 0, numread);
				} while (true);// 点击取消就停止下载.
				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};
}
/**
 * 
  @Name: InteractionService.java 
  @Author: 123456 
  @Date: 2015-4-9 
  @Description: 上传考勤数据到服务器
 */

 class UpdateAttendanceAsyncTask extends AsyncTask<String, Integer, String>{

	@Override
	protected String doInBackground(String... arg0) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     
		String date = formatter.format(new java.util.Date());
		boolean updateTag = false;
		Attendance[] m_Attendance = null;
		long attendanceLen = 0;
		SharedPreferences sharedPreferences = InteractionService.g_context.getSharedPreferences("config",Context.MODE_PRIVATE);
		String lastUpdateTimer = sharedPreferences.getString("lastUpdateTimer", "NULL");
		StringBuffer tmp = new StringBuffer();
		StringBuffer tmpStr = new StringBuffer();
		if(lastUpdateTimer.equals("NULL"))  //没有上传过数据，首次上传
		{	
			tmp.append("select count (*) from Attendance");
			tmpStr.append("select * from Attendance");
		}
		else  //已经上传过数据了，从上次上传数据截止时间上传
		{	
			String m_dateTime1 = "datetime('"+lastUpdateTimer+"')";		    
		    tmp.append("select count (*) from Attendance where datetime(IO_DateTime)>=");
		    tmp.append(m_dateTime1);
		    tmpStr.append("select * from Attendance where datetime(IO_DateTime)>=");
		    tmpStr.append(m_dateTime1);
		}
	
		DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
		dbAdapter.open();
		attendanceLen = dbAdapter.getCount(tmp.toString());
		if (attendanceLen<=0)
		{	
			dbAdapter.close();
			dbAdapter = null;
			return null;
		}
		m_Attendance = new Attendance[(int) (attendanceLen)];
		m_Attendance = dbAdapter.getAttendanceQuery(tmpStr.toString());
		dbAdapter.close();
		dbAdapter = null;
		String path = "http://120.26.113.21:8088/ws/HoopsonAttend.asmx";  //正式
		//System.out.println("新服 ："+path);
		if(attendanceLen<=0)
		{
			return null;
		}
	
		final int UPDATECOUNT = 500; //单次上传条数
		
		for(int j=0; j<attendanceLen/UPDATECOUNT+1; j++)
		{
			long len=0;
			
			long a=attendanceLen/(long)UPDATECOUNT;
			long b=attendanceLen%(long)UPDATECOUNT;

			if(a==0)
			{
				if(b!=0)
					len = b;
				else
					len = 0;
			}
			else
			{
				if(attendanceLen-(UPDATECOUNT)*(j+1)>=0)
					len = UPDATECOUNT;
				else
				{
					if(b==0)
						return null;
					len = b;
				}
			}	
		
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");        
			Date tmpDate = null;   
			String str = null;  
			
			JSONArray jsonarray = new JSONArray();//json数组  
			int i=0;
			for(i=j*UPDATECOUNT; i<len+j*UPDATECOUNT; i++)
			{
				
				JSONObject jsonObj = new JSONObject();//json形式  
				try {
					if(m_Attendance[i].NAME == null)
					{
						jsonObj.put("UserName", "null");
					}
					else
						jsonObj.put("UserName", m_Attendance[i].NAME);
					jsonObj.put("AttendanceId", Secure.getString(InteractionService.g_context.getContentResolver(),Secure.ANDROID_ID));
					jsonObj.put("AttendanceTime",m_Attendance[i].dateTime);  
					try {   
						tmpDate = format.parse(m_Attendance[i].date);    
					} catch (ParseException e) {    
					    e.printStackTrace();   
					}   
					str = format.format(tmpDate);  

					jsonObj.put("AttendanceDate", str);
					
					
					 //把字符串转化为时间，方法一  
			        SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
			        Date date1 = null;
					try {
						date1 = format1.parse(m_Attendance[i].dateTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
					jsonObj.put("AttendanceNo", ""+	date1.getTime());
			
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}//
				jsonarray.put(jsonObj);
				jsonObj = null;
			}
			SoapObject request= new SoapObject ("http://tempuri.org/", "UploadAttendanceInfo");
			request.addProperty("JsonData", jsonarray.toString());
			SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.bodyOut=request;
			envelope.dotNet = true; 
			envelope.setOutputSoapObject(request);  
			HttpTransportSE ht=new HttpTransportSE(path);
			  
			try {
				ht.call("http://tempuri.org/UploadAttendanceInfo", envelope);
			} catch (IOException e1) {
					// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			} catch (XmlPullParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
						
			 SoapObject result = (SoapObject) envelope.bodyIn;
		//	 System.out.println("返回值： "+result);
			 String tmp1 =  result.getProperty(0).toString();
			 try {
				JSONObject resultStr = new JSONObject(tmp1);
				int ResultCode = resultStr.getInt("ResultCode");
				resultStr.getString("Msg");
				if(ResultCode == 1)
				{
					updateTag = true;
				}
				else
				{
					updateTag = false;
					return null;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		
		if(updateTag == true)
		{
			Editor editor;
			editor = sharedPreferences.edit();
			editor.putString("lastUpdateTimer", date);
			editor.commit();
		}
		
		return null;
	}
 }




 