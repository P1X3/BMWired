package com.osovskiy.bmwinterface;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.osovskiy.bmwinterface.lib.BusMessage;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 8/16/2014.
 */
public class BusInterfaceWorker extends Thread
{
  private static final int MSG_MIN_SIZE = 5;

  public static final int MSG_NEW_SYNC_STATE = 0;
  public static final int MSG_NEW_MESSAGE = 1;

  private final String TAG = this.getClass().getSimpleName();

  private UsbSerialPort port;
  private byte[] buffer;
  private int tail, head;
  private boolean sync;
  private Handler outputHandler;

  private Handler inputHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      super.handleMessage(msg);
    }
  };

  public BusInterfaceWorker(UsbSerialPort port, Handler handler)
  {
    Log.d(TAG, "Creating WorkerThread");
    this.port = port;
    this.outputHandler = handler;


    buffer = new byte[4096];
    tail = 0;
    head = 0;
    sync = false;
  }

  public Handler getHandler()
  {
    return inputHandler;
  }

  private void setSync(boolean sync)
  {
    Log.d(TAG, "Sync state changed to " + sync);
    this.sync = sync;

    Message msg = outputHandler.obtainMessage();
    msg.arg1 = MSG_NEW_SYNC_STATE;
    msg.arg2 = ( this.sync ) ? 1 : 0;
    outputHandler.sendMessage(msg);
  }

  private void newMessage(BusMessage message)
  {
    Message msg = outputHandler.obtainMessage();
    msg.arg1 = MSG_NEW_MESSAGE;
    msg.arg2 = ( this.sync ) ? 1 : 0;
    outputHandler.sendMessage(msg);
  }

  @Override
  public void run()
  {
    try
    {
      byte[] readBuffer = new byte[2048];
      while ( !Thread.currentThread().isInterrupted() ) // TODO: FIX!
      {
        int bytesRead = port.read(readBuffer, 100);

        if ( bytesRead > ( buffer.length - size() ) )
          throw new BufferOverflowException();

        for ( int i = 0; i < bytesRead; i++ )
        {
          buffer[head] = readBuffer[i];
          head = ( head + 1 ) % buffer.length;
        }

        process();

        /*if ( wri.size() > 0 )
        {
          BusMessage msg = writeQueue.remove();
          port.write(msg.getRaw(), 100);
        }*/
      }
    }
    catch ( IOException e )
    {

    }
    finally
    {
      try
      {
        port.close();
      }
      catch ( IOException e )
      {
        e.printStackTrace();
      }
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
    return buffer[( tail + offset ) % buffer.length];
  }

  /**
   * Process data in a buffer. Process is done when there are less then 5 bytes in buffer,
   * or assumed message length is bigger than available bytes. BAD PRACTICE!
   */
  private void process()
  {
    Log.d(TAG, "Starting processing");
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
            setSync(true);
            truncate(assumedLength);
            newMessage(busMessage);
          }
          else
          {
            setSync(false);
            truncate();
          }
        }
        else
          working = false;
      }
      else
        working = false;
    }
    Log.d(TAG, "Finished processing");
  }

  /**
   * Get number of bytes in buffer
   *
   * @return The number of data bytes in buffer
   */
  private int size()
  {
    return ( buffer.length + head - tail ) % buffer.length;
  }

  /**
   * Truncate single byte. Used when out of sync
   */
  private void truncate()
  {
    truncate(1);
  }

  /**
   * Truncate certain amount of bytes. Used when buffer is in sync and valid message was read
   *
   * @param amount The number of bytes
   */
  private void truncate(int amount)
  {
    tail = ( tail + amount ) % buffer.length;
  }
}
