package com.likeshare.server;

/*
 *  new ServerAdapter(Port) //設定port
 * 	messageListen() //訊息監聽端口    有 2 種端口 
 * 	fileListen(); //資料監聽端口 
 * 	sendMessage("fileY"); // 送出訊息
 *  Msg = sa.waitMessage(); // 接收訊息
 *  readData(byte[] buffer) // 接收檔案
 * 	
 * */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerAdapter implements Runnable
{
	private int port;
	private Socket fileSocket;
	private Socket messageSocket;
	private Socket commandSocket;
	private InputStream fileIn;
	private OutputStream fileOut;
	private InputStream messageIn;
	private OutputStream messageOut;
	private InputStream commandIn;
	private OutputStream commandOut;
	private String mac;
	private int fileOver = 0;

	public ServerAdapter(int port)
	{
		this.setPort(port);
	}

	// 開始監聽訊息
	public void messageListen(Socket sk) throws IOException
	{
		messageSocket = sk;
		messageIn = messageSocket.getInputStream();
		messageOut = messageSocket.getOutputStream();
	}

	// 開始監聽資料
	public void fileListen(Socket sk) throws IOException
	{
		// fileServer = new ServerSocket(port);
		fileSocket = sk;
		fileIn = fileSocket.getInputStream();
		fileOut = fileSocket.getOutputStream();
	}

	public void commandListen(Socket sk) throws IOException
	{
		// fileServer = new ServerSocket(port);
		commandSocket = sk;
		commandIn = commandSocket.getInputStream();
		commandOut = commandSocket.getOutputStream();
	}

	public String waitCommand() throws IOException
	{
		// Scanner read = new Scanner(messageIn,"utf-8");
		 BufferedReader read = new BufferedReader(new
		 InputStreamReader(commandIn,
		 "utf-8"));
		//byte[] b = new byte[8192];
		//int i = commandIn.read(b);
		//String s = new String(b,0,i);
		return read.readLine();
	}

	public void sendCommand(String msg)
	{
		PrintStream ps = new PrintStream(commandOut);
		ps.println(msg);
		//ps.flush();
		
	}

	// 等待對方傳送訊息
	public String waitMessage() throws IOException
	{
		// Scanner read = new Scanner(messageIn,"utf-8");
		 BufferedReader read = new BufferedReader(new
		 InputStreamReader(messageIn,
		 "utf-8"));
		//byte[] b = new byte[8192];
		//int i = messageIn.read(b);
		//String s = new String(b,0,i);
		return read.readLine();
	}

	// 傳送訊息
	public void sendMessage(String msg)
	{
		PrintStream ps = new PrintStream(messageOut);
		ps.println(msg);
		//ps.flush();

	}

	// 傳送資料
	public void sendData(byte[] buffer,int off,int len) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(fileOut);
		dos.write(buffer,off,len);
		dos.flush();
	}

	// 接收資料
	public int readData(byte[] buffer) throws IOException
	{
		DataInputStream dis = new DataInputStream(fileIn);
		return dis.read(buffer,0,buffer.length);
	}

	public void run()
	{
		try
		{
			String msg = waitMessage();
			if(msg.equals("fileOver"))
				setFileOver(1);
			System.out.println("00:" + msg);
		} catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 以下為各個屬性的Getter與Setter
	 */
	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public Socket getFileSocket()
	{
		return fileSocket;
	}

	public void setFileSocket(Socket fileSocket)
	{
		this.fileSocket = fileSocket;
	}

	public InputStream getFileIn()
	{
		return fileIn;
	}

	public void setFileIn(InputStream fileIn)
	{
		this.fileIn = fileIn;
	}

	public OutputStream getFileOut()
	{
		return fileOut;
	}

	public void setFileOut(OutputStream fileOut)
	{
		this.fileOut = fileOut;
	}

	public Socket getMessageSocket()
	{
		return messageSocket;
	}

	public void setMessageSocket(Socket messageSocket)
	{
		this.messageSocket = messageSocket;
	}

	public InputStream getMessageIn()
	{
		return messageIn;
	}

	public void setMessageIn(InputStream messageIn)
	{
		this.messageIn = messageIn;
	}

	public OutputStream getMessageOut()
	{
		return messageOut;
	}

	public void setMessageOut(OutputStream messageOut)
	{
		this.messageOut = messageOut;
	}

	public synchronized int getFileOver()
	{
		return fileOver;
	}

	public synchronized void setFileOver(int fileOver)
	{
		this.fileOver = fileOver;
	}

	public void setSendUrgentData() throws Exception
	{
		messageSocket.sendUrgentData(0xFF);
	}

	public String getMac()
	{
		return mac;
	}

	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public String getIP()
	{
		return fileSocket.getInetAddress().getHostAddress();
	}

	public InputStream getCommandIn()
	{
		return commandIn;
	}

	public void setCommandIn(InputStream commandIn)
	{
		this.commandIn = commandIn;
	}

	public OutputStream getCommandOut()
	{
		return commandOut;
	}

	public void setCommandOut(OutputStream commandOut)
	{
		this.commandOut = commandOut;
	}
}
