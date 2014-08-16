package com.osovskiy.bmwinterface;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

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
  private AudioManager _audioManager;
  private long _lastWidgetUpdateTime;
  private BusInterface busInterface;

  // Dummy test variables
  private int messageCount = 0;

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

    _audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    _state = State.IDLING;

    Handler tempHandler = new Handler();
    busInterface = new BusInterface(getApplicationContext(), tempHandler);
    busInterface.addEventListener(busInterfaceListener);
    super.onCreate();
  }

  @Override
  public void onDestroy()
  {
    Log.d(TAG, "onDestroy");
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
      else if (intent.getAction().equals(ACTION_LOCK_CAR))
      {
        BusMessage msg = new BusMessage(BusMessage.Type.GENERAL_LOCK_ALL);
        busInterface.send(msg);
      }
      else if (intent.getAction().equals(ACTION_UNLOCK_CAR))
      {
        BusMessage msg = new BusMessage(BusMessage.Type.GENERAL_UNLOCK_ALL);
        busInterface.send(msg);
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
    remoteViews.setTextViewText(R.id.textMessagesCount, "Msg: " + String.valueOf(messageCount));
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

  BusInterface.EventListener busInterfaceListener = new BusInterface.EventListener()
  {
    @Override
    public void newMessage(BusMessage message)
    {
      messageCount++;
      Log.d(TAG, message.toString());
      if ( message.getType() != null )
      {
        if ( message.getType() == BusMessage.Type.MFSW_VOLUME_UP )
        {
          _audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
        else if ( message.getType() == BusMessage.Type.MFSW_VOLUME_DOWN )
        {
          _audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        }
        else if ( message.getType() == BusMessage.Type.MFSW_NEXT_PRESSED )
        {
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          synchronized ( this )
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            sendOrderedBroadcast(mediaIntent, null);
          }
        }
        else if ( message.getType() == BusMessage.Type.MFSW_NEXT_RELEASED )
        {
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          synchronized ( this )
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
            sendOrderedBroadcast(mediaIntent, null);
          }
        }
        else if ( message.getType() == BusMessage.Type.MFSW_PREVIOUS_PRESSED )
        {
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          synchronized ( this )
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            sendOrderedBroadcast(mediaIntent, null);
          }
        }
        else if ( message.getType() == BusMessage.Type.MFSW_PREVIOUS_RELEASED )
        {
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          synchronized ( this )
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            sendOrderedBroadcast(mediaIntent, null);
          }
        }
        else if ( message.getType() == BusMessage.Type.MFSW_DIAL_PRESSED )
        {
          // TODO: Call Google Now voice intent
        }
      }
      updateWidgetStatus(0);
    }

    @Override
    public void newSyncState(boolean sync)
    {
      Toast.makeText(getApplicationContext(), "Sync state changed to " + sync, Toast.LENGTH_SHORT).show();
    }
  };

  private enum State
  {
    STARTING,
    IDLING,
    LISTENING
  }
}
