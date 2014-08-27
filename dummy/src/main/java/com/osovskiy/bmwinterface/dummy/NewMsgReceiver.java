package com.osovskiy.bmwinterface.dummy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.osovskiy.bmwinterface.lib.BusMessage;

/**
 * Created by Administrator on 8/22/2014.
 */
public class NewMsgReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    Log.d("TAG", intent.getAction());
    BusMessage msg = intent.getParcelableExtra("msg");
    Toast.makeText(context, "New message received: " + msg.toString(), Toast.LENGTH_SHORT).show();
  }
}
