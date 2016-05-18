package com.example.db;

public class ShutOpenDeviceClass {
	public byte[] openDeviceTime = new byte[5];
	public byte[] closeDeviceTime = new byte[5];
	public int m_switch=0;
	
	public String  toString()
	{
		String str = " openDEviceTime : "+new String(openDeviceTime)+" closeDeviceTime : "+new String(closeDeviceTime)+"  switch : "+m_switch;
		return str;
	}
}
