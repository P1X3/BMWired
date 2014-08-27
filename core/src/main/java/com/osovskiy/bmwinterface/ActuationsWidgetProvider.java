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
public class ActuationsWidgetProvider extends AppWidgetProvider
{
  public static final String LOCK_CLICKED = "lockClicked";
  public static final String UNLOCK_CLICKED = "unlockClicked";

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
  {
    Log.d("WidgetProvider", "onUpdate");

    for ( int i = 0; i < appWidgetIds.length; i++ )
    {
      Intent lockIntent = new Intent(context, ActuationsWidgetProvider.class);
      lockIntent.setAction(LOCK_CLICKED);
      Intent unlockIntent = new Intent(context, ActuationsWidgetProvider.class);
      unlockIntent.setAction(UNLOCK_CLICKED);

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.actuations_widget_layout);
      remoteViews.setOnClickPendingIntent(R.id.buttonLockCar, PendingIntent.getBroadcast(context, 0, lockIntent, 0));
      remoteViews.setOnClickPendingIntent(R.id.buttonUnlockCar, PendingIntent.getBroadcast(context, 0, unlockIntent, 0));

      ComponentName componentName = new ComponentName(context, ActuationsWidgetProvider.class);
      AppWidgetManager manager = AppWidgetManager.getInstance(context);
      manager.updateAppWidget(componentName, remoteViews);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent)
  {
    super.onReceive(context, intent);
    if ( intent.getAction() != null )
      Log.d("WidgetProvider", intent.getAction());

    if ( LOCK_CLICKED.equals(intent.getAction()) )
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      serviceIntent.setAction(BMWiService.ACTION_LOCK_CAR);
      context.startService(serviceIntent);
    }
    else if ( UNLOCK_CLICKED.equals(intent.getAction()) )
    {
      Intent serviceIntent = new Intent(context, BMWiService.class);
      serviceIntent.setAction(BMWiService.ACTION_UNLOCK_CAR);
      context.startService(serviceIntent);
    }
  }
}
