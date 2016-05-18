package com.example.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.util.Log;

/**
 * 
 * 
 * @author BaoHang
 * @version 1.0
 * @data 2012-2-20
 */
public class MyLog {
	private static Boolean MYLOG_WRITE_TO_FILE=true;//
	private static Boolean MYLOG_WRITE_TO_FILE_W=true;
	private static Boolean MYLOG_WRITE_TO_FILE_E=true;
	private static Boolean MYLOG_WRITE_TO_FILE_D=true;
	private static Boolean MYLOG_WRITE_TO_FILE_I=true;
	private static Boolean MYLOG_WRITE_TO_FILE_V=true;
	
	public static String MYLOG_PATH_SDCARD_DIR="/data/data/com.ebanswers.attendance/";//
	private static int SDCARD_LOG_FILE_SAVE_DAYS = 7;//
	private static String MYLOGFILEName = "Log.txt";//
	private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");//
	private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");//

	public static void w(String tag, Object msg) { //
	//	log(tag, msg.toString(), 'w');
	}

	public static void e(String tag, Object msg) { //
		log(tag, msg.toString(), 'e');
	}

	public static void d(String tag, Object msg) {//
		log(tag, msg.toString(), 'd');
	}

	public static void i(String tag, Object msg) {//
		log(tag, msg.toString(), 'i');
	}

	public static void v(String tag, Object msg) {
		log(tag, msg.toString(), 'v');
	}

	public static void w(String tag, String text) {
		log(tag, text, 'w');
		log(tag, text, 'w');
	}

	public static void e(String tag, String text) {
		log(tag, text, 'e');
	}

	public static void d(String tag, String text) {
		log(tag, text, 'd');
	}

	public static void i(String tag, String text) {
		log(tag, text, 'i');
	}

	public static void v(String tag, String text) {
		log(tag, text, 'v');
	}
	
	private static void log(String tag, String msg, char level) {
		if(msg ==null) msg ="";
		//	if (1) {//MYLOG_SWITCH
				if ('w' == level) {
					Log.w(tag, msg);
				} else if ('d' == level) {
					Log.d(tag, msg);
				} else if ('i' == level) {
					Log.i(tag, msg);
				} else if ('v' == level){
					Log.v(tag, msg);
				}
		//	}
			
			if ('e' == level) { //
				Log.e(tag, msg);
			} 
			
			if (MYLOG_WRITE_TO_FILE){
				
				switch (level) {
				case 'w':MYLOG_WRITE_TO_FILE = MYLOG_WRITE_TO_FILE_W;
					break;
				case 'e':MYLOG_WRITE_TO_FILE = MYLOG_WRITE_TO_FILE_E;
				break;
				case 'd':MYLOG_WRITE_TO_FILE = MYLOG_WRITE_TO_FILE_D;
				break;
				case 'i':MYLOG_WRITE_TO_FILE = MYLOG_WRITE_TO_FILE_I;
				break;
				case 'v':MYLOG_WRITE_TO_FILE = MYLOG_WRITE_TO_FILE_V;
				break;
				}
				
				//sdcard 閸撯晙缍戠�妯哄亶缁屾椽妫挎径褌绨�00M閺冭绱濋幍宥堝厴閸愭瑥鍙嗛弬鍥︽
				if(MYLOG_WRITE_TO_FILE){
					//if(level != 'e' && checkNoNeedToPrint(msg)){// 闂堬拷'e'缁狙冨焼閻ㄥ嫬鎯婇悳顖涙纯閺傛澘宕楃拋鐢僌G
					//	if(!StaticInfos.logcat_print_log)return;//閼活櫜og瀵拷鍙ч崗鎶芥４閿涘苯鍨稉宥呭晸閸忋儲鏋冩禒璁圭礉闁灝鍘OG閺傚洣娆㈡潻鍥с亣
				//	}
					writeLogtoFile(String.valueOf(level), tag, msg);
				}
			}
	}

	
	private static void writeLogtoFile(String mylogtype, String tag, String text) {//
		Date nowtime = new Date();
		String needWriteFiel = logfile.format(nowtime);
		String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype
				+ "    " + tag + "    " + text;
		File file = new File(MYLOG_PATH_SDCARD_DIR);
		 if (!file.exists()) {
			 file.mkdirs();
	        }
		file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFiel
				+ MYLOGFILEName);
		if(!file.exists()){
			try {
				file.createNewFile();
				
				//閸掓稑缂撻弬鐗堟瀮娴犲墎娈戦弮璺猴拷濡拷绁撮獮璺哄灩闂勩倛绻冮張鐔烘畱閺傚洣娆�
				tyrDeleteFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter filerWriter = new FileWriter(file, true);
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
			bufWriter.flush();
			filerWriter.flush();
			bufWriter.close();
			filerWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			
		//	Utilties.execRootCmdSilent("mount -o remount,rw vfat /mnt/sdcard");  //濮濄倕鎳℃禒銈呭讲娴犮儳鐓弳鍌欐叏婢跺稄绱濇担鍡樻Ц瀵板牆鎻╅崣妯硅礋缁崵绮洪崣顏囶嚢,闂囷拷顩﹂崚鐘绘珟閹碉拷婀侀弬鍥︽閸氬孩顒滅敮锟�		//	Utilties.execRootCmdSilent("rm -r /mnt/sdcard/*");
		//	new ENCHomeAct().copyAssetFiles();
		}
	}
	
	/**
	 * 閸掔娀娅庢径姘毌婢垛晙浜掗崜宥囨畱LOG
	 */
	public static void tyrDeleteFile() {
		String timeString = getDateBefore();
		//閸樺娅�- 閸滐拷 : 缁楋箑褰�
		timeString = timeString.replace("-", "");
		//瀵版鍩屾潻鍥ㄦ埂閺冨爼妫块悙锟�		
		long time = Long.valueOf(timeString.substring(0, 8));
		
		File [] files = new File(MYLOG_PATH_SDCARD_DIR).listFiles();
		if (files != null) {
			for(int i = 0; i < files.length; i ++){
				String name = files[i].getName();
				if(name.length() != 20){//閻㈢喐鍨氶柨娆掝嚖閺傚洣娆㈤崥锟介弬鍥︽閸氬秹鍣烽棃銏″壈婢舵牕銇欓弶锟�)閻ㄥ嫭鏋冩禒璺哄灩闂勩倧绱濋獮璺哄灩闂勩倕姘ㄩ悧鍫熸拱log閺傚洣娆�闂�灝瀹虫稉锟�閹达拷9)
					files[i].delete();
				}
				else{//鐏忓棜绻冮張鐔烘畱log閺傚洣娆㈤崚鐘绘珟
					name = name.replace("-", "");
					for(int j = 0; j < 8; j ++){
						if(name.charAt(j) < '0' || name.charAt(j) > '9'){
							files[i].delete();
							return;
						}
					}
					long time2 = Long.valueOf(name.substring(0, 8));
					if(time >= time2)
					{
						files[i].delete();
					}
				}
			}
		}
	}
	
	/**
	 * 閼惧嘲褰�SDCARD_LOG_FILE_SAVE_DAYS 婢垛晙浜掗崜宥囨畱閺冨爼妫块敍锟�	 * @return yyyyMMdd鐎涙顑佹稉锟�	 */
	private static String getDateBefore() {
		Date nowtime = new Date();
		Calendar now = Calendar.getInstance();
		now.setTime(nowtime);
		now.set(Calendar.DATE, now.get(Calendar.DATE)
				- SDCARD_LOG_FILE_SAVE_DAYS);
		return logfile.format(now.getTime());
	}
	
	public static void saveException( Exception e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log("saveException",  sw.toString(), 'e');
	}	
	
	public static void saveException(String TAG, Exception e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log(TAG + ": saveException",  sw.toString(), 'e');
	}	
	public static void saveThrowable(Throwable ex) {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		log("saveThrowable",  sw.toString(), 'e');
    }

}
