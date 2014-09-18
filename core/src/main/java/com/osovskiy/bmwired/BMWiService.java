package com.osovskiy.bmwired;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.osovskiy.bmwired.bus.BusInterface;
import com.osovskiy.bmwired.lib.BusMessage;
import com.osovskiy.bmwired.lib.IBMWiService;
import com.osovskiy.bmwired.lib.IBMWiServiceCallback;
import com.osovskiy.bmwired.lib.Utils;
import com.osovskiy.bmwired.utils.CallbackRegistry;

import java.util.UUID;

public class BMWiService extends Service
{
  private final static String TAG = BMWiService.class.getSimpleName();

  public final static String ACTION_STOP_SERVICE = "BMWiService.ACTION_STOP_SERVICE";
  public final static String EVENT_USB_DEVICE_ATTACHED = "BMWiService.EVENT_USB_DEVICE_ATTACHED";

  private CallbackRegistry callbackRegistry = new CallbackRegistry();
  private BusInterface busInterface;
  private SharedPreferences prefs;

  @Override
  public IBinder onBind(Intent intent)
  {
    Log.d(TAG, "onBind");
    PackageManager pm = getPackageManager();
    int caller = Binder.getCallingUid();
    Log.d(TAG, "Caller UID: " + caller);
    String[] packages = pm.getPackagesForUid(caller);
    for ( String s : packages )
    {
      try
      {
        ApplicationInfo applicationInfo = pm.getApplicationInfo(s, PackageManager.GET_META_DATA);
        Log.d(TAG, "Package: " + applicationInfo.packageName);
        boolean sendPermissionGranted = pm.checkPermission(Utils.PERMISSION_SEND_MESSAGE, applicationInfo.packageName) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "Permission granted: " + sendPermissionGranted);
        if ( sendPermissionGranted )
          return mBinder;
      }
      catch ( PackageManager.NameNotFoundException e )
      {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public void onCreate()
  {
    prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    openInterface();
    registerReceiver(receiver, new IntentFilter(Utils.ACTION_SEND_BUS_MESSAGE), Utils.PERMISSION_SEND_MESSAGE, null);
    super.onCreate();
  }

  @Override
  public void onDestroy()
  {
    if ( busInterface != null )
      busInterface.destroy();

    unregisterReceiver(receiver);
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    if ( intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_STOP_SERVICE) )
      stopSelf();

    openInterface();
    return START_STICKY;
  }

  private void openInterface()
  {
    if ( busInterface == null )
      busInterface = new BusInterface(getApplicationContext(), eventListener);

    BusInterface.Type selectedInterfaceType = BusInterface.Type.valueOf(prefs.getString(getString(R.string.preference_key_interface_type), "Serial"));

    if ( busInterface.getInterfaceType() != selectedInterfaceType )
      busInterface.closeInterface();

    busInterface.openInterface(selectedInterfaceType);
  }

  private final BusInterface.EventListener eventListener = new BusInterface.EventListener()
  {
    @Override
    public void newMessage(final BusMessage message)
    {
      Intent intent = new Intent(Utils.ACTION_NEW_BUS_MESSAGE);
      intent.putExtra(BusMessage.class.getSimpleName(), message);
      sendBroadcast(intent);

      callbackRegistry.callAll(new CallbackRegistry.CallbackAction()
      {
        @Override
        public void run(IBMWiServiceCallback callback)
        {
          try
          {
            callback.newMessageFromBus(message);
          }
          catch ( Exception e )
          {
            callbackRegistry.unregister(callback);
          }
        }
      });
    }

    @Override
    public void workerClosing(final ClosingReason closingReason)
    {
      callbackRegistry.callAll(new CallbackRegistry.CallbackAction()
      {
        @Override
        public void run(IBMWiServiceCallback callback)
        {
          try
          {
            callback.onInterfaceClosed(closingReason.toString());
          }
          catch ( RemoteException e )
          {

          }
        }
      });
    }
  };
  private final IBMWiService.Stub mBinder = new IBMWiService.Stub()
  {
    @Override
    public void sendMessageToBus(BusMessage msg) throws RemoteException
    {
      busInterface.queueMessage(msg);
    }

    @Override
    public void sendMessageFromBus(BusMessage msg) throws RemoteException
    {
      eventListener.newMessage(msg);
    }

    @Override
    public String registerCallback(IBMWiServiceCallback callback)
    {
      return callbackRegistry.register(callback).toString();
    }

    @Override
    public void unregisterCallback(String uuid)
    {
      callbackRegistry.unregister(UUID.fromString(uuid));
    }
  };
  private final BroadcastReceiver receiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if ( intent.getAction() != null && intent.getAction().equals(Utils.ACTION_SEND_BUS_MESSAGE) )
        busInterface.queueMessage((BusMessage) intent.getParcelableExtra(BusMessage.class.getSimpleName()));
    }
  };
}
