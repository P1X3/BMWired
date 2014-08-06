package com.osovskiy.bmwinterface;

import com.hoho.android.usbserial.util.HexDump;

import java.util.Arrays;

/**
 * Created by Vadim on 6/30/2014.
 */
public class BusMessage
{
  private Device source;
  private Device destination;
  private byte[] payload;
  private byte[] raw;
  private Type type;

  public Device getSource()
  {
    return source;
  }

  public Device getDestination()
  {
    return destination;
  }

  public byte[] getPayload()
  {
    return payload;
  }

  public byte[] getRaw()
  {
    return raw;
  }

  public Type getType()
  {
    return type;
  }

  private BusMessage(byte data[])
  {
    payload = Arrays.copyOfRange(data, 3, data.length-2);
    source = Device.tryParse(data[0]);
    destination = Device.tryParse(data[2]);
    raw = data;
    type = Type.tryParse(raw); // TODO: Replace blocking code
  }

  public BusMessage(Type type)
  {
    raw = type.raw;
    this.type = type;
  }

  /**
   * Try to parse a message from byte array using checksum validation
   * @param msg
   * @return BusMessage or null if checksum is not valid
   */
  public static BusMessage tryParse(byte[] msg)
  {
    byte testChecksum = 0;
    for ( int i = 0; i < msg.length - 1; i++ )
    {
      testChecksum ^= msg[i];
    }

    if ( testChecksum != msg[msg.length - 1] )
      return null;

    return new BusMessage(msg);
  }

  @Override
  public String toString()
  {
    return "BusMessage[" + (( source == null) ? "null" : source) + " -> " + (( destination == null) ? "null" : destination) + "]{" + (( type == null) ? "null" : type) + "}{" + HexDump.toHexString(payload) + "}";
  }

  public enum Type // TODO: Storing raw messages is redundant
  {
    MFSW_VOLUME_UP(new byte[]{ 0x50, 0x04, 0x68, 0x32, 0x11, 0x1F }),
    MFSW_VOLUME_DOWN(new byte[]{ 0x50, 0x04, 0x68, 0x32, 0x10, 0x1E }),
    MFSW_NEXT_PRESSED(new byte[]{ 0x50, 0x04, 0x68, 0x3B, 0x01, 0x06 }),
    MFSW_NEXT_1SEC(new byte[]{ 0x50, 0x04, 0x68, 0x3B, 0x11, 0x16 }),
    MFSW_NEXT_RELEASED(new byte[]{ 0x50, 0x04, 0x68, 0x3B, 0x21, 0x26 }),
    MFSW_PREVIOUS_PRESSED(new byte[]{ 0x50, 0x04, 0x68, 0x3B, 0x08, 0x0F }),
    MFSW_PREVIOUS_1SEC(new byte[]{ 0x50, 0x04, 0x68, 0x3B, 0x18, 0x1F }),
    MFSW_PREVIOUS_RELEASED(new byte[]{ 0x50, 0x04, 0x68, 0x3B, 0x28, 0x2F }),
    MFSW_RT(new byte[]{ 0x50, 0x03, (byte) 0xC8, 0x01, (byte) 0x9A }),
    MFSW_DIAL_PRESSED(new byte[]{ 0x50, 0x04, (byte) 0xC8, 0x3B, (byte) 0x80, 0x27 }),
    MFSW_DIAL_1SEC(new byte[]{ 0x50, 0x04, (byte) 0xC8, 0x3B, (byte) 0x90, 0x37 }),
    MFSW_DIAL_RELEASED(new byte[]{ 0x50, 0x04, (byte) 0xC8, 0x3B, (byte) 0xA0, 0x07 }),
    GENERAL_LOCK_ALL(new byte[]{ 0x3F, 0x05, 0x00, 0x0C, (byte) 0x97, 0x01, (byte) 0xA0 }),
    GENERAL_UNLOCK_ALL(new byte[]{ 0x00, 0x05, 0x00, 0x0C, (byte) 0x96, 0x01, (byte) 0x9E });

    byte[] raw;

    Type(byte data[])
    {
      raw = data;
    }

    public static Type tryParse(byte data[])
    {
      for ( Type t : Type.values() )
      {
        if ( Arrays.equals(t.raw, data) )
          return t;
      }
      return null;
    }
  }

  public enum Device
  {
    Broadcast(0x00),  SHD(0x06),
    CD(0x18),
    HKM(0x24),  FUM(0x28),
    CCM(0x30),  NAV(0x3B),  DIA(0x3F),
    FBZV(0x40), GTF(0x43),  EWS(0x44),  CID(0x46),  FMBT(0x47),
    MFL(0x50),  MML(0x51),  IHK(0x5B),
    PDC(0x60),  CDCD(0x66), RAD(0x68),  DSP(0x6A),
    RDC(0x70),  SM(0x72),   SDRS(0x73), CDCD2(0x76),NAVE(0x7F),
    IKE(0x80),
    MMR(0x9B),  CVM(0x9C),
    FMID(0xA0), ACM(0xA4),  FHK(0xA7),  HAVC(0xA8), EHC(0xAC),
    SES(0xB0),  TV(0xBB),   LCM(0xBF),
    MID(0xC0),  PHONE(0xC8),
    LKM(0xD0),  SMAD(0xDA),
    IRIS(0xE0), OBS(0xE7),  ISP(0xE8),  LWSMTV(0xED),
    CSU(0xF5),  Broadcast2(0xFF);

    private final byte id;

    Device(int id)
    {
      this.id = (byte) id;
    }

    public byte getId()
    {
      return this.id;
    }

    public static Device tryParse(byte device)
    {
      for (Device d: Device.values())
      {
        if (d.id == device)
          return d;
      }
      return null;
    }
  }
}
