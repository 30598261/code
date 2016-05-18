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
 * ���ڶ�ȡini�����ļ�
 * @author USER
 *
 */
public class IniReader {
	//���ڴ�������ļ�������ֵ
	protected HashMap<String, Properties> sections = new HashMap<String, Properties>();
	private transient String currtionSecion;
	private transient Properties current;
	static public String androidVersion =null;
	static public String androidName = null;
	/**
	 * ��ȡ�ļ�
	 * @param filename �ļ���
	 * @throws IOException
	 */
	public IniReader(String name, Context context) throws IOException
	{
		FileInputStream fis = null; // ��  
		OutputStream fos ;  
		Properties pp;  
		if(fileIsExists()==true)
		{
			pp = new Properties();  
			fis = new FileInputStream(name);  
			if(fis!=null)
			{
				pp.load(fis); 
				androidVersion = pp.get("androidVersion").toString();// ��ȡ�����ļ���Gps_device�ֶε���Ϣ������=�ź��������  
				androidName = pp.get("androidName").toString();// ͬ��  
				GData.setName(androidName);
				System.out.println("androidVersion : "+androidVersion+" androidName : "+androidName);
			}
			else
			{
				System.out.println("û�д��ļ�");
			}
		}
	}
	
	/**
	 * ����ÿ�ζ�ȡ�ļ�һ��
	 * @param reader �ļ���
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
			throw new IOException("�ļ����ݶ�ȡʧ��");
		}
	}
	
	/**
	 * ��ȡini�ļ�������ֵ
	 * @param line ini�ļ�ÿ������
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
	 * ���ڻ�ȡ����ֵ��ֵ
	 * @param section �������Ե�ֵ
	 * @param name ����ֵ����
	 * @return ����ֵ��ֵ
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
