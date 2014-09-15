package com.osovskiy.bmwired.bus.worker.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeBluetoothSocket implements BluetoothSocketWrapper
{
  private final String TAG = this.getClass().getSimpleName();

  private BluetoothSocket socket;

  public NativeBluetoothSocket(BluetoothSocket socket)
  {
    this.socket = socket;
  }

  @Override
  public InputStream getIS() throws IOException
  {
    return socket.getInputStream();
  }

  @Override
  public OutputStream getOS() throws IOException
  {
    return socket.getOutputStream();
  }

  @Override
  public String getRemoveDeviceName()
  {
    return socket.getRemoteDevice().getName();
  }

  @Override
  public void connect() throws IOException
  {
    Log.d(TAG, "Connecting");
    socket.connect();
  }

  @Override
  public String getRemoveDeviceAddress()
  {
    return null;
  }

  @Override
  public void close() throws IOException
  {
    Log.d(TAG, "Closing");
    getIS().close();
    getOS().flush();
    getOS().close();
    socket.close();
  }

  @Override
  public BluetoothSocket getUnderlyingSocket()
  {
    return socket;
  }
}
