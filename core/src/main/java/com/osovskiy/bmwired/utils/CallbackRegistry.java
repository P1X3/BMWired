package com.osovskiy.bmwired.utils;

import com.osovskiy.bmwired.lib.IBMWiServiceCallback;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class CallbackRegistry extends TreeMap<UUID, IBMWiServiceCallback>
{
  public UUID register(IBMWiServiceCallback callback)
  {
    UUID uuid = UUID.randomUUID();
    put(uuid, callback);
    return uuid;
  }

  public IBMWiServiceCallback unregister(UUID uuid)
  {
    return  remove(uuid);
  }

  public boolean unregister(IBMWiServiceCallback callback)
  {
    return values().remove(callback);
  }

  public void callAll(CallbackAction callbackAction)
  {
    if (callbackAction == null)
      return;

    Collection<IBMWiServiceCallback> callbacks = values();
    for ( IBMWiServiceCallback callback: callbacks)
      callbackAction.run(callback);
  }

  public void call(UUID uuid, CallbackAction callbackAction)
  {
    if (callbackAction == null)
      return;

    callbackAction.run(get(uuid));
  }

  public void call(List<UUID> uuids, CallbackAction callbackAction)
  {
    if (callbackAction == null)
      return;

    for (UUID uuid: uuids)
      callbackAction.run(get(uuid));
  }

  public interface CallbackAction
  {
    public void run(IBMWiServiceCallback callback);
  }
}