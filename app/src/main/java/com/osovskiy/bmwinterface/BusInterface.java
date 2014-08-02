package com.osovskiy.bmwinterface;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 8/1/2014.
 */
public class BusInterface
{
  private static final int MSG_MIN_SIZE = 5;

  private Context context;
  private Handler handler;

  private List<EventListener> eventListeners;
  private ProbeTable usbProbeTable;
  private WorkerThread workerThread;

  public BusInterface(Context context, Handler handler)
  {
    this.context = context;
    this.handler = handler;

    eventListeners = new LinkedList<EventListener>();

    usbProbeTable = new ProbeTable();
    usbProbeTable.addProduct(0x10C4, 0x8584, Cp21xxSerialDriver.class);
  }

  public void addEventListener(EventListener eventListener)
  {
    eventListeners.add(eventListener);
  }

  public boolean removeEventListener(EventListener eventListener)
  {
    return eventListeners.remove(eventListener);
  }

  public boolean tryOpen()
  {
    UsbSerialProber prober = new UsbSerialProber(usbProbeTable);
    UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

    List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
    if ( availableDrivers.size() > 0 )
    {
      UsbSerialPort serialPort;
      try
      {
        UsbSerialDriver driver = availableDrivers.get(0);
        //int vendorId = driver.getDevice().getVendorId();
        //int productId = driver.getDevice().getProductId();

        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        serialPort = driver.getPorts().get(0);

        if ( serialPort == null )
          throw new IOException();

        serialPort.open(connection);
        serialPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_EVEN);
      }
      catch ( IOException e )
      {
        return false;
      }
      workerThread = new WorkerThread(serialPort);
      return true;
    }
    return false;
  }

  private void fireNewMessage(final BusMessage message)
  {
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
      this.port = port;

      buffer = new byte[4096];
      tail = 0;
      head = 0;
      sync = false;
    }

    private void setSync(boolean sync)
    {
      this.sync = sync;
      fireNewSync(sync);
    }

    @Override
    public void run()
    {
      try
      {
        byte[] readBuffer = new byte[2048];
        while ( true ) // TODO: FIX!
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
