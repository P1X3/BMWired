package com.osovskiy.bmwinterface;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Vadim on 6/30/2014.
 */
public class StatusWidgetProvider extends AppWidgetProvider
{
  public static final String START_CLICKED = "startClicked";
  public static final String STOP_CLICKED = "stopClicked";
  public static final String UPDATE_CLICKED = "updateClicked";
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
  {
    Log.d("WidgetProvider", "onUpdate");

    for (int i = 0; i < appWidgetIds.length; i++)
    {
      Intent intent = new Intent(context, BMWiService.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
      intent.setAction(BMWiService.ACTION_UPDATE_WIDGET_STATUS);
      context.startService(intent);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent)
  {
    super.onReceive(context, intent);
    if (intent.getAction() != null)
      Log.d("WidgetProvider", intent.getAction());

    if (START_CLICKED.equals(intent.getAction()))
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      serviceIntent.setAction(BMWiService.ACTION_START_SERVICE);
      context.startService(serviceIntent);
    }
    else if (STOP_CLICKED.equals(intent.getAction()))
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      serviceIntent.setAction(BMWiService.ACTION_STOP_SERVICE);
      context.startService(serviceIntent);
    }
    else if (UPDATE_CLICKED.equals(intent.getAction()))
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      serviceIntent.setAction(BMWiService.ACTION_UPDATE_WIDGET_STATUS);
      context.startService(serviceIntent);
    }
  }
}
