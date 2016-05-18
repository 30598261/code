package com.ebanswers.object;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SendMsgClass {

	public Socket m_socket = null;
	public OutputStream os = null;
	public BufferedOutputStream outByte = null;
	
	void SetSocket(Socket socket)
	{
		m_socket = socket;
		if(m_socket != null)
		{
			 if(outByte==null)
	    	  {
	    		  try {
					os= m_socket.getOutputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		 outByte= new BufferedOutputStream(os);
	    	}
		}
	}
}
