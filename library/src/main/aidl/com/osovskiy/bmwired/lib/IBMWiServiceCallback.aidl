// IBMWiServiceCallback.aidl
package com.osovskiy.bmwired.lib;

import com.osovskiy.bmwired.lib.BusMessage;

interface IBMWiServiceCallback
{
  void newMessageFromBus(in BusMessage msg);
}
