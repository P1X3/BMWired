package com.osovskiy.bmwinterface;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.osovskiy.bmwinterface.fragment.plugins.PluginsFragment;
import com.osovskiy.bmwinterface.fragment.Preferences;
import com.osovskiy.bmwinterface.fragment.Setup;

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

  public MainActivity()
  {
    fragments.put("Plugins", PluginsFragment.class);
    fragments.put("Preferences", Preferences.class);
    fragments.put("Setup", Setup.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    String action = getIntent().getAction();
    if (action != null && action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED"))
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
