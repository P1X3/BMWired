package com.osovskiy.bmwired.lib;

public class Utils
{
  public static final String ACTION_NEW_BUS_MESSAGE = "com.osovskiy.bmwired.ACTION_NEW_BUS_MESSAGE";
  public static final String ACTION_SEND_BUS_MESSAGE = "com.osovskiy.bmwired.ACTION_SEND_BUS_MESSAGE";

  public static final String PERMISSION_SEND_MESSAGE = "com.osovskiy.bmwired.permission.SEND_MSG";
  public static final String PERMISSION_RECEIVE_MESSAGE = "com.osovskiy.bmwired.permission.RECEIVE_MSG";

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
