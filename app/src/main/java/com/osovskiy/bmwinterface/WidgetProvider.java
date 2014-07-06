package com.osovskiy.bmwinterface;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by Vadim on 6/30/2014.
 */
public class WidgetProvider extends AppWidgetProvider
{
  public static final String START_CLICKED = "startClicked";
  public static final String STOP_CLICKED = "stopClicked";
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
  {
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    ComponentName widget = new ComponentName(context, WidgetProvider.class);

    Intent startServiceIntent = new Intent(context,getClass());
    startServiceIntent.setAction(START_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonStart, PendingIntent.getBroadcast(context, 0, startServiceIntent, 0));

    Intent stopServiceIntent = new Intent(context,getClass());
    stopServiceIntent.setAction(STOP_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonStop, PendingIntent.getBroadcast(context, 0, stopServiceIntent, 0));

    appWidgetManager.updateAppWidget(widget, remoteViews);

    Intent updateIntent = new Intent(context, BMWiService.class);
    updateIntent.setAction(BMWiService.ACTION_UPDATE_WIDGET);
    context.startService(updateIntent);
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
      context.startService(serviceIntent);
    }
    else if (STOP_CLICKED.equals(intent.getAction()))
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      serviceIntent.setAction(BMWiService.ACTION_STOP_SERVICE);
      context.startService(serviceIntent);
    }
  }
}
