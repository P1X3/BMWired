package com.osovskiy.bmwinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.osovskiy.bmwinterface.lib.Utils;

/**
 * Created by Vadim on 6/29/2014.
 */
public class BMWiReceiver extends BroadcastReceiver
{
  private final String TAG = this.getClass().getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent)
  {
    String action = intent.getAction();

    if (action != null)
    {
      if (action.equals(Utils.ACTION_SEND_BUS_MESSAGE))
      {
        Toast.makeText(context, "Received msg from app" ,Toast.LENGTH_SHORT).show();
        return;
      }
    }
    Toast.makeText(context, "Received broadcast: " + intent.getAction(), Toast.LENGTH_SHORT).show();
    Intent serviceIntent = new Intent(context, BMWiService.class);
    context.startService(serviceIntent);
  }
}
