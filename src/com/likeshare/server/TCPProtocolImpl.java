package com.likeshare.server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * TCPProtocol的實現
 * 
 * @date
 * @time 
 * @version 
 */
public class TCPProtocolImpl implements TCPProtocol {
	private int bufferSize;
    protected CommandEvents cmd = new CommandEvents();
	public TCPProtocolImpl(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel socketchinnel = ((ServerSocketChannel) key.channel())
				.accept();
		socketchinnel.configureBlocking(false);
		socketchinnel.register(key.selector(), SelectionKey.OP_READ,
				ByteBuffer.allocate(bufferSize));
		key.interestOps(SelectionKey.OP_ACCEPT);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// 獲得與客戶端通信的信道
		SocketChannel socketchinnel = (SocketChannel) key.channel();
		// 得到並清空緩衝區
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		buffer.clear();
		try {
			// 讀取信息獲得讀取的字節數
			long bytesRead = socketchinnel.read(buffer);

			if (bytesRead == -1) {
				// 沒有讀取到內容的情況
				System.out.println("客戶端離線:"+socketchinnel.socket().getRemoteSocketAddress());
				cmd.logout(socketchinnel);
				socketchinnel.close();
			} else {
				// 將緩衝區準備為數據傳出狀態
				buffer.flip();
				// 將字節轉化為為UTF-16的字符串
				String receivedString = Charset.forName("UTF-16").newDecoder()
						.decode(buffer).toString();
				// 收到的訊息
				System.out.println("接收到來自"
						+ socketchinnel.socket().getRemoteSocketAddress()
						+ "的訊息:" + receivedString);
				String tmp[] = receivedString.split(",");
				switch(tmp[0])
				{
				case "0": // SignUp
					cmd.signUp(socketchinnel,tmp);
					break;
				case "1": // Login
					cmd.tryLogin(socketchinnel,tmp);
					break;
				case "2": // get My All Device to socketchinnel
					cmd.sendAllDevice(socketchinnel);
					break;
				case "3": // get My All Friend
					cmd.sendAllFriends(socketchinnel,tmp[1]);
					break;
				case "4": // 請求建立連線
					cmd.sendFriendDevice(socketchinnel, tmp[1]);
					break;
				case "5":// 
					cmd.createConnect(socketchinnel, tmp[1]);
					break;
				
				}
				// 準備發送的文本
				/*String sendString = "你好,客戶端. @" + new Date().toString()
						+ "，已經收到你的信息" + receivedString;
				buffer = ByteBuffer.wrap(sendString.getBytes("UTF-16"));
				socketchinnel.write(buffer);*/

				// 設置為下一次讀取或是寫入做準備
				key.interestOps(SelectionKey.OP_READ);
			}
		} catch (Exception e) { // Exception  Logout
			System.out.println("客戶端離線:"+socketchinnel.socket().getRemoteSocketAddress());
			key.cancel();
			cmd.logout(socketchinnel);
			if (key.channel() != null) {
				key.channel().close();
			}
		}
	}
   
	public void handleWrite(SelectionKey key) throws IOException {
		// do nothing
	}
}