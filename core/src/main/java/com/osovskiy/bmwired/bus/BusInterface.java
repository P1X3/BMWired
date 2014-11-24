package com.osovskiy.bmwired.bus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.osovskiy.bmwired.bus.worker.BusInterfaceWorker;
import com.osovskiy.bmwired.lib.BusMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BusInterface
{
  private static final String TAG = BusInterface.class.getSimpleName();

  protected Context context;

  protected BusInterfaceWorker workerThread;
  protected EventListener eventListener;
  protected BlockingQueue<BusMessage> queue = new LinkedBlockingQueue<>();
  protected SharedPreferences preferences;

  protected BusInterface(Context context, EventListener el)
  {
    Log.d(TAG, "Creating BusInterface");
    this.context = context;
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    eventListener = el;
  }

  public abstract void open();

  public abstract void close();

  public void queueMessage(BusMessage message)
  {
    queue.add(message);
  }

  public enum Type
  {
    Serial,
    Bluetooth
  }

  public interface EventListener
  {
    void newMessage(BusMessage message);

    void workerClosing(ClosingReason reason);

    public enum ClosingReason
    {
      ConnectionLost,
      NonRecoverableError,
      Normal
    }
  }
}
