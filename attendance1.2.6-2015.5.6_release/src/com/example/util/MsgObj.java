package com.example.util;

public class MsgObj {
	
	public int deviceId;
	public int data_type;
	public byte data_len;
	public int msg_id;
	public byte[] buffer;
	public String msg_data;

	
	public MsgObj(byte [] tmp, int len)
	{
		buffer = new byte[len];
		System.arraycopy(tmp, 0, buffer, 0, len);
	}
	
	static public int Byte2Int(byte[] bs)
	{	
		int num = bs[0] & 0xFF;
		num |= ((bs[1] << 8) & 0xFF00);
		return num;
	}
	
	
	public void parseMsgPackage()
	{
		byte [] data = new byte[4];
		data_type = buffer[8];
		data_len = buffer[10];
		msg_id = buffer[9];
	}
	
	private String getInfoBuff(byte[] buff, int len)
	{
		byte[] temp = new byte[len];
		int i=0;
		for(i=0; i<len; i++)
		{
			temp[i] = buff[i+17];
		}
		return new String(temp);
	}		 
}
