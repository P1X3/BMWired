package com.osovskiy.bmwired.fragment.debugging;

import com.osovskiy.bmwired.lib.BusMessage;

/**
 * Created by Administrator on 8/29/2014.
 */
public class DebugBusMessage extends BusMessage
{
  private String description;

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public DebugBusMessage(BusMessage busMessage)
  {
    super(busMessage.getSource(), busMessage.getDestination(), busMessage.getPayload());
  }

  public DebugBusMessage(BusDevice source, BusDevice destination, byte[] payload)
  {
    super(source, destination, payload);
  }

  @Override
  public String toString()
  {
    return description + ": " + super.toString();
  }
}
