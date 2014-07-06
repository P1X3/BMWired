package com.osovskiy.bmwinterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.osovskiy.bmwinterface.R;

public class MyActivity extends Activity
{

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Intent serviceIntent = new Intent(getApplicationContext(), BMWiService.class);
    serviceIntent.setAction(BMWiService.EVENT_USB_DEVICE_ATTACHED);
    startService(serviceIntent);
    finish();
  }
}
