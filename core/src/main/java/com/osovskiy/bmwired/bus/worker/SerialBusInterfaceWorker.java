package com.osovskiy.bmwired.bus.worker;

import android.os.Handler;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.osovskiy.bmwired.bus.BusInterface;
import com.osovskiy.bmwired.lib.BusMessage;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class SerialBusInterfaceWorker extends BusInterfaceWorker
{
  private byte[] readBuffer = new byte[2048];
  private UsbSerialPort port;

  public SerialBusInterfaceWorker(UsbSerialPort port, Handler handler, BusInterface.EventListener el, BlockingQueue<BusMessage> queue)
  {
    super(handler, el, queue);
    this.port = port;
  }

  @Override
  public void read() throws IOException
  {
    int bytesRead = port.read(readBuffer, 100);
    append(readBuffer, bytesRead);
  }

  @Override
  public void write() throws InterruptedException, IOException
  {
    while ( queue.size() > 0 )
    {
      BusMessage msg = queue.take();
      port.write(msg.build(), 0);
    }
  }

  @Override
  public void close() throws Exception
  {
    port.close();
  }
}
