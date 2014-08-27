package com.osovskiy.bmwinterface;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.osovskiy.bmwinterface.lib.BusMessage;
import com.osovskiy.bmwinterface.lib.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Vadim on 6/29/2014.
 */
public class BMWiService extends Service
{
  private final String TAG = this.getClass().getSimpleName();

  private final int WIDGET_UPDATE_DELAY = 1 * 1000;

  public static final String ACTION_UPDATE_WIDGET_STATUS = "BMWiService.ACTION_UPDATE_WIDGET_STATUS";
  public static final String ACTION_START_SERVICE = "BMWiService.ACTION_START_SERVICE";
  public static final String ACTION_STOP_SERVICE = "BMWiService.ACTION_STOP_SERVICE";
  public static final String ACTION_LOCK_CAR = "BMWiService.ACTION_LOCK_CAR";
  public static final String ACTION_UNLOCK_CAR = "BMWiService.ACTION_UNLOCK_CAR";
  public static final String EVENT_USB_DEVICE_ATTACHED = "BMWiService.EVENT_USB_DEVICE_ATTACHED";

  private volatile State _state;
  private long _lastWidgetUpdateTime;
  private BusInterface busInterface;

  public BMWiService()
  {
    Log.d(TAG, "Constructor");
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    Log.d(TAG, "onBind");
    return null;
  }

  @Override
  public void onCreate()
  {
    Log.d(TAG, "onCreate");
    _state = State.STARTING;
    _lastWidgetUpdateTime = 0;
    _state = State.IDLING;

    Handler tempHandler = new Handler();
    busInterface = new BusInterface(getApplicationContext(), tempHandler);
    super.onCreate();
  }

  @Override
  public void onDestroy()
  {
    Log.d(TAG, "onDestroy");

    if ( busInterface != null )
      busInterface.destroy();

    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    Log.d(TAG, "onStartCommand");
    if ( intent.getAction() != null )
    {
      Log.d(TAG, "Action: " + intent.getAction());
      if ( intent.getAction().equals(ACTION_UPDATE_WIDGET_STATUS) )
      {
        updateWidgetStatus(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
      }
      else if ( intent.getAction().equals(ACTION_STOP_SERVICE) )
      {
        updateWidgetStatus(1);
        stopSelf();
      }
      else if ( intent.getAction().equals(ACTION_LOCK_CAR) )
      {
        Log.d(TAG, "Broadcasting");
        Intent testIntent = new Intent(Utils.ACTION_NEW_BUS_MESSAGE);
        BusMessage msg = BusMessage.tryParse(new byte[]{ 0x3F, 0x05, 0x00, 0x0C, 0x41, 0x01, 0x76 });
        testIntent.putExtra("msg", msg);
        sendBroadcast(testIntent);
      }
      else if ( intent.getAction().equals(ACTION_UNLOCK_CAR) )
      {

      }
    }

    if ( _state != State.LISTENING )
    {
      if ( busInterface.tryOpen() )
        _state = State.LISTENING;
      else
        _state = State.IDLING;
    }

    return START_STICKY;
  }

  private void updateWidgetStatus(int appWidgetId)
  {
    Log.d(TAG, "updateWidgetStatus");

    if ( ( System.currentTimeMillis() - _lastWidgetUpdateTime ) < WIDGET_UPDATE_DELAY )
      return;

    _lastWidgetUpdateTime = System.currentTimeMillis();

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss a");
    Calendar calendar = Calendar.getInstance();

    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.status_widget_layout);
    remoteViews.setTextViewText(R.id.textServiceStatus, "State: " + String.valueOf(_state));
    remoteViews.setTextViewText(R.id.textUpdateTime, "Updated: " + String.valueOf(sdf.format(calendar.getTime())));

    Log.d(TAG, "updateWidgetStatus:" + sdf.format(calendar.getTime()));

    Intent updateWidgetIntent = new Intent(getApplicationContext(), StatusWidgetProvider.class);
    updateWidgetIntent.setAction(StatusWidgetProvider.UPDATE_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.widgetRelativeLayout, PendingIntent.getBroadcast(getApplicationContext(), 0, updateWidgetIntent, 0));

    Intent startServiceIntent = new Intent(getApplicationContext(), StatusWidgetProvider.class);
    startServiceIntent.setAction(StatusWidgetProvider.START_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonStart, PendingIntent.getBroadcast(getApplicationContext(), 0, startServiceIntent, 0));

    Intent stopServiceIntent = new Intent(getApplicationContext(), StatusWidgetProvider.class);
    stopServiceIntent.setAction(StatusWidgetProvider.STOP_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonStop, PendingIntent.getBroadcast(getApplicationContext(), 0, stopServiceIntent, 0));

    ComponentName componentName = new ComponentName(getApplicationContext(), StatusWidgetProvider.class);
    AppWidgetManager manager = AppWidgetManager.getInstance(this);
    manager.updateAppWidget(componentName, remoteViews);
  }

  private enum State
  {
    STARTING,
    IDLING,
    LISTENING
  }
}
