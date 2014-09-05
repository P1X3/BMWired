package com.osovskiy.bmwinterface.bus.worker;

import android.os.Handler;
import android.util.Log;

import com.osovskiy.bmwinterface.bus.BusInterface;
import com.osovskiy.bmwinterface.lib.BusMessage;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public abstract class BusInterfaceWorker extends Thread
{
  public static final int MSG_NEW_SYNC_STATE = 0;
  public static final int MSG_NEW_MESSAGE = 1;
  private static final int MSG_MIN_SIZE = 5;
  private final String TAG = this.getClass().getSimpleName();

  private byte[] buffer;
  private int tail, head;
  private Handler outputHandler;
  private BusInterface.EventListener eventListener;
  protected BlockingQueue<BusMessage> queue;

  public BusInterfaceWorker(Handler handler, BusInterface.EventListener el, BlockingQueue<BusMessage> queue)
  {
    Log.d(TAG, "Creating WorkerThread");
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

  private void fireWorkerClosing(final BusInterface.EventListener.ClosingReason closingReason)
  {
    if ( eventListener != null && outputHandler != null )
    {
      outputHandler.post(new Runnable()
      {
        @Override
        public void run()
        {
          eventListener.workerClosing(closingReason);
        }
      });
    }
  }

  /**
   * Implementation to read data from interface
   * @throws Exception
   */
  public abstract void read() throws Exception;

  /**
   * Implementation to write pending BusMessages
   * @throws Exception
   */
  public abstract void write() throws Exception;

  /**
   * Implementation to close and release any used resources
   * @throws Exception
   */
  public abstract void close() throws Exception;

  @Override
  public void run()
  {
    try
    {
      while ( !Thread.currentThread().isInterrupted() )
      {
        read();
        process();
        write();
      }

      close();
      fireWorkerClosing(BusInterface.EventListener.ClosingReason.Normal);
    }
    catch ( Exception e )
    {
      fireWorkerClosing(BusInterface.EventListener.ClosingReason.NonRecoverableError);
      e.printStackTrace();
    }
  }

  /**
   * Append data to the buffer
   * @param data Buffer with data to be appended
   * @param size Amount of data in buffer
   */
  protected void append(byte[] data, int size)
  {
    for ( int i = 0; i < size; i++ )
    {
      buffer[head] = data[i];
      head = ( head + 1 ) % buffer.length;
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
