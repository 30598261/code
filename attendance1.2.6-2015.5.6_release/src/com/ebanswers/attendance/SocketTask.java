package com.ebanswers.attendance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 閸愬懘鍎寸痪璺ㄢ柤缁拷鐠愮喕鐭楁稉搴㈢槨娑擃亜顓归幋椋庮伂閻ㄥ嫭鏆熼幑顕�拷娣囷拷
 * 
 * @author Administrator
 */
public final class SocketTask implements Runnable {
	private Socket s;
	public  DataInputStream input;
	public DataOutputStream output;


	public SocketTask(Socket socket) {
		s = socket;
		try {
			input = new DataInputStream(s.getInputStream());
			output = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 閸欐垿锟藉☉鍫熶紖
	 * 
	 * @param msg
	 * @param datas
	 */
	public void sendMsg(String msg, byte[] datas) {
		try {
			if (null != msg) {
				output.writeUTF(msg);
			}
			if (null != datas) {
				output.writeInt(datas.length);
				output.write(datas, 0, datas.length);
			}
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				String msgCtn = input.readUTF();
				/*
				if (msgCtn.startsWith(ContentFlag.REGOSTER_FLAG)) { 
					String name = msgCtn.substring(
							ContentFlag.REGOSTER_FLAG.length()).trim();
					String userId = XmlParser.saveUserInfo(name, input);
					this.sendMsg(userId, null);
					taskList.remove(this);
					break;
				}
				*/ 
			}
		} catch (Exception e) {
			e.printStackTrace();
			Server.taskList.remove(this);
		} finally {
			try {
				if (null != input)
					input.close();
				if (null != output)
					output.close();
				if (null != s)
					s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}