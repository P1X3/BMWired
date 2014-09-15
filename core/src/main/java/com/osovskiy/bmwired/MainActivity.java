package com.osovskiy.bmwired;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.osovskiy.bmwired.fragment.debugging.DebuggingFragment;
import com.osovskiy.bmwired.fragment.plugins.PluginsFragment;
import com.osovskiy.bmwired.fragment.preferences.PreferencesFragment;
import com.osovskiy.bmwired.fragment.setup.SetupFragment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends Activity implements ActionBar.TabListener
{
  private static final String TAG = MainActivity.class.getSimpleName();
  private Map<Integer, Class<? extends Fragment>> fragments = new LinkedHashMap<>();

  private boolean serviceBound = false;
  private Messenger serviceMessenger = null;

  private ServiceConnection serviceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
      Log.d(TAG, "Connected to the service");
      serviceMessenger = new Messenger(service);
      serviceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      Log.d(TAG, "Disconnected from the service");
      serviceMessenger = null;
      serviceBound = false;
    }
  };

  public MainActivity()
  {
    fragments.put(R.string.tab_plugins, PluginsFragment.class);
    fragments.put(R.string.tab_preferences, PreferencesFragment.class);
    fragments.put(R.string.tab_setup, SetupFragment.class);
    fragments.put(R.string.tab_debugging, DebuggingFragment.class);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    bindService(new Intent(this, BMWiService.class), serviceConnection, 0);
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    if ( serviceBound )
    {
      unbindService(serviceConnection);
      serviceBound = false;
    }
  }

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

    ActionBar actionBar = getActionBar();
    if (actionBar != null)
    {
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

      for ( Map.Entry<Integer, Class<? extends Fragment>> e : fragments.entrySet() )
      {
        ActionBar.Tab tab = actionBar.newTab();
        tab.setTag(e.getKey());
        tab.setText(getString(e.getKey()));
        tab.setTabListener(this);
        actionBar.addTab(tab);
      }
    }
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
  {
    Log.d(TAG, "Tab \"" + tab.getText() + "\" selected");
    Fragment fragment = null;
    try
    {
      Class<? extends Fragment> fragmentClass = fragments.get((Integer)tab.getTag());
      Constructor<? extends Fragment> fragmentConstructor = fragmentClass.getConstructor();
      fragment = fragmentConstructor.newInstance();

      Bundle bundle = new Bundle();
      bundle.putParcelable(Messenger.class.getSimpleName(), serviceMessenger);
      fragment.setArguments(bundle);
    }
    catch ( NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e )
    {
      e.printStackTrace();
    }
    FragmentManager fm = getFragmentManager();
    fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
  {

  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
  {

  }
}
