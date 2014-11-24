package com.osovskiy.bmwired.bus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.osovskiy.bmwired.R;
import com.osovskiy.bmwired.bus.worker.BluetoothBusInterfaceWorker;
import com.osovskiy.bmwired.bus.worker.bluetooth.BluetoothConnector;
import com.osovskiy.bmwired.bus.worker.bluetooth.BluetoothSocketWrapper;
import com.osovskiy.bmwired.utils.PreferencesWrapper;

import java.util.UUID;

public class BluetoothBusInterface extends BusInterface
{
  private static final String TAG = BluetoothBusInterface.class.getSimpleName();
  private Preferences preferences;

  private AsyncTask<Void, String, BluetoothSocketWrapper> bluetoothConnectTask;

  public BluetoothBusInterface(Context context, EventListener el)
  {
    super(context, el);
    preferences = new Preferences(context);
  }

  @Override
  public void open()
  {
    String selectedBluetooth = preferences.bluetoothDeviceMac();

    if ( selectedBluetooth != null )
    {
      Log.d(TAG, "Attempting to open bluetooth device " + selectedBluetooth);
      BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
      final BluetoothConnector connector = new BluetoothConnector(adapter, adapter.getRemoteDevice(selectedBluetooth), UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

      bluetoothConnectTask = new AsyncTask<Void, String, BluetoothSocketWrapper>()
      {
        @Override
        protected BluetoothSocketWrapper doInBackground(Void... params)
        {
          BluetoothSocketWrapper socket;
          while ( ( socket = connector.connect() ) == null && !isCancelled() )
          {
            try
            {
              Thread.sleep(2 * 1000);
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
          if ( !isCancelled() )
            workerThread = new BluetoothBusInterfaceWorker(socket, new Handler(), eventListener, queue);
        }
      };
      bluetoothConnectTask.execute();
    }
  }

  @Override
  public void close()
  {
    if (bluetoothConnectTask != null)
    {
      bluetoothConnectTask.cancel(true); // TODO: Does not work
    }
    bluetoothConnectTask = null;
  }

  private static class Preferences extends PreferencesWrapper
  {
    protected Preferences(Context context)
    {
      super(context);
    }

    public String bluetoothDeviceMac()
    {
      return sharedPreferences.getString(context.getString(R.string.preference_bluetooth_mac_key), context.getString(R.string.preference_bluetooth_mac_default));
    }
  }
}
