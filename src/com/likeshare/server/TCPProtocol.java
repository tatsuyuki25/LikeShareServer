package com.likeshare.server;
import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * TCPServerSelector與特定協議間通信的接口
 * 
 * @date    2010-2-3
 * @time    上午08:42:42
 * @version 1.00
 */
public interface TCPProtocol{
  /**
   * 接收一個SocketChannel的處理
   * @param key
   * @throws IOException
   */
  void handleAccept(SelectionKey key) throws IOException;
  
  /**
   * 從一個SocketChannel讀取信息的處理
   * @param key
   * @throws IOException
   */
  void handleRead(SelectionKey key) throws IOException;
  
  /**
   * 向一個SocketChannel寫入信息的處理
   * @param key
   * @throws IOException
   */
  void handleWrite(SelectionKey key) throws IOException;
}