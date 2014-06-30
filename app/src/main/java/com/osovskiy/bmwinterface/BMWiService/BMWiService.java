package com.osovskiy.bmwinterface.BMWiService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Vadim on 6/29/2014.
 */
public class BMWiService extends Service
{
  private final String TAG = this.getClass().getSimpleName();

  private volatile State _state;
  private volatile Thread _workerThread;
  private UsbManager _usbManager;
  private volatile MessageProcessor _messageProcessor;

  public BMWiService()
  {

  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  @Override
  public void onCreate()
  {
    _state = State.STARTING;
    _usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    _messageProcessor = new MessageProcessor();
    _messageProcessor.setEventListener(messageProcessorListener);

    super.onCreate();
  }

  @Override
  public void onDestroy()
  {
    if (_workerThread != null)
      _workerThread.interrupt();

    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    if (_state == State.STARTING || _state == State.IDLING)
    {
      List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(_usbManager);

      if (availableDrivers.size() > 0)
      {
        for (Iterator<UsbSerialDriver> iterator = availableDrivers.iterator(); iterator.hasNext(); )
        {
          UsbSerialDriver driver = iterator.next();
          int vendorId = driver.getDevice().getVendorId();
          int productId = driver.getDevice().getProductId();

          if (vendorId == 4292 && productId == 60000)
          {
            _workerThread = new ListeningThread(driver);
            break;
          }
        }
      }
    }

    return START_STICKY;
  }

  MessageProcessor.EventListener messageProcessorListener = new MessageProcessor.EventListener()
  {
    @Override
    public void newMessage(BusMessage message)
    {
      if (message.getType() != null)
      {
        Toast.makeText(getApplicationContext(), "Received " + (BusMessage.Type.valueOf(message.getType().name())) + " message", Toast.LENGTH_SHORT).show();
      }
    }
  };

  private class ListeningThread extends Thread
  {
    private UsbSerialDriver _driver;

    public ListeningThread(UsbSerialDriver driver)
    {
      _state = com.osovskiy.bmwinterface.BMWiService.State.LISTENING;
      _driver = driver;
    }

    public void run()
    {
      UsbDeviceConnection connection = null;
      UsbSerialPort serialPort = null;
      try
      {
        connection = _usbManager.openDevice(_driver.getDevice());
        serialPort = _driver.getPorts().get(0);

        if (connection == null)
        {

        }

        serialPort.open(connection);
        serialPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_EVEN);

        byte buffer[] = new byte[1024];
        while (!Thread.currentThread().isInterrupted())
        {
          int bytesRead = serialPort.read(buffer, 100);
          _messageProcessor.appendBuffer(Arrays.copyOf(buffer, bytesRead));
          _messageProcessor.process();
          Log.d(TAG, "Read " + bytesRead + " byte(s)");
        }

        serialPort.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        if (serialPort != null)
          try
          {
            serialPort.close();
          } catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    }
  }
}
