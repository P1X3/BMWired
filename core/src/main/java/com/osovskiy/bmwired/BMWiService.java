package com.osovskiy.bmwired;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.osovskiy.bmwired.bus.BusInterface;
import com.osovskiy.bmwired.lib.BusMessage;
import com.osovskiy.bmwired.lib.IBMWiService;
import com.osovskiy.bmwired.lib.IBMWiServiceCallback;
import com.osovskiy.bmwired.lib.Utils;

import java.util.ArrayList;
import java.util.List;

public class BMWiService extends Service
{
  private List<IBMWiServiceCallback> callbacks = new ArrayList<>();
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

      try
      {
        for ( IBMWiServiceCallback callback : callbacks )
          callback.newMessageFromBus(message);
      }
      catch ( RemoteException e )
      {
        e.printStackTrace();
      }
    }

    @Override
    public void newSync(boolean sync)
    {
      Log.d(TAG, "New sync state: " + sync);
    }

    @Override
    public void workerClosing(ClosingReason closingReason)
    {
      Log.d(TAG, "Worker closed " + closingReason.toString());
    }
  };

  private final IBMWiService.Stub mBinder = new IBMWiService.Stub()
  {
    @Override
    public void sendMessageToBus(BusMessage msg) throws RemoteException
    {
      busInterface.sendMsg(msg);
    }

    @Override
    public void sendMessageFromBus(BusMessage msg) throws RemoteException
    {
      //TODO: Broadcast new message from bus
    }

    @Override
    public void registerCallback(IBMWiServiceCallback callback) throws RemoteException
    {
      callbacks.add(callback);
    }

    @Override
    public void unregisterCallback(IBMWiServiceCallback callback) throws RemoteException
    {
      callbacks.remove(callback);
    }
  };
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
    return mBinder;
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
    if ( intent != null )
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

    busInterface.tryOpen(( prefs.getBoolean(getString(R.string.preference_key_bluetooth_interface), false) ) ? BusInterface.Type.BLUETOOTH : BusInterface.Type.SERIAL);

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
          if ( busInterface == null )
            busInterface = new BusInterface(getApplicationContext(), new Handler(), eventListener);
          busInterface.tryOpen(( prefs.getBoolean(getString(R.string.preference_key_bluetooth_interface), false) ) ? BusInterface.Type.BLUETOOTH : BusInterface.Type.SERIAL);
          break;
        case MSG_BUSINTERFACE_CLOSE:
          if ( busInterface != null )
            busInterface.destroy();
          busInterface = null;
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
}
