package com.osovskiy.bmwired.bus;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.osovskiy.bmwired.R;
import com.osovskiy.bmwired.bus.worker.SerialBusInterfaceWorker;

import java.io.IOException;
import java.util.List;

public class SerialBusInterface extends BusInterface
{
  private static final String TAG = SerialBusInterface.class.getSimpleName();

  public SerialBusInterface(Context context, EventListener el)
  {
    super(context, el);
  }

  @Override
  public void open()
  {
    String selectedDevice = preferences.getString(context.getString(R.string.preference_key_serial_name), null);
    int selectedPort = Integer.valueOf(preferences.getString(context.getString(R.string.preference_key_serial_port), "0"));

    Log.d(TAG, "Opening " + selectedDevice + ", port " + selectedPort);

    if (selectedDevice != null)
    {
      UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
      List<UsbSerialDriver> availableDriver = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

      for (UsbSerialDriver driver : availableDriver)
      {
        if (driver.getDevice().getDeviceName().equals(selectedDevice))
        {
          try
          {
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            UsbSerialPort serialPort = driver.getPorts().get(selectedPort);

            if (serialPort == null)
            {
              throw new IOException();
            }

            serialPort.open(connection);
            serialPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_EVEN);

            workerThread = new SerialBusInterfaceWorker(serialPort, new Handler(), eventListener, queue);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }

  @Override
  public void close()
  {

  }
}
