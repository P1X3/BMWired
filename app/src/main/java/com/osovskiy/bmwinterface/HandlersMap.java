package com.osovskiy.bmwinterface;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class HandlersMap
{
  private TreeMap<BusMessage.Device, TreeMap<BusMessage.Device, List<Class<?>>>> mainMap;

  HandlersMap()
  {
    mainMap = new TreeMap<BusMessage.Device, TreeMap<BusMessage.Device, List<Class<?>>>>();
  }

  void subscribe(BusMessage.Device source, BusMessage.Device destination, Class<?> clazz)
  {
    TreeMap<BusMessage.Device, List<Class<?>>> subMap = mainMap.get(source);
    if (subMap == null)
      mainMap.put(source, (subMap = new TreeMap<BusMessage.Device, List<Class<?>>>()));
    List<Class<?>> handlerList = subMap.get(destination);
    if (handlerList == null)
      subMap.put(destination, (handlerList = new ArrayList<Class<?>>()));

    handlerList.add(clazz);
  }

  void unsubscribe()
  {
    throw new UnsupportedOperationException();
  }

  List<Class<?>> get(BusMessage.Device source, BusMessage.Device destination)
  {
    TreeMap<BusMessage.Device, List<Class<?>>> sourceMap = mainMap.get(source);
    if (sourceMap != null)
      return sourceMap.get(destination);;
    return null;
  }
}
