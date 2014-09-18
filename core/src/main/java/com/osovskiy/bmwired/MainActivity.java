package com.osovskiy.bmwired;

import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.osovskiy.bmwired.fragment.FragmentAdapter;
import com.osovskiy.bmwired.fragment.plugins.PluginsFragment;
import com.osovskiy.bmwired.fragment.preferences.PreferencesFragment;
import com.osovskiy.bmwired.fragment.setup.SetupFragment;
import com.osovskiy.bmwired.view.SlidingTabLayout;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity
{
  private static final String TAG = MainActivity.class.getSimpleName();
  List<Map.Entry<String, Fragment>> fragments = new ArrayList<>();

  FragmentAdapter adapter;
  ViewPager viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    Intent intent = new Intent(this, BMWiService.class);
    startService(intent);

    String action = getIntent().getAction();
    if ( action != null && action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) )
    {
      Log.d(TAG, "USB_DEVICE_ATTACHED intent received");
      Intent service = new Intent(getApplicationContext(), BMWiService.class);
      service.setAction(BMWiService.EVENT_USB_DEVICE_ATTACHED);
      startService(service);
      finish();
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    fragments.add(new AbstractMap.SimpleEntry<String, Fragment>(getString(R.string.tab_plugins), new PluginsFragment()));
    fragments.add(new AbstractMap.SimpleEntry<String, Fragment>(getString(R.string.tab_preferences), new PreferencesFragment()));
    fragments.add(new AbstractMap.SimpleEntry<String, Fragment>(getString(R.string.tab_setup), new SetupFragment()));

    adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
    viewPager = (ViewPager) findViewById(R.id.viewpager);
    viewPager.setAdapter(adapter);
    ((SlidingTabLayout) findViewById(R.id.sliding_tabs)).setViewPager(viewPager);
  }
}
