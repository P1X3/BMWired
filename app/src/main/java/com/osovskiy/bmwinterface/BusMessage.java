package com.osovskiy.bmwinterface;

import android.os.Parcel;
import android.os.Parcelable;

import com.hoho.android.usbserial.util.HexDump;

import java.util.Arrays;

/**
 * Created by Vadim on 6/30/2014.
 */
public class BusMessage implements Parcelable
{
  private Device source;
  private Device destination;
  private byte[] payload;
  private byte[] raw;

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


  private BusMessage(byte data[])
  {
    payload = Arrays.copyOfRange(data, 3, data.length-2);
    source = Device.tryParse(data[0]);
    destination = Device.tryParse(data[2]);
    raw = data;
  }

  private BusMessage(Parcel parcel)
  {
    this.source = (Device)parcel.readSerializable();
    this.destination = (Device)parcel.readSerializable();
    parcel.readByteArray(this.payload);
  }

  private BusMessage(Device source, Device destination, byte[] payload)
  {
    this.source = source;
    this.destination = destination;
    this.payload = payload;
  }

  /**
   * Build message from byte array if it contains valid data
   * @param msg
   * @return BusMessage or null if checksum is not valid
   */
  public static BusMessage build(byte[] msg)
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
    return "BusMessage[" + (( source == null) ? "null" : source) + " -> " + (( destination == null) ? "null" : destination) + "]{" + HexDump.toHexString(payload) + "}";
  }

  @Override
  public int describeContents()
  {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeSerializable(source);
    dest.writeSerializable(destination);
    dest.writeByteArray(payload);
  }

  public static final Parcelable.Creator<BusMessage> CREATOR = new Parcelable.Creator<BusMessage>()
  {

    @Override
    public BusMessage createFromParcel(Parcel source)
    {
      return new BusMessage(source);
    }

    @Override
    public BusMessage[] newArray(int size)
    {
      return new BusMessage[size];
    }
  };

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
