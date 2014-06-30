package com.osovskiy.bmwinterface;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.osovskiy.bmwinterface.BMWiService.BMWiService;

/**
 * Created by Vadim on 6/30/2014.
 */
public class WidgetProvider extends AppWidgetProvider
{
  private static final String START_CLICKED = "startClicked";
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
  {
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    ComponentName widget = new ComponentName(context, WidgetProvider.class);

    Intent startServiceIntent = new Intent(context,getClass());
    startServiceIntent.setAction(START_CLICKED);

    remoteViews.setOnClickPendingIntent(R.id.button, PendingIntent.getBroadcast(context, 0, startServiceIntent, 0));

    appWidgetManager.updateAppWidget(widget, remoteViews);
  }

  @Override
  public void onReceive(Context context, Intent intent)
  {
    super.onReceive(context, intent);

    if (START_CLICKED.equals(intent.getAction()))
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      context.startService(serviceIntent);
    }
  }
}
