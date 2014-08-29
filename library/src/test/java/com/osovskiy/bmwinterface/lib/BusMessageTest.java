package com.osovskiy.bmwinterface.lib;

import android.os.Bundle;
import android.test.AndroidTestCase;

import java.util.Arrays;

public class BusMessageTest extends AndroidTestCase
{
  /**
   * Create a bus message using source, sestination, and payload
   * @result A valid bus message will be created if checksum and parameters are valid
   */
  public void testNewBusMessage() throws Exception
  {
    /* 0x00 0x06 0x18 0x54 0xFD 0xDE 0xAD 0xC4*/
    BusMessage busMessage = new BusMessage(BusMessage.BusDevice.Broadcast, BusMessage.BusDevice.CD, new byte[] {0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD});
    assertEquals((byte)0xC4, busMessage.calculateChecksum());
    assertEquals(BusMessage.BusDevice.Broadcast, busMessage.getSource());
    assertEquals(BusMessage.BusDevice.CD, busMessage.getDestination());
    assertTrue(Arrays.equals(new byte[] {0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD}, busMessage.getPayload()));
  }

  /**
   * Build message into byte array
   * @result Valid byte array will be produced by build() method
   */
  public void testBuild()
  {
    BusMessage busMessage = new BusMessage(BusMessage.BusDevice.Broadcast, BusMessage.BusDevice.CD, new byte[] {0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD});
    assertTrue(Arrays.equals(new byte[] {0x00, 0x06, 0x18, 0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD, (byte)0xC4}, busMessage.build()));
  }

  /**
   * Test is BusMessage is parceable
   * @result
   */
  public void testParceable()
  {
    BusMessage busMessage = new BusMessage(BusMessage.BusDevice.Broadcast, BusMessage.BusDevice.CD, new byte[] {0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD});
    Bundle bundle = new Bundle();
    bundle.putParcelable(BusMessage.class.getSimpleName(), busMessage);
    BusMessage busMessage1 = bundle.getParcelable(BusMessage.class.getSimpleName());

    assertEquals(busMessage.calculateChecksum(), busMessage1.calculateChecksum());
    assertEquals(busMessage.getSource(), busMessage1.getSource());
    assertEquals(busMessage.getDestination(), busMessage1.getDestination());
    assertTrue(Arrays.equals(busMessage.getPayload(), busMessage1.getPayload()));
  }

  /**
   * Test tryParse call with valid message
   * @result Valid BusMessage will be created
   */
  public void testTryParseTrue()
  {
    BusMessage busMessage = BusMessage.tryParse(new byte[] {0x00, 0x06, 0x18, 0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD, (byte)0xC4});
    assertNotNull(busMessage);
  }

  /**
   * Test tryParse with invalid checksum
   * @result BusMessage will be null
   */
  public void testTryParseFalse()
  {
    BusMessage busMessage = BusMessage.tryParse(new byte[] {0x00, 0x06, 0x18, 0x54, (byte)0xFD, (byte)0xDE, (byte)0xAD, (byte)0xCC});
    assertNull(busMessage);
  }

  /**
   * Test tryParse with corrupted payload
   * @result BusMessage will be null
   */
  public void testTryParseFalse2()
  {
    BusMessage busMessage = BusMessage.tryParse(new byte[] {0x00, 0x06, 0x18, 0x12, (byte)0xBE, (byte)0xEF, (byte)0xAD, (byte)0xC4});
    assertNull(busMessage);
  }
}