package com.osovskiy.bmwired.fragment.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.osovskiy.bmwired.R;

public class PreferencesFragment extends PreferenceFragment
{
  SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
  {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
      setupPreferences(sharedPreferences);
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    setupPreferences(getPreferenceManager().getSharedPreferences());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
  }

  @Override
  public void onPause()
  {
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    super.onPause();
  }

  private void setupPreferences(SharedPreferences sharedPreferences)
  {
    String selectedInterface = sharedPreferences.getString(getString(R.string.preference_interface_type_key), "Serial");

    if ( selectedInterface.equals("Serial") )
    {
      findPreference(getString(R.string.preference_category_serial)).setEnabled(true);
      findPreference(getString(R.string.preference_category_bluetooth)).setEnabled(false);
      findPreference(getString(R.string.preference_category_ioio)).setEnabled(false);
    }
    else if ( selectedInterface.equals("Bluetooth") )
    {
      findPreference(getString(R.string.preference_category_serial)).setEnabled(false);
      findPreference(getString(R.string.preference_category_bluetooth)).setEnabled(true);
      findPreference(getString(R.string.preference_category_ioio)).setEnabled(false);
    }
    else if ( selectedInterface.equals("IOIO") )
    {
      findPreference(getString(R.string.preference_category_serial)).setEnabled(false);
      findPreference(getString(R.string.preference_category_bluetooth)).setEnabled(false);
      findPreference(getString(R.string.preference_category_ioio)).setEnabled(true);
    }
  }
}
