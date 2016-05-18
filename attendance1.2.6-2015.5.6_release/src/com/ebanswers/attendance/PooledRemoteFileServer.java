package com.ebanswers.attendance;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.ebanswers.object.Person;
import com.ebanswers.object.PersonEx;
import com.ebanswers.object.SaveConnectionClass;
import com.ebanswers.object.SortAttendanceClass;
import com.ebanswers.object.SortClass;
import com.example.db.Attendance;
import com.example.db.DBAdapter;
import com.example.db.ShutOpenDeviceClass;
import com.example.util.DataConvert;
import com.example.util.DataRecvProc;
import com.example.util.MsgObj;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PooledRemoteFileServer {
   protected int maxConnections;
   protected int listenPort=31344; 
   public static int SERVERPORT = 31344;  //注意这里和上面重定向的配置是有关系的。
   static int connectCount = 0;
   static public DataRecvProc mc; 
   public static Handler g_tcpSendThreadhandler;
   public static String g_tcpIP="";
   public static boolean serverTag = true;
   Context mContext; 
   ServerSocket server= null;
   Socket incomingConnection = null;
   static Map<Socket, PooledConnectionHandler> m_map = new HashMap<Socket, PooledConnectionHandler>();
   private ExecutorService executorService;
   static public List<PooledConnectionHandler> taskList = new ArrayList<PooledConnectionHandler>();
   private boolean quit = false;
   public Socket m_socket=null;

   
   public void SetSocket(Socket sock)
   {
	   m_socket = sock;
   }
  
   public void setI(DataRecvProc mc){
	   //设置收到数据回调函数
	   PooledRemoteFileServer.mc=mc; 
   }
   
   public boolean GetServerStat()
   {
	   return this.quit;
   }

   public void setDeviceId(String str)
   {
	   //设置本机的设备序列号
       PooledConnectionHandler.SerialNumber = str;
   }
   
	//创建服务端ServerSocket对象
   public PooledRemoteFileServer(int aListenPort, int maxConnections,Context m_context) {
       listenPort= aListenPort;
        this.maxConnections = maxConnections;
        mContext = m_context;
   }
   
   public void CloseTcpConnect()
   {
		if(incomingConnection!=null)
		{
		   try {
		   incomingConnection.shutdownInput();
	   	   incomingConnection.shutdownOutput();
	   	   incomingConnection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   	
	   	   connectCount=connectCount-1;
	   	   PooledConnectionHandler.SetCount(connectCount);
	   }
   }
 
	
   public void acceptConnections() throws SocketException {
	  // System.out.println(Runtime.getRuntime().availableProcessors());  //读取cpu核心数
	   executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
 	        	
			try {
				server = new ServerSocket(listenPort, maxConnections);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			g_tcpIP = getLocalIpAddress();
					
			  while (!quit) {
			    Socket socket = null;
			    try {
				   	//	System.out.println("服务器start listen ");
		                // 接收客户连接,只要客户进行了连接,就会触发accept();从而建立连接
				   	
				   		if(server!=null)
				   		{
					   		socket = server.accept();
					     	PooledConnectionHandler m_pooledConnectionHandler = new PooledConnectionHandler(socket); //处理数据收的线程
							taskList.add(m_pooledConnectionHandler);
							//m_map.put(m_pooledConnectionHandler, socket);
							m_map.put(socket, m_pooledConnectionHandler);
							executorService.execute(m_pooledConnectionHandler);
							//executorService.submit(m_pooledConnectionHandler);
				   		}
			        } catch (Exception e) {
			        	e.printStackTrace();
			        //	System.out.println("#### 线程池创建线程失败");
			        }
			    }
    }
   
	public void quit() {
		this.quit = true;
		try {
			for (PooledConnectionHandler tast : taskList) {
				tast.CloseSocket();
			}
			server.close();
			server = null;
			executorService.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	

	
   public static String getLocalIpAddress() throws SocketException 
	{
	    String ipv4 = null;  
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
		{
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();	enumIpAddr.hasMoreElements();) 
			{
				InetAddress inetAddress = enumIpAddr.nextElement();
			
				
				//如果不是回环地址
				if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) 
				 {
					//直接返回本地IP地址
					ipv4 = inetAddress.getHostAddress();
				//	System.out.println("ipv4 : "+ipv4);
					if(ipv4.equals("192.168.43.1")!=true)
					{
						return ipv4;
					}
				 }
			
			}
		}
		
		return ipv4;
	}
 
   public int GetConnectCount()
   {
	   return PooledConnectionHandler.GetCount();
   }
}



class PooledConnectionHandler implements Runnable {
	  //网络数据处理线程
	   static int m_connectCount = 0;
	   private boolean serverRuning = false;
	   public Socket connection=null;
	   protected static List pool = new LinkedList();
	   String recvMessageServer;
	   public static String SerialNumber;
	   char[] buffer = new char[1100];
	   char[] tmpBuffer = new char[1100];
	   byte[] bufferByte = new byte[2200];
       int count = 0;
       int	m_nRecvSize = 0; //当前读取到的tcp连接的数据数
       byte[]  m_cbDataBuf = new byte[1024];
       byte[] chatfrom = new byte[1024];
       int NPACKSIZE = 252;   //每个数据包的大小
       Handler m_handler;
       ProcNetData m_procNetData=null;
      // BufferedReader mBufferedReaderServer = null;

       
       public void SetSocket(Socket m_sendSocket)
       {
    	   connection = m_sendSocket;
       }
       
       PooledConnectionHandler(Socket m_sendSocket)
       {
    	    connection = m_sendSocket;
    		m_procNetData = new ProcNetData();
			m_procNetData.start();
       }
     
       public void SendTcpData(byte[] str) throws IOException
       {
    	   sendTcpData(str);
       }
    
	   public int  Connection() throws IOException {	
		   InputStream m_inputStream = connection.getInputStream();
		   DataInputStream m_dataInputStream = new DataInputStream(m_inputStream); 
	   try
	   {
	       serverRuning = true;
	       while(m_procNetData.getState() == Thread.State.WAITING)
	       { //消息处理线程还没准备好
	    	   Thread.sleep(100);
	       }
	     
	     //  while(((count = m_inputStream.read(bufferByte))>0))  //按字节流读取
	       while(((count = m_dataInputStream.read(bufferByte))>0))
		   { 
	    	//	System.out.println("### recv data count : "+ count);
	    	  // for(int k=0; k<40; k++)
	    	  // {
	    	//	   System.out.println("### k  : "+k +"  bufferByte[k] :"+bufferByte[k]);
	    	  // }
				m_nRecvSize += count;
				  while (m_nRecvSize >= 12)
					{
					  	if(bufferByte[8] != 5)
		            		NPACKSIZE = 252;  //普通消息
		            	else
		            		NPACKSIZE = 832;  //消息中带有utf8汉字数据
					    byte[]  packageLenByte = new byte[4];
						System.arraycopy(bufferByte, 10, packageLenByte, 0, 4);
						int buf_len  = MsgObj.Byte2Int(packageLenByte);
						
						// 判断是否已接收到足够一个完整包的数据
						if (m_nRecvSize < NPACKSIZE)
						{
							// 还不够拼凑出一个完整包
							break;
						}
					   	Message msg = m_handler.obtainMessage();
						Arrays.fill(m_cbDataBuf, (byte)0);
						// 拷贝到数据缓存
						System.arraycopy(bufferByte, 0, m_cbDataBuf, 0, NPACKSIZE);
						// 从接收缓存移除
						System.arraycopy(bufferByte, NPACKSIZE, bufferByte, 0, m_nRecvSize-NPACKSIZE);
						m_nRecvSize -= NPACKSIZE;
						byte[] tmpStr = new byte[NPACKSIZE];
						System.arraycopy(m_cbDataBuf, 0, tmpStr, 0, NPACKSIZE);
						msg.obj = connection;
						Bundle b = new Bundle();  
						b.putByteArray("msg", tmpStr);
			
						msg.setData(b);  
						m_handler.sendMessage(msg);
				   }
		   }
	       PooledRemoteFileServer.m_map.remove(connection);
	       m_inputStream.close();
			connection.close();
			connection = null;
			serverRuning = false;
			m_connectCount = m_connectCount-1;
			return 1;
			
		}
		catch (Exception e)
		{
			recvMessageServer = "接收异常:" + e.getMessage();//消息换行
			System.out.println(recvMessageServer);
			PooledRemoteFileServer.m_map.remove(connection);
			if(m_inputStream != null)
			{
				m_inputStream.close();
				m_inputStream = null;					
			}
			
			if(connection !=null)
			{
				connection.close();
				connection = null;
			}
			serverRuning = false;
			m_connectCount = m_connectCount-1;
			return 0;
		}
	}
     public BufferedOutputStream outByte = null;
	 OutputStream os = null;
	 public  void CloseSocket()
	 {
		 try {
			 if(connection!=null)
			 {	  
				 PooledRemoteFileServer.m_map.remove(connection);
				 connection.close();
				 connection = null;
			 }
			 if(outByte != null)
			 {
				 outByte.close();
				 outByte = null;
				 
			 }
			 if(os != null)
			 {
				 os.close();
				 os = null;
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }

	 public static char[] getChars (byte[] bytes) {
	      Charset cs = Charset.forName ("UTF-8");
	      ByteBuffer bb = ByteBuffer.allocate (bytes.length);
	      bb.put (bytes);
	                 bb.flip ();
	       CharBuffer cb = cs.decode (bb);
	  
	       return cb.array();
	}
	 
	 

	 
	    // 取得某个月有多少天  
	    public static int getDaysOfMonth(int year, int month) {  
	        Calendar cal = Calendar.getInstance();  
	        cal.set(Calendar.YEAR, year);  
	        cal.set(Calendar.MONTH, month-1);  
	        int days_of_month = cal.getActualMaximum(Calendar.DAY_OF_MONTH);  
	        return days_of_month;  
	    }  
	  
	 static public List<SortAttendanceClass> m_time7daysSortList = new ArrayList<SortAttendanceClass>();
	 static public List<SortAttendanceClass> m_time30daysSortList = new ArrayList<SortAttendanceClass>();
	 static public List<SortAttendanceClass> m_time90daysSortList = new ArrayList<SortAttendanceClass>();
	 static public List<SortAttendanceClass> m_time365daysSortList = new ArrayList<SortAttendanceClass>();
	 static public List<SortAttendanceClass> m_timeAlldaysSortList = new ArrayList<SortAttendanceClass>();
	 static Map<String, String> m_mapName = new HashMap<String, String>();
	       
	 @SuppressWarnings("deprecation")
	 int SortAttendance()
	 {	 
		 int ret=0;
		int cur_day = 0;
		 Calendar calTmp = Calendar.getInstance();
		//  日期的DATE减去10  就是往后推10 天 同理 +10 就是往后推十天
		calTmp.add(Calendar.DATE, cur_day);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//
		int w = calTmp.get(Calendar.DAY_OF_WEEK);
		 
	 	int begindays = 0, endDays=0;
	 	switch(w)
	 	{
	 	case 1:  //星期天
	 		begindays = 13;
	 		endDays = 7;
	 		break;
	 	case 2:  //星期1
	 		begindays = 7;
	 		endDays = 1;
	 		break;
	 	case 3:  //星期2
	 		begindays = 8;
	 		endDays = 2;
	 		break;
	 	case 4:  //星期3
	 		begindays = 9;
	 		endDays = 3;
	 		break;
	 	case 5:  //星期4
	 		begindays = 10;
	 		endDays = 4;
	 		break;
	 	case 6:  //星期5
	 		begindays = 11;
	 		endDays = 5;
	 		break;
	 	case 7:  //星期6
	 		begindays = 12;
	 		endDays = 6;
	 		break;
	 	default:
	 		break;
	 	}

		Calendar newtime1 = Calendar.getInstance();
		newtime1.add(Calendar.DATE, -begindays);
		Calendar newtime2 = Calendar.getInstance();
		newtime2.add(Calendar.DATE, -endDays);

	
	 	int days30_year;
	 	int days30_month;
	 	int days30_day;
	 	int curmonth, curyear, curday;
	 	curyear = calTmp.get(Calendar.YEAR);
	 	curmonth  = calTmp.get(Calendar.MONTH) + 1;
	 	if(curmonth==1)
	 	{
	 		days30_year = curyear-1;
	 		days30_month = 12;
	 	}
	 	else
	 	{
	 		days30_year= curyear;
	 		days30_month = curmonth-1;
	 	}
	
	 	int day30_beginDay = 1;
	 	int days30_endDay = getDaysOfMonth(days30_year, days30_month);
	 	int day90_year;
	 	int day90_month = 0;
	 	int day90_day = 0;
	 	if(curmonth<=3)
	 	{
	 		day90_year = curyear-1;
	 		day90_month = 9;
	 		day90_day = 31;
	 	}
	 	else
	 	{
	 		day90_year = curyear;
	 		switch(curmonth)
	 		{
	 		case 4:
	 		case 5:
	 		case 6:
	 			day90_month = 1;
	 			day90_day = 31;
	 			break;
	 		case 7:
	 		case 8:
	 		case 9:
	 			day90_month = 4;
	 			day90_day = 30;
	 			break;
	 		case 10:
	 		case 11:
	 		case 12:
	 			day90_month = 7;
	 			day90_day = 30;
	 			break;
	 		default:
	 			break;
	 		}
	 	}
	 	
	 	String tmp1 = ""+newtime1.get(Calendar.YEAR)+"-"+(newtime1.get(Calendar.MONTH)+1)+"-"+newtime1.get(Calendar.DAY_OF_MONTH)+" 00:00:00";
		String tmp2 = ""+newtime2.get(Calendar.YEAR)+"-"+(newtime2.get(Calendar.MONTH)+1)+"-"+newtime2.get(Calendar.DAY_OF_MONTH)+" 23:59:59";
    			    				
		//上月
		String day30Time1 = ""+days30_year+"-"+days30_month+"-"+day30_beginDay+" 00:00:00";
		String day30Time2 = ""+days30_year+"-"+days30_month+"-"+days30_endDay+" 23:59:59";
		//上季度
		String day90Time1 = ""+day90_year+"-"+day90_month+"-"+1+" 00:00:00";
		String day90Time2 = ""+day90_year+"-"+(day90_month+2)+"-"+day90_day+" 23:59:59";
		
		Date d1 = null;
		Date d2 = null;
		Date d30Time1 = null;
		Date d30Time2 = null;
		Date d90Time1=null;
		Date d90Time2=null;
		try {
			d1 = sf.parse(tmp1);
			d2 = sf.parse(tmp2);
			d30Time1 = sf.parse(day30Time1);
			d30Time2 = sf.parse(day30Time2);
		    d90Time1 = sf.parse(day90Time1);
		    d90Time2 = sf.parse(day90Time2);
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	 	 	
	 	if(m_mapName.size()>0)
	 		m_mapName.clear();
	 	if(!m_time7daysSortList.isEmpty())
	 		m_time7daysSortList.clear();
	 	if(!m_time30daysSortList.isEmpty())
	 		m_time30daysSortList.clear();
	 	if(!m_time90daysSortList.isEmpty())
	 		m_time90daysSortList.clear();
	 	if(!m_time365daysSortList.isEmpty())
	 		m_time365daysSortList.clear();
	 	if(!m_timeAlldaysSortList.isEmpty())
	 		m_timeAlldaysSortList.clear();
		 
		DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
		// 打开数据库
		dbAdapter.open();
		long len = dbAdapter.getCount("select count (*) from Person");
		Person[] m_Person = new Person[(int) (len)];
		m_Person = dbAdapter.queryPersonAllData();

	 	String oneTime = null, twoTime = null;  //oneTime 当天最后一次打卡，twoTime 当天最早一次打卡
	 	int year, month, day, hour, minute, second;
	 	int preYear=0, preMonth=0, preDay=0;
	 	long timeLen, days7Len, days30Len, days90Len, days365Len, daysAllLen;
	 	
	 	if(len>0)
	 	{
	 		for(int i=0; i<len; i++)
	 		{
	 		    timeLen=0;
		    	days7Len = 0;
		    	days30Len = 0;
		    	days90Len = 0;
		    	days365Len = 0;
		    	preYear=0;
		    	preMonth=0;
		    	preDay=0;
		    	year = month=day = 0;
		    	oneTime = twoTime = null;
	 			String name = m_mapName.get(""+m_Person[i].name);
	 			if(name==null)
	 			{	
	 				//没找到
	 				String tmp ;
	 				tmp="select count (*) from Attendance where name='"+m_Person[i].name+"'";
     			    long attendancelen = dbAdapter.getCount(tmp.toString()); 
     	
     			    if(attendancelen > 0)
     			    {
	        			    Attendance[] m_Attendance = new Attendance[(int)(attendancelen)];	
	        			    String sql="select * from Attendance where name='"+m_Person[i].name+"' order by IO_DateTime desc ";    	
	        			    m_Attendance = dbAdapter.getAttendanceQuery(sql);
	        			    long startTime1=System.currentTimeMillis();
	        			    for(int j=0; j<attendancelen; j++)
	        			    {
	           			    	String timestr =  m_Attendance[j].dateTime;
	           		
	           			    	Date m_date=null;
	           			    	try {
									m_date = sf.parse(timestr);
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	           			    	year = m_date.getYear();
	           			    	month = m_date.getMonth()+1;
	           			    	day = m_date.getDate();
	           			    	hour = m_date.getHours();
	           			    	minute = m_date.getMinutes();
	           			    	second = m_date.getSeconds();
	 
	        			    	if(year==preYear&&month==preMonth&&day==preDay&&j!=attendancelen-1)
	        			    	{  //同一天且已经有了一次打卡数据，把此次打卡数据保存在twoTime,做为最后一次打卡;
	        			    		twoTime = timestr;
	        			    	}
	        			    	else
	        			    	{  //不是同一天,计算已保存的打卡时间，把此条打卡时间保存入oneTime,做为另一次统计的时间
	        			    		preYear = year;
	        			    		preMonth = month;
	        			    		preDay = day; 

	        			    		if(j==attendancelen-1)
	        			    		{
	        			    			twoTime=timestr;
	        			    		}
	        			    			
	        			    		// 这一天的第一次打卡记录
	        			    		if(oneTime==null)
        			    			{
        			    				oneTime = timestr;  //起始时间与结束时间一样。
        			    			}
	        			    			
	        			    		if(twoTime!=null)
	        			    		{
	        			    			int hour1=0, minute1=0, year1=0, month1=0,day1=0, second1=0;	        			    			
	        			    			Date m_date1=null;
	        			 			   	try {
											m_date1 = sf.parse(oneTime);
										} catch (ParseException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
								
	    	           			    	year = m_date1.getYear();
	    	           			    	month = m_date1.getMonth()+1;
	    	           			    	day = m_date1.getDate();
	    	           			    	hour = m_date1.getHours();
	    	           			    	minute = m_date1.getMinutes();
	    	           			    	second = m_date1.getSeconds();
	    	           			    	
	        			    				Date m_date2 = null;
	    	           			    		try {
												m_date2 = sf.parse(twoTime);
											} catch (ParseException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
	    	           			    		year1 = m_date2.getYear();
	    	           			    		month1 = m_date2.getMonth()+1;
	    	           			    		day1 = m_date2.getDate();
	    	           			    		hour1 = m_date2.getHours();
	    	           			    		minute1 = m_date2.getMinutes();
	    	           			    		second1 = m_date2.getSeconds();
	        			    				if(year!=year1||month!=month1||day!=day1)
	        			    				{
	        			    					oneTime = twoTime = null;
	        			    					continue;
	        			    				}
	        			    				//全记录
	        			    				int tmptimeLen  = hour*60+minute-hour1*60-minute1;   //累积时间,需要保存下来
	        			    					timeLen += 	tmptimeLen;	   
	        			    					
	        			    				//本年度
	        			    				String day365Time1 = ""+curyear+"-01-01 00:00:00";
	        			    				String day365Time2 = ""+curyear+"-12-31 23:59:59";
	        			    				try
	        			    				{
	        			    				    Date cur = sf.parse(twoTime);
	        			    				    if(cur.getTime()-d1.getTime()>=0 && d2.getTime()-cur.getTime()>=0)
	        			    				    	days7Len+= tmptimeLen;  //1周
	        			     				    if(cur.getTime()-d30Time1.getTime()>=0 && d30Time2.getTime()-cur.getTime()>=0)
	        			    				    	days30Len+= tmptimeLen;  //1个月
	        			    				    if(cur.getTime()-d90Time1.getTime()>=0 && d90Time2.getTime()-cur.getTime()>=0)
	        			    				    	days90Len+= tmptimeLen;  //3个月
	        			    				    
	        			    				    Date d365Time1 = sf.parse(day365Time1);
	        			    				    Date d365Time2 = sf.parse(day365Time2);
	        			    				    String tmpStr = day365Time1+"  "+day365Time2;
	        			    				    if(cur.getTime()-d365Time1.getTime()>=0 && d365Time2.getTime()-cur.getTime()>=0)
	        			    				    {
	        			    				    	days365Len+= tmptimeLen;  //本年度	
	        			    				    }
	        			    				}
	        			    				catch (Exception e)
	        			    				{	 
	        			    				}
	        			    				twoTime=null;
	        			    				oneTime = timestr;
	        			    			}
	        			    			else
	        			    			{
	        			    				oneTime = timestr;
	        			    				twoTime = null;
	        			    			}
	        			    		}
			 					}
			 					oneTime=twoTime=null;			 					
     			    }
     			    else
     			    {
     			    	days7Len = 0;
     			    	days30Len = 0;
     			    	days90Len = 0;
     			    	days365Len = 0;
     			    	timeLen = 0;
     			    }
     			 	SortAttendanceClass m_timeLenSort7 = new SortAttendanceClass();
 					m_timeLenSort7.name = m_Person[i].name;
 					m_timeLenSort7.department = m_Person[i].Department;
 					m_timeLenSort7.timedaysLen = days7Len;
 					m_time7daysSortList.add(m_timeLenSort7);
 					SortAttendanceClass m_timeLenSort30 = new SortAttendanceClass();
 					m_timeLenSort30.name = m_Person[i].name;
 					m_timeLenSort30.department = m_Person[i].Department;
 					m_timeLenSort30.timedaysLen = days30Len;
 					m_time30daysSortList.add(m_timeLenSort30);
 					SortAttendanceClass m_timeLenSort90 = new SortAttendanceClass();
 					m_timeLenSort90.name = m_Person[i].name;
 					m_timeLenSort90.department = m_Person[i].Department;
 					m_timeLenSort90.timedaysLen = days90Len;
 					m_time90daysSortList.add(m_timeLenSort90);
 					SortAttendanceClass m_timeLenSort365 = new SortAttendanceClass();
 					m_timeLenSort365.name = m_Person[i].name;
 					m_timeLenSort365.department = m_Person[i].Department;
 					m_timeLenSort365.timedaysLen = days365Len;
 					m_time365daysSortList.add(m_timeLenSort365);
 					SortAttendanceClass m_timeLenSortAll = new SortAttendanceClass();
 					m_timeLenSortAll.name = m_Person[i].name;
 					m_timeLenSortAll.department = m_Person[i].Department;
 					m_timeLenSortAll.timedaysLen = timeLen;
 					m_timeAlldaysSortList.add(m_timeLenSortAll);
 					m_mapName.put(m_Person[i].name, m_Person[i].name);
 					
     		     //   long allafterTime=System.currentTimeMillis();
	 		    //    long alltimeDistance=allafterTime-startTime;
	 		      //  System.out.println("总耗时 : "+alltimeDistance+"ms");
	 			}

	 	       }
	 			
	 			int tmpLen = 0;
 				Comparator comp = new SortClass();  
 				Collections.sort(m_time7daysSortList, comp);
 				tmpLen = m_time7daysSortList.size();
 				/*
 				for(int i= 0; i<tmpLen; i++)
 				{
 					
 				}
 				*/
 				Collections.sort(m_time30daysSortList, comp);
 				tmpLen = m_time30daysSortList.size();
 				/*
 				for(int i= 0; i<tmpLen; i++)
 				{
 					
 				}
 				*/
 				Collections.sort(m_time90daysSortList, comp);
 				tmpLen = m_time90daysSortList.size();
 				/*
 				for(int i= 0; i<tmpLen; i++)
 				{
 					
 				}
 				*/
 				Collections.sort(m_time365daysSortList, comp);
 				tmpLen = m_time365daysSortList.size();
 				/*
 				for(int i= 0; i<tmpLen; i++)
 				{
 					
 				}
 				*/
 				Collections.sort(m_timeAlldaysSortList, comp);
 				tmpLen = m_timeAlldaysSortList.size();
 				/*
 				for(int i= 0; i<tmpLen; i++)
 				{
 					
 				}
 				*/
	 		}
	 		else
	 			ret = -1;
	 		dbAdapter.close();
	 		dbAdapter = null;
	 		return ret;
	 }
	 
	@SuppressWarnings("unchecked")
	public  void processRequest(Socket requestToHandle) {

			connection =  requestToHandle;
	    }
	    public static void SetCount(int connectcount)
	    {
	    	m_connectCount = connectcount;
	    }
	    public static int GetCount()
	    {
	    	return m_connectCount;
	    }
	 
	    public void run() {
	    	try {
	        while(true) {
				if(Connection()==0)
				{
					break;
				}
			} 
			
	      }catch (Exception e) {
				e.printStackTrace();
				Server.taskList.remove(this);
			} finally {
				try {
					if (null != connection)
					{
						   PooledRemoteFileServer.m_map.remove(connection);
						connection.close();
						connection = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	   }
	  
	  
	    public  void sendTcpData(byte[] str) throws IOException
	    {  
	    	/*
	    	//发送tcp数据到pc
	    	  if(outByte==null)
	    	  {
	    		  os= connection.getOutputStream();
	    		  if(os!=null)
	    			  outByte= new BufferedOutputStream(os);
	    		  else
	    			  return;
	    	  }
	    	  if(outByte==null)
	    		  return;
	    	  
	    	  */
	    	  os= connection.getOutputStream();
    		  if(os!=null)
    			  outByte= new BufferedOutputStream(os);
    		  else
    			  return;
	    	  outByte.write(str);
	    	  outByte.flush();    
	    	  outByte = null;
	    	  os = null;
	    }
	    
		private String getInfoBuff(char[] buff, int count)
		{
			byte[] m_byte = DataConvert.getBytes(buff);
			int len = buff[12]; //数据长度
			char[] temp = new char[len+14];  //len为data长度，14为包头长度
			for(int i=0; i<22;i ++)
			{
				int d;
				if(buff[i]>128)
					d =	(buff[i]>>2)&0xff;
				else
					d= buff[i]&0x0ff;
			}
			if(m_byte[0] == -17)
			{
			}
			else
			{
			}
	
			return new String(temp);
		}	
		
		private Object savedataObj = new Object() ;
		//处理网络数据线程
		 class ProcNetData extends Thread {
			 static final String TAG="PooledRemoteFileServer";
			Socket m_socket=null;
			void SetSocket(Socket m_socket)
			{
				this.m_socket = m_socket;
			}
			@Override
			public void run() {			
				super.run();
				Looper.prepare();
	            class MyHandler extends Handler{             
	                
					private Object m_startTime1;
					public MyHandler(Looper looper){
	                	super (looper);
	                	}
	                
	                public MyHandler(){
	                }
	                
	                public void handleMessage(Message msg) { // 处理消息
	                //	System.out.println("###  net   thread : "+Thread.currentThread().getId());
	                	Socket m_socket = (Socket)msg.obj;
	                	Bundle b = msg.getData();  
	                	byte buffer[] = (byte[]) b.getByteArray("msg");
	                    //合法数据
					if (buffer[0] == -17 && buffer[1] == 1) {
						MsgObj msgobj = new MsgObj(buffer, buffer.length);
						msgobj.parseMsgPackage();
						int head_len = 12;
						int data_len;
						byte[] headBuf = new byte[head_len];
						// 网络数据包包头
						headBuf[0] = -17;
						headBuf[1] = 1;
						headBuf[2] = 0; // device_id
						headBuf[3] = 0; // device_id
						headBuf[4] = 0; // device_id
						headBuf[5] = 0; // deivce_id
						headBuf[6] = 0; // 填位
						headBuf[7] = 0; // 填位
						headBuf[8] = 1; // type
						headBuf[11] = 0;

	        				switch(msgobj.msg_id)
	        				{
		        				case 1:
		        				{ 
								DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
								// 打开数据库
								dbAdapter.open();
								long len = dbAdapter.getCount("select count (*) from Person");
								Person[] m_Person = new Person[(int) (len)];
								m_Person = dbAdapter.queryPersonAllData();
								//m_Person = dbAdapter.queryPersonExAllData();
								dbAdapter.close();
								dbAdapter = null;
								data_len = 76;
								byte[] sendBuf = new byte[data_len + head_len];
								byte[] recordBuf = new byte[4 + head_len];
								headBuf[9] = 1; // msg_id
								headBuf[10] = (byte) (head_len + data_len); // buf_len

								if (len > 0) {
									headBuf[9] = 4;
									headBuf[10] = (byte) (head_len + 4);
									Arrays.fill(recordBuf, (byte) 0);
									System.arraycopy(headBuf, 0, recordBuf, 0,
											head_len);
									System.arraycopy(InteractionService.intToBytes((int) len), 0,
											recordBuf, 0 + head_len, 4);

									try {
										SendTcpData(recordBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
								
									
									for (int i = 0; i < len; i++) {
										// 数据长度 16+30+30=76;
										Arrays.fill(sendBuf, (byte) 0);
										headBuf[9] = 1; // msg_id
										headBuf[10] = (byte) (head_len + data_len); // buf_len
										System.arraycopy(headBuf, 0, sendBuf, 0,head_len);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].id), 0,sendBuf, 0 + head_len, 4);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].autoID), 0,sendBuf, 4 + head_len, 4);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].Gender), 0,sendBuf, 8 + head_len, 4);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].Admin), 0,sendBuf, 12 + head_len, 4);
										System.arraycopy(m_Person[i].name.getBytes(), 0,sendBuf, 16 + head_len,m_Person[i].name.getBytes().length);
										System.arraycopy(m_Person[i].Department.getBytes(), 0, sendBuf,46 + head_len,m_Person[i].Department.getBytes().length);
									//	System.out.println(m_Person[i].toString());
										try {
											SendTcpData(sendBuf);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									System.out.println("BBBBBBBBBB");
									try {
										headBuf[8] = 109;
										headBuf[9] = 109;
										headBuf[10] = (byte) head_len;
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} else {
									try {
										// 数据为空
										headBuf[8] = 126; // type
										headBuf[10] = (byte) head_len;
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							break;
							
		        				case 2:
		        				{
		        					byte[]  timeData = new byte [14];
		        					System.arraycopy(buffer, 12, timeData, 0, 14);
		                       	    String m_month, m_day, m_hour, m_minute, m_second;
		                    	    m_month=String.format("%02d", timeData[2]);
		                    	    m_day = String.format("%02d", timeData[3]);
		                    	    m_hour = String.format("%02d", timeData[4]);
		                    	    m_minute = String.format("%02d", timeData[5]);
		                    	    m_second = String.format("%02d", timeData[6]);
		                    	  
		                       		String m_date1 = ""+timeData[0]+timeData[1]+"-"+m_month+"-"+m_day;
			        				String m_time1 = ""+m_hour+":"+m_minute+":"+m_second;
			        				
			        	       	    m_month=String.format("%02d", timeData[9]);
		                    	    m_day = String.format("%02d", timeData[10]);
		                    	    m_hour = String.format("%02d", timeData[11]);
		                    	    m_minute = String.format("%02d", timeData[12]);
		                    	    m_second = String.format("%02d", timeData[13]);
		         		                    	
		                    	    
			        				String m_date2 = ""+timeData[7]+timeData[8]+"-"+m_month+"-"+m_day;
			        				String m_time2 = ""+m_hour+":"+m_minute+":"+m_second;
			        				String m_dateTime1 = "datetime('"+m_date1+" "+m_time1+"')";
			        				String m_dateTime2 = "datetime('"+m_date2+" "+m_time2+"')";
			        				DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
			        		    	//打开数据库
			        			    dbAdapter.open();
			        			    
			        			    StringBuffer tmp = new StringBuffer("select count (*) from Attendance where datetime(IO_DateTime)>=");
			        			    tmp.append(m_dateTime1);
			        			    tmp.append(" and datetime(IO_DateTime)<=");
			        			    tmp.append(m_dateTime2);
			  
			        			    long len = dbAdapter.getCount(tmp.toString()); 
			       
			        			    tmp=null;
			        			    if(len > 0)
			        			    {
				        			    Attendance[] m_Attendance = new Attendance[(int)(len)];
				        			    StringBuffer tmp1 = new StringBuffer("select * from Attendance where datetime(IO_DateTime)>=");
				        			    tmp1.append(m_dateTime1);
				        			    tmp1.append(" and datetime(IO_DateTime)<=");
				        			    tmp1.append(m_dateTime2);
				        			    
				        			    m_Attendance = dbAdapter.getAttendanceQuery(tmp1.toString());
				        			    tmp1=null;
				        				dbAdapter.close();
				        				dbAdapter =null;
				        			    data_len = 72;
				        				byte[]  sendBuf = new byte[data_len+head_len];
				        				byte[]  sendBuf1 = new byte[data_len*10+head_len];
				        				byte[]  recordBuf = new byte[4+head_len];
				        				Arrays.fill(sendBuf, (byte)0);
				        			

				        				headBuf[10] = (byte) (head_len+4);  //buf_len		
				        				headBuf[9] = 5;
			        					Arrays.fill(recordBuf, (byte)0);
			        					System.arraycopy(headBuf, 0, recordBuf, 0, head_len);
			        					System.arraycopy(InteractionService.intToBytes((int)len), 0, recordBuf, 0+head_len, 4);	
			        					
							
				                    	try {  //发送数据包总个数
				                    		SendTcpData(recordBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									
										headBuf[10] = (byte) (head_len+data_len);  //buf_len	
										int k=0;
				        				for(int i=0; i<len; i++)
				        				{	        	
				        					//数据长度  32+20+20=72;
				        					Arrays.fill(sendBuf, (byte)0);
				        					headBuf[9] = 2;    //msg_id
				        					     					
				        					System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].ID), 0, sendBuf, 0+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].autoID), 0, sendBuf, 4+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].year), 0, sendBuf, 8+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].month), 0, sendBuf, 12+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].day), 0, sendBuf, 16+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].hour), 0, sendBuf, 20+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].minute), 0, sendBuf, 24+head_len, 4);
				        					System.arraycopy(InteractionService.intToBytes(m_Attendance[i].second), 0, sendBuf, 28+head_len, 4);
				        					System.arraycopy(m_Attendance[i].date.getBytes(), 0, sendBuf, 32+head_len, m_Attendance[i].date.getBytes().length);     	
				        					System.arraycopy(m_Attendance[i].time.getBytes(), 0, sendBuf, 52+head_len, m_Attendance[i].time.getBytes().length);     			
				        					try {
				        						SendTcpData(sendBuf);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
				        				}
				        				
				        				try {
				        					//传??数据完毕
				        					headBuf[8] = 127;
				        					headBuf[10] = (byte) head_len;
				        					SendTcpData(headBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
			        			    }
			        			    else
			        			    {
			        			    	try {
				        					//数据为空
				        					headBuf[8] = 126;   //type
				        					headBuf[10] = (byte) head_len;
				        					SendTcpData(headBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
			        			    }
		        				}
		        				break;
		        				case 9: {
									// tcp服务器密码验证
									SharedPreferences sharedPreferences;
									Context mContext;
									mContext = InteractionService.g_context;
									sharedPreferences = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
									String m_pwdStr = sharedPreferences.getString("PassWord", "");
									if (m_pwdStr.equals("") == false) // 密码不为空
									{
										int len = buffer[10] - 12;
										byte[] tmpPwd = new byte[len];
										System.arraycopy(buffer, 12, tmpPwd, 0, len);
										String tmp = new String(tmpPwd);
										if (m_pwdStr.equals(tmp) == true) {
											headBuf[8] = 108; // type
										} else {
											// 密码验证错误
											headBuf[8] = 116; // type
										}
										headBuf[10] = (byte) head_len;
										try {
											SendTcpData(headBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
									} else // 密码为空
									{
										// 密码为空，任意pc主控端都可链接
										headBuf[8] = 108; // type
										headBuf[10] = (byte) head_len;
										
										try {
											SendTcpData(headBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
								break;
		        				case 10:
		        				{
		        					// 修改用户信息
									byte[]  tmpName = new byte[30];
		                    		byte[]  tmpID = new byte[4];
		                    		byte[]  tmpAdmin = new byte[4];
		                    		byte[]  tmpDepartment = new byte[30];
		                    			                    		
		                    		System.arraycopy(buffer, 24, tmpID , 0, 4);
		                    		System.arraycopy(buffer, 28, tmpAdmin, 0, 4);
		                    		System.arraycopy(buffer, 40, tmpName, 0, 30);
		                    		System.arraycopy(buffer, 100, tmpDepartment, 0, 30);
		                    		Person m_people = new Person();
		                    		m_people.id = MsgObj.Byte2Int(tmpID);
		                    		m_people.Gender = 0;
		                    		m_people.Admin =  MsgObj.Byte2Int(tmpAdmin);
		                    		m_people.name = new String(tmpName);
		                    		m_people.Department = new String(tmpDepartment);
		                    		
									// 修改用户信息
		                    		DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
			        		    	//打开数据库
			        			    dbAdapter.open();
			        			 
			        			    dbAdapter.updatePersonOneData(m_people.id, m_people);
			        				
			        				dbAdapter.close();
			        				dbAdapter =null;
			        				
			        				//修改信息成功
		        					headBuf[8] = 114;  //type
		        					headBuf[10] = (byte) head_len;    
		        					
			                		try {
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
			        				break;
		        				}
						case 11: 
							{
								// 收到心跳包，用来判断网络连接是否存在
								// 2分钟收不到心跳包，即是断开连接
								// 注册
								headBuf[8] = 110; // type
								headBuf[10] = (byte) head_len;
								try {
									SendTcpData(headBuf);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							break;
						case 12: {
							//发送消息到ui线程去更新ui
							Message childMsg = InteractionService.testHandler.obtainMessage();
							childMsg.what= 12;
			   			    InteractionService.testHandler.sendMessage(childMsg);
						}
							break;
						case 13: {
							// 新加消息，1 ID 对多名字， 传递所有数据
							byte[] timeData = new byte[14];
							System.arraycopy(buffer, 12, timeData, 0, 14);
							String m_month, m_day, m_hour, m_minute, m_second;
							m_month=String.format("%02d", timeData[2]);
	                    	m_day = String.format("%02d", timeData[3]);
	                    	m_hour = String.format("%02d", timeData[4]);
	                    	m_minute = String.format("%02d", timeData[5]);
	                    	m_second = String.format("%02d", timeData[6]);
	        
							String m_date1 = "" + timeData[0] + timeData[1]
									+ "-" + m_month + "-" + m_day;
							String m_time1 = "" + m_hour + ":" + m_minute + ":"
									+ m_second;
					
					   	    m_month=String.format("%02d", timeData[9]);
                    	    m_day = String.format("%02d", timeData[10]);
                    	    m_hour = String.format("%02d", timeData[11]);
                    	    m_minute = String.format("%02d", timeData[12]);
                    	    m_second = String.format("%02d", timeData[13]);
         		                    	
							String m_date2 = "" + timeData[7] + timeData[8]
									+ "-" + m_month + "-" + m_day;
							String m_time2 = "" + m_hour + ":" + m_minute + ":"
									+ m_second;
							String m_dateTime1 = "datetime('" + m_date1 + " "
									+ m_time1 + "')";
							String m_dateTime2 = "datetime('" + m_date2 + " "
									+ m_time2 + "')";
		
							StringBuffer tmp = new StringBuffer("select count (*) from Attendance where datetime(IO_DateTime)>=");
							tmp.append(m_dateTime1);
							tmp.append(" and datetime(IO_DateTime)<=");
							tmp.append(m_dateTime2);
							
							DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
							// 打开数据库
							dbAdapter.open();
							long attendanceLen = dbAdapter.getCount(tmp.toString());

							tmp = null;
							if (attendanceLen > 0) {
								long personLen = dbAdapter.getCount("select count (*) from Person");
								Person[] m_Person = new Person[(int) (personLen)];
								m_Person = dbAdapter.queryPersonAllData();
								data_len = 76;
								byte[] sendBuf = new byte[data_len + head_len];
								byte[] recordBuf = new byte[4 + head_len];
								headBuf[9] = 1; // msg_id
								headBuf[10] = (byte) (head_len + data_len); // buf_len

								if (personLen > 0) {
									headBuf[9] = 4;
									headBuf[10] = (byte) (head_len + 4);
									Arrays.fill(recordBuf, (byte) 0);
									System.arraycopy(headBuf, 0, recordBuf, 0, head_len);
									System.arraycopy(InteractionService.intToBytes((int) personLen + (int) attendanceLen), 0, recordBuf, 0 + head_len, 4);
										try {
											SendTcpData(recordBuf);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
								
									for (int i = 0; i < personLen; i++) {
										// 数据长度 16+30+30=76;
										Arrays.fill(sendBuf, (byte) 0);
										headBuf[9] = 1; // msg_id
										headBuf[10] = (byte) (head_len + data_len); // buf_len
										System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].id), 0, sendBuf, 0 + head_len, 4);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].autoID), 0, sendBuf, 4 + head_len, 4);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].Gender), 0, sendBuf, 8 + head_len, 4);
										System.arraycopy(InteractionService.intToBytes(m_Person[i].Admin), 0, sendBuf, 12 + head_len, 4);
										System.arraycopy(m_Person[i].name.getBytes(), 0, sendBuf, 16 + head_len, m_Person[i].name.getBytes().length);
										System.arraycopy(m_Person[i].Department.getBytes(), 0, sendBuf, 46 + head_len, m_Person[i].Department.getBytes().length);
									try {
										SendTcpData(sendBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									}						
								}
								Attendance[] m_Attendance = new Attendance[(int) (attendanceLen)];
								StringBuffer tmp1 = new StringBuffer(
										"select * from Attendance where datetime(IO_DateTime)>=");
								tmp1.append(m_dateTime1);
								tmp1.append(" and datetime(IO_DateTime)<=");
								tmp1.append(m_dateTime2);

								m_Attendance = dbAdapter.getAttendanceQuery(tmp1.toString());

								tmp1 = null;

								data_len = 72 + 30;

								byte[] sendAttendanceBuf = new byte[data_len + head_len];
								byte[] recordAttendanceBuf = new byte[4 + head_len];
								Arrays.fill(sendAttendanceBuf, (byte) 0);
								headBuf[8]=1;
								headBuf[10] = (byte) (head_len + 4); // buf_len
								headBuf[9] = 5;
								Arrays.fill(recordAttendanceBuf, (byte) 0);
								System.arraycopy(headBuf, 0,
										recordAttendanceBuf, 0, head_len);
								System.arraycopy(
										InteractionService.intToBytes((int) attendanceLen), 0,
										recordAttendanceBuf, 0 + head_len, 4);

								headBuf[10] = (byte) (head_len + data_len); // buf_len
								int k = 0;

								for (int i = 0; i < attendanceLen; i++) {
									// 数据长度 32+20+20=72; 72+30 = 102;
									Arrays.fill(sendAttendanceBuf, (byte) 0);
									headBuf[9] = 2; // msg_id

									System.arraycopy(headBuf, 0,sendAttendanceBuf, 0, head_len);
									System.arraycopy(InteractionService.intToBytes(m_Attendance[i].ID), 0, sendAttendanceBuf, 0 + head_len, 4);
									System.arraycopy(InteractionService.intToBytes(m_Attendance[i].autoID), 0, sendAttendanceBuf, 4 + head_len,4);
									System.arraycopy(InteractionService.intToBytes(m_Attendance[i].year),0, sendAttendanceBuf, 8 + head_len,4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].month),
											0, sendAttendanceBuf,
											12 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].day), 0,
											sendAttendanceBuf, 16 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].hour),
											0, sendAttendanceBuf,
											20 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].minute),
											0, sendAttendanceBuf,
											24 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].second),
											0, sendAttendanceBuf,
											28 + head_len, 4);
									System.arraycopy(
											m_Attendance[i].date.getBytes(),
											0,
											sendAttendanceBuf,
											32 + head_len,
											m_Attendance[i].date.getBytes().length);
									System.arraycopy(
											m_Attendance[i].time.getBytes(),
											0,
											sendAttendanceBuf,
											52 + head_len,
											m_Attendance[i].time.getBytes().length);
									if (m_Attendance[i].NAME != null)
									{
										System.arraycopy(m_Attendance[i].NAME.getBytes(),0,sendAttendanceBuf,72 + head_len,m_Attendance[i].NAME.getBytes().length);
									}
									try {
										SendTcpData(sendAttendanceBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
									// 传送数据完毕
									headBuf[8] = 127;
									headBuf[10] = (byte) head_len;
									try {
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								
							} else {
									headBuf[8] = 126; // type
									headBuf[10] = (byte) head_len;
									try {
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							
							}
							dbAdapter.close();
							dbAdapter = null;
						}
							break;
						case 14: { // 插播消息
							int len = buffer[10] * 127 + buffer[11] - 12;
							
							byte tmp[] = new byte[4];
							System.arraycopy(buffer, 16, tmp, 0, 4);
							int dataLen = InteractionService.BytesToInt(tmp);
							System.arraycopy(buffer, 20, tmp, 0, 4);
							int colorR = InteractionService.BytesToInt(tmp);
							System.arraycopy(buffer, 24, tmp, 0, 4);
							int colorG = InteractionService.BytesToInt(tmp);
							System.arraycopy(buffer, 28, tmp, 0, 4);
							int colorB = InteractionService.BytesToInt(tmp);
							int m_appendTag = buffer[12];
							int timeType = buffer[13];
							int time = buffer[14];
							int timeCount = 0;
							switch (timeType) 
							{
							case 1: // 天
								timeCount = 24 * 60 * 60 * time;
								break;
							case 2: // 小时
								timeCount = 60 * 60 * time;
								break;
							case 3: // 秒
								timeCount = 60 * time;
								break;
							default:
								break;
							}

							byte[] tmpName = new byte[dataLen];
							System.arraycopy(buffer, 32, tmpName, 0, dataLen);
							Bundle bundle = new Bundle();
							char[] tmpStr = new char[dataLen / 2 + 1];
							tmpStr = PooledConnectionHandler.getChars(tmpName);
							bundle.putString("MSG", new String(tmpStr));
							bundle.putInt("timeCount", timeCount);
							bundle.putInt("APPEND_TAG", m_appendTag);
							bundle.putInt("COLOR_R", colorR);
							bundle.putInt("COLOR_G", colorG);
							bundle.putInt("COLOR_B", colorB);
							bundle.putInt("WINDOW_MODEL", m_appendTag);
													
							//发送消息到ui线程去更新ui
							Message childMsg = InteractionService.testHandler.obtainMessage();
							childMsg.what= 14;
			   			    childMsg.setData(bundle);
			   			    InteractionService.testHandler.sendMessage(childMsg);
			   			    
							// 密码为空，任意pc主控端都可链接
							headBuf[8] = 107; // type
							headBuf[10] = (byte) head_len;
							try {
								SendTcpData(headBuf);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
							break;
						case 15: { // 跑马灯消息启动
							break;
						}
						case 16: { // 跑马灯消息停止							
							//发送消息到ui线程去更新ui
							Message childMsg = InteractionService.testHandler.obtainMessage();
							childMsg.what= 16;
			   			    InteractionService.testHandler.sendMessage(childMsg);
							
							// 密码为空，任意pc主控端都可链接
							headBuf[8] = 106; // type
							headBuf[10] = (byte) head_len;
							try {
								SendTcpData(headBuf);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
						case 17:// 更新用户部门信息
						{
							byte tmp[] = new byte[4];
							System.arraycopy(buffer, 12, tmp, 0, 4);
							int dataCount = InteractionService.BytesToInt(tmp);
							System.arraycopy(buffer, 16, tmp, 0, 4);
							int dataNum = InteractionService.BytesToInt(tmp);
							System.arraycopy(buffer, 20, tmp, 0, 4);
							int nameLen = InteractionService.BytesToInt(tmp);
							System.arraycopy(buffer, 24, tmp, 0, 4);
							int departmentLen = InteractionService.BytesToInt(tmp);
							byte[]  name = new byte[nameLen];
							byte [] department = new byte[departmentLen];
							System.arraycopy(buffer, 28, name, 0, nameLen);
							System.arraycopy(buffer, 58, department, 0, departmentLen);
							String  strName = new String(name);
							String  strDepartment = new String(department);			
							DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
							// 打开数据库
							dbAdapter.open();
							String sql ="update Person set Department='"+strDepartment+"' where name = '"+strName+"'";
	     	      			try {
	     	      				 dbAdapter.db.execSQL(sql);
	     	      				} catch (SQLException e) {
	     	      	                System.out.println("update failed");
	     	      	        }
	     	      			dbAdapter.close();
							dbAdapter = null;
							if(dataCount==dataNum+1)
							{
								headBuf[8] = 105; // type
								headBuf[10] = (byte) head_len;
								try {
									SendTcpData(headBuf);
									} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							name = null;
							department = null;
							strName = null;
							strDepartment = null;
							
							break;
						}
						case 18:
						{
							//单班次时间考勤设置
							byte[]  m_startTime1Byte = new byte[5];
                    		byte[]  m_endTime1Byte = new byte[5];      
                    		byte[]  m_overTimeByte = new byte[5];
                    		System.arraycopy(buffer, 12, m_startTime1Byte , 0, 5);
                    	    System.arraycopy(buffer, 18, m_endTime1Byte, 0, 5);
                    		System.arraycopy(buffer, 24, m_overTimeByte, 0, 5);
                    		String m_startTime = new String(m_startTime1Byte);
                    		String m_endTime = new String(m_endTime1Byte);
                    		String m_overTime = new String(m_overTimeByte);
                    		int m_beginHour3 = buffer[30];
                    		int m_beginMinute3 = buffer[31];
                    		int m_endHour3 = buffer[32];
                    		int m_endMinute3= buffer[33];
                    		int m_moreTime5 = buffer[34];
                    		int m_moreTime6 = buffer[35];
                    		int m_noLateTime3 = buffer[36];
                
                    		Editor editor;
                    		SharedPreferences sharedPreferences = InteractionService.g_context.getSharedPreferences("config",Context.MODE_PRIVATE);
                    		editor = sharedPreferences.edit();
                    		editor.putInt("ATTENDANCE_TIMER", 1);  //单班次考勤
                    		editor.putString("startTime1", m_startTime);
                    		editor.putString("endTime1", m_endTime);
                    		String End = new String("end"); //要查找的字符串
                    		Pattern p = Pattern.compile(End);  //模式
                    		Matcher m = p.matcher(m_overTime);
                    		if(m.find())
                    		{
                    			editor.putString("overTime", "0");
                    		}
                    		else
                    		{
                    			editor.putString("overTime", m_overTime);
                    		}
                    		
                    		SimpleDateFormat m_dateFormat = new SimpleDateFormat("HH:mm");
                    		Date date=null;
                    		try
                    		{
                    			date = m_dateFormat.parse(m_startTime);
                    			editor.putInt("startTimeHour3", date.getHours());
                    			editor.putInt("startTimeMinute3", date.getMinutes());
                    			date = m_dateFormat.parse(m_endTime);
                    			editor.putInt("endTimeHour3", date.getHours());
                    			editor.putInt("endTimeMinute3", date.getMinutes());                    			
                    		}
                    		catch (ParseException e)
                    		{
                    			System.out.println(e.getMessage());
                    		}
             
                    		editor.putInt("beginHour3", m_beginHour3);
                    		editor.putInt("beginMinute3", m_beginMinute3);
                    		editor.putInt("endHour3", m_endHour3);
                    		editor.putInt("endMinute3", m_endMinute3);
                    		editor.putInt("moreTime5", m_moreTime5);
                    		editor.putInt("moreTime6", m_moreTime6);
                    		editor.putInt("noLateTime3", m_noLateTime3);
                    		editor.commit();
                    		
                    		headBuf[8] = 104; // type
							headBuf[10] = (byte) head_len;
							try {
								SendTcpData(headBuf);
								} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
						case 19:
						{
							// 双班次考勤时间设置
							byte[]  m_startTime1Byte = new byte[5];
                    		byte[]  m_startTime2Byte = new byte[5];
                    		byte[]  m_endTime1Byte = new byte[5];
                    		byte[]  m_endTime2Byte = new byte[5];
                    		byte[]  m_overTimeByte = new byte[5];
                    		System.arraycopy(buffer, 12, m_startTime1Byte , 0, 5);
                    		System.arraycopy(buffer, 18, m_startTime2Byte, 0, 5);
                    		System.arraycopy(buffer, 24, m_endTime1Byte, 0, 5);
                    		System.arraycopy(buffer, 30, m_endTime2Byte, 0, 5);
                    		System.arraycopy(buffer, 36, m_overTimeByte, 0, 5);
                    		String m_startTime1 = new String(m_startTime1Byte);
                    		String m_startTime2 = new String(m_startTime2Byte);
                    		String m_endTime1 = new String(m_endTime1Byte);
                    		String m_endTime2 = new String(m_endTime2Byte);
                    		String m_overTime = new String(m_overTimeByte);
                    		int m_beginHour1 = buffer[36+6];
                    		int m_beginMinute1 = buffer[37+6];
                    		int m_beginHour2 = buffer[38+6];
                    		int m_beginMinute2 = buffer[39+6];
                    		int m_endHour1 = buffer[40+6];
                    		int m_endMinute1 = buffer[41+6];
                    		int m_endHour2 = buffer[42+6];
                    		int m_endMinute2 = buffer[43+6];
                    		int m_moreTime1 = buffer[44+6];
                    		int m_moreTime2 = buffer[45+6];
                    		int m_moreTime3 = buffer[46+6];
                    		int m_moreTime4 = buffer[47+6];
                    		int m_noLateTime1 = buffer[48+6];
                    		int m_noLateTime2 = buffer[49+6];
                    	
                    		Editor editor;
                    		SharedPreferences sharedPreferences = InteractionService.g_context.getSharedPreferences("config",Context.MODE_PRIVATE);
                    		editor = sharedPreferences.edit();
                    		editor.putInt("ATTENDANCE_TIMER", 2);  //双班次考勤
                    		editor.putString("startTime1", m_startTime1);
                    		editor.putString("startTime2", m_startTime2);
                    		editor.putString("endTime1", m_endTime1);
                    		editor.putString("endTime2", m_endTime2);
                    		
                    		String End = new String("end"); //要查找的字符串
                    		Pattern p = Pattern.compile(End);  //模式
                    		Matcher m = p.matcher(m_overTime);
                    		if(m.find())
                    		{
                    			editor.putString("overTime", "0");
                    		}
                    		else
                    		{
                    			editor.putString("overTime", m_overTime);
                    		}
                    		
                    		
                    		SimpleDateFormat m_dateFormat = new SimpleDateFormat("HH:mm");
                    		Date date=null;
                    		try
                    		{
                    			date = m_dateFormat.parse(m_startTime1);
                    			editor.putInt("startTimeHour1", date.getHours());
                    			editor.putInt("startTimeMinute1", date.getMinutes());
                    			date = m_dateFormat.parse(m_endTime1);
                    			editor.putInt("endTimeHour1", date.getHours());
                    			editor.putInt("endTimeMinute1", date.getMinutes());
                    			
                    			date = m_dateFormat.parse(m_startTime2);
                    			editor.putInt("startTimeHour2", date.getHours());
                    			editor.putInt("startTimeMinute2", date.getMinutes());
                    			date = m_dateFormat.parse(m_endTime2);
                    			editor.putInt("endTimeHour2", date.getHours());
                    			editor.putInt("endTimeMinute2", date.getMinutes());
                    		}
                    		catch (ParseException e)
                    		{
                    			System.out.println(e.getMessage());
                    		}
							                    
                    		editor.putInt("beginHour1", m_beginHour1);
                    		editor.putInt("beginMinute1", m_beginMinute1);
                    		editor.putInt("beginHour2",m_beginHour2);
                    		editor.putInt("beginMinute2",m_beginMinute2);
                    		editor.putInt("endHour1", m_endHour1);
                    		editor.putInt("endHour2",m_endHour2);
                    		editor.putInt("endMinute1", m_endMinute1);
                    		editor.putInt("endMinute2", m_endMinute2);
                    		editor.putInt("moreTime1", m_moreTime1);
                    		editor.putInt("moreTime2", m_moreTime2);
                    		editor.putInt("moreTime3", m_moreTime3);
                    		editor.putInt("moreTime4", m_moreTime4);
                    		editor.putInt("noLateTime1", m_noLateTime1);
                    		editor.putInt("noLateTime2", m_noLateTime2);
                    		editor.commit();
                    		
                    		headBuf[8] = 104; // type
							headBuf[10] = (byte) head_len;
							try {
								SendTcpData(headBuf);
								} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					
						case 20:
						{
							Bundle m_bundle = new Bundle();
							ShutOpenDeviceClass[] cTime = new ShutOpenDeviceClass[7];
							
					
							for(int i=0; i<7; i++)  //共7天的开关机时间
							{
								cTime[i]=new ShutOpenDeviceClass();
								System.arraycopy(buffer, 12+i*6, cTime[i].openDeviceTime , 0, 5);
								System.arraycopy(buffer, 54+i*6, cTime[i].closeDeviceTime, 0, 5);
								cTime[i].m_switch = buffer[96+i];
							}				
							
							JSONObject jsonObject = new JSONObject();     
							try {  //默认值， 开机00：03  关机23:58
								jsonObject.put("monday_open_time", new String(cTime[0].openDeviceTime));
							    jsonObject.put("monday_close_time", new String(cTime[0].closeDeviceTime));  
							    if(cTime[0].m_switch==0)
								   	jsonObject.put("monday_switch", "false");  
								else
								   	jsonObject.put("monday_switch", "true");
								    
								jsonObject.put("tuesday_open_time",new String(cTime[1].openDeviceTime));
								jsonObject.put("tuesday_close_time", new String(cTime[1].closeDeviceTime));  
								if(cTime[1].m_switch==0)
								  	jsonObject.put("tuesday_switch", "false");  
								else
								   	jsonObject.put("tuesday_switch", "true");  
					
								    jsonObject.put("wednesday_open_time", new String(cTime[2].openDeviceTime));
								    jsonObject.put("wednesday_close_time", new String(cTime[2].closeDeviceTime)); 
								    if(cTime[2].m_switch==0)
								    	jsonObject.put("wednesday_switch", "false");  
								    else
								    	jsonObject.put("wednesday_switch", "true");  
					
								    jsonObject.put("thursday_open_time", new String(cTime[3].openDeviceTime)); 
								    jsonObject.put("thursday_close_time", new String(cTime[3].closeDeviceTime));
								    if(cTime[3].m_switch==0)
								    	jsonObject.put("thursday_switch", "false");  
								    else
								    	jsonObject.put("thursday_switch", "true");  
					
								    jsonObject.put("friday_open_time", new String(cTime[4].openDeviceTime)); 
								    jsonObject.put("friday_close_time", new String(cTime[4].closeDeviceTime)); 							
								    if(cTime[4].m_switch==0)
								    	jsonObject.put("friday_switch", "false");  
								    else
								    	jsonObject.put("friday_switch", "true");  
					
								    jsonObject.put("saturday_open_time", new String(cTime[5].openDeviceTime)); 
								    jsonObject.put("saturday_close_time", new String(cTime[5].closeDeviceTime)); 
								    if(cTime[5].m_switch==0)
								    	jsonObject.put("saturday_switch", "false");  
								    else
								    	jsonObject.put("saturday_switch", "true");  
					 
								    jsonObject.put("sunday_open_time", new String(cTime[6].openDeviceTime));
								    jsonObject.put("sunday_close_time", new String(cTime[6].closeDeviceTime)); 
								    if(cTime[6].m_switch==0)
								    	jsonObject.put("sunday_switch", "false");  
								    else
								    	jsonObject.put("sunday_switch", "true");  
					 
								} catch (JSONException e) {
									e.printStackTrace();
								} 
								System.out.println(jsonObject.toString());
								m_bundle.putString("setting", jsonObject.toString());
								SaveConnectionClass m_SaveConnectionClass = new SaveConnectionClass();
								m_SaveConnectionClass.Set(connection);
								m_bundle.putSerializable("connection", m_SaveConnectionClass);
								
							Message childMsg = InteractionService.testHandler.obtainMessage();
							childMsg.what= 17;
							childMsg.setData(m_bundle); 
			   			    InteractionService.testHandler.sendMessage(childMsg);
							break;
						}
						/*
						case 21:
						{
                    		Editor editor;
                    		SharedPreferences sharedPreferences = InteractionService.g_context.getSharedPreferences("config",Context.MODE_WORLD_READABLE);
                    		editor = sharedPreferences.edit();
                    		editor.putInt("ATTENDANCE_TIMER", buffer[12]);  //双班次考勤             
                       		editor.commit();
                       		                     		
                    		headBuf[8] = 104; // type
							headBuf[10] = (byte) head_len;
							try {
								SendTcpData(headBuf);
								} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
								
							break;
						}
						*/
						case 22:
						{
							// 通知pc端，开始计算排名
						
							headBuf[8] = 100; // type
							headBuf[10] = (byte) head_len;
							try {
								SendTcpData(headBuf);
								} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
								

						    int dateLen = 76;
						    byte[]  sendBuf = new byte[dateLen+head_len];
						    /*
						    String preSerchAttendanceSortTime;									
							Calendar calendar = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String dateStr = sdf.format(calendar.getTime());  //此次查询时间
							Editor editor;
                    		SharedPreferences sharedPreferences = InteractionService.g_context.getSharedPreferences("config",Context.MODE_WORLD_READABLE);
                    		preSerchAttendanceSortTime = sharedPreferences.getString("serchAttendanceSortTime", "NULL");
                    		
                    		editor = sharedPreferences.edit();
                    		editor.putString("serchAttendanceSortTime", dateStr);
                    		editor.commit();  //保存此次查询考勤排名时间
                    		if(preSerchAttendanceSortTime.equals("NULL")!=true)  //上一次查询考勤排名时间
                    		{
                    			try {
                    				Calendar cal = Calendar.getInstance();
                    				cal.setTime( sdf.parse(preSerchAttendanceSortTime));
                    		
                    				if(cal.get(Calendar.WEEK_OF_YEAR) != calendar.get(Calendar.WEEK_OF_YEAR))
                    				{
                    					
                    				}
                    				
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                    		}
                    		*/
							try {
								SortAttendance();
								} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
								
                    	
	        				int listLen = m_time7daysSortList.size();
	        				if(listLen>0)
	        				{   //上周排行
					 	        for(int k=0; k<listLen ; k++)
						 	    { 	try {
						 	    	Arrays.fill(sendBuf, (byte) 0);
						 	       	headBuf[9] = 6; // type
						 	     	headBuf[10] = (byte)( head_len+dateLen);
						 	      
						 	       	System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
						 	       	System.arraycopy(InteractionService.intToBytes(0), 0, sendBuf, 0+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(listLen), 0, sendBuf, 4+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(k+1), 0, sendBuf, 8+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes((int)m_time7daysSortList.get(k).timedaysLen), 0, sendBuf, 12+head_len, 4);     
		        					System.arraycopy(m_time7daysSortList.get(k).name.getBytes(), 0, sendBuf,16+head_len, m_time7daysSortList.get(k).name.getBytes().length);     	
		        					System.arraycopy(m_time7daysSortList.get(k).department.getBytes(), 0, sendBuf, 46+head_len, m_time7daysSortList.get(k).department.getBytes().length);  
						 	       	SendTcpData(sendBuf);
									} catch (IOException e) {
									// TODO Auto-generated catch block
										e.printStackTrace();
									}
						 	    }
				 	        
					 	        listLen = m_time30daysSortList.size();
					 	        for(int k=0; k<listLen ; k++)
						 	    {   //上月排行
					 	          	try {
						 	    	Arrays.fill(sendBuf, (byte) 0);
						 	       	headBuf[9] = 6; // type
						 	     	headBuf[10] = (byte)( head_len+dateLen);
						 	    
						 	       	System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
						 	       	System.arraycopy(InteractionService.intToBytes(1), 0, sendBuf, 0+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(listLen), 0, sendBuf, 4+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(k+1), 0, sendBuf, 8+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes((int)m_time30daysSortList.get(k).timedaysLen), 0, sendBuf, 12+head_len, 4);     
		        					System.arraycopy(m_time30daysSortList.get(k).name.getBytes(), 0, sendBuf,16+head_len, m_time30daysSortList.get(k).name.getBytes().length);     	
		        					System.arraycopy(m_time30daysSortList.get(k).department.getBytes(), 0, sendBuf, 46+head_len, m_time30daysSortList.get(k).department.getBytes().length);  
						 	       	SendTcpData(sendBuf);
									} catch (IOException e) {
									// TODO Auto-generated catch block
										e.printStackTrace();
									}
						 	    }
					 	        
					 	        listLen = m_time90daysSortList.size();
					 	        for(int k=0; k<listLen ; k++)
						 	    {   //上季度排行
					 	         	try {
						 	    	Arrays.fill(sendBuf, (byte) 0);
						 	       	headBuf[9] = 6; // type
						 	     	headBuf[10] = (byte)( head_len+dateLen);
						 	 
						 	       	System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
						 	       	System.arraycopy(InteractionService.intToBytes(2), 0, sendBuf, 0+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(listLen), 0, sendBuf, 4+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(k+1), 0, sendBuf, 8+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes((int)m_time90daysSortList.get(k).timedaysLen), 0, sendBuf, 12+head_len, 4);     
		        					System.arraycopy(m_time90daysSortList.get(k).name.getBytes(), 0, sendBuf,16+head_len, m_time90daysSortList.get(k).name.getBytes().length);     	
		        					System.arraycopy(m_time90daysSortList.get(k).department.getBytes(), 0, sendBuf, 46+head_len, m_time90daysSortList.get(k).department.getBytes().length);  
						 	       	SendTcpData(sendBuf);
									} catch (IOException e) {
									// TODO Auto-generated catch block
										e.printStackTrace();
									}
						 	    }
					 	        
					 	        listLen = m_time365daysSortList.size();
					 	        for(int k=0; k<listLen ; k++)
						 	    {   //全年排行
					 	       	try {
						 	    	Arrays.fill(sendBuf, (byte) 0);
						 	       	headBuf[9] = 6; // type
						 	     	headBuf[10] = (byte)( head_len+dateLen);
						 	   
						 	       	System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
						 	       	System.arraycopy(InteractionService.intToBytes(3), 0, sendBuf, 0+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(listLen), 0, sendBuf, 4+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(k+1), 0, sendBuf, 8+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes((int)m_time365daysSortList.get(k).timedaysLen), 0, sendBuf, 12+head_len, 4);     
		        					System.arraycopy(m_time365daysSortList.get(k).name.getBytes(), 0, sendBuf,16+head_len, m_time365daysSortList.get(k).name.getBytes().length);     	
		        					System.arraycopy(m_time365daysSortList.get(k).department.getBytes(), 0, sendBuf, 46+head_len, m_time365daysSortList.get(k).department.getBytes().length);  
						 	       	SendTcpData(sendBuf);
									} catch (IOException e) {
									// TODO Auto-generated catch block
										e.printStackTrace();
									}
						 	    }
				 	        
					 	        listLen = m_timeAlldaysSortList.size();
					 	        for(int k=0; k<listLen ; k++)
						 	    {   //所有排行
					 	        	try {
						 	    	Arrays.fill(sendBuf, (byte) 0);
						 	       	headBuf[9] = 6; // type
						 	     	headBuf[10] = (byte)( head_len+dateLen);
						 	      
							 	       	System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
							 	       	System.arraycopy(InteractionService.intToBytes(4), 0, sendBuf, 0+head_len, 4);
			        					System.arraycopy(InteractionService.intToBytes(listLen), 0, sendBuf, 4+head_len, 4);
			        					System.arraycopy(InteractionService.intToBytes(k+1), 0, sendBuf, 8+head_len, 4);
			        					System.arraycopy(InteractionService.intToBytes((int)m_timeAlldaysSortList.get(k).timedaysLen), 0, sendBuf, 12+head_len, 4);     
			        					System.arraycopy(m_timeAlldaysSortList.get(k).name.getBytes(), 0, sendBuf,16+head_len, m_timeAlldaysSortList.get(k).name.getBytes().length);     	
			        					System.arraycopy(m_timeAlldaysSortList.get(k).department.getBytes(), 0, sendBuf, 46+head_len, m_timeAlldaysSortList.get(k).department.getBytes().length);  
							 	       	SendTcpData(sendBuf);
										} catch (IOException e) {
										// TODO Auto-generated catch block
											e.printStackTrace();
										}
						 	    	}	
	        				}
	        				else
	        				{
	        				   	try {
	        					Arrays.fill(sendBuf, (byte) 0);
					 	       	headBuf[9] = 6; // type
					 	     	headBuf[10] = (byte)( head_len+dateLen);
						 	       	System.arraycopy(headBuf, 0, sendBuf, 0, head_len);
						 	       	System.arraycopy(InteractionService.intToBytes(0), 0, sendBuf, 0+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(0), 0, sendBuf, 4+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(0), 0, sendBuf, 8+head_len, 4);
		        					System.arraycopy(InteractionService.intToBytes(0), 0, sendBuf, 12+head_len, 4);     
		        					//System.arraycopy(m_timeAlldaysSortList.get(k).name.getBytes(), 0, sendBuf,16+head_len, m_timeAlldaysSortList.get(k).name.getBytes().length);     	
		        					//System.arraycopy(m_timeAlldaysSortList.get(k).department.getBytes(), 0, sendBuf, 46+head_len, m_timeAlldaysSortList.get(k).department.getBytes().length);  
						 	       	SendTcpData(sendBuf);
									} catch (IOException e) {
									// TODO Auto-generated catch blockazi
										e.printStackTrace();
									}
	        				}
							break;
						}
						case 23:
						{   // 收到设置person用户信息， 保存到数据库中，更新用户数据
							// 修改用户信息
							byte[]  tmpID = new byte[4];
							byte[]	tmpAdmin = new byte[4];
							//byte[]  tmpNum = new byte[4];
							byte[]  tmpBirthdaySwitch = new byte[4];
							byte[]  tmpAttendanceTimes = new byte[4];
							byte[]	tmpMaleLen = new byte[4];
							byte[]	tmpNameLen = new byte[4];
							byte[]	tmpBirthdayLen = new byte[4];
							byte[]	tmpDepartmentLen = new byte[4];
							byte[]  tmpTelLen = new byte[4];
							byte[]  tmpMsglen = new byte[4];
							byte[]  tmpNumLen = new byte[4];
							 //head 头文件长12， 所以，数据结算从13开始
						    System.arraycopy(buffer,12+8, tmpID , 0, 4);  //id号
						    System.arraycopy(buffer,12+12, tmpAdmin , 0, 4);  //权限
						    System.arraycopy(buffer, 16+12, tmpNumLen , 0, 4);   //工号长度
						    System.arraycopy(buffer,20+12, tmpBirthdaySwitch , 0, 4);
						    System.arraycopy(buffer, 24+12, tmpAttendanceTimes , 0, 4);
						    System.arraycopy(buffer, 28+12, tmpMaleLen , 0, 4);
						    System.arraycopy(buffer, 32+12, tmpNameLen , 0, 4);
						    System.arraycopy(buffer, 36+12, tmpBirthdayLen , 0, 4);
						    System.arraycopy(buffer, 40+12, tmpDepartmentLen , 0, 4);
						    System.arraycopy(buffer, 44+12, tmpTelLen , 0, 4);
						    System.arraycopy(buffer, 48+12, tmpMsglen , 0, 4);
						    
						    int MaleLen = MsgObj.Byte2Int(tmpMaleLen);
						    int NameLen = MsgObj.Byte2Int(tmpNameLen);
						    int BirthdayLen = MsgObj.Byte2Int(tmpBirthdayLen);
						    int DepartmentLen = MsgObj.Byte2Int(tmpDepartmentLen);
						    int TelLen =  MsgObj.Byte2Int(tmpTelLen);
							int MsgLen = MsgObj.Byte2Int(tmpMsglen);
							int NumLen = MsgObj.Byte2Int(tmpNumLen);
							byte[]  tmpGender = new byte[MaleLen];
							byte[]  tmpName = new byte[NameLen];
							byte[]  tmpBirthday = new byte[BirthdayLen];
							byte[]  tmpDepartment = new byte[DepartmentLen];
							byte[]  tmpTel= new byte[TelLen];
							byte[]  tmpMsg = new byte[MsgLen];
							byte[]  tmpNum = new byte[NumLen];
						    System.arraycopy(buffer, 52+12, tmpGender , 0, MaleLen);
						    System.arraycopy(buffer, 56+12, tmpName , 0, NameLen);
						    System.arraycopy(buffer, 88+12, tmpBirthday , 0, BirthdayLen);
						    System.arraycopy(buffer, 104+12, tmpDepartment , 0, DepartmentLen);
						    System.arraycopy(buffer, 136+12, tmpTel , 0, TelLen);
						    System.arraycopy(buffer, 152+12, tmpNum , 0, NumLen);
						    System.arraycopy(buffer, 152+12+16, tmpMsg , 0, MsgLen);
						 
                    		PersonEx m_personEx = new PersonEx();
                    		m_personEx.id = MsgObj.Byte2Int(tmpID);
                    		m_personEx.Num  = new String(tmpNum);
                    		m_personEx.birthdaySwitch = MsgObj.Byte2Int(tmpBirthdaySwitch);
                    		m_personEx.attendance_times = MsgObj.Byte2Int(tmpAttendanceTimes);
                    		m_personEx.male = new String(tmpGender);
                    		m_personEx.name = new String(tmpName);
                    		m_personEx.Birthday = new String(tmpBirthday);
                    		m_personEx.Department = new String(tmpDepartment);
                    		m_personEx.tel = new String(tmpTel);
                    		m_personEx.msg = new String(tmpMsg);
                    	//	Log.e(TAG, "   str ; "+m_personEx.toString());
							// 修改用户信息
                    		DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
	        			    dbAdapter.open();
	        			    int ret = (int) dbAdapter.updatePersonExData(m_personEx.id, m_personEx);
	        				dbAdapter.close();
	        				dbAdapter =null;
	        				
	        				headBuf[10] = (byte) head_len;    
	        				if(ret >0)
	        				{
		        				//修改信息成功
	        					headBuf[8] = 114;  //type
	        				}
	        				else
	        				{
	        	  				//没有信息修改
	        					headBuf[8] = 113;  //type
	        				}
	        				try {
								SendTcpData(headBuf);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
						case 24:   //新的人名信息查询
        				{ 
							DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
							// 打开数据库
							dbAdapter.open();
							long len = dbAdapter.getCount("select count (*) from Person");
							PersonEx[] m_PersonEx = new PersonEx[(int) (len)];
							m_PersonEx = dbAdapter.queryPersonExAllData();
							dbAdapter.close();
							dbAdapter = null;
							data_len = 368;
							byte[] sendBuf = new byte[data_len + head_len];
							byte[] recordBuf = new byte[4 + head_len];
							headBuf[9] = 1; // msg_id
							headBuf[10] = (byte) (head_len + data_len); // buf_len
							if (len > 0) {
								headBuf[8] =1;
								headBuf[9] = 4;
								headBuf[10] = (byte) (head_len + 4);
								Arrays.fill(recordBuf, (byte) 0);
								System.arraycopy(headBuf, 0, recordBuf, 0,
										head_len);
								System.arraycopy(InteractionService.intToBytes((int) len), 0,
										recordBuf, 0 + head_len, 4);
	
								try {
									SendTcpData(recordBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								for (int i = 0; i < len; i++) 
								{
									Arrays.fill(sendBuf, (byte) 0);
									headBuf[8] = 10; //  数据类型 10: 大包数据
									headBuf[9] = 1; // msg_id
									headBuf[10] = (byte) ((head_len + data_len)/127); // buf_len
									headBuf[11] = (byte) ((head_len + data_len)%127); // buf_len
									System.arraycopy(headBuf, 0, sendBuf, 0,head_len);
									System.arraycopy(InteractionService.intToBytes(0), 0,sendBuf, 0 + head_len, 4);  //record_id
									System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].autoID), 0,sendBuf, 4 + head_len, 4);
									System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].id), 0,sendBuf, 8 + head_len, 4);	
									System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].Admin), 0,sendBuf, 12 + head_len, 4);
									System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].birthdaySwitch),0,sendBuf, 20 + head_len, 4);
									System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].attendance_times), 0,sendBuf, 24 + head_len, 4);
									if(m_PersonEx[i].male!=null)
										System.arraycopy(m_PersonEx[i].male.getBytes(), 0,sendBuf, 52 + head_len, m_PersonEx[i].male.getBytes().length);
									if(m_PersonEx[i].name!=null)
										System.arraycopy(m_PersonEx[i].name.getBytes(), 0,sendBuf, 56 + head_len, m_PersonEx[i].name.getBytes().length);
									if(m_PersonEx[i].Birthday!=null)
										System.arraycopy(m_PersonEx[i].Birthday.getBytes(), 0,sendBuf, 88 + head_len, m_PersonEx[i].Birthday.getBytes().length);
									if(m_PersonEx[i].Department!=null)
										System.arraycopy(m_PersonEx[i].Department.getBytes(), 0,sendBuf, 104 + head_len, m_PersonEx[i].Department.getBytes().length);
									if(m_PersonEx[i].tel!=null)
										System.arraycopy(m_PersonEx[i].tel.getBytes(), 0,sendBuf, 136+ head_len, m_PersonEx[i].tel.getBytes().length);
									if(m_PersonEx[i].Num!=null)
										System.arraycopy(m_PersonEx[i].Num.getBytes(), 0,sendBuf, 152 + head_len, m_PersonEx[i].Num.getBytes().length);
									if(m_PersonEx[i].msg!=null)
										System.arraycopy(m_PersonEx[i].msg.getBytes(), 0,sendBuf, 168 + head_len, m_PersonEx[i].msg.getBytes().length);
								//	System.out.println(m_PersonEx[i].toString());
												
									try {
										SendTcpData(sendBuf);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
	
								try {
									headBuf[8] = 109;
									headBuf[9] = 109;
									headBuf[10] = (byte) head_len;
									SendTcpData(headBuf);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								try {
									// 数据为空
									headBuf[8] = 126; // type
									headBuf[10] = (byte) head_len;
									SendTcpData(headBuf);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
					}
        				break;
						case 25: {
							// 新加消息，1 ID 对多名字， 传递所有数据
							byte[] timeData = new byte[14];
							System.arraycopy(buffer, 12, timeData, 0, 14);
							String m_month, m_day, m_hour, m_minute, m_second;
							m_month=String.format("%02d", timeData[2]);
	                    	m_day = String.format("%02d", timeData[3]);
	                    	m_hour = String.format("%02d", timeData[4]);
	                    	m_minute = String.format("%02d", timeData[5]);
	                    	m_second = String.format("%02d", timeData[6]);
	        
							String m_date1 = "" + timeData[0] + timeData[1]
									+ "-" + m_month + "-" + m_day;
							String m_time1 = "" + m_hour + ":" + m_minute + ":"
									+ m_second;
					
					   	    m_month=String.format("%02d", timeData[9]);
                    	    m_day = String.format("%02d", timeData[10]);
                    	    m_hour = String.format("%02d", timeData[11]);
                    	    m_minute = String.format("%02d", timeData[12]);
                    	    m_second = String.format("%02d", timeData[13]);
         		                    	
							String m_date2 = "" + timeData[7] + timeData[8]+ "-" + m_month + "-" + m_day;
							String m_time2 = "" + m_hour + ":" + m_minute + ":"+ m_second;
							String m_dateTime1 = "datetime('" + m_date1 + " "+ m_time1 + "')";
							String m_dateTime2 = "datetime('" + m_date2 + " "+ m_time2 + "')";
		
							StringBuffer tmp = new StringBuffer("select count (*) from Attendance where datetime(IO_DateTime)>=");
							tmp.append(m_dateTime1);
							tmp.append(" and datetime(IO_DateTime)<=");
							tmp.append(m_dateTime2);
						
							DBAdapter dbAdapter = new DBAdapter(InteractionService.g_context);
							// 打开数据库
							dbAdapter.open();
							long attendanceLen = dbAdapter.getCount(tmp.toString());

							tmp = null;
							if (attendanceLen > 0) {
								long personLen = dbAdapter.getCount("select count (*) from Person");
								PersonEx[] m_PersonEx = new PersonEx[(int) (personLen)];
								m_PersonEx = dbAdapter.queryPersonExAllData();
								data_len = 368;
								byte[] sendBuf = new byte[data_len + head_len];
								byte[] recordBuf = new byte[4 + head_len];
								headBuf[9] = 1; // msg_id
								headBuf[10] = (byte) (head_len + data_len); // buf_len
								
								
								

								if (personLen > 0) {
									headBuf[9] = 4;
									headBuf[10] = (byte) (head_len + 4);
									Arrays.fill(recordBuf, (byte) 0);
									System.arraycopy(headBuf, 0, recordBuf, 0, head_len);
									System.arraycopy(InteractionService.intToBytes((int) personLen + (int) attendanceLen), 0, recordBuf, 0 + head_len, 4);
									try {
										SendTcpData(recordBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
										
									for (int i = 0; i < personLen; i++) {
										Arrays.fill(sendBuf, (byte) 0);
										headBuf[8] = 10; //  数据类型 10: 大包数据
										headBuf[9] = 1; // msg_id
										headBuf[10] = (byte) ((head_len + data_len)/127); // buf_len
										headBuf[11] = (byte) ((head_len + data_len)%127); // buf_len
												System.arraycopy(headBuf, 0, sendBuf, 0,head_len);
												System.arraycopy(InteractionService.intToBytes(0), 0,sendBuf, 0 + head_len, 4);  //record_id
												System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].autoID), 0,sendBuf, 4 + head_len, 4);
												System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].id), 0,sendBuf, 8 + head_len, 4);	
												System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].Admin), 0,sendBuf, 12 + head_len, 4);
												System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].birthdaySwitch),0,sendBuf, 20 + head_len, 4);
												System.arraycopy(InteractionService.intToBytes(m_PersonEx[i].attendance_times), 0,sendBuf, 24 + head_len, 4);												
												
												if(m_PersonEx[i].male!=null)
													System.arraycopy(m_PersonEx[i].male.getBytes(), 0,sendBuf, 52 + head_len, m_PersonEx[i].male.getBytes().length);
												if(m_PersonEx[i].name!=null)
													System.arraycopy(m_PersonEx[i].name.getBytes(), 0,sendBuf, 56 + head_len, m_PersonEx[i].name.getBytes().length);
												if(m_PersonEx[i].Birthday!=null)
													System.arraycopy(m_PersonEx[i].Birthday.getBytes(), 0,sendBuf, 88 + head_len, m_PersonEx[i].Birthday.getBytes().length);
												if(m_PersonEx[i].Department!=null)
													System.arraycopy(m_PersonEx[i].Department.getBytes(), 0,sendBuf, 104 + head_len, m_PersonEx[i].Department.getBytes().length);
												if(m_PersonEx[i].tel!=null)
													System.arraycopy(m_PersonEx[i].tel.getBytes(), 0,sendBuf, 136+ head_len, m_PersonEx[i].tel.getBytes().length);
												if(m_PersonEx[i].Num!=null)
													System.arraycopy(m_PersonEx[i].Num.getBytes(), 0,sendBuf, 152 + head_len, m_PersonEx[i].Num.getBytes().length);
												if(m_PersonEx[i].msg!=null)
													System.arraycopy(m_PersonEx[i].msg.getBytes(), 0,sendBuf, 168 + head_len, m_PersonEx[i].msg.getBytes().length);
												try {
													SendTcpData(sendBuf); 
												} catch (IOException e) {
													e.printStackTrace();
												}
												
											}
								}
								
								Attendance[] m_Attendance = new Attendance[(int) (attendanceLen)];
								StringBuffer tmp1 = new StringBuffer("select * from Attendance where datetime(IO_DateTime)>=");
								tmp1.append(m_dateTime1);
								tmp1.append(" and datetime(IO_DateTime)<=");
								tmp1.append(m_dateTime2);
								m_Attendance = dbAdapter.getAttendanceQuery(tmp1.toString());

								data_len = 72 + 30;

								byte[] sendAttendanceBuf = new byte[data_len + head_len];
								byte[] recordAttendanceBuf = new byte[4 + head_len];
								Arrays.fill(sendAttendanceBuf, (byte) 0);
								headBuf[8]=1;
								headBuf[10] = (byte) (head_len + 4); // buf_len
								headBuf[9] = 5;
								Arrays.fill(recordAttendanceBuf, (byte) 0);
								System.arraycopy(headBuf, 0,
										recordAttendanceBuf, 0, head_len);
								System.arraycopy(
										InteractionService.intToBytes((int) attendanceLen), 0,
										recordAttendanceBuf, 0 + head_len, 4);
								headBuf[10] = (byte) (head_len + data_len); // buf_len
								int k = 0;

								for (int i = 0; i < attendanceLen; i++) {
									// 数据长度 32+20+20=72; 72+30 = 102;
									Arrays.fill(sendAttendanceBuf, (byte) 0);
									headBuf[9] = 2; // msg_id

									System.arraycopy(headBuf, 0,sendAttendanceBuf, 0, head_len);
									System.arraycopy(InteractionService.intToBytes(m_Attendance[i].ID), 0, sendAttendanceBuf, 0 + head_len, 4);
									System.arraycopy(InteractionService.intToBytes(m_Attendance[i].autoID), 0, sendAttendanceBuf, 4 + head_len,4);
									System.arraycopy(InteractionService.intToBytes(m_Attendance[i].year),0, sendAttendanceBuf, 8 + head_len,4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].month),
											0, sendAttendanceBuf,
											12 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].day), 0,
											sendAttendanceBuf, 16 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].hour),
											0, sendAttendanceBuf,
											20 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].minute),
											0, sendAttendanceBuf,
											24 + head_len, 4);
									System.arraycopy(
											InteractionService.intToBytes(m_Attendance[i].second),
											0, sendAttendanceBuf,
											28 + head_len, 4);
									System.arraycopy(
											m_Attendance[i].date.getBytes(),
											0,
											sendAttendanceBuf,
											32 + head_len,
											m_Attendance[i].date.getBytes().length);
									System.arraycopy(
											m_Attendance[i].time.getBytes(),
											0,
											sendAttendanceBuf,
											52 + head_len,
											m_Attendance[i].time.getBytes().length);
									if (m_Attendance[i].NAME != null)
									{
										System.arraycopy(m_Attendance[i].NAME.getBytes(),0,sendAttendanceBuf,72 + head_len,m_Attendance[i].NAME.getBytes().length);
									}
									try {

										SendTcpData(sendAttendanceBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
									// 传送数据完毕
									headBuf[8] = 127;
									headBuf[10] = (byte) head_len;
									try {
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								
							} else {
									headBuf[8] = 126; // type
									headBuf[10] = (byte) head_len;
									try {
										SendTcpData(headBuf);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
							dbAdapter.close();
							dbAdapter = null;
						}
					break;
						default:
							System.out.println("在这里 缺省");
							break;
	        				}
		               	}
	                    else  //不是有效数据包
	                    {
	                    	System.out.println(" recv   err  data  package");
	                    }
	                }            
	            }
	            m_handler  = new MyHandler();
				Looper.loop();
			}
	
		 }
		 
		 public static int compare_date(String DATE1, String DATE2) {
			 DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
			 try {
			 Date dt1 = df.parse(DATE1);
			 Date dt2 = df.parse(DATE2);
			 if (dt1.getTime() > dt2.getTime()) {
				 return 1;
			 } else if (dt1.getTime() < dt2.getTime()) {
				 return -1;
			 } else {
			 return 0;
			 }
			 } catch (Exception exception) {
			 exception.printStackTrace();
			 }
			 return 0;
			 }		 
}

