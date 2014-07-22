package com.osovskiy.bmwinterface;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;

import com.hoho.android.usbserial.driver.Cp21xxSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Vadim on 6/29/2014.
 */
public class BMWiService extends Service
{
  private final String TAG = this.getClass().getSimpleName();

  private final int WIDGET_UPDATE_DELAY = 1*1000;

  public static final String ACTION_UPDATE_WIDGET = "BMWiService.ACTION_UPDATE_WIDGET";
  public static final String ACTION_START_SERVICE = "BMWiService.ACTION_START_SERVICE";
  public static final String ACTION_STOP_SERVICE = "BMWiService.ACTION_STOP_SERVICE";
  public static final String EVENT_USB_DEVICE_ATTACHED = "BMWiService.EVENT_USB_DEVICE_ATTACHED";

  private volatile State _state;
  private volatile Thread _workerThread;
  private UsbManager _usbManager;
  private volatile MessageProcessor _messageProcessor;
  private ProbeTable _usbProbeTable;
  private AudioManager _audioManager;
  private Handler _messageProcessorHandler;
  private long _lastWidgetUpdateTime;

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

    _messageProcessorHandler = new Handler();

    _usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
      if (_usbManager == null)
          Log.d(TAG, "usbmanger null");
    _messageProcessor = new MessageProcessor();
    _messageProcessor.addEventListener(messageProcessorListener);
    _messageProcessor.setServiceHandler(_messageProcessorHandler);

    _audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

    _usbProbeTable = new ProbeTable();
    _usbProbeTable.addProduct(0x10C4, 0x8584, Cp21xxSerialDriver.class);

    _state = State.IDLING;
    super.onCreate();
  }

  @Override
  public void onDestroy()
  {
    Log.d(TAG, "onDestroy");
    if (_workerThread != null)
      _workerThread.interrupt();

    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    Log.d(TAG, "onStartCommand");
    if (intent.getAction() != null)
    {
      Log.d(TAG, "Action: " + intent.getAction());
      if (intent.getAction().equals(ACTION_UPDATE_WIDGET))
      {
        updateWidget();
      }
      else if (intent.getAction().equals(ACTION_STOP_SERVICE))
      {
        stopSelf();
      }
    }

    if (_state != State.LISTENING)
    {
      Log.d(TAG, "Starting listening worker");

      UsbSerialProber usbProber = new UsbSerialProber(_usbProbeTable);

      List<UsbSerialDriver> availableDrivers = usbProber.findAllDrivers(_usbManager);
      Log.d(TAG, "AvailableDrivers:" + availableDrivers.size());

      if (availableDrivers.size() > 0)
      {
        UsbSerialDriver driver = availableDrivers.get(0);
        int vendorId = driver.getDevice().getVendorId();
        int productId = driver.getDevice().getProductId();

        Log.d(TAG, "Using USB Serial Driver: " + vendorId + "/" + productId);

        _workerThread = new ListeningThread(driver);
        _workerThread.start();
        _state = State.LISTENING;
      }
    }

    return START_STICKY;
  }

  private void updateWidget()
  {
    Log.d(TAG, "updateWidget");

    if ((System.currentTimeMillis() - _lastWidgetUpdateTime) < WIDGET_UPDATE_DELAY)
      return;

    _lastWidgetUpdateTime = System.currentTimeMillis();

    SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
    Calendar calendar = Calendar.getInstance();

    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
    remoteViews.setTextViewText(R.id.textSyncStatus, "Sync: " + String.valueOf(_messageProcessor.isSynced()));
    remoteViews.setTextViewText(R.id.textMessagesCount, "Msg: " + String.valueOf(messageCount));
    remoteViews.setTextViewText(R.id.textServiceStatus, "State: " + String.valueOf(_state));
    remoteViews.setTextViewText(R.id.textWorkerStatus, "Worker: " + String.valueOf((_workerThread != null)));
    remoteViews.setTextViewText(R.id.textUpdateTime, "Updated: " + String.valueOf(sdf.format(calendar.getTime())));

    Log.d(TAG, "updateWidget:"+sdf.format(calendar.getTime()));

    Intent updateWidgetIntent = new Intent(getApplicationContext(),WidgetProvider.class);
    updateWidgetIntent.setAction(WidgetProvider.UPDATE_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonUpdate, PendingIntent.getBroadcast(getApplicationContext(), 0, updateWidgetIntent, 0));

    Intent startServiceIntent = new Intent(getApplicationContext(),WidgetProvider.class);
    startServiceIntent.setAction(WidgetProvider.START_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonStart, PendingIntent.getBroadcast(getApplicationContext(), 0, startServiceIntent, 0));

    Intent stopServiceIntent = new Intent(getApplicationContext(),WidgetProvider.class);
    stopServiceIntent.setAction(WidgetProvider.STOP_CLICKED);
    remoteViews.setOnClickPendingIntent(R.id.buttonStop, PendingIntent.getBroadcast(getApplicationContext(), 0, stopServiceIntent, 0));

    ComponentName componentName = new ComponentName(getApplicationContext(), WidgetProvider.class);
    AppWidgetManager manager = AppWidgetManager.getInstance(this);
    manager.updateAppWidget(componentName, remoteViews);
  }

  MessageProcessor.EventListener messageProcessorListener = new MessageProcessor.EventListener()
  {
    @Override
    public void newMessage(BusMessage message)
    {
      messageCount++;
      Log.d(TAG, message.toString());
      if (message.getType() != null)
      {
        if (message.getType() == BusMessage.Type.MFSW_VOLUME_UP)
        {
          _audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
        else if (message.getType() == BusMessage.Type.MFSW_VOLUME_DOWN)
        {
          _audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        }
        else if (message.getType() == BusMessage.Type.MFSW_NEXT_PRESSED)
        {
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          synchronized (this)
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            sendOrderedBroadcast(mediaIntent, null);

            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
            sendOrderedBroadcast(mediaIntent, null);
          }
        }
        else if (message.getType() == BusMessage.Type.MFSW_PREVIOUS_PRESSED)
        {
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          synchronized (this)
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            sendOrderedBroadcast(mediaIntent, null);

            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            sendOrderedBroadcast(mediaIntent, null);
          }
        }
        else if (message.getType() == BusMessage.Type.MFSW_DIAL_PRESSED)
        {
          // TODO: Call Google Now voice intent
        }
      }
      updateWidget();
    }
  };

  private enum State
  {
    STARTING,
    IDLING,
    LISTENING
  }

  private class ListeningThread extends Thread
  {
    private UsbSerialDriver _driver;

    public ListeningThread(UsbSerialDriver driver)
    {
      Log.d(TAG, "ListeningThread Constructor");
      _state = BMWiService.State.LISTENING;
      _driver = driver;
    }

    public void run()
    {
      Log.d(TAG, "ListeningThread run");
      UsbSerialPort serialPort = null;
      try
      {
        UsbDeviceConnection connection = _usbManager.openDevice(_driver.getDevice());
        serialPort = _driver.getPorts().get(0);

        if (connection == null)
        {

        }

        serialPort.open(connection);
        serialPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_EVEN);

        byte buffer[] = new byte[1024];
        while (!Thread.currentThread().isInterrupted())
        {
          int bytesRead = serialPort.read(buffer, 100);
          _messageProcessor.appendBuffer(Arrays.copyOf(buffer, bytesRead));
          _messageProcessor.process();
        }

        serialPort.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        if (serialPort != null)
          try
          {
            serialPort.close();
          } catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    }
  }
}
