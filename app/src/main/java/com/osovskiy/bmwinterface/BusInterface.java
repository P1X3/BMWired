package com.osovskiy.bmwinterface;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Created by Administrator on 8/1/2014.
 */
public class BusInterface
{
  private static final String TAG = BusInterface.class.getSimpleName();

  private Context context;
  private Handler handler;

  private Queue<BusMessage> writeQueue;
  private ProbeTable usbProbeTable;
  private BusInterfaceWorker workerThread;

  private Handler newMessageHandler = new Handler()
  {
    @Override
    public void handleMessage(Message message)
    {
      switch ( message.arg1 )
      {
        case BusInterfaceWorker.MSG_NEW_SYNC_STATE:
          //TODO: Add sync update notification
          break;
        case BusInterfaceWorker.MSG_NEW_MESSAGE:
          BusMessage msg = message.getData().getParcelable("msg");
          List<Class<?>> handlers = handlersMap.get(msg.getSource(), msg.getDestination());
          for (Class<?> clazz : handlers)
          {
            try
            {
              Class<?>[] paramTypes = new Class<?>[] { Object.class };
              Method m = clazz.getMethod("handleBusMessage", paramTypes);
              m.invoke(null, message);
            }
            catch ( NoSuchMethodException e )
            {
              e.printStackTrace();
            }
            catch ( InvocationTargetException e )
            {
              e.printStackTrace();
            }
            catch ( IllegalAccessException e )
            {
              e.printStackTrace();
            }
          }
          break;
      }
    }
  };

  public BusInterface(Context context, Handler handler)
  {
    Log.d(TAG, "Creating BusInterface");
    this.context = context;
    this.handler = handler;

    writeQueue = new ArrayDeque<BusMessage>();

    usbProbeTable = new ProbeTable();
    usbProbeTable.addProduct(0x10C4, 0x8584, Cp21xxSerialDriver.class);
  }

  public void destroy()
  {
    Log.d(TAG, "Destroying BusInterface");
    if (workerThread != null)
      workerThread.interrupt();
    workerThread = null;
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
    Log.d(TAG, "Serial devices available: " + availableDrivers.size());
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
      workerThread = new BusInterfaceWorker(serialPort, newMessageHandler);
      return true;
    }
    return false;
  }

  private static HandlersMap handlersMap = new HandlersMap();
  public static void subscribe(BusMessage.Device source, BusMessage.Device destination, Class<?> clazz)
  {
    Log.i(TAG, "New subscriber"); //TODO: Add more meaningful log message
    handlersMap.subscribe(source, destination, clazz);
  }
}
