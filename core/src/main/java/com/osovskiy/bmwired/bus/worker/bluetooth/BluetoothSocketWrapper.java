package com.osovskiy.bmwired.bus.worker.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BluetoothSocketWrapper
{
  InputStream getIS() throws IOException;

  OutputStream getOS() throws IOException;

  String getRemoveDeviceName();

  void connect() throws IOException;

  String getRemoveDeviceAddress();

  void close() throws IOException;

  BluetoothSocket getUnderlyingSocket();
}
