package com.example.util;

import java.io.IOException;

/**
 *  测试网络是否连接到公网上
  @Name: NetStatic.java 
  @Author: 123456 
  @Date: 2015-5-12 
  @Description:
 */
public class NetStat {
	static Process p = null;
	static void destroyProcess()
	{
		if(p!=null)
		{
			p.destroy();
			p = null;
		}
	}
	
	private static boolean testNet(String str)
	{
		String result = null;
		destroyProcess();
		
		try {
			//System.out.println("开始测试网络连接 ："+str);
			p = Runtime.getRuntime().exec("ping -c 1 -w 10 " + str);//ping  1次 等100秒 
			
			// PING的状态
			int status = p.waitFor();
			if (status == 0) 
			{
				result = "successful~";
				//System.out.println("网络连接畅通");
				return true;
			} else {
				result = "failed~  cannot reach the IP address";
			}
		} catch (IOException e) {
			result = "failed~ IOException";
		} catch (InterruptedException e) {
			result = "failed~ InterruptedException";
		} finally {
			//System.out.println("result = " + result);
		}
		destroyProcess();
		return false;
	}
	
	public static final boolean ping() {
		
		//先测试cloud.hoopson.com 不通的话 测试www.baidu.com 
		boolean retTag = false;
		retTag = testNet("www.baidu.com");		
		if(retTag == false)
		{
			retTag = testNet("cloud.hoopson.com");
		}

		return retTag;
	}
}
