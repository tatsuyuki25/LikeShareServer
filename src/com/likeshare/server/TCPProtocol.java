package com.likeshare.server;
import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * TCPServerSelector�P�S�w��ĳ���q�H�����f
 * 
 * @date    2010-2-3
 * @time    �W��08:42:42
 * @version 1.00
 */
public interface TCPProtocol{
  /**
   * �����@��SocketChannel���B�z
   * @param key
   * @throws IOException
   */
  void handleAccept(SelectionKey key) throws IOException;
  
  /**
   * �q�@��SocketChannelŪ���H�����B�z
   * @param key
   * @throws IOException
   */
  void handleRead(SelectionKey key) throws IOException;
  
  /**
   * �V�@��SocketChannel�g�J�H�����B�z
   * @param key
   * @throws IOException
   */
  void handleWrite(SelectionKey key) throws IOException;
}