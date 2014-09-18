package com.osovskiy.bmwired.bus.worker;

import android.os.Handler;

import com.osovskiy.bmwired.bus.BusInterface;
import com.osovskiy.bmwired.lib.BusMessage;

import java.util.concurrent.BlockingQueue;

public class IOIOBusInterfaceWorker extends BusInterfaceWorker
{
  public IOIOBusInterfaceWorker(Handler handler, BusInterface.EventListener el, BlockingQueue<BusMessage> queue)
  {
    super(handler, el, queue);
  }

  @Override
  public void read() throws Exception
  {

  }

  @Override
  public void write() throws Exception
  {

  }

  @Override
  public void close() throws Exception
  {

  }
}
