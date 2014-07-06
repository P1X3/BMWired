package com.osovskiy.bmwinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Vadim on 6/29/2014.
 */
public class BMWiBroadcastReceiver extends BroadcastReceiver
{
  private final String TAG = this.getClass().getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent)
  {
    Toast.makeText(context, "Received broadcast: " + intent.getAction(), Toast.LENGTH_SHORT).show();
    Intent serviceIntent = new Intent(context, BMWiService.class);
    context.startService(serviceIntent);
  }
}
