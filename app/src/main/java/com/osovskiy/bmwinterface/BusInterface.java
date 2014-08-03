package com.osovskiy.bmwinterface;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Administrator on 8/1/2014.
 */
public class BusInterface
{
  private final String TAG = this.getClass().getSimpleName();

  private static final int MSG_MIN_SIZE = 5;

  private Context context;
  private Handler handler;

  private List<EventListener> eventListeners;
  private Queue<BusMessage> writeQueue;
  private ProbeTable usbProbeTable;
  private WorkerThread workerThread;

  public BusInterface(Context context, Handler handler)
  {
    Log.d(TAG, "Creating BusInterface");
    this.context = context;
    this.handler = handler;

    eventListeners = new LinkedList<EventListener>();
    writeQueue = new ArrayDeque<BusMessage>();

    usbProbeTable = new ProbeTable();
    usbProbeTable.addProduct(0x10C4, 0x8584, Cp21xxSerialDriver.class);
  }

  public void addEventListener(EventListener eventListener)
  {
    Log.d(TAG, "Adding new event listener");
    eventListeners.add(eventListener);
  }

  public void destroy()
  {
    Log.d(TAG, "Destroying BusInterface");
    if (workerThread != null)
      workerThread.interrupt();
    workerThread = null;


  }

  public boolean removeEventListener(EventListener eventListener)
  {
    Log.d(TAG, "Removing event listener");
    return eventListeners.remove(eventListener);
  }

  public void send(BusMessage message)
  {
    Log.d(TAG, "Adding new message to write buffer: " + message.toString());
    writeQueue.add(message);
  }

  public boolean tryOpen()
  {
    Log.d(TAG, "Attempting to open serial device");
    UsbSerialProber prober = new UsbSerialProber(usbProbeTable);
    UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

    List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
    Log.d(TAG, "Serial devices available: " + availableDrivers);
    if ( availableDrivers.size() > 0 )
    {
      UsbSerialPort serialPort;
      try
      {
        UsbSerialDriver driver = availableDrivers.get(0);
        Log.d(TAG, "Opening device " + driver.getDevice().getDeviceName());

        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        serialPort = driver.getPorts().get(0);

        if ( serialPort == null )
          throw new IOException();

        serialPort.open(connection);
        serialPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_EVEN);
      }
      catch ( IOException e )
      {
        Log.d(TAG, e.getMessage());
        return false;
      }
      Log.d(TAG, "Starting worker thread");
      workerThread = new WorkerThread(serialPort);
      return true;
    }
    return false;
  }

  private void fireNewMessage(final BusMessage message)
  {
    Log.d(TAG, "Firing new message event");
    if ( handler == null )
      return;

    handler.post(new Runnable()
    {
      @Override
      public void run() // runs on service thread
      {
        for ( EventListener el : eventListeners )
        {
          el.newMessage(message);
        }
      }
    });
  }

  private void fireNewSync(final boolean sync)
  {
    Log.d(TAG, "Firing new sync state event");
    if ( handler == null )
      return;

    handler.post(new Runnable()
    {
      @Override
      public void run() // runs on service thread
      {
        for ( EventListener el : eventListeners )
        {
          el.newSyncState(sync);
        }
      }
    });
  }

  public interface EventListener
  {
    void newMessage(BusMessage message);

    void newSyncState(boolean sync);
  }

  private class WorkerThread extends Thread
  {
    private UsbSerialPort port;
    private byte[] buffer;
    private int tail, head;
    private boolean sync;

    public WorkerThread(UsbSerialPort port)
    {
      Log.d(TAG, "Creating WorkerThread");
      this.port = port;

      buffer = new byte[4096];
      tail = 0;
      head = 0;
      sync = false;
    }

    private void setSync(boolean sync)
    {
      Log.d(TAG, "Sync state changed to " + sync);
      this.sync = sync;
      fireNewSync(sync);
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

          if (workerThread.size() > 0)
          {
            BusMessage msg = writeQueue.remove();
            port.write(msg.getRaw(), 100);
          }
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
              fireNewMessage(busMessage);
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
}
