package com.likeshare.server;

/**
 * 0 上線狀態
 * 1 所有線上設備
 * 2 我的所有設備
 *   未完成  告知所有好友 我上線了
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class CommandEvents {
	private SQLConnect sqlCmd = new SQLConnect();
	private HashMap<String, SocketChannel> value_key = new HashMap<String, SocketChannel>();
	private HashMap<SocketChannel, String> key_value = new HashMap<SocketChannel, String>();
	private HashMap<SocketChannel, String> key_account = new HashMap<SocketChannel, String>();
	private ServerSocket server;

	/**
	 * 嘗試登入 (完成)
	 * 
	 * @param socketchinnel
	 * @param tmp
	 * @throws Exception
	 */
	public void tryLogin(SocketChannel socketchinnel, String[] tmp)
			throws Exception {

		String account = tmp[1];
		String pass = tmp[2];
		String mac = tmp[3] + "";
		sqlCmd = new SQLConnect();
		if (sqlCmd.checkUser(account, pass)) {
			ServerUI.append(account + "登入");

			// set search keyword
			value_key.put(mac, socketchinnel);
			key_value.put(socketchinnel, mac);
			key_account.put(socketchinnel, account);

			sendMessage(socketchinnel, "0,true");
			sendAllFriendsLoginDevice(account, mac);

			sqlCmd.setLogin(account, mac, socketchinnel.socket()
					.getInetAddress().getHostAddress(), tmp[4]);

		} else {

			sendMessage(socketchinnel, "0,false");
			ServerUI.append(account + "失敗");

		}

	}

	/**
	 * 回傳註冊是否成功(完成)
	 * 
	 * @param socketchinnel
	 * @param tmp
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void signUp(SocketChannel socketchinnel, String[] tmp)
			throws UnsupportedEncodingException, IOException {
		String account = tmp[1];
		String pass = tmp[2];
		String name = tmp[3];
		if (sqlCmd.signUp(account, pass, name)) {
			sendMessage(socketchinnel, "3,true");
			ServerUI.append(account + "註冊成功");
		} else {
			sendMessage(socketchinnel, "3,false");
			ServerUI.append(account + "註冊失敗");
		}
	}

	/**
	 * 登出 移除資訊 (完成)
	 * 
	 * @param socketchinnel
	 */
	public void logout(SocketChannel socketchinnel) {
		sqlCmd = new SQLConnect();
		String Mac = getMac(socketchinnel);
		sqlCmd.logout(Mac);
		ServerUI.append("out");

		value_key.remove(Mac);
		key_value.remove(socketchinnel);
	}

	/**
	 * 送出好友名單
	 * 
	 * @param socketchinnel
	 * @param account
	 */
	public void sendAllFriends(SocketChannel socketchinnel, String account) {
		ServerUI.append(account + "請求好友名單");
		sqlCmd = new SQLConnect();
		ArrayList<String> friends = sqlCmd.getFriends(account);
		String message = "4," + friends.size();
		for (int i = 0; i < friends.size(); i++) {
			System.out.println(friends.get(i));
			message += "," + friends.get(i);
		}
		try {
			sendMessage(socketchinnel, message);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerUI.append(account + "請求好友成功");

	}

	/**
	 * 取得 來自 socketchinnel 的所有設備 並回傳 (完成)
	 * 
	 * @param socketchinnel
	 *            格式 2,長度,id,name,mac,type
	 */
	public void sendAllDevice(SocketChannel socketchinnel) {
		String message = "";
		sqlCmd = new SQLConnect();
		String account = getAccount(socketchinnel);
		ServerUI.append(account + "請求設備清單");
		ArrayList<String> device = sqlCmd.getDevices(account,
				getMac(socketchinnel));
		System.out.println(device.size());
		message = "2," + device.size();
		for (int i = 0; i < device.size(); i++) {
			System.out.println(device.get(i));
			message += "," + device.get(i);
		}
		try {
			sendMessage(socketchinnel, message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 送出好友擁有設備
	 * 
	 * @param socketchinnel
	 * @param account
	 */
	public void sendFriendDevice(SocketChannel socketchinnel, String account) {
		String message = "";
		sqlCmd = new SQLConnect();

		ServerUI.append(account + "設備清單被取得");
		ArrayList<String> device = sqlCmd.getDevices(account, "1");
		System.out.println(device.size());
		message = "5," + device.size();
		for (int i = 0; i < device.size(); i++) {
			System.out.println(device.get(i));
			message += "," + device.get(i);
		}
		try {
			sendMessage(socketchinnel, message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 取得MAC (完成)
	 * 
	 * @param socketchinnel
	 * @return MAC
	 */
	public String getMac(SocketChannel socketchinnel) {
		return key_value.get(socketchinnel);
	}

	/**
	 * 取得帳號 (完成)
	 * 
	 * @param socketchinnel
	 * @return account
	 */
	public String getAccount(SocketChannel socketchinnel) {
		return key_account.get(socketchinnel);
	}

	/**
	 * 告知所有好友我上線了 (完成)
	 * 
	 * @param account
	 * @param mac
	 */

	public void sendAllFriendsLoginDevice(String account, String mac) {
		sqlCmd = new SQLConnect();
		String tmp = sqlCmd.getLoginDevices(account, mac);
		System.out.println(1 + "|" + tmp);
		if (!tmp.equals("no")) {
			String[] str = tmp.split(",");
			System.out.println(str[0]);
			for (int i = 0; i < str.length; i++) {
				SocketChannel socketchinnel = value_key.get(str[i]);
				try {
					sendMessage(socketchinnel, "1," + mac + "");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 建立雙方連線
	 * 
	 * @param socketchinnel
	 * @param mac
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public void createConnect(final SocketChannel socketchinnel,
			final String mac) throws UnsupportedEncodingException, IOException {
		/**
		 * 1.開啟伺服器 Port 2.告知接收端 連接伺服器 port 3.告知傳送端開始連接伺服器 4.傳送檔案
		 */
		new Thread(new Runnable() {
			public void run() {

				ServerAdapter saT = new ServerAdapter(8808);
				ServerAdapter saR = new ServerAdapter(8809);
				// 開始請聽port
				new Thread(new Runnable() {
					public void run() {
						
						try {
							// 告知接收端 連接伺服器 8808 port
							sendMessage(value_key.get(mac), "6,8808,R,"+mac);
							// 告知傳送端 連接 伺服器 8809 port
							sendMessage(socketchinnel, "6,8809,T"); // T = 請你傳送檔案
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // R = 請你接收檔案
					}
				}).start();
				try {
					ServerSocket t = new ServerSocket(8808);
					ServerSocket r = new ServerSocket(8809);
					System.out.println("0");
					saT.messageListen(t.accept());
					System.out.println("01");
					saT.fileListen(t.accept());
					System.out.println("02");
					
					
					saR.messageListen(r.accept());
					System.out.println("04");
					saR.fileListen(r.accept());
					System.out.println("05");
					
					System.out.println("1");
					// 等待接收端 連接 傳送端
					String str = saT.waitMessage(); // 收到 true
					System.out.println("1" + str);
					System.out.println("3");
					// 等待傳送端連接 傳送端
					String str1 = saR.waitMessage(); // 收到11,檔案資訊
					System.out.println("4" + str1);
					String[] Msg = str1.split(":");
					// 開始傳送與接收
					if (Msg[0].toString().equals("11")) {
						saT.sendMessage(str1);
						saR.sendMessage("Y");
						System.out.println("送出");
						int len;
						int max_len = 0;
						int max = Integer.parseInt(Msg[2]);
						byte[] buffer = new byte[8192];
						while ((len = saR.readData(buffer)) != -1) {
							max_len += len;
							saT.sendData(buffer, 0, len);
							if (max == max_len)
								break;
						}
						System.out.println("送完");
						t.close();
						r.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("err");
				}

			}
		}).start();
	}

	/**
	 * 送出命令 (完成) 0登入成功(true/false) 1上線通知所有人 2送出自己的所有設備 3註冊成功(true/false) 4好友名單
	 * 5好友設備 6告知連線指定port
	 * 
	 * @param socketchinnel
	 * @param message
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void sendMessage(SocketChannel socketchinnel, String message)
			throws UnsupportedEncodingException, IOException {

		socketchinnel.write(ByteBuffer.wrap(message.getBytes("UTF-16")));
	}

	public void sendMessage(SelectionKey key, String message)
			throws UnsupportedEncodingException, IOException {
		SocketChannel socketchinnel = (SocketChannel) key.channel();
		socketchinnel.write(ByteBuffer.wrap(message.getBytes("UTF-16")));
	}

}
