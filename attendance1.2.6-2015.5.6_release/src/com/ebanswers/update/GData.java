package com.ebanswers.update;

import android.app.Application;

public class GData {
	static private boolean m_isDownload;
	static public int version=1270;
	static public String name=null;
	static public int fileType = 0;
	
	static public boolean isDownload() {
		return m_isDownload;
	}

	static public void setDownload(boolean isDownload) {
		m_isDownload = isDownload;
	}

	public static int getFileType() {
		return fileType;
	}

	public static void setFileType(int fileType) {
		GData.fileType = fileType;
	}

	public static int getVersion() {
		return version;
	}

	public static void setVersion(int version) {
		GData.version = version;
	}

	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		if(name==null)
		{
			GData.name="update.apk";
		}
		else
		{
			GData.name = name;
		}
	}
	
}
