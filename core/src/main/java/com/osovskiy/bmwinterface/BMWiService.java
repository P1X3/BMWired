package com.osovskiy.bmwinterface;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.osovskiy.bmwinterface.bus.BusInterface;
import com.osovskiy.bmwinterface.lib.BusMessage;
import com.osovskiy.bmwinterface.lib.Utils;

public class BMWiService extends Service
{
  public static final String ACTION_STOP_SERVICE = "BMWiService.ACTION_STOP_SERVICE";
  public static final String EVENT_USB_DEVICE_ATTACHED = "BMWiService.EVENT_USB_DEVICE_ATTACHED";
  public static final int MSG_REGISTER_CLIENT = 0;
  public static final int MSG_UNREGISTER_CLIENT = 1;
  public static final int MSG_SENDTO_BUS = 2;
  public static final int MSG_SENDFROM_BUS = 3;
  public static final int MSG_BUSINTERFACE_OPEN = 4;
  public static final int MSG_BUSINTERFACE_CLOSE = 5;
  private final String TAG = this.getClass().getSimpleName();
  private final BusInterface.EventListener eventListener = new BusInterface.EventListener()
  {
    @Override
    public void newMessage(BusMessage message)
    {
      Log.d(TAG, "Broadcasting new message from bus");
      Intent intent = new Intent(Utils.ACTION_NEW_BUS_MESSAGE);
      intent.putExtra(BusMessage.class.getSimpleName(), message);
      sendBroadcast(intent);
    }

    @Override
    public void newSync(boolean sync)
    {
      Log.d(TAG, "New sync state: " + sync);
    }

    @Override
    public void workerClosing()
    {

    }
  };
  private final Messenger messenger = new Messenger(new IncomingHandler());
  private final BroadcastReceiver receiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      Log.d(TAG, "New broadcast message");
      if ( intent.getAction() != null )
      {
        Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();

        if ( intent.getAction().equals(Utils.ACTION_SEND_BUS_MESSAGE) )
        {
          Log.d(TAG, "Sending new message to bus");
          busInterface.sendMsg((BusMessage) intent.getParcelableExtra(BusMessage.class.getSimpleName()));
        }
      }
    }
  };
  private BusInterface busInterface;
  private SharedPreferences prefs;

  public BMWiService()
  {
    Log.d(TAG, "Constructor");
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    Log.d(TAG, "onBind");
    return messenger.getBinder();
  }

  @Override
  public void onCreate()
  {
    Log.d(TAG, "onCreate");

    busInterface = new BusInterface(getApplicationContext(), new Handler(), eventListener);
    prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Utils.ACTION_SEND_BUS_MESSAGE);
    registerReceiver(receiver, intentFilter, Utils.PERMISSION_SEND_MESSAGE, null);
    super.onCreate();
  }

  @Override
  public void onDestroy()
  {
    Log.d(TAG, "onDestroy");

    if ( busInterface != null )
      busInterface.destroy();

    unregisterReceiver(receiver);

    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    Log.d(TAG, "onStartCommand");
    if (intent != null)
    {
      String action = intent.getAction();
      if ( action != null )
      {
        Log.d(TAG, "Action: " + action);
        if ( action.equals(ACTION_STOP_SERVICE) )
        {
          stopSelf();
        }
      }
    }

    busInterface.tryOpen((prefs.getBoolean("bluetooth_interface", false)) ? BusInterface.Type.BLUETOOTH : BusInterface.Type.SERIAL);

    return START_STICKY;
  }

  /**
   * Handler for incoming messages from bound clients
   */
  private class IncomingHandler extends Handler
  {
    @Override
    public void handleMessage(Message msg)
    {
      switch ( msg.what )
      {
        case MSG_REGISTER_CLIENT:

          break;
        case MSG_UNREGISTER_CLIENT:

          break;
        case MSG_SENDTO_BUS:
          msg.getData().setClassLoader(BusMessage.class.getClassLoader());
          busInterface.sendMsg((BusMessage) msg.getData().getParcelable(BusMessage.class.getSimpleName()));
          break;
        case MSG_SENDFROM_BUS:
          msg.getData().setClassLoader(BusMessage.class.getClassLoader());
          eventListener.newMessage((BusMessage) msg.getData().getParcelable(BusMessage.class.getSimpleName()));
          break;
        case MSG_BUSINTERFACE_OPEN:
          if (busInterface == null)
            busInterface = new BusInterface(getApplicationContext(), new Handler(), eventListener);
          busInterface.tryOpen((prefs.getBoolean("bluetooth_interface", false)) ? BusInterface.Type.BLUETOOTH : BusInterface.Type.SERIAL);
          break;
        case MSG_BUSINTERFACE_CLOSE:
          if (busInterface != null)
            busInterface.destroy();
          busInterface = null;
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
}
