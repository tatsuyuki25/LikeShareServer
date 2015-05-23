package com.likeshare.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class MultServer extends Thread {
	/**
	 * NIO
	 */
	// 緩衝區大小
	private static final int BufferSize = 1024;
	// 超時時間，單位毫秒
	private static final int TimeOut = 3000;
	// 本地監聽端口
	private static final int ListenPort = 1978;
	/**
	 * BIO
	 */
	private ServerAdapter sa;
	private ServerSocket server;
	private String account;
	private String pass;
	private String mac;
	private SQLConnect sc = new SQLConnect();
	private HashMap<String, ServerAdapter> multsa = new HashMap<String, ServerAdapter>();
	private int port = 8888;
	private static int no = 0;

	public void run() {
		/**
		 * NIO
		 */

		Selector selector;
		try {
			// 創建選擇器
			selector = Selector.open();
			// 打開監聽信道
			ServerSocketChannel listenerChannel = ServerSocketChannel.open();
			// 與本地端口綁定
			listenerChannel.socket().bind(new InetSocketAddress(ListenPort));
			// 設置為非阻塞模式
			listenerChannel.configureBlocking(false);
			// 將選擇器綁定到監聽信道,只有非阻塞信道才可以註冊選擇器.並在註冊過程中指出該信道可以進行Accept操作
			listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
			// 創建一個處理協議的實現類,由它來具體操作
			TCPProtocol protocol = new TCPProtocolImpl(BufferSize);

			// 反覆循環,等待IO
			while (true) {

				try {
					// 等待某信道就緒(或超時)
					if (selector.select(TimeOut) == 0) {
						System.out.print("獨自等待.");
						continue;
					}

					// 取得迭代器.selectedKeys()中包含了每個準備好某一I/O操作的信道的SelectionKey
					// Iterator<SelectionKey> keyIter = selector.selectedKeys()
					// .iterator();
					for (SelectionKey key : selector.selectedKeys()) {

						// while (keyIter.hasNext()) {
						// SelectionKey key = keyIter.next();
						selector.selectedKeys().remove(key);

						if (key.isAcceptable()) {
							// 有客戶端連接請求時
							protocol.handleAccept(key);
						}

						else if (key.isReadable()) {
							// 從客戶端讀取數據
							protocol.handleRead(key);
						}

						else if (key.isValid() && key.isWritable()) {
							// 客戶端可寫時
							protocol.handleWrite(key);
						}

						// 移除處理過的鍵
						// keyIter.remove();

					}
				} catch (Exception e) {
					e.printStackTrace();

				}

			}

			// BIO
			/*
			 * try { server = new ServerSocket(port); } catch (IOException e1) {
			 * // TODO Auto-generated catch block e1.printStackTrace();
			 * ServerUI.append("Server Port Exception"); }
			 */

			// tryLogin();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void tryLogin() {
		new Thread(new Runnable() {
			public void run() {

				ServerAdapter sa = null;

				try {

					sa = new ServerAdapter(port);
					sa.messageListen(server.accept());
					sa.fileListen(server.accept());
					sa.commandListen(server.accept());
					String str = sa.waitMessage();
					tryLogin();
					sc = new SQLConnect();
					String[] tmp = str.split(",");
					System.out.println(str);
					if (tmp[0].equals("1")) {
						account = tmp[1];
						pass = tmp[2];
						mac = tmp[3] + "";
						if (sc.checkUser(account, pass)) {
							multsa.put(mac, sa);
							ServerUI.append(account + "登入");
							sa.sendMessage("true");
							sc.setLogin(account, mac, sa.getIP(), tmp[4]);
							sa.setMac(mac);
							// sa.sendMessage("ok");
							sendAllLogin(account, mac);
							waitMessage(account, mac);
						} else {
							ServerUI.append(account + "登入失敗");
							sa.sendMessage("false");
							sa = null;
						}
					} else {
						account = tmp[1];
						pass = tmp[2];
						String name = tmp[3];
						mac = tmp[4];
						if (sc.signUp(account, pass, name)) {
							multsa.put(mac, sa);
							ServerUI.append(account + "註冊成功");
							ServerUI.append(account + "登入");
							sa.sendMessage("true");
							sc.setLogin(account, mac, sa.getIP(), tmp[5]);
							sa.setMac(mac);
							sa.sendMessage("ok");
							sendAllLogin(account, mac);
							waitMessage(account, mac);
						} else {
							sa.sendMessage("false");
							ServerUI.append(account + "註冊失敗");
							sa = null;
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					ServerUI.append(account + "登出");
					sc = new SQLConnect();
				//	sc.logout(account, sa.getMac());
					ServerUI.append("out");
					multsa.remove(mac);
					sa = null;

				}
			}
		}).start();
	}

	public void sendAllLogin(final String account, final String mac) {
		new Thread(new Runnable() {
			public void run() {
				sc = new SQLConnect();
				String tmp = sc.getLoginDevices(account, mac);
				System.out.println(1 + "|" + tmp);
				if (!tmp.equals("no")) {
					String[] str = tmp.split(",");
					System.out.println(str[0]);
					for (int i = 0; i < str.length; i++) {
						ServerAdapter sa = multsa.get(str[i]);
						try {
							sa.sendCommand("1," + mac + "");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	public void sendAllLogout(final String account, final String mac) {
		new Thread(new Runnable() {
			public void run() {
				sc = new SQLConnect();
				String tmp = sc.getLoginDevices(account, mac);
				System.out.println(1 + "|" + tmp);
				if (!tmp.equals("no")) {
					String[] str = tmp.split(",");
					System.out.println(str[0]);
					for (int i = 0; i < str.length; i++) {
						ServerAdapter sa = multsa.get(str[i]);
						try {
							sa.sendCommand("2," + mac + "");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	public void waitMessage(final String account, final String mac) {
		ServerUI.append(account + "等待命令");
		new Thread(new Runnable() {
			public void run() {
				ServerAdapter sa = multsa.get(mac);
				while (true) {
					try {
						String msg = sa.waitMessage();
						System.out.println(msg);
						String[] str = msg.split(",");
						switch (str[0]) {
						case "2":
							getMyDevices(str[1], mac, mac);
							break;
						case "3":
							getMyFriends(account, mac);
							break;
						case "4":
							setConnect(mac, str[1]);
							break;
						case "5":
							receiver(mac, str[1], str[2], str[3]);
							break;
						case "6":
							addFriend(account, str[1], mac);
							break;
						case "7":
							FriendRename(account, str[1], str[2]);
							break;
						case "8":
							FriendDelete(account, str[1]);
							break;
						case "9":
							getMyDevices(str[1], "1", mac);
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						ServerUI.append(account + "登出");
						sc = new SQLConnect();
						//sc.logout(account, sa.getMac());
						sendAllLogout(account, sa.getMac());
						multsa.remove(mac);
						sa = null;
						break;
					}
				}
			}
		}).start();
	}

	private void FriendRename(String myAccount, String fAccount, String name) {
		sc = new SQLConnect();
		sc.FriendRename(myAccount, fAccount, name);
	}

	private void FriendDelete(String myAccount, String fAccount) {
		sc = new SQLConnect();
		sc.FriendDelete(myAccount, fAccount);
	}

	private void addFriend(String myAccount, String fAccount, String mac) {
		ServerUI.append(myAccount + "請求將" + fAccount + "加為好友");
		ServerAdapter sa = multsa.get(mac);
		sc = new SQLConnect();
		if (sc.addFriend(myAccount, fAccount)) {
			sa.sendCommand("6,true");
		} else {
			sa.sendCommand("6,false");
		}
	}

	private void getMyDevices(String account, String mac, String mac2)
			throws Exception {
		sc = new SQLConnect();
		ServerUI.append(account + "請求設備清單");
		ServerAdapter sa = multsa.get(mac2);
		ArrayList<String> devices = sc.getDevices(account, mac);
		System.out.println(devices.size());
		String s = devices.size() + "";
		sa.sendMessage(s);
		for (int i = 0; i < devices.size(); i++) {
			System.out.println(devices.get(i));
			sa.waitMessage();
			sa.sendMessage(devices.get(i));
		}
		ServerUI.append(account + "請求設備清單" + sa.waitMessage());
	}

	private void getMyFriends(String account, String mac) throws Exception {
		sc = new SQLConnect();
		ServerUI.append(account + "請求好友名單");
		ServerAdapter sa = multsa.get(mac);
		ArrayList<String> friends = sc.getFriends(account);
		System.out.println(friends.size());
		String s = friends.size() + "";
		sa.sendMessage(s);
		for (int i = 0; i < friends.size(); i++) {
			System.out.println(friends.get(i));
			sa.waitMessage();
			sa.sendMessage(friends.get(i));
		}
		ServerUI.append(account + "請求好友名單" + sa.waitMessage());
	}

	private void setConnect(String macTrans, String macReceiver)
			throws Exception {
		ServerUI.append("建立連線");
		ServerAdapter trans = multsa.get(macTrans);
		ServerAdapter receiver = multsa.get(macReceiver);
		String receiverIP = receiver.getIP();
		String transIP = trans.getIP();
		if (trans.getIP().equals(receiver.getIP())) {
			receiver.sendCommand("3,getIP");
			receiverIP = receiver.waitCommand();
			trans.sendCommand("3,getIP");
			transIP = trans.waitCommand();

			System.out.println("reip" + receiverIP);
			System.out.println("tip" + transIP);

			receiver.sendCommand("5,2");
			trans.sendCommand("4,1," + receiverIP);
			String tb = trans.waitCommand();
			String rb = receiver.waitCommand();
			if (tb.equals("false") | rb.equals("false")) {
				trans.sendCommand("false");
				receiver.sendCommand("false");
				System.out.println("1no");

				receiver.sendCommand("5,1," + transIP);
				trans.sendCommand("4,2");
				tb = trans.waitCommand();
				rb = receiver.waitCommand();
				if (tb.equals("false") | rb.equals("false")) {
					trans.sendCommand("false");
					receiver.sendCommand("false");
					System.out.println("2no");
				} else {
					trans.sendCommand("true");
					receiver.sendCommand("true");
					System.out.println("2ok");
				}
			} else {
				trans.sendCommand("true");
				receiver.sendCommand("true");
				System.out.println("1ok");
			}
		} else {
			System.out.println("reip" + receiverIP);
			System.out.println("tip" + transIP);

			receiver.sendCommand("5,2");
			trans.sendCommand("4,1," + receiverIP);
			String tb = trans.waitCommand();
			String rb = receiver.waitCommand();
			if (tb.equals("false") | rb.equals("false")) {
				trans.sendCommand("false");
				receiver.sendCommand("false");
				System.out.println("3no");

				receiver.sendCommand("5,1," + transIP);
				trans.sendCommand("4,2");
				tb = trans.waitCommand();
				System.out.println(tb);
				rb = receiver.waitCommand();
				System.out.println(rb);
				if (tb.equals("false") | rb.equals("false")) {
					trans.sendCommand("false");
					receiver.sendCommand("false");
					System.out.println("4no");
				} else {
					trans.sendCommand("true");
					receiver.sendCommand("true");
					System.out.println("4ok");
				}
			} else {
				trans.sendCommand("true");
				receiver.sendCommand("true");
				System.out.println("3ok");
			}
		}
	}

	private void receiver(String tmac, String fileName, String fileSize,
			String rmac) throws Exception {
		int len;
		int max_len = 0;
		int max = Integer.parseInt(fileSize);
		byte[] buffer = new byte[8192];
		File file = null;
		FileOutputStream fos;
		ServerAdapter sa = multsa.get(tmac);
		sa.sendMessage("Y");
		try {
			file = new File("D:/downloads/" + fileName);
			fos = new FileOutputStream(file);
			while ((len = sa.readData(buffer)) != -1) {
				max_len += len;
				fos.write(buffer, 0, len);
				if (max == max_len)
					break;
			}
			// downLength = 0;
			// sa.setFileOver(0);
			fos.flush();
			fos.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			file.delete(); // 下載失敗 清除
		}
		sa = multsa.get(rmac);
		File f = new File("D:/downloads/" + fileName);
		FileInputStream fis;
		fileName = f.getName();
		fis = new FileInputStream("D:/downloads/" + fileName);
		int size = fis.available(); // 取得檔案大小
		sa.sendMessage("11:" + fileName + ":" + size); // 傳送檔案資訊
		String s = sa.waitCommand(); // 等待訊息
		System.out.println("gg");
		if (s.toString().equals("Y")) {
			buffer = new byte[8192];
			while ((len = fis.read(buffer)) != -1) {
				sa.sendData(buffer, 0, len);
			}
			fis.close();
		}
	}
}