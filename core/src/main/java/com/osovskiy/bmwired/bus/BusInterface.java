package com.osovskiy.bmwired.bus;

import android.content.Context;
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

  protected BusInterface(Context context, EventListener el)
  {
    Log.d(TAG, "Creating BusInterface");
    this.context = context;
    eventListener = el;
  }

  /**
   * Open interface
   */
  public abstract void open();

  /**
   * Close interface
   */
  public abstract void close();

  /**
   * Queue message to be sent to the interface
   *
   * @param message Validated BusMessage
   */
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
    /**
     * Called when new message is received from the interface
     *
     * @param message Validated BusMessage
     */
    void newMessage(BusMessage message);

    /**
     * Called when worker is closing
     *
     * @param reason The reason worker has been closed
     */
    void workerClosing(ClosingReason reason);

    public enum ClosingReason
    {
      ConnectionLost,
      NonRecoverableError,
      Normal
    }
  }
}
