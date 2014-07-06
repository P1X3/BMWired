package com.osovskiy.bmwinterface;

import java.util.Arrays;

/**
 * Created by Vadim on 6/30/2014.
 */
public class BusMessage
{
  private byte[] raw;
  private Type type;

  public byte[] getRaw()
  {
    return raw;
  }

  public Type getType()
  {
    return type;
  }

  public BusMessage(byte data[])
  {
    raw = data;
    type = Type.tryParse(raw); // TODO: Replace blocking code
  }

  public static BusMessage tryParse(byte[] msg)
  {
    byte testChecksum = 0;
    for (int i = 0; i < msg.length-1; i++)
    {
      testChecksum ^= msg[i];
    }

    if (testChecksum != msg[msg.length-1])
      return null;

    return new BusMessage(msg);
  }

  @Override
  public String toString()
  {
    return "BusMessage{" +
            "raw=" + Arrays.toString(raw) +
            ", type=" + type +
            '}';
  }

  public enum Type // TODO: Storing raw messages is redundant
  {
    MFSW_VOLUME_UP(new byte[] {0x50, 0x04, 0x68, 0x32, 0x11, 0x1F}),
    MFSW_VOLUME_DOWN(new byte[] {0x50, 0x04, 0x68, 0x32, 0x10, 0x1E}),
    MFSW_NEXT_PRESSED(new byte[] {0x50, 0x04, 0x68, 0x3B, 0x01, 0x06}),
    MFSW_NEXT_1SEC(new byte[] {0x50, 0x04, 0x68, 0x3B, 0x11, 0x16}),
    MFSW_NEXT_RELEASED(new byte[] {0x50, 0x04, 0x68, 0x3B, 0x21, 0x26}),
    MFSW_PREVIOUS_PRESSED(new byte[] {0x50, 0x04, 0x68, 0x3B, 0x08, 0x0F}),
    MFSW_PREVIOUS_1SEC(new byte[] {0x50, 0x04, 0x68, 0x3B, 0x18, 0x1F}),
    MFSW_PREVIOUS_RELEASED(new byte[] {0x50, 0x04, 0x68, 0x3B, 0x28, 0x2F}),
    MFSW_RT(new byte[] {0x50, 0x03, (byte)0xC8, 0x01, (byte)0x9A}),
    MFSW_DIAL_PRESSED(new byte[] {0x50, 0x04, (byte)0xC8, 0x3B, (byte)0x80, 0x27}),
    MFSW_DIAL_1SEC(new byte[] {0x50, 0x04, (byte)0xC8, 0x3B, (byte)0x90, 0x37}),
    MFSW_DIAL_RELEASED(new byte[] {0x50, 0x04, (byte)0xC8, 0x3B, (byte)0xA0, 0x07});


    byte[] raw;

    Type(byte data[])
    {
      raw = data;
    }

    public static Type tryParse(byte data[])
    {
      for(Type t: Type.values())
      {
        if (t.raw.equals(data))
          return t;
      }
      return null;
    }
  }
}
