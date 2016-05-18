package com.ebanswers.object;

import java.io.Serializable;
import java.net.Socket;

public class SaveConnectionClass implements Serializable{
	public Socket m_connection;
	public void Set(Socket m_socket)
	{
		m_connection = m_socket;
	}
	
	Socket Get()
	{
		return m_connection;
	}
}
