package com.osovskiy.bmwired.bus.worker.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class FallbackBluetoothSocket extends NativeBluetoothSocket
{
  private final String TAG = this.getClass().getSimpleName();

  private BluetoothSocket fallbackSocket;

  public FallbackBluetoothSocket(BluetoothSocket temp) throws FallbackException
  {
    super(temp);
    try
    {
      Class<?> cls = temp.getRemoteDevice().getClass();
      Class<?>[] paramTypes = new Class<?>[]{ Integer.TYPE };

      Method m = cls.getMethod("createRfcommSocket", paramTypes);
      Object[] params = new Object[]{ 1 };
      fallbackSocket = (BluetoothSocket) m.invoke(temp.getRemoteDevice(), params);
    }
    catch ( Exception e )
    {
      throw new FallbackException(e);
    }

  }

  @Override
  public InputStream getIS() throws IOException
  {
    return fallbackSocket.getInputStream();
  }

  @Override
  public OutputStream getOS() throws IOException
  {
    return fallbackSocket.getOutputStream();
  }

  @Override
  public void connect() throws IOException
  {
    Log.d(TAG, "Connecting");
    fallbackSocket.connect();
  }

  @Override
  public void close() throws IOException
  {
    Log.d(TAG, "Closing");
    getIS().close();
    getOS().flush();
    getOS().close();
    fallbackSocket.close();
    super.close();
  }

  @Override
  public BluetoothSocket getUnderlyingSocket()
  {
    return fallbackSocket;
  }

  public static class FallbackException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public FallbackException(Exception e)
    {
      super(e);
    }
  }
}
