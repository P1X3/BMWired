package com.osovskiy.bmwinterface;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.osovskiy.bmwinterface.lib.BusMessage;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class BusInterfaceWorker extends Thread
{
  public static final int MSG_NEW_SYNC_STATE = 0;
  public static final int MSG_NEW_MESSAGE = 1;
  private static final int MSG_MIN_SIZE = 5;
  private final String TAG = this.getClass().getSimpleName();

  private UsbSerialPort port;
  private byte[] buffer;
  private int tail, head;
  private Handler outputHandler;
  private BusInterface.EventListener eventListener;
  private BlockingQueue<BusMessage> queue;

  public BusInterfaceWorker(UsbSerialPort port, Handler handler, BusInterface.EventListener el, BlockingQueue<BusMessage> queue)
  {
    Log.d(TAG, "Creating WorkerThread");
    this.port = port;
    this.outputHandler = handler;
    this.eventListener = el;
    this.queue = queue;

    buffer = new byte[4096];
    tail = 0;
    head = 0;
  }

  private void fireNewSync(final boolean sync)
  {
    Log.d(TAG, "Sync state changed to " + sync);
    if ( eventListener != null && outputHandler != null )
    {
      outputHandler.post(new Runnable()
      {
        @Override
        public void run()
        {
          eventListener.newSync(sync);
        }
      });
    }
  }

  private void fireNewMessage(final BusMessage message)
  {
    if ( eventListener != null && outputHandler != null )
    {
      outputHandler.post(new Runnable()
      {
        @Override
        public void run()
        {
          eventListener.newMessage(message);
        }
      });
    }
  }

  @Override
  public void run()
  {
    try
    {
      byte[] readBuffer = new byte[2048];
      while ( !Thread.currentThread().isInterrupted() )
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

        while ( queue.size() > 0 )
        {
          BusMessage msg = queue.take();
          port.write(msg.build(), 0);
        }
      }
    }
    catch ( IOException e )
    {

    }
    catch ( InterruptedException e )
    {
      e.printStackTrace();
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
            fireNewSync(true);
            truncate(assumedLength);
            fireNewMessage(busMessage);
          }
          else
          {
            fireNewSync(false);
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
