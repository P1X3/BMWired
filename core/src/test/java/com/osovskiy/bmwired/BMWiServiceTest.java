package com.osovskiy.bmwired;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

public class BMWiServiceTest extends ServiceTestCase<BMWiService>
{
  public BMWiServiceTest()
  {
    super(BMWiService.class);
  }

  public void testStartable() throws Exception
  {
    Intent intent = new Intent(getContext(), BMWiService.class);
    startService(intent);

    BMWiService service = getService();
    assertNotNull(service);
  }

  public void testBindable()
  {
    Intent intent = new Intent(getContext(), BMWiService.class);
    IBinder binder = bindService(intent);
    assertNotNull(binder);
  }
}