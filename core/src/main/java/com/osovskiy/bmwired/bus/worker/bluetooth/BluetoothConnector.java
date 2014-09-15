package com.osovskiy.bmwired.bus.worker.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnector
{
  private final String TAG = this.getClass().getSimpleName();

  private BluetoothAdapter adapter;
  private BluetoothDevice device;
  private BluetoothSocketWrapper socket;
  private UUID uuid;
  private boolean forceFallback;

  public void setForceFallback(boolean forceFallback)
  {
    this.forceFallback = forceFallback;
  }

  public BluetoothConnector(BluetoothAdapter adapter, BluetoothDevice device, UUID uuid)
  {
    this.adapter = adapter;
    this.device = device;
    this.uuid = uuid;
  }

  public boolean isConnected()
  {
    return ( socket != null ) && socket.getUnderlyingSocket().isConnected();
  }

  public BluetoothSocketWrapper connect()
  {
    if ( isConnected() )
      return socket;

    adapter.cancelDiscovery();

    try
    {
      socket = new NativeBluetoothSocket(device.createInsecureRfcommSocketToServiceRecord(uuid));
      if (forceFallback)
        socket = new FallbackBluetoothSocket(socket.getUnderlyingSocket());
      socket.connect();
    }
    catch ( IOException e )
    {
      // Connection attempt failed, using fallback
      if (!forceFallback)
      {
        try
        {
          socket = new FallbackBluetoothSocket(socket.getUnderlyingSocket());
          Thread.sleep(500);
          socket.connect();
        }
        catch ( InterruptedException e1 )
        {
          Log.w(TAG, e1.getMessage(), e1);
        }
        catch ( FallbackBluetoothSocket.FallbackException e1 )
        {
          Log.w(TAG, "Could not initialize fallback socket.", e1);
        }
        catch ( IOException e1 )
        {
          Log.w(TAG, "Fallback failed.");
        }
      }
    }
    catch ( FallbackBluetoothSocket.FallbackException e )
    {
      e.printStackTrace();
    }

    if ( !socket.getUnderlyingSocket().isConnected() )
    {
      return null;
    }

    return socket;
  }

  public void close()
  {
    if ( socket != null )
    {
      try
      {
        socket.close();
        socket = null;
      }
      catch ( IOException e )
      {
        e.printStackTrace();
      }
    }
  }
}
