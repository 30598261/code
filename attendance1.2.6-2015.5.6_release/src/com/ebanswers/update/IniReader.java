package com.ebanswers.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;

import android.content.Context;

/**
 * 用于读取ini配置文件
 * @author USER
 *
 */
public class IniReader {
	//用于存放配置文件的属性值
	protected HashMap<String, Properties> sections = new HashMap<String, Properties>();
	private transient String currtionSecion;
	private transient Properties current;
	static public String androidVersion =null;
	static public String androidName = null;
	/**
	 * 读取文件
	 * @param filename 文件名
	 * @throws IOException
	 */
	public IniReader(String name, Context context) throws IOException
	{
		FileInputStream fis = null; // 读  
		OutputStream fos ;  
		Properties pp;  
		if(fileIsExists()==true)
		{
			pp = new Properties();  
			fis = new FileInputStream(name);  
			if(fis!=null)
			{
				pp.load(fis); 
				androidVersion = pp.get("androidVersion").toString();// 获取配置文件的Gps_device字段的信息，既它=号后面的数据  
				androidName = pp.get("androidName").toString();// 同上  
				GData.setName(androidName);
				System.out.println("androidVersion : "+androidVersion+" androidName : "+androidName);
			}
			else
			{
				System.out.println("没有此文件");
			}
		}
	}
	
	/**
	 * 设置每次读取文件一行
	 * @param reader 文件流
	 * @throws IOException 
	 */
	private void reader(BufferedReader reader) throws IOException {
		// TODO Auto-generated method stub
		String line = null;
		try {
			while((line = reader.readLine()) != null)
			{
				parseLine(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException("文件内容读取失败");
		}
	}
	
	/**
	 * 获取ini文件的属性值
	 * @param line ini文件每行数据
	 */
	private void parseLine(String line) {
		// TODO Auto-generated method stub
		System.out.println(line);
		try {
			if (line != null) {
				line = line.trim();
				if (line.matches("\\[.*\\]")) {
					currtionSecion = line.replaceFirst("\\[(.*)\\]", "$1");
					current = new Properties();
					sections.put(currtionSecion, current);
				} else if (line.matches(".*=.*")) {
					if (current != null) {
						int i = line.indexOf('=');
						String name = line.substring(0, i-1);
						
						String value = line.substring(i+2,i+3);
						
						current.setProperty(name, value);
						
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用于获取属性值的值
	 * @param section 整体属性的值
	 * @param name 属性值名字
	 * @return 属性值的值
	 */
	public String getValue(String section, String name)
	{
		Properties p = (Properties)sections.get(section);
		
		if(p == null)
		{
			return null;			
		}
		String value = p.getProperty(name);
	
		return value;
	}
	
	public boolean fileIsExists(){
        try{
                File f=new File("/sdcard/updateApk/version.ini");
                if(!f.exists()){
                        return false;
                }
        }catch (Exception e) {
                // TODO: handle exception
                return false;
        }
        return true;
}
}
