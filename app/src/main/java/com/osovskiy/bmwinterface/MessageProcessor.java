package com.osovskiy.bmwinterface;

import android.os.Handler;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vadim on 6/29/2014.
 */
public class MessageProcessor
{
  private static final int MSG_MIN_SIZE = 5;
  private static final int BUFFER_SIZE = 4096;

  private byte[] _buffer;
  private int _bufferTail, _bufferHead;
  private boolean _synced;
  private List<EventListener> _eventListeners;
  private Handler _serviceHandler;

  public void addEventListener(EventListener eventListener)
  {
    _eventListeners.add(eventListener);
  }

  public boolean removeEventListener(EventListener eventListener)
  {
    return _eventListeners.remove(eventListener);
  }

  public void setServiceHandler(Handler handler)
  {
    _serviceHandler = handler;
  }

  private void fireHandler(final BusMessage message)
  {
    if ( _serviceHandler == null )
      return;

    _serviceHandler.post(new Runnable()
    {
      @Override
      public void run() // runs on service thread
      {
        for ( EventListener el : _eventListeners )
        {
          el.newMessage(message);
        }
      }
    });
  }

  public MessageProcessor()
  {
    _buffer = new byte[BUFFER_SIZE];
    _bufferTail = 0;
    _bufferHead = 0;
    _synced = false;
    _eventListeners = new ArrayList<EventListener>();
    _serviceHandler = null;
  }

  public boolean isSynced()
  {
    return _synced;
  }

  /**
   * Add data to the buffer. Called by thread that reads data from serial port.
   *
   * @param data The bytes to append to buffer
   */
  protected void appendBuffer(byte data[])
  {
    if ( data.length > ( BUFFER_SIZE - size() ) )
      throw new BufferOverflowException();

    for ( byte aData : data )
    {
      _buffer[_bufferHead] = aData;
      _bufferHead = ( _bufferHead + 1 ) % BUFFER_SIZE;
    }
  }

  /**
   * Peek at the first available byte in a buffer
   *
   * @return The first data byte
   */
  protected byte peek()
  {
    return peek(0);
  }

  /**
   * Peek at specific byte in a buffer
   *
   * @param offset The number of bytes to offset by
   * @return The data byte at offset position
   */
  protected byte peek(int offset)
  {
    return _buffer[( _bufferTail + offset ) % BUFFER_SIZE];
  }

  /**
   * Process data in a buffer. Process is done when there are less then 5 bytes in buffer,
   * or assumed message length is bigger than available bytes. BAD PRACTICE!
   */
  protected void process()
  {
    boolean working = true;
    while ( working )
    {
      if ( size() >= MSG_MIN_SIZE ) // At least five bytes in buffer (minimum message length)
      {
        int assumedLength = ( peek(1) & 0xFF ) + 2;
        if ( size() >= assumedLength )
        {
          ByteBuffer byteBuffer = ByteBuffer.allocate(assumedLength);

          for ( int i = 0; i < assumedLength; i++ )
          {
            byteBuffer.put(peek(i));
          }

          BusMessage busMessage = BusMessage.tryParse(byteBuffer.array());

          if ( busMessage != null )
          {
            _synced = true;
            truncate(assumedLength);

            fireHandler(busMessage);
          }
          else
          {
            _synced = false;
            truncate();
          }
        }
        else
          working = false;
      }
      else
        working = false;
    }
  }

  /**
   * Get number of bytes in buffer
   *
   * @return The number of data bytes in buffer
   */
  protected int size()
  {
    return ( BUFFER_SIZE + _bufferHead - _bufferTail ) % BUFFER_SIZE;
  }

  /**
   * Truncate single byte. Used when out of sync
   */
  protected void truncate()
  {
    truncate(1);
  }

  /**
   * Truncate certain amount of bytes. Used when buffer is in sync and valid message was read
   *
   * @param amount The number of bytes
   */
  protected void truncate(int amount)
  {
    _bufferTail = ( _bufferTail + amount ) % BUFFER_SIZE;
  }

  public interface EventListener
  {
    void newMessage(BusMessage message);
    //void onSyncStateChange(boolean state);
  }
}
