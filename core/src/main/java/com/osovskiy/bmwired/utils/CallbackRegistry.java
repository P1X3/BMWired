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

  public UUID register(IBMWiServiceCallback callback)
  {
    UUID uuid = UUID.randomUUID();
    registry.put(uuid, callback);
    return uuid;
  }

  public IBMWiServiceCallback unregister(UUID uuid)
  {
    return registry.remove(uuid);
  }

  public boolean unregister(IBMWiServiceCallback callback)
  {
    return registry.values().remove(callback);
  }

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

  public void call(UUID uuid, CallbackAction callbackAction)
  {
    if (callbackAction == null)
    {
      return;
    }

    callbackAction.run(registry.get(uuid));
  }

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