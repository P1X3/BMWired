package com.osovskiy.bmwinterface;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 8/16/2014.
 */
public class MediaControlsSystem extends BusSystem
{
  public static enum Event
  {
    VOLUME_UP,
    VOLUME_DOWN,
    NEXT_PRESS,
    NEXT_HOLD,
    NEXT_RELEASE,
    PREVIOUS_PRESS,
    PREVIOUS_HOLD,
    PREVIOUS_RELEASE
  }

  private final static Handler handler = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      super.handleMessage(msg);
    }
  };

  static
  {
    BusInterface.subscribe(BusMessage.Device.MFL, BusMessage.Device.RAD, MediaControlsSystem.class);
  }

  @Override
  public void handleBusMessage(BusMessage message)
  {

  }
}
