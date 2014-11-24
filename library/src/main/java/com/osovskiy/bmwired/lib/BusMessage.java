package com.osovskiy.bmwired.lib;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BusMessage implements Parcelable
{
  public static final Creator<BusMessage> CREATOR = new Creator<BusMessage>()
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
  private BusDevice source;
  private BusDevice destination;
  private byte[] payload;

  /**
   * Construct BusMessage using data byte array
   *
   * @param data Byte array containing valid data
   */
  private BusMessage(byte data[])
  {
    payload = Arrays.copyOfRange(data, 3, data.length - 2);
    source = BusDevice.tryParse(data[0]);
    destination = BusDevice.tryParse(data[2]);
  }

  private BusMessage(Parcel parcel)
  {
    this.source = (BusDevice) parcel.readSerializable();
    this.destination = (BusDevice) parcel.readSerializable();
    this.payload = new byte[parcel.readInt()];
    parcel.readByteArray(this.payload);
  }

  /**
   * Construct BusMessage
   *
   * @param source      BusDevice that message is sent from
   * @param destination BusDevice that message is sent to
   * @param payload     Payload
   */
  public BusMessage(BusDevice source, BusDevice destination, byte[] payload)
  {
    this.source = source;
    this.destination = destination;
    this.payload = payload;
  }

  /**
   * Build message from byte array if it contains valid data
   *
   * @param msg Byte array containing the message
   * @return BusMessage or null if checksum is not valid
   */
  public static BusMessage tryParse(byte[] msg)
  {
    byte testChecksum = 0;
    for (int i = 0; i < msg.length - 1; i++)
    {
      testChecksum ^= msg[i];
    }

    if (testChecksum != msg[msg.length - 1])
    {
      return null;
    }

    return new BusMessage(msg);
  }

  public BusDevice getSource()
  {
    return source;
  }

  public BusDevice getDestination()
  {
    return destination;
  }

  public byte[] getPayload()
  {
    return payload;
  }

  /**
   * Build byte array containing valid message data ready to be sent to interface
   *
   * @return Byte array containing valid data
   */
  public byte[] build()
  {
    ByteBuffer bb = ByteBuffer.allocate(payload.length + 4);

    byte length = (byte) (bb.capacity() - 2);

    bb.put(source.getId());
    bb.put(length);
    bb.put(destination.getId());
    bb.put(payload);
    bb.put(calculateChecksum());

    return bb.array();
  }

  /**
   * Calculate checksum
   *
   * @return Byte containing checksum
   */
  public byte calculateChecksum()
  {
    byte checksum = source.getId();
    checksum ^= (byte) (payload.length + 2);
    checksum ^= destination.getId();
    for (byte b : payload)
    {
      checksum ^= b;
    }

    return checksum;
  }

  @Override
  public String toString()
  {
    return "BusMessage[" + ((source == null) ? "null" : source) + " -> " + ((destination == null) ? "null" : destination) + "]";
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
    dest.writeInt(payload.length);
    dest.writeByteArray(payload);
  }

  public enum BusDevice
  {
    Broadcast(0x00), SHD(0x06),
    CD(0x18),
    HKM(0x24), FUM(0x28),
    CCM(0x30), NAV(0x3B), DIA(0x3F),
    FBZV(0x40), GTF(0x43), EWS(0x44), CID(0x46), FMBT(0x47),
    MFL(0x50), MML(0x51), IHK(0x5B),
    PDC(0x60), CDCD(0x66), RAD(0x68), DSP(0x6A),
    RDC(0x70), SM(0x72), SDRS(0x73), CDCD2(0x76), NAVE(0x7F),
    IKE(0x80),
    MMR(0x9B), CVM(0x9C),
    FMID(0xA0), ACM(0xA4), FHK(0xA7), HAVC(0xA8), EHC(0xAC),
    SES(0xB0), TV(0xBB), LCM(0xBF),
    MID(0xC0), PHONE(0xC8),
    LKM(0xD0), SMAD(0xDA),
    IRIS(0xE0), OBS(0xE7), ISP(0xE8), LWSMTV(0xED),
    CSU(0xF5), Broadcast2(0xFF);

    private final byte id;

    BusDevice(int id)
    {
      this.id = (byte) id;
    }

    public static BusDevice tryParse(byte device)
    {
      for (BusDevice d : BusDevice.values())
      {
        if (d.id == device)
        {
          return d;
        }
      }
      return null;
    }

    public byte getId()
    {
      return this.id;
    }
  }
}
