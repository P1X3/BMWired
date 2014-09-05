package com.osovskiy.bmwinterface.bus.worker;

import android.os.Handler;

import com.osovskiy.bmwinterface.bus.BusInterface;
import com.osovskiy.bmwinterface.bus.worker.bluetooth.BluetoothSocketWrapper;
import com.osovskiy.bmwinterface.lib.BusMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class BluetoothBusInterfaceWorker extends BusInterfaceWorker
{
  private BluetoothSocketWrapper socket;
  private InputStream inputStream;
  private OutputStream outputStream;
  private byte[] readBuffer = new byte[2048];

  public BluetoothBusInterfaceWorker(BluetoothSocketWrapper socket, Handler handler, BusInterface.EventListener el, BlockingQueue<BusMessage> queue)
  {
    super(handler, el, queue);
    this.socket = socket;
    try
    {
      this.inputStream = this.socket.getIS();
      this.outputStream = this.socket.getOS();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void read() throws IOException
  {
    int bytesRead = inputStream.read(readBuffer);
    append(readBuffer, bytesRead);
  }

  @Override
  public void write() throws InterruptedException, IOException
  {
    BusMessage msg = queue.take();
    outputStream.write(msg.build());
    outputStream.flush();
  }

  @Override
  public void close() throws Exception
  {
    outputStream.flush();
    outputStream.close();
    inputStream.close();
    socket.close();
  }
}
