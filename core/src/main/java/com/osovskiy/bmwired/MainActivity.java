package com.osovskiy.bmwired;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

/**
 * Created by Administrator on 8/22/2014.
 */
public class MainActivity extends Activity implements ActionBar.TabListener
{
  private Map<String, Class<? extends Fragment>> fragments = new LinkedHashMap<>();

  private boolean serviceBound = false;
  private Messenger serviceMessenger = null;

  private ServiceConnection serviceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
      serviceMessenger = new Messenger(service);
      serviceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      serviceMessenger = null;
      serviceBound = false;
    }
  };

  public MainActivity()
  {
    fragments.put("Plugins", PluginsFragment.class);
    fragments.put("Preferences", PreferencesFragment.class);
    fragments.put("Setup", SetupFragment.class);
    fragments.put("Debugging", DebuggingFragment.class);
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
    if ( action != null && action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED") )
    {
      Intent service = new Intent(getApplicationContext(), BMWiService.class);
      service.setAction(BMWiService.EVENT_USB_DEVICE_ATTACHED);
      startService(service);
      finish();
    }

    super.onCreate(savedInstanceState);


    setContentView(R.layout.activity_main);

    ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    for ( Map.Entry<String, Class<? extends Fragment>> e : fragments.entrySet() )
    {
      ActionBar.Tab tab = actionBar.newTab();
      tab.setText(e.getKey());
      tab.setTabListener(this);
      actionBar.addTab(tab);
    }
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
  {
    Log.d("LOL", tab.getText() + " selected");
    Fragment fragment = null;
    try
    {
      String tabName = String.valueOf(tab.getText());
      Class<? extends Fragment> fragmentClass = fragments.get(tabName);
      Constructor<? extends Fragment> fragmentConstructor = fragmentClass.getConstructor();
      fragment = fragmentConstructor.newInstance();

      Bundle bundle = new Bundle();
      bundle.putParcelable(Messenger.class.getSimpleName(), serviceMessenger);
      fragment.setArguments(bundle);
    }
    catch ( NoSuchMethodException e )
    {
      e.printStackTrace();
    }
    catch ( InvocationTargetException e )
    {
      e.printStackTrace();
    }
    catch ( InstantiationException e )
    {
      e.printStackTrace();
    }
    catch ( IllegalAccessException e )
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
