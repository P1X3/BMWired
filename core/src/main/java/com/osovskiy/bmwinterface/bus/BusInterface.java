package com.osovskiy.bmwinterface.bus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.osovskiy.bmwinterface.bus.worker.BluetoothBusInterfaceWorker;
import com.osovskiy.bmwinterface.bus.worker.BusInterfaceWorker;
import com.osovskiy.bmwinterface.bus.worker.SerialBusInterfaceWorker;
import com.osovskiy.bmwinterface.bus.worker.bluetooth.BluetoothConnector;
import com.osovskiy.bmwinterface.bus.worker.bluetooth.BluetoothSocketWrapper;
import com.osovskiy.bmwinterface.lib.BusMessage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 8/1/2014.
 */
public class BusInterface
{
  private static final String TAG = BusInterface.class.getSimpleName();

  private Context context;

  private ProbeTable usbProbeTable;
  private BusInterfaceWorker workerThread;
  private EventListener eventListener;
  private BlockingQueue<BusMessage> queue = new LinkedBlockingQueue<>();
  private AsyncTask<Void, String, BluetoothSocketWrapper> bluetoothConnectTask;

  public BusInterface(Context context, Handler handler, EventListener el)
  {
    Log.d(TAG, "Creating BusInterface");
    this.context = context;

    usbProbeTable = new ProbeTable();
    usbProbeTable.addProduct(0x10C4, 0x8584, Cp21xxSerialDriver.class);

    eventListener = el;
  }

  public void destroy()
  {
    Log.d(TAG, "Destroying BusInterface");
    if ( workerThread != null )
      workerThread.interrupt();
    workerThread = null;

    if (bluetoothConnectTask != null)
      bluetoothConnectTask.cancel(true);
    bluetoothConnectTask = null;
  }

  public Type tryOpen(Type type)
  {
    if ( workerThread != null )
    {
      if ( workerThread instanceof BluetoothBusInterfaceWorker )
        return Type.BLUETOOTH;
      else if ( workerThread instanceof SerialBusInterfaceWorker )
        return Type.SERIAL;
    }

    switch ( type )
    {
      case BLUETOOTH:
      {
        Log.d(TAG, "Attempting to open bluetooth device");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothConnector connector = new BluetoothConnector(adapter, adapter.getRemoteDevice("00:14:01:02:30:79"), UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

        bluetoothConnectTask = new AsyncTask<Void, String, BluetoothSocketWrapper>()
        {
          @Override
          protected BluetoothSocketWrapper doInBackground(Void... params)
          {
            BluetoothSocketWrapper socket;
            while ((socket = connector.connect()) == null && !isCancelled())
            {
              try
              {
                Thread.currentThread().sleep(2*1000);
              }
              catch ( InterruptedException e )
              {
                e.printStackTrace();
              }
            }
            return socket;
          }

          @Override
          protected void onPostExecute(BluetoothSocketWrapper socket)
          {
            if (!isCancelled())
              workerThread = new BluetoothBusInterfaceWorker(socket, new Handler(), eventListener, queue);
          }
        };
        bluetoothConnectTask.execute();
      } break;
      case SERIAL:
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
            return null;
          }
          Log.d(TAG, "Starting serial worker thread");
          workerThread = new SerialBusInterfaceWorker(serialPort, new Handler(), eventListener, queue);
        }
      } break;
    }
    return null;
  }

  public void sendMsg(BusMessage message)
  {
    queue.add(message);
  }

  public interface EventListener
  {
    void newMessage(BusMessage message);

    void newSync(boolean sync);
  }

  public enum Type
  {
    SERIAL,
    BLUETOOTH
  }
}
