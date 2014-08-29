package com.osovskiy.bmwinterface;

import android.os.Handler;
import android.test.AndroidTestCase;

import com.osovskiy.bmwinterface.lib.BusMessage;

public class BusInterfaceTest extends AndroidTestCase
{
  public void testCreate()
  {
    BusInterface.EventListener eventListener = new BusInterface.EventListener()
    {
      @Override
      public void newMessage(BusMessage message)
      {

      }

      @Override
      public void newSync(boolean sync)
      {

      }
    };

    BusInterface busInterface = new BusInterface(getContext(), new Handler(), eventListener);
    busInterface.destroy();
  }

  public void testCreateAndOpenFalse()
  {
    BusInterface.EventListener eventListener = new BusInterface.EventListener()
    {
      @Override
      public void newMessage(BusMessage message)
      {

      }

      @Override
      public void newSync(boolean sync)
      {

      }
    };

    BusInterface busInterface = new BusInterface(getContext(), new Handler(), eventListener);
    assertFalse(busInterface.tryOpen());
    busInterface.destroy();
  }
}