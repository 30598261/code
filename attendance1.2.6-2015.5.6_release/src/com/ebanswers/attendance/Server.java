package com.ebanswers.attendance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
	private ExecutorService executorService;// 缁捐法鈻煎Ч锟�	private int port;// 閻╂垵鎯夌粩顖氬經
	private boolean quit = false;// 闁拷鍤�
	private ServerSocket server;
	static public List<SocketTask> taskList = new ArrayList<SocketTask>();// 娣囨繂鐡ㄩ幍锟芥箒閸氼垰濮╅惃鍓唎cket闂嗗棗鎮�

	private Map<Integer, SocketTask> m_map = new HashMap<Integer, SocketTask>();
	private int port;
	public Server(int port) {
		this.port = port;
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);
	}

	/**
	 * 閺堝秴濮熼崳銊х矒濮濓拷閸忔娊妫撮幍锟芥箒缁捐法鈻�
	 */
	public void quit() {
		this.quit = true;
		try {
			for (SocketTask tast : taskList) {
				tast.input.close();
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 閺堝秴濮熼崳銊ユ儙閸旓拷
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		server = new ServerSocket(port);
		new Thread(new Runnable() {
			public void run() {
				while (!quit) {
					try {
						Socket socket = server.accept();
						SocketTask newTask = new SocketTask(socket);
						taskList.add(newTask);
						executorService.execute(newTask);
					} catch (Exception e) {
						System.out.println("閺堝秴濮熼崳銊х矒濮濐枎绱掗崗鎶芥４閹碉拷婀佺痪璺ㄢ柤");
					}
				}
			}
		}).start();
	}

/*
	public final class SocketTask implements Runnable {
		private Socket s;
		private DataInputStream input;
		private DataOutputStream output;


		public SocketTask(Socket socket) {
			s = socket;
			try {
				input = new DataInputStream(s.getInputStream());
				output = new DataOutputStream(s.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	
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
					
					//if (msgCtn.startsWith(ContentFlag.REGOSTER_FLAG)) { 
					//	String name = msgCtn.substring(
					//			ContentFlag.REGOSTER_FLAG.length()).trim();
					//	String userId = XmlParser.saveUserInfo(name, input);
					//	this.sendMsg(userId, null);
					//	taskList.remove(this);
					//	break;
					//}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				taskList.remove(this);
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
	*/
}
