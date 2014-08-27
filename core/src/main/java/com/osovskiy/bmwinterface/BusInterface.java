package com.osovskiy.bmwinterface;

import android.content.Context;
import android.content.Intent;
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
import com.osovskiy.bmwinterface.lib.BusMessage;
import com.osovskiy.bmwinterface.lib.Utils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Created by Administrator on 8/1/2014.
 */
public class BusInterface
{
  private static final String TAG = BusInterface.class.getSimpleName();

  private Context context;

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
          boolean sync = ( message.arg2 == 1 );

          break;
        case BusInterfaceWorker.MSG_NEW_MESSAGE:
          BusMessage msg = message.getData().getParcelable("msg");
          Intent newMsgIntent = new Intent(Utils.ACTION_NEW_BUS_MESSAGE);
          newMsgIntent.putExtra(BusMessage.class.getCanonicalName(), msg);
          context.sendBroadcast(newMsgIntent, Utils.PERMISSION_RECEIVE_MESSAGE);
          break;
      }
    }
  };

  public BusInterface(Context context, Handler handler)
  {
    Log.d(TAG, "Creating BusInterface");
    this.context = context;

    writeQueue = new ArrayDeque<BusMessage>();

    usbProbeTable = new ProbeTable();
    usbProbeTable.addProduct(0x10C4, 0x8584, Cp21xxSerialDriver.class);
  }

  public void destroy()
  {
    Log.d(TAG, "Destroying BusInterface");
    if ( workerThread != null )
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
}
