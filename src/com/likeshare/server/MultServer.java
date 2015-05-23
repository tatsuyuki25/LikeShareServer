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
	// �w�İϤj�p
	private static final int BufferSize = 1024;
	// �W�ɮɶ��A���@��
	private static final int TimeOut = 3000;
	// ���a��ť�ݤf
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
			// �Ыؿ�ܾ�
			selector = Selector.open();
			// ���}��ť�H�D
			ServerSocketChannel listenerChannel = ServerSocketChannel.open();
			// �P���a�ݤf�j�w
			listenerChannel.socket().bind(new InetSocketAddress(ListenPort));
			// �]�m���D����Ҧ�
			listenerChannel.configureBlocking(false);
			// �N��ܾ��j�w���ť�H�D,�u���D����H�D�~�i�H���U��ܾ�.�æb���U�L�{�����X�ӫH�D�i�H�i��Accept�ާ@
			listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
			// �Ыؤ@�ӳB�z��ĳ����{��,�ѥ��Ө���ާ@
			TCPProtocol protocol = new TCPProtocolImpl(BufferSize);

			// ���д`��,����IO
			while (true) {

				try {
					// ���ݬY�H�D�N��(�ζW��)
					if (selector.select(TimeOut) == 0) {
						System.out.print("�W�۵���.");
						continue;
					}

					// ���o���N��.selectedKeys()���]�t�F�C�ӷǳƦn�Y�@I/O�ާ@���H�D��SelectionKey
					// Iterator<SelectionKey> keyIter = selector.selectedKeys()
					// .iterator();
					for (SelectionKey key : selector.selectedKeys()) {

						// while (keyIter.hasNext()) {
						// SelectionKey key = keyIter.next();
						selector.selectedKeys().remove(key);

						if (key.isAcceptable()) {
							// ���Ȥ�ݳs���ШD��
							protocol.handleAccept(key);
						}

						else if (key.isReadable()) {
							// �q�Ȥ��Ū���ƾ�
							protocol.handleRead(key);
						}

						else if (key.isValid() && key.isWritable()) {
							// �Ȥ�ݥi�g��
							protocol.handleWrite(key);
						}

						// �����B�z�L����
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
							ServerUI.append(account + "�n�J");
							sa.sendMessage("true");
							sc.setLogin(account, mac, sa.getIP(), tmp[4]);
							sa.setMac(mac);
							// sa.sendMessage("ok");
							sendAllLogin(account, mac);
							waitMessage(account, mac);
						} else {
							ServerUI.append(account + "�n�J����");
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
							ServerUI.append(account + "���U���\");
							ServerUI.append(account + "�n�J");
							sa.sendMessage("true");
							sc.setLogin(account, mac, sa.getIP(), tmp[5]);
							sa.setMac(mac);
							sa.sendMessage("ok");
							sendAllLogin(account, mac);
							waitMessage(account, mac);
						} else {
							sa.sendMessage("false");
							ServerUI.append(account + "���U����");
							sa = null;
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					ServerUI.append(account + "�n�X");
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
		ServerUI.append(account + "���ݩR�O");
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
						ServerUI.append(account + "�n�X");
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
		ServerUI.append(myAccount + "�ШD�N" + fAccount + "�[���n��");
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
		ServerUI.append(account + "�ШD�]�ƲM��");
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
		ServerUI.append(account + "�ШD�]�ƲM��" + sa.waitMessage());
	}

	private void getMyFriends(String account, String mac) throws Exception {
		sc = new SQLConnect();
		ServerUI.append(account + "�ШD�n�ͦW��");
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
		ServerUI.append(account + "�ШD�n�ͦW��" + sa.waitMessage());
	}

	private void setConnect(String macTrans, String macReceiver)
			throws Exception {
		ServerUI.append("�إ߳s�u");
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
			file.delete(); // �U������ �M��
		}
		sa = multsa.get(rmac);
		File f = new File("D:/downloads/" + fileName);
		FileInputStream fis;
		fileName = f.getName();
		fis = new FileInputStream("D:/downloads/" + fileName);
		int size = fis.available(); // ���o�ɮפj�p
		sa.sendMessage("11:" + fileName + ":" + size); // �ǰe�ɮ׸�T
		String s = sa.waitCommand(); // ���ݰT��
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