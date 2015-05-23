package com.likeshare.server;

/**
 * 0 �W�u���A
 * 1 �Ҧ��u�W�]��
 * 2 �ڪ��Ҧ��]��
 *   ������  �i���Ҧ��n�� �ڤW�u�F
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
	 * ���յn�J (����)
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
			ServerUI.append(account + "�n�J");

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
			ServerUI.append(account + "����");

		}

	}

	/**
	 * �^�ǵ��U�O�_���\(����)
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
			ServerUI.append(account + "���U���\");
		} else {
			sendMessage(socketchinnel, "3,false");
			ServerUI.append(account + "���U����");
		}
	}

	/**
	 * �n�X ������T (����)
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
	 * �e�X�n�ͦW��
	 * 
	 * @param socketchinnel
	 * @param account
	 */
	public void sendAllFriends(SocketChannel socketchinnel, String account) {
		ServerUI.append(account + "�ШD�n�ͦW��");
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
		ServerUI.append(account + "�ШD�n�ͦ��\");

	}

	/**
	 * ���o �Ӧ� socketchinnel ���Ҧ��]�� �æ^�� (����)
	 * 
	 * @param socketchinnel
	 *            �榡 2,����,id,name,mac,type
	 */
	public void sendAllDevice(SocketChannel socketchinnel) {
		String message = "";
		sqlCmd = new SQLConnect();
		String account = getAccount(socketchinnel);
		ServerUI.append(account + "�ШD�]�ƲM��");
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
	 * �e�X�n�;֦��]��
	 * 
	 * @param socketchinnel
	 * @param account
	 */
	public void sendFriendDevice(SocketChannel socketchinnel, String account) {
		String message = "";
		sqlCmd = new SQLConnect();

		ServerUI.append(account + "�]�ƲM��Q���o");
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
	 * ���oMAC (����)
	 * 
	 * @param socketchinnel
	 * @return MAC
	 */
	public String getMac(SocketChannel socketchinnel) {
		return key_value.get(socketchinnel);
	}

	/**
	 * ���o�b�� (����)
	 * 
	 * @param socketchinnel
	 * @return account
	 */
	public String getAccount(SocketChannel socketchinnel) {
		return key_account.get(socketchinnel);
	}

	/**
	 * �i���Ҧ��n�ͧڤW�u�F (����)
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
	 * �إ�����s�u
	 * 
	 * @param socketchinnel
	 * @param mac
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public void createConnect(final SocketChannel socketchinnel,
			final String mac) throws UnsupportedEncodingException, IOException {
		/**
		 * 1.�}�Ҧ��A�� Port 2.�i�������� �s�����A�� port 3.�i���ǰe�ݶ}�l�s�����A�� 4.�ǰe�ɮ�
		 */
		new Thread(new Runnable() {
			public void run() {

				ServerAdapter saT = new ServerAdapter(8808);
				ServerAdapter saR = new ServerAdapter(8809);
				// �}�l��ťport
				new Thread(new Runnable() {
					public void run() {
						
						try {
							// �i�������� �s�����A�� 8808 port
							sendMessage(value_key.get(mac), "6,8808,R,"+mac);
							// �i���ǰe�� �s�� ���A�� 8809 port
							sendMessage(socketchinnel, "6,8809,T"); // T = �ЧA�ǰe�ɮ�
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // R = �ЧA�����ɮ�
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
					// ���ݱ����� �s�� �ǰe��
					String str = saT.waitMessage(); // ���� true
					System.out.println("1" + str);
					System.out.println("3");
					// ���ݶǰe�ݳs�� �ǰe��
					String str1 = saR.waitMessage(); // ����11,�ɮ׸�T
					System.out.println("4" + str1);
					String[] Msg = str1.split(":");
					// �}�l�ǰe�P����
					if (Msg[0].toString().equals("11")) {
						saT.sendMessage(str1);
						saR.sendMessage("Y");
						System.out.println("�e�X");
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
						System.out.println("�e��");
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
	 * �e�X�R�O (����) 0�n�J���\(true/false) 1�W�u�q���Ҧ��H 2�e�X�ۤv���Ҧ��]�� 3���U���\(true/false) 4�n�ͦW��
	 * 5�n�ͳ]�� 6�i���s�u���wport
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
