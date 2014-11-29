package com.osovskiy.bmwired.utils;

import com.osovskiy.bmwired.lib.IBMWiServiceCallback;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class CallbackRegistry
{
  TreeMap<UUID, IBMWiServiceCallback> registry;

  public CallbackRegistry()
  {
    registry = new TreeMap<>();
  }

  /**
   * Register new {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback}
   * @param callback
   * @return Unique {@link java.util.UUID} assigned to the {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback}
   */
  public UUID register(IBMWiServiceCallback callback)
  {
    UUID uuid = UUID.randomUUID();
    registry.put(uuid, callback);
    return uuid;
  }

  /**
   * Unregister {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback}
   * @param uuid
   * @return
   */
  public IBMWiServiceCallback unregister(UUID uuid)
  {
    return registry.remove(uuid);
  }

  /**
   * Unregister {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback}
   * @param callback
   * @return
   */
  public boolean unregister(IBMWiServiceCallback callback)
  {
    return registry.values().remove(callback);
  }

  /**
   * Run {@link com.osovskiy.bmwired.utils.CallbackRegistry.CallbackAction} on all registered {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback} objects
   * @param callbackAction
   */
  public void callAll(CallbackAction callbackAction)
  {
    if (callbackAction == null)
    {
      return;
    }

    Collection<IBMWiServiceCallback> callbacks = registry.values();
    for (IBMWiServiceCallback callback : callbacks)
    {
      callbackAction.run(callback);
    }
  }

  /**
   * Run {@link com.osovskiy.bmwired.utils.CallbackRegistry.CallbackAction} on a specific {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback} object
   * @param uuid {@link java.util.UUID} of {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback} that was assigned during registration process
   * @param callbackAction
   */
  public void call(UUID uuid, CallbackAction callbackAction)
  {
    if (callbackAction == null)
    {
      return;
    }

    callbackAction.run(registry.get(uuid));
  }

  /**
   * Run {@link com.osovskiy.bmwired.utils.CallbackRegistry.CallbackAction} on a list of specific {@link com.osovskiy.bmwired.lib.IBMWiServiceCallback} objects
   * @param uuidList List of {@link java.util.UUID}
   * @param callbackAction
   */
  public void call(List<UUID> uuidList, CallbackAction callbackAction)
  {
    if (callbackAction == null)
    {
      return;
    }

    for (UUID uuid : uuidList)
    {
      callbackAction.run(registry.get(uuid));
    }
  }

  public interface CallbackAction
  {
    public void run(IBMWiServiceCallback callback);
  }
}