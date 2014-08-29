package com.osovskiy.bmwinterface.lib;

/**
 * Created by Administrator on 8/22/2014.
 */
public class Utils
{
  public static final String ACTION_NEW_BUS_MESSAGE = "com.osovskiy.bmwinterface.ACTION_NEW_BUS_MESSAGE";
  public static final String ACTION_SEND_BUS_MESSAGE = "com.osovskiy.bmwinterface.ACTION_SEND_BUS_MESSAGE";

  public static final String PERMISSION_SEND_MESSAGE = "com.osovskiy.bmwinterface.permission.SEND_MSG_PERMISSION";
  public static final String PERMISSION_RECEIVE_MESSAGE = "com.osovskiy.bmwinterface.permission.SEND_RECEIVE_PERMISSION";

  public static byte[] hexStringToByteArray(String s)
  {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }
}
