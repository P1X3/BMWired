package com.osovskiy.bmwinterface.dummy;

import android.app.Activity;
import android.os.Bundle;

import com.osovskiy.bmwinterface.lib.BusMessage;
import com.osovskiy.bmwinterface.lib.Utils;


public class MyActivity extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    BusMessage msg = new BusMessage(BusMessage.BusDevice.Broadcast, BusMessage.BusDevice.CD, new byte[] { 0x00, 0x0C, (byte)0xFB, 0x01 });
    sendBroadcast(msg.getIntent(Utils.ACTION_SEND_BUS_MESSAGE), Utils.PERMISSION_SEND_MESSAGE);
    finish();
  }
}
